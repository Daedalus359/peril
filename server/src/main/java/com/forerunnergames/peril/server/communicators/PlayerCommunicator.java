package com.forerunnergames.peril.server.communicators;

import com.esotericsoftware.kryo.Kryo;

import com.forerunnergames.peril.common.net.events.interfaces.PlayerEvent;
import com.forerunnergames.peril.common.net.events.interfaces.PlayersEvent;
import com.forerunnergames.peril.common.net.packets.person.PersonIdentity;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.peril.server.controllers.ClientPlayerMapping;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Utils;
import com.forerunnergames.tools.net.Remote;
import com.forerunnergames.tools.net.client.ClientCommunicator;

import com.google.common.base.Optional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.objenesis.instantiator.ObjectInstantiator;
import org.objenesis.strategy.InstantiatorStrategy;
import org.objenesis.strategy.StdInstantiatorStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PlayerCommunicator implements ClientCommunicator
{
  private static final Logger log = LoggerFactory.getLogger (PlayerCommunicator.class);
  private final ClientCommunicator clientCommunicator;
  private final Kryo kryo = new Kryo ();

  @SuppressWarnings ({ "rawtypes", "unchecked" })
  public PlayerCommunicator (final ClientCommunicator clientCommunicator)
  {
    Arguments.checkIsNotNull (clientCommunicator, "clientCommunicator");

    this.clientCommunicator = clientCommunicator;

    // Workaround for https://github.com/EsotericSoftware/kryo/issues/216
    kryo.setInstantiatorStrategy (new InstantiatorStrategy ()
    {
      @Override
      public ObjectInstantiator newInstantiatorOf (final Class type)
      {
        try
        {
          type.getConstructor ();
          return new Kryo.DefaultInstantiatorStrategy ().newInstantiatorOf (type);
        }
        catch (final NoSuchMethodException | SecurityException ignored)
        {
          return new StdInstantiatorStrategy ().newInstantiatorOf (type);
        }
      }
    });
  }

  @Override
  public void sendTo (final Remote client, final Object object)
  {
    Arguments.checkIsNotNull (client, "client");
    Arguments.checkIsNotNull (object, "object");

    clientCommunicator.sendTo (client, object);

    log.debug ("Sent client [{}] object [{}].", client, object);
  }

  @Override
  public void sendToAll (final Object object)
  {
    Arguments.checkIsNotNull (object, "object");

    clientCommunicator.sendToAll (object);

    log.debug ("Sent all clients object [{}].", object);
  }

  @Override
  public void sendToAllExcept (final Remote client, final Object object)
  {
    Arguments.checkIsNotNull (client, "client");
    Arguments.checkIsNotNull (object, "object");

    clientCommunicator.sendToAllExcept (client, object);

    log.debug ("Sent all clients except client [{}] object [{}].", client, object);
  }

  public static Object deepClone (Object object)
  {
    try
    {
      ByteArrayOutputStream baos = new ByteArrayOutputStream ();
      ObjectOutputStream oos = new ObjectOutputStream (baos);
      oos.writeObject (object);
      ByteArrayInputStream bais = new ByteArrayInputStream (baos.toByteArray ());
      ObjectInputStream ois = new ObjectInputStream (bais);
      return ois.readObject ();
    }
    catch (Exception e)
    {
      e.printStackTrace ();
      return null;
    }
  }

  public void sendToPlayer (final PlayerPacket player,
                            final Object object,
                            final ClientPlayerMapping clientPlayerMapping)
  {
    Arguments.checkIsNotNull (clientPlayerMapping, "clientPlayerMapping");
    Arguments.checkIsNotNull (player, "player");
    Arguments.checkIsNotNull (object, "object");

    log.debug ("Sending player [{}] object [{}]...", player, object);

    // Deep copy the object to avoid changing the PersonIdentity of existing PlayerPacket references.
    final Object objectCopy = kryo.copy (object);

    // Look for players in the copied object to set the PersonIdentity of.
    final Optional <PlayerEvent> playerEvent = Utils.optionalCast (objectCopy, PlayerEvent.class);
    final Optional <PlayersEvent> playersEvent = Utils.optionalCast (objectCopy, PlayersEvent.class);

    // Set the PersonIdentity of any players contained within the copied object.
    if (playerEvent.isPresent ()) setEventPlayerIdentity (playerEvent.get (), player);
    if (playersEvent.isPresent ()) setEventPlayerIdentity (playersEvent.get (), player);

    // Send out the copied & prepped object.
    sendToPlayerInternal (player, objectCopy, clientPlayerMapping);
  }

  public void sendToAllPlayers (final Object object, final ClientPlayerMapping clientPlayerMapping)
  {
    Arguments.checkIsNotNull (clientPlayerMapping, "clientPlayerMapping");
    Arguments.checkIsNotNull (object, "object");

    log.debug ("Sending all players object [{}]...", object);

    // Deep copy the object to avoid changing the PersonIdentity of existing PlayerPacket references.
    final Object objectCopy = kryo.copy (object);

    // Look for players in the copied object to set the PersonIdentity of.
    final Optional <PlayerEvent> playerEvent = Utils.optionalCast (objectCopy, PlayerEvent.class);
    final Optional <PlayersEvent> playersEvent = Utils.optionalCast (objectCopy, PlayersEvent.class);

    for (final PlayerPacket player : clientPlayerMapping.players ())
    {
      // Set the PersonIdentity of any players contained within the copied object.
      if (playerEvent.isPresent ()) setEventPlayerIdentity (playerEvent.get (), player);
      if (playersEvent.isPresent ()) setEventPlayerIdentity (playersEvent.get (), player);

      // Send out the copied & prepped object.
      sendToPlayerInternal (player, objectCopy, clientPlayerMapping);
    }
  }

  public void sendToAllPlayersExcept (final PlayerPacket excludedPlayer,
                                      final Object object,
                                      final ClientPlayerMapping clientPlayerMapping)
  {
    Arguments.checkIsNotNull (clientPlayerMapping, "clientPlayerMapping");
    Arguments.checkIsNotNull (excludedPlayer, "excludedPlayer");
    Arguments.checkIsNotNull (object, "object");

    final Optional <Remote> clientQuery = clientPlayerMapping.clientFor (excludedPlayer);

    if (!clientQuery.isPresent ())
    {
      log.warn ("Ignoring attempt to exclude disconnected player [{}] from receiving object [{}]", excludedPlayer,
                object);
    }

    log.debug ("Sending all players except player [{}] object [{}]...", excludedPlayer, object);

    // Deep copy the object to avoid changing the PersonIdentity of existing PlayerPacket references.
    final Object objectCopy = kryo.copy (object);

    // Look for players in the copied object to set the PersonIdentity of.
    final Optional <PlayerEvent> playerEvent = Utils.optionalCast (objectCopy, PlayerEvent.class);
    final Optional <PlayersEvent> playersEvent = Utils.optionalCast (objectCopy, PlayersEvent.class);

    for (final PlayerPacket player : clientPlayerMapping.players ())
    {
      // Set the PersonIdentity of any players contained within the copied object.
      if (playerEvent.isPresent ()) setEventPlayerIdentity (playerEvent.get (), player);
      if (playersEvent.isPresent ()) setEventPlayerIdentity (playersEvent.get (), player);

      // Send out the copied & prepped object.
      if (player.isNot (excludedPlayer)) sendToPlayerInternal (player, objectCopy, clientPlayerMapping);
    }
  }

  /**
   * Sets the {@link PersonIdentity} on the player in the specified {@link PlayerEvent}.
   *
   * If the specified receiver is the player in the event, then the player in the event has {@code PersonIdentity.SELF},
   * otherwise {@code PersonIdentity.NON_SELF}.
   */
  private void setEventPlayerIdentity (final PlayerEvent event, final PlayerPacket receiver)
  {
    setEventPlayerIdentity (event.getPlayer (), receiver);
  }

  /**
   * Sets the {@link PersonIdentity} on the players in the specified {@link PlayersEvent}.
   *
   * If the specified receiver matches any player in the event, then that player in the event has
   * {@code PersonIdentity.SELF}, otherwise {@code PersonIdentity.NON_SELF}.
   */
  private void setEventPlayerIdentity (final PlayersEvent event, final PlayerPacket receiver)
  {
    for (final PlayerPacket playerFromEvent : event.getPlayers ())
    {
      setEventPlayerIdentity (playerFromEvent, receiver);
    }
  }

  private void setEventPlayerIdentity (final PlayerPacket playerFromEvent, final PlayerPacket receiver)
  {
    final PersonIdentity identity = receiver.is (playerFromEvent) ? PersonIdentity.SELF : PersonIdentity.NON_SELF;
    playerFromEvent.setIdentity (identity);

    log.trace ("Set {} of player [{}] to {} [{}].", PersonIdentity.class.getSimpleName (), playerFromEvent,
               PersonIdentity.class.getSimpleName (), identity);
  }

  private void sendToPlayerInternal (final PlayerPacket player,
                                     final Object object,
                                     final ClientPlayerMapping clientPlayerMapping)
  {
    Arguments.checkIsNotNull (clientPlayerMapping, "clientPlayerMapping");
    Arguments.checkIsNotNull (player, "player");
    Arguments.checkIsNotNull (object, "object");

    final Optional <Remote> clientQuery = clientPlayerMapping.clientFor (player);

    if (!clientQuery.isPresent ())
    {
      log.warn ("Ignoring attempt to send object [{}] to non-existent player [{}]", object, player);
      return;
    }

    clientCommunicator.sendTo (clientQuery.get (), object);

    log.trace ("Internal: Sent player [{}] object [{}].", player, object);
  }
}
