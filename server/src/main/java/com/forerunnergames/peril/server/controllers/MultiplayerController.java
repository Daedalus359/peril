package com.forerunnergames.peril.server.controllers;

import static com.forerunnergames.tools.net.events.EventFluency.clientFrom;

import com.forerunnergames.peril.common.game.GameConfiguration;
import com.forerunnergames.peril.common.net.GameServerConfiguration;
import com.forerunnergames.peril.common.net.GameServerType;
import com.forerunnergames.peril.common.net.NetworkEventHandler;
import com.forerunnergames.peril.common.net.events.client.request.ChatMessageRequestEvent;
import com.forerunnergames.peril.common.net.events.client.request.JoinGameServerRequestEvent;
import com.forerunnergames.peril.common.net.events.client.request.PlayerJoinGameRequestEvent;
import com.forerunnergames.peril.common.net.events.client.request.PlayerRequestEvent;
import com.forerunnergames.peril.common.net.events.server.denied.JoinGameServerDeniedEvent;
import com.forerunnergames.peril.common.net.events.server.denied.PlayerJoinGameDeniedEvent;
import com.forerunnergames.peril.common.net.events.server.interfaces.PlayerInputRequestEvent;
import com.forerunnergames.peril.common.net.events.server.interfaces.PlayerResponseDeniedEvent;
import com.forerunnergames.peril.common.net.events.server.interfaces.PlayerResponseSuccessEvent;
import com.forerunnergames.peril.common.net.events.server.notification.PlayerLeaveGameEvent;
import com.forerunnergames.peril.common.net.events.server.success.ChatMessageSuccessEvent;
import com.forerunnergames.peril.common.net.events.server.success.JoinGameServerSuccessEvent;
import com.forerunnergames.peril.common.net.events.server.success.PlayerJoinGameSuccessEvent;
import com.forerunnergames.peril.common.net.messages.DefaultChatMessage;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.peril.common.settings.NetworkSettings;
import com.forerunnergames.peril.core.model.state.events.CreateGameEvent;
import com.forerunnergames.peril.core.model.state.events.DestroyGameEvent;
import com.forerunnergames.peril.server.communicators.CoreCommunicator;
import com.forerunnergames.peril.server.communicators.PlayerCommunicator;
import com.forerunnergames.peril.server.controllers.ClientPlayerMapping.RegisteredClientPlayerNotFoundException;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Event;
import com.forerunnergames.tools.common.controllers.ControllerAdapter;
import com.forerunnergames.tools.net.NetworkConstants;
import com.forerunnergames.tools.net.Remote;
import com.forerunnergames.tools.net.client.ClientConfiguration;
import com.forerunnergames.tools.net.client.ClientConnector;
import com.forerunnergames.tools.net.client.DefaultClientConfiguration;
import com.forerunnergames.tools.net.events.local.ClientCommunicationEvent;
import com.forerunnergames.tools.net.events.local.ClientConnectionEvent;
import com.forerunnergames.tools.net.events.local.ClientDisconnectionEvent;
import com.forerunnergames.tools.net.events.remote.origin.client.ResponseRequestEvent;
import com.forerunnergames.tools.net.events.remote.origin.server.ServerNotificationEvent;
import com.forerunnergames.tools.net.events.remote.origin.server.ServerRequestEvent;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.listener.Handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MultiplayerController extends ControllerAdapter
{
  // @formatter:off
  private static final Logger log = LoggerFactory.getLogger (MultiplayerController.class);
  private static final int CALL_EVENT_HANDLER_FIRST = 10;
  private static final int CALL_EVENT_HANDLER_LAST = 0;
  private final Multimap <PlayerPacket, PlayerInputRequestEvent> playerInputRequestEventCache = HashMultimap.create ();
  private final Map <String, Remote> playerJoinGameRequestCache = Collections.synchronizedMap (new HashMap <String, Remote> ());
  private final Set <Remote> clientsInServer = Collections.synchronizedSet (new HashSet <Remote> ());
  private final ClientPlayerMapping clientsToPlayers;
  private final ClientConnectorDaemon connectorDaemon = new ClientConnectorDaemon ();
  private final GameServerConfiguration gameServerConfig;
  private final ClientConnector clientConnector;
  private final PlayerCommunicator playerCommunicator;
  private final CoreCommunicator coreCommunicator;
  private final MBassador <Event> eventBus;
  private boolean shouldShutDown = false;
  private int connectionTimeoutMillis = NetworkSettings.CLIENT_CONNECTION_TIMEOUT_MS;
  @Nullable
  private Remote host = null;
  @Nullable
  private NetworkEventHandler networkEventHandler = null;
  // @formatter:on

  public MultiplayerController (final GameServerConfiguration gameServerConfig,
                                final ClientConnector clientConnector,
                                final PlayerCommunicator playerCommunicator,
                                final CoreCommunicator coreCommunicator,
                                final MBassador <Event> eventBus)
  {
    Arguments.checkIsNotNull (gameServerConfig, "gameServerConfig");
    Arguments.checkIsNotNull (clientConnector, "clientConnector");
    Arguments.checkIsNotNull (playerCommunicator, "playerCommunicator");
    Arguments.checkIsNotNull (coreCommunicator, "coreCommunicator");
    Arguments.checkIsNotNull (eventBus, "eventBus");

    this.gameServerConfig = gameServerConfig;
    this.clientConnector = clientConnector;
    this.playerCommunicator = playerCommunicator;
    this.coreCommunicator = coreCommunicator;
    this.eventBus = eventBus;

    clientsToPlayers = new ClientPlayerMapping (coreCommunicator, gameServerConfig.getPlayerLimit ());
  }

  @Override
  public void initialize ()
  {
    log.trace ("Initializing {} for game server '{}'", getClass ().getSimpleName (),
               gameServerConfig.getGameServerName ());

    eventBus.subscribe (this);
    eventBus.publish (new CreateGameEvent ());
    networkEventHandler = new ServerNetworkEventHandler (this, eventBus.getRegisteredErrorHandlers ());
  }

  @Override
  public boolean shouldShutDown ()
  {
    return shouldShutDown;
  }

  @Override
  public void shutDown ()
  {
    log.debug ("Shutting down...");

    eventBus.publish (new DestroyGameEvent ());
    eventBus.unsubscribe (this);
    connectorDaemon.threadPool.shutdown ();
    shouldShutDown = true;
  }

  public void setClientConnectTimeout (final int connectionTimeoutMillis)
  {
    this.connectionTimeoutMillis = connectionTimeoutMillis;
  }

  public GameConfiguration getGameConfiguration ()
  {
    return gameServerConfig;
  }

  public boolean isPlayerInGame (final PlayerPacket player)
  {
    Arguments.checkIsNotNull (player, "player");

    return clientsToPlayers.clientFor (player).isPresent ();
  }

  public boolean isClientInServer (final Remote client)
  {
    Arguments.checkIsNotNull (client, "client");

    return clientsInServer.contains (client);
  }

  // <<<<< inbound events from core module >>>>> //

  @Handler
  public void onEvent (final PlayerJoinGameSuccessEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.trace ("Event received [{}]", event);

    final String playerName = event.getPlayerName ();

    // if no client mapping is available, silently ignore success event and tell core to remove this player
    // this shouldn't happen, since this would imply that core processed a player join game request without
    // server receiving one... so basically the result of a bug or a hack.
    if (!playerJoinGameRequestCache.containsKey (playerName))
    {
      log.warn ("No client join game request in cache for player: {}. Player will be removed.", playerName);
      coreCommunicator.notifyRemovePlayerFromGame (event.getPlayer ());
      return;
    }

    // fetch and remove player name from request cache
    final Remote client = playerJoinGameRequestCache.remove (playerName);

    final PlayerPacket newPlayer = event.getPlayer ();

    // this should cover the case where the client disconnected before the successful join game request was processed.
    // core will be notified that the player has left. The subsequent PlayerLeaveGameEvent will be ignored since there
    // will be no client mapping for the disconnected player.
    if (!clientsInServer.contains (client))
    {
      log.warn ("Client [{}] for player [{}] is no longer connected to the server. Player will be removed.", client,
                event.getPlayerName ());
      coreCommunicator.notifyRemovePlayerFromGame (event.getPlayer ());
      return;
    }

    // add a player/client mapping since the client still exists in the game server
    final Optional <PlayerPacket> oldPlayer = clientsToPlayers.put (client, newPlayer);

    if (oldPlayer.isPresent ())
    {
      // this generally shouldn't happen... but if it does, log a warning message
      log.warn ("Overwrote previous player mapping for client [{}] | old player: [{}] | new player: [{}]", client,
                oldPlayer.get (), newPlayer);
    }

    sendToAllPlayers (event);
  }

  @Handler
  public void onEvent (final PlayerJoinGameDeniedEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.trace ("Event received [{}]", event);

    final String playerName = event.getPlayerName ();

    // if no client mapping is available, silently ignore denied event
    // this is to prevent failure under cases such as client disconnecting while join request is being processed
    if (!playerJoinGameRequestCache.containsKey (playerName)) return;

    final Remote client = playerJoinGameRequestCache.get (playerName);

    playerCommunicator.sendTo (client, event);
    clientConnector.disconnect (client);
    playerJoinGameRequestCache.remove (playerName);
  }

  @Handler (priority = CALL_EVENT_HANDLER_FIRST)
  public void onEvent (final PlayerLeaveGameEvent event)
  {
    final Optional <Remote> client = clientsToPlayers.clientFor (event.getPlayer ());
    if (!client.isPresent ())
    {
      log.warn ("No client mapping for player in received event [{}].", event);
      return;
    }
    // remove client mapping
    remove (client.get ());
  }

  @Handler (priority = CALL_EVENT_HANDLER_LAST)
  public void onEvent (final ServerNotificationEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.trace ("Event received [{}]", event);

    sendToAllPlayers (event);
  }

  @Handler
  public void onEvent (final PlayerInputRequestEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.trace ("Event received [{}]", event);

    final boolean wasAdded = playerInputRequestEventCache.put (event.getPlayer (), event);
    assert wasAdded;

    sendToPlayer (event.getPlayer (), event);
  }

  @Handler
  public void onEvent (final PlayerResponseSuccessEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.trace ("Event received [{}]", event);

    sendToAllPlayers (event);
  }

  @Handler
  public void onEvent (final PlayerResponseDeniedEvent <?> event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.trace ("Event received [{}]", event);

    sendToPlayer (event.getPlayer (), event);
  }

  // <<<<< remote inbound/outbound event communication >>>>> //

  @Handler
  public void onEvent (final ClientConnectionEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.trace ("Event received [{}]", event);
    log.info ("Client [{}] connected.", clientFrom (event));

    connectorDaemon.onConnect (clientFrom (event));
  }

  @Handler
  public void onEvent (final ClientDisconnectionEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.trace ("Event received [{}]", event);

    final Remote client = clientFrom (event);

    log.info ("Client [{}] disconnected.", client);

    Optional <PlayerPacket> playerQuery = Optional.absent ();
    try
    {
      playerQuery = clientsToPlayers.playerFor (client);
    }
    catch (final RegisteredClientPlayerNotFoundException e)
    {
      log.error ("Error resolving client to player.", e);
    }

    if (!playerQuery.isPresent ())
    {
      log.warn ("Client [{}] disconnected but did not exist as a player.", client);
      remove (client);
      return;
    }

    final PlayerPacket disconnectedPlayer = playerQuery.get ();
    coreCommunicator.notifyRemovePlayerFromGame (disconnectedPlayer);
  }

  @Handler
  void onEvent (final ClientCommunicationEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.trace ("Event received [{}]", event);

    networkEventHandler.handle (event.getMessage (), event.getClient ());
  }

  void handleEvent (final JoinGameServerRequestEvent event, final Remote client)
  {
    Arguments.checkIsNotNull (event, "event");
    Arguments.checkIsNotNull (client, "client");

    log.trace ("Event received [{}]", event);
    log.info ("Received join game server request from {}", client);

    // Clients should not be able to join if they do not have a valid ip address.
    // This reeks of hacking...
    if (!NetworkConstants.isValidIpAddress (client.getAddress ()))
    {
      sendJoinGameServerDenied (client, "Your IP address [" + client.getAddress () + "] is invalid.");
      return;
    }

    // Client cannot join if its external address matches that of the server.
    // Reason: Servers only allow clients to join on separate machines to help prevent
    // a single user from controlling multiple players to manipulate the game's outcome.
    // While clients could be on a LAN sharing an external address and actually be on separate machines, it
    // is required for LAN clients to join using the server's internal network address, which circumvents this problem.
    if (serverHasAddress () && client.getAddress ().equals (getServerAddress ()))
    {
      sendJoinGameServerDenied (client, "You cannot join this game having the same IP address [" + client.getAddress ()
              + "] as the game server.\nIf you are on the same network as the server, you can join using the game server's internal IP address to play a LAN game.");
      return;
    }

    // check if client is already in server
    if (clientsInServer.contains (client))
    {
      sendJoinGameServerDenied (client, "You have already joined this game server.");
      return;
    }

    // local host cannot join a dedicated server
    if (!isHostAndPlay () && isLocalHost (client))
    {
      sendJoinGameServerDenied (client, "You cannot join a dedicated game server as localhost.");
      return;
    }

    // local host must join first in a host-and-play server
    if (isHostAndPlay () && !isHostConnected () && !isLocalHost (client))
    {
      sendJoinGameServerDenied (client, "Waiting for the host to connect...");
      return;
    }

    // only one local host can join a host-and-play server
    if (isHostAndPlay () && isHostConnected () && isLocalHost (client))
    {
      sendJoinGameServerDenied (client, "The host has already joined this game server.");
      return;
    }

    // local host has joined the host-and-play server
    if (isHostAndPlay () && !isHostConnected () && isLocalHost (client)) host = client;

    sendJoinGameServerSuccess (client, clientsToPlayers.players ());
  }

  void handleEvent (final PlayerJoinGameRequestEvent event, final Remote client)
  {
    Arguments.checkIsNotNull (event, "event");
    Arguments.checkIsNotNull (client, "client");

    log.trace ("Event received [{}]", event);

    // client is connected but not in game server; just ignore request event
    if (!clientsInServer.contains (client))
    {
      log.warn ("Ignoring join game request from player [{}] | REASON: unrecognized client [{}].",
                event.getPlayerName (), client);
      clientConnector.disconnect (client);
      return;
    }

    // spam guard: if client request is already being processed, ignore new request event
    if (playerJoinGameRequestCache.containsKey (event.getPlayerName ())) return;

    log.trace ("Received [{}] from player [{}]", event, event.getPlayerName ());

    playerJoinGameRequestCache.put (event.getPlayerName (), client);

    eventBus.publish (event);
  }

  void handleEvent (final ChatMessageRequestEvent event, final Remote client)
  {
    Arguments.checkIsNotNull (event, "event");
    Arguments.checkIsNotNull (client, "client");

    log.debug ("Event received [{}]", event);

    Optional <PlayerPacket> playerQuery;
    try
    {
      playerQuery = clientsToPlayers.playerFor (client);
    }
    catch (final RegisteredClientPlayerNotFoundException e)
    {
      log.error ("Error resolving client to player.", e);
      return;
    }

    if (!playerQuery.isPresent ())
    {
      log.warn ("Ignoring chat message [{}] from non-player client [{}]", event, client);
      return;
    }

    sendToAllPlayers (new ChatMessageSuccessEvent (
            new DefaultChatMessage (playerQuery.get (), event.getMessageText ())));
  }

  void handleEvent (final PlayerRequestEvent event, final Remote client)
  {
    Arguments.checkIsNotNull (event, "event");
    Arguments.checkIsNotNull (client, "client");

    log.trace ("Event received [{}]", event);

    Optional <PlayerPacket> playerQuery;
    try
    {
      playerQuery = clientsToPlayers.playerFor (client);
    }
    catch (final RegisteredClientPlayerNotFoundException e)
    {
      log.error ("Error resolving client to player.", e);
      return;
    }

    if (!playerQuery.isPresent ())
    {
      log.warn ("Ignoring event [{}] from non-player client [{}]", event, client);
      return;
    }

    final PlayerPacket player = playerQuery.get ();

    coreCommunicator.publishPlayerRequestEvent (player, event);
  }

  void handleEvent (final ResponseRequestEvent event, final Remote client)
  {
    Arguments.checkIsNotNull (event, "event");
    Arguments.checkIsNotNull (client, "client");

    log.trace ("Event received [{}]", event);

    Optional <PlayerPacket> playerQuery;
    try
    {
      playerQuery = clientsToPlayers.playerFor (client);
    }
    catch (final RegisteredClientPlayerNotFoundException e)
    {
      log.error ("Error resolving client to player.", e);
      return;
    }

    if (!playerQuery.isPresent ())
    {
      log.warn ("Ignoring event [{}] from non-player client [{}]", event, client);
      return;
    }

    final PlayerPacket player = playerQuery.get ();

    if (!waitingForResponseToEventFromPlayer (event.getRequestType (), player))
    {
      log.warn ("Ignoring event [{}] from player [{}] because no prior corresponding server request of type [{}] was sent to that player.",
                event, player, event.getRequestType ());
      return;
    }

    handlePlayerResponseTo (event.getRequestType (), event, player);
  }

  // <<<<< internal event utility methods and types >>>>>> //

  private static boolean isLocalHost (final Remote client)
  {
    return client.getAddress ().equals (NetworkConstants.LOCALHOST_ADDRESS);
  }

  private static ClientConfiguration createClientConfig (final String clientAddress, final int clientPort)
  {
    return new DefaultClientConfiguration (clientAddress, clientPort);
  }

  private boolean serverHasAddress ()
  {
    return !gameServerConfig.getServerAddress ().isEmpty ();
  }

  private String getServerAddress ()
  {
    return gameServerConfig.getServerAddress ();
  }

  private void sendToPlayer (final PlayerPacket player, final Object object)
  {
    playerCommunicator.sendToPlayer (player, object, clientsToPlayers);
  }

  private void sendToAllPlayers (final Object object)
  {
    playerCommunicator.sendToAllPlayers (object, clientsToPlayers);
  }

  private void remove (final Remote client)
  {
    clientsInServer.remove (client);
    clientsToPlayers.remove (client);
  }

  private void sendJoinGameServerSuccess (final Remote client, final ImmutableSet <PlayerPacket> players)
  {
    final Event successEvent = new JoinGameServerSuccessEvent (gameServerConfig,
            createClientConfig (client.getAddress (), client.getPort ()), players);

    playerCommunicator.sendTo (client, successEvent);
    clientsInServer.add (client);

    log.info ("Client [{}] successfully joined game server.", client);
  }

  private void sendJoinGameServerDenied (final Remote client, final String reason)
  {
    playerCommunicator.sendTo (client, new JoinGameServerDeniedEvent (
            new DefaultClientConfiguration (client.getAddress (), client.getPort ()), reason));

    clientConnector.disconnect (client);

    log.warn ("Denied [{}] from [{}]; REASON: {}", JoinGameServerRequestEvent.class.getSimpleName (), client, reason);
  }

  private boolean isHostConnected ()
  {
    return host != null && clientsInServer.contains (host);
  }

  private boolean isHostAndPlay ()
  {
    return gameServerConfig.getGameServerType () == GameServerType.HOST_AND_PLAY;
  }

  private boolean waitingForResponseToEventFromPlayer (final Class <? extends ServerRequestEvent> requestClass,
                                                       final PlayerPacket player)
  {
    for (final PlayerInputRequestEvent request : playerInputRequestEventCache.get (player))
    {
      if (requestClass.isInstance (request)) return true;
    }

    return false;
  }

  private void handlePlayerResponseTo (final Class <? extends ServerRequestEvent> requestClass,
                                       final ResponseRequestEvent responseRequest,
                                       final PlayerPacket player)
  {
    for (final PlayerInputRequestEvent request : playerInputRequestEventCache.get (player))
    {
      if (requestClass.isInstance (request))
      {
        coreCommunicator.publishPlayerResponseRequestEvent (player, responseRequest);
        final boolean wasRemoved = playerInputRequestEventCache.remove (player, request);
        assert wasRemoved;
        return;
      }
    }

    log.warn ("Ignoring event [{}] from player [{}] because no prior corresponding request of type [{}] was sent to that player.",
              responseRequest, player, requestClass);
  }

  private final class ClientConnectorDaemon
  {
    private final ExecutorService threadPool = Executors.newCachedThreadPool ();

    public void onConnect (final Remote client)
    {
      Arguments.checkIsNotNull (client, "client");

      threadPool.execute (new WaitForConnectionTask (client));
    }

    private class WaitForConnectionTask implements Runnable
    {
      private final Remote client;

      WaitForConnectionTask (final Remote client)
      {
        Arguments.checkIsNotNull (client, "client");

        this.client = client;
      }

      @Override
      public void run ()
      {
        try
        {
          Thread.sleep (connectionTimeoutMillis);
        }
        catch (final InterruptedException ignored)
        {
        }

        if (clientsInServer.contains (client)) return;

        clientConnector.disconnect (client);

        log.info ("Client connection timed out [{}].", client.getAddress ());
      }
    }
  }
}
