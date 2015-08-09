package com.forerunnergames.peril.integration.server;

import com.esotericsoftware.minlog.Log;

import com.forerunnergames.peril.core.shared.net.kryonet.KryonetRegistration;
import com.forerunnergames.peril.core.shared.net.packets.person.PlayerPacket;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Event;
import com.forerunnergames.tools.common.Exceptions;
import com.forerunnergames.tools.common.Result;
import com.forerunnergames.tools.common.Strings;
import com.forerunnergames.tools.net.Remote;
import com.forerunnergames.tools.net.client.AbstractClientController;
import com.forerunnergames.tools.net.client.Client;

import com.google.common.base.Optional;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class TestClient extends AbstractClientController
{
  private static final int DEFAULT_CONNECT_TIMEOUT_MS = 3000;
  private static final int DEFAULT_WAIT_TIMEOUT_MS = 6000;
  private static final int DEFAULT_MAX_ATTEMPTS = 2;
  private static final AtomicInteger clientCount = new AtomicInteger ();
  private final int clientId = clientCount.getAndIncrement ();
  private final ExecutorService exec = Executors.newSingleThreadExecutor ();
  private final ConcurrentLinkedQueue <Event> inboundEventQueue = new ConcurrentLinkedQueue <> ();
  private PlayerPacket player;

  public TestClient (final Client client)
  {
    super (client, KryonetRegistration.CLASSES);
  }

  public Result <String> connect (final String addr, final int tcpPort)
  {
    Arguments.checkIsNotNull (addr, "addr");
    Arguments.checkIsNotNegative (tcpPort, "tcpPort");

    return connect (addr, tcpPort, DEFAULT_CONNECT_TIMEOUT_MS, DEFAULT_MAX_ATTEMPTS);
  }

  public void sendEvent (final Event event)
  {
    Arguments.checkIsNotNull (event, "event");

    if (!isConnected ()) throw new IllegalStateException ("Test client not yet connected!");
    send (event);
  }

  public void dispose ()
  {
    shutDown ();
  }

  public <T> Optional <T> waitForEventCommunication (final Class <T> type, final boolean exceptionOnFail)
  {
    Arguments.checkIsNotNull (type, "type");

    final Exchanger <T> exchanger = new Exchanger <> ();
    exec.execute (new Runnable ()
    {
      @Override
      public void run ()
      {
        try
        {
          while (!exec.isShutdown ())
          {
            final Event next = inboundEventQueue.poll ();
            if (type.isInstance (next)) exchanger.exchange (type.cast (next));
            Thread.yield ();
          }
        }
        catch (final InterruptedException e)
        {
          Log.warn ("Listener thread timed out on exchange.");
        }
      }
    });
    try
    {
      final T event = exchanger.exchange (null, DEFAULT_WAIT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
      return Optional.fromNullable (event);
    }
    catch (InterruptedException | TimeoutException e)
    {
      if (exceptionOnFail) throw new IllegalStateException (Strings.format ("No events received of type {}.", type));
      return Optional.absent ();
    }
  }

  public int getClientId ()
  {
    return clientId;
  }

  public void setPlayer (final PlayerPacket player)
  {
    Arguments.checkIsNotNull (player, "player");

    this.player = player;
  }

  public PlayerPacket getPlayer ()
  {
    if (player == null) Exceptions.throwIllegalState ("Player has not been set.");
    return player;
  }

  @Override
  public String toString ()
  {
    return Strings.format ("{}-{}", getClass ().getSimpleName (), clientId);
  }

  @Override
  protected void onConnectionTo (final Remote server)
  {
    Arguments.checkIsNotNull (server, "server");
  }

  @Override
  protected void onDisconnectionFrom (final Remote server)
  {
    Arguments.checkIsNotNull (server, "server");
  }

  @Override
  protected void onCommunication (final Object object, final Remote server)
  {
    Arguments.checkIsNotNull (object, "object");
    Arguments.checkIsNotNull (server, "server");

    inboundEventQueue.offer ((Event) object);
  }
}
