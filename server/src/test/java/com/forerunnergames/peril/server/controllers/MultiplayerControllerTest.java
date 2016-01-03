package com.forerunnergames.peril.server.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.forerunnergames.peril.common.eventbus.EventBusFactory;
import com.forerunnergames.peril.common.eventbus.EventBusHandler;
import com.forerunnergames.peril.common.game.DefaultGameConfiguration;
import com.forerunnergames.peril.common.game.GameConfiguration;
import com.forerunnergames.peril.common.game.GameMode;
import com.forerunnergames.peril.common.game.InitialCountryAssignment;
import com.forerunnergames.peril.common.game.rules.ClassicGameRules;
import com.forerunnergames.peril.common.map.DefaultMapMetadata;
import com.forerunnergames.peril.common.map.MapMetadata;
import com.forerunnergames.peril.common.map.MapType;
import com.forerunnergames.peril.common.net.DefaultGameServerConfiguration;
import com.forerunnergames.peril.common.net.GameServerConfiguration;
import com.forerunnergames.peril.common.net.GameServerType;
import com.forerunnergames.peril.common.net.events.client.request.JoinGameServerRequestEvent;
import com.forerunnergames.peril.common.net.events.client.request.PlayerJoinGameRequestEvent;
import com.forerunnergames.peril.common.net.events.client.request.PlayerRequestEvent;
import com.forerunnergames.peril.common.net.events.client.request.response.PlayerSelectCountryResponseRequestEvent;
import com.forerunnergames.peril.common.net.events.server.denied.JoinGameServerDeniedEvent;
import com.forerunnergames.peril.common.net.events.server.denied.PlayerJoinGameDeniedEvent;
import com.forerunnergames.peril.common.net.events.server.notification.PlayerLeaveGameEvent;
import com.forerunnergames.peril.common.net.events.server.request.PlayerSelectCountryRequestEvent;
import com.forerunnergames.peril.common.net.events.server.success.JoinGameServerSuccessEvent;
import com.forerunnergames.peril.common.net.events.server.success.PlayerJoinGameSuccessEvent;
import com.forerunnergames.peril.common.net.kryonet.KryonetRemote;
import com.forerunnergames.peril.common.net.packets.defaults.DefaultPlayerPacket;
import com.forerunnergames.peril.common.net.packets.person.PersonIdentity;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.peril.common.settings.GameSettings;
import com.forerunnergames.peril.core.model.people.player.PlayerColor;
import com.forerunnergames.peril.core.model.people.player.PlayerTurnOrder;
import com.forerunnergames.peril.server.communicators.CoreCommunicator;
import com.forerunnergames.peril.server.communicators.PlayerCommunicator;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Event;
import com.forerunnergames.tools.common.Strings;
import com.forerunnergames.tools.net.NetworkConstants;
import com.forerunnergames.tools.net.Remote;
import com.forerunnergames.tools.net.client.ClientCommunicator;
import com.forerunnergames.tools.net.client.ClientConfiguration;
import com.forerunnergames.tools.net.client.ClientConnector;
import com.forerunnergames.tools.net.events.local.ClientCommunicationEvent;
import com.forerunnergames.tools.net.events.local.ClientConnectionEvent;
import com.forerunnergames.tools.net.events.local.ClientDisconnectionEvent;
import com.forerunnergames.tools.net.events.remote.origin.client.ResponseRequestEvent;
import com.forerunnergames.tools.net.events.remote.origin.server.DeniedEvent;
import com.forerunnergames.tools.net.server.DefaultServerConfiguration;
import com.forerunnergames.tools.net.server.ServerConfiguration;

import com.google.common.collect.ImmutableSet;

import java.net.InetSocketAddress;
import java.util.UUID;

import net.engio.mbassy.bus.MBassador;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class MultiplayerControllerTest
{
  private static final String DEFAULT_TEST_GAME_SERVER_NAME = "test-server";
  private static final GameServerType DEFAULT_GAME_SERVER_TYPE = GameServerType.DEDICATED;
  private static final String DEFAULT_TEST_SERVER_ADDRESS = "server@test";
  private static final int DEFAULT_TEST_SERVER_PORT = 8888;
  private final EventBusHandler eventHandler = new EventBusHandler ();
  private final ClientConnector mockClientConnector = mock (ClientConnector.class, Mockito.RETURNS_SMART_NULLS);
  private final ClientCommunicator mockClientCommunicator = mock (ClientCommunicator.class,
                                                                  Mockito.RETURNS_SMART_NULLS);
  private final CoreCommunicator mockCoreCommunicator = mock (CoreCommunicator.class, Mockito.RETURNS_SMART_NULLS);
  private final MultiplayerControllerBuilder mpcBuilder = builder (mockClientConnector,
                                                                   new PlayerCommunicator (mockClientCommunicator),
                                                                   mockCoreCommunicator);
  private int clientCount = 0;
  private MBassador <Event> eventBus;

  @Before
  public void setup ()
  {
    eventBus = EventBusFactory.create (ImmutableSet.of (EventBusHandler.createEventBusFailureHandler ()));
    eventHandler.subscribe (eventBus);

    initializeCoreCommunication (); // default behavior - core returns empty player data
  }

  @After
  public void tearDown ()
  {
    eventHandler.unsubscribe (eventBus);
    eventBus.shutdown ();
  }

  @Test
  public void testSuccessfulHostClientJoinGameServer ()
  {
    mpcBuilder.gameServerType (GameServerType.HOST_AND_PLAY).build (eventBus);

    final Remote host = createAndAddHost ();
    verify (mockClientCommunicator, only ())
            .sendTo (eq (host), argThat (new JoinGameServerSuccessEventMatcher (host, createDefaultServerConfig ())));
  }

  @Test
  public void testSuccessfulClientJoinGameServer ()
  {
    mpcBuilder.build (eventBus);

    final Remote client = createAndAddClient ();
    verify (mockClientCommunicator, only ())
            .sendTo (eq (client),
                     argThat (new JoinGameServerSuccessEventMatcher (client, createDefaultServerConfig ())));
  }

  @Test
  public void testClientJoinRequestBeforeHostDenied ()
  {
    mpcBuilder.gameServerType (GameServerType.HOST_AND_PLAY).build (eventBus);

    // @formatter:off
    final Remote client = createAndAddClient ();
    verify (mockClientCommunicator, only ()).sendTo (eq (client), argThat (new JoinGameServerDeniedEventMatcher (client)));
    verify (mockClientConnector, only ()).disconnect (eq (client));
    // @formatter:on
  }

  @Test
  public void testHostClientJoinDedicatedGameServerDenied ()
  {
    mpcBuilder.gameServerType (GameServerType.DEDICATED).build (eventBus);

    final Remote host = createAndAddHost ();
    verify (mockClientCommunicator, only ()).sendTo (eq (host), argThat (new JoinGameServerDeniedEventMatcher (host)));
  }

  @Test
  public void testNonHostClientJoinGameServerAfterHostSuccessful ()
  {
    mpcBuilder.gameServerType (GameServerType.HOST_AND_PLAY).build (eventBus);

    final ServerConfiguration serverConfig = createDefaultServerConfig ();

    final Remote host = createAndAddHost ();
    verify (mockClientCommunicator, only ())
            .sendTo (eq (host), argThat (new JoinGameServerSuccessEventMatcher (host, serverConfig)));

    final Remote client = createAndAddClient ();
    verify (mockClientCommunicator).sendTo (eq (client),
                                            argThat (new JoinGameServerSuccessEventMatcher (client, serverConfig)));
  }

  @Test
  public void testHostClientJoinGameServerAfterHostDenied ()
  {
    mpcBuilder.gameServerType (GameServerType.HOST_AND_PLAY).build (eventBus);

    final Remote host = createAndAddHost ();
    verify (mockClientCommunicator, only ())
            .sendTo (eq (host), argThat (new JoinGameServerSuccessEventMatcher (host, createDefaultServerConfig ())));

    final Remote duplicateHost = createAndAddHost ();
    verify (mockClientCommunicator).sendTo (eq (duplicateHost),
                                            argThat (new JoinGameServerDeniedEventMatcher (duplicateHost)));
  }

  @Test
  public void testClientJoinRequestDeniedBecauseInvalidIpEmpty ()
  {
    mpcBuilder.build (eventBus);

    // @formatter:off
    final Remote client = createAndAddClientWithAddress ("");
    verify (mockClientCommunicator, only ()).sendTo (eq (client), argThat (new JoinGameServerDeniedEventMatcher (client)));
    verify (mockClientConnector, only ()).disconnect (eq (client));
    // @formatter:on
  }

  @Test
  public void testClientJoinRequestDeniedBecauseMatchesServerIp ()
  {
    mpcBuilder.serverAddress ("1.2.3.4").build (eventBus);

    // @formatter:off
    final Remote client = createAndAddClientWithAddress ("1.2.3.4");
    verify (mockClientCommunicator, only ()).sendTo (eq (client), argThat (new JoinGameServerDeniedEventMatcher (client)));
    verify (mockClientConnector, only ()).disconnect (eq (client));
    // @formatter:on
  }

  @Test
  public void testValidPlayerJoinGameRequestPublished ()
  {
    mpcBuilder.build (eventBus);

    final Remote client = createAndAddClient ();
    verify (mockClientCommunicator, only ())
            .sendTo (eq (client),
                     argThat (new JoinGameServerSuccessEventMatcher (client, createDefaultServerConfig ())));

    final String playerName = "Test Player 1";
    final Event event = new PlayerJoinGameRequestEvent (playerName);
    simulateClientCommunication (event, client);
    assertLastEventWas (event);
  }

  @Test
  public void testIgnorePlayerJoinGameRequestBeforeJoiningGameServer ()
  {
    mpcBuilder.build (eventBus);

    // Connect client to server, but do not join client to game server.
    final Remote client = createClient ();
    simulateClientConnection (client);

    // Simulate bad request.
    final Event event = simulateClientCommunication (new PlayerJoinGameRequestEvent ("Test Player 1"), client);

    // Assert that no event was published after receiving bad request.
    assertLastEventWas (event);

    // Verify the controller did not send anything via the communicator.
    verifyNoMoreInteractions (mockClientCommunicator);
  }

  @Test
  public void testPlayerJoinGameSuccess ()
  {
    addClientAndMockPlayerToGameServer ("Test Player 1", mpcBuilder.build (eventBus));
  }

  @Test
  public void testPlayerJoinGameDenied ()
  {
    final MultiplayerController mpc = mpcBuilder.build (eventBus);
    final Remote client = addClientToGameServer ();

    final String playerName = "Test-Player-0";
    simulateClientCommunication (new PlayerJoinGameRequestEvent (playerName), client);

    final PlayerPacket mockPlayer = createPlayer (playerName);

    // @formatter:off
    // make up a reason... doesn't have to be true :)
    final PlayerJoinGameDeniedEvent.Reason reason = PlayerJoinGameDeniedEvent.Reason.DUPLICATE_ID;
    final DeniedEvent <PlayerJoinGameDeniedEvent.Reason> deniedEvent = new PlayerJoinGameDeniedEvent (playerName, reason);
    // @formatter:on

    initializeCoreCommunication (mockPlayer);
    simulateCoreCommunication (deniedEvent);

    verify (mockClientCommunicator).sendTo (eq (client), eq (deniedEvent));
    assertFalse (mpc.isPlayerInGame (mockPlayer));
  }

  @Test
  public void testPlayerLeaveGame ()
  {
    final MultiplayerController mpc = mpcBuilder.build (eventBus);
    final ClientPlayerTuple clientPlayer = addClientAndMockPlayerToGameServer ("Test Player 1", mpc);

    initializeCoreCommunication (clientPlayer.player ());

    simulateClientDisconnection (clientPlayer.client ());
    verify (mockCoreCommunicator).notifyRemovePlayerFromGame (eq (clientPlayer.player ()));
    simulateCoreCommunication (new PlayerLeaveGameEvent (clientPlayer.player (), ImmutableSet.<PlayerPacket> of ()));

    // make sure nothing was sent to the disconnecting player
    verify (mockClientCommunicator, never ()).sendTo (eq (clientPlayer.client ()), isA (PlayerLeaveGameEvent.class));
    assertFalse (mpc.isPlayerInGame (clientPlayer.player ()));
  }

  @Test
  public void testValidPlayerSelectCountryResponseRequestEvent ()
  {
    // Create a game server with manual initial country assignment.
    final MultiplayerController mpc = mpcBuilder.initialCountryAssignment (InitialCountryAssignment.MANUAL)
            .build (eventBus);

    final ClientPlayerTuple clientPlayer = addClientAndMockPlayerToGameServer ("Test Player 1", mpc);

    initializeCoreCommunication (clientPlayer.player ());

    // Request that the player/client select an available country.
    simulateCoreCommunication (new PlayerSelectCountryRequestEvent (clientPlayer.player ()));
    verify (mockClientCommunicator).sendTo (eq (clientPlayer.client ()), isA (PlayerSelectCountryRequestEvent.class));

    // Simulate player/client selecting a country.
    final Event event = new PlayerSelectCountryResponseRequestEvent ("Test Country 1");
    simulateClientCommunication (event, clientPlayer.client ());

    // Verify that player/client's country selection was published.
    assertEventFiredExactlyOnce (PlayerSelectCountryResponseRequestEvent.class);
    assertEventFiredExactlyOnce (event);
  }

  @Test
  public void testInvalidPlayerSelectCountryResponseRequestEventIgnoredBecauseClientIsNotAPlayer ()
  {
    // Create a game server with manual initial country assignment.
    final MultiplayerController mpc = mpcBuilder.initialCountryAssignment (InitialCountryAssignment.MANUAL)
            .build (eventBus);

    final ClientPlayerTuple clientPlayer = addClientAndMockPlayerToGameServer ("Test Player 1", mpc);

    // Simulate player/client selecting a country.
    final Event event = simulateClientCommunication (new PlayerSelectCountryResponseRequestEvent ("Test Country 1"),
                                                     clientPlayer.client ());

    // Verify that player/client's country selection was NOT published.
    assertLastEventWas (event);
  }

  @Test
  public void testInvalidPlayerSelectCountryResponseRequestEventBecauseWrongClient ()
  {
    // Create a game server with manual initial country assignment.
    final MultiplayerController mpc = mpcBuilder.initialCountryAssignment (InitialCountryAssignment.MANUAL)
            .build (eventBus);

    final ClientPlayerTuple first = addClientAndMockPlayerToGameServer ("Test Player 1", mpc);
    final ClientPlayerTuple second = addClientAndMockPlayerToGameServer ("Test Player 2", mpc);

    initializeCoreCommunication (first.player (), second.player ());

    // Request that the player/client select an available country.
    simulateCoreCommunication (new PlayerSelectCountryRequestEvent (first.player ()));
    verify (mockClientCommunicator).sendTo (eq (first.client ()), isA (PlayerSelectCountryRequestEvent.class));

    // Simulate WRONG player/client selecting a country.
    final Event event = simulateClientCommunication (new PlayerSelectCountryResponseRequestEvent ("Test Country 1"),
                                                     second.client ());

    // Verify that player/client's country selection was NOT published.
    assertLastEventWas (event);
  }

  @Test
  public void testInvalidPlayerSelectCountryResponseRequestEventBecauseWrongClientAfterMultipleRequests ()
  {
    // Create a game server with manual initial country assignment.
    final MultiplayerController mpc = mpcBuilder.initialCountryAssignment (InitialCountryAssignment.MANUAL)
            .build (eventBus);

    final ClientPlayerTuple first = addClientAndMockPlayerToGameServer ("Test Player 1", mpc);
    final ClientPlayerTuple second = addClientAndMockPlayerToGameServer ("Test Player 2", mpc);

    initializeCoreCommunication (first.player (), second.player ());

    // Request that the first player/client select an available country.
    final Event selectCountryRequestEvent1 = new PlayerSelectCountryRequestEvent (first.player ());
    simulateCoreCommunication (selectCountryRequestEvent1);
    verify (mockClientCommunicator).sendTo (first.client (), selectCountryRequestEvent1);
    // Make sure that the request was not sent to the second player/client.
    verify (mockClientCommunicator, never ()).sendTo (second.client (), selectCountryRequestEvent1);

    // Simulate & verify first player/client selecting a country.
    final Event selectCountryResponseRequestEvent1 = new PlayerSelectCountryResponseRequestEvent ("Test Country 1");
    simulateClientCommunication (selectCountryResponseRequestEvent1, first.client ());
    assertLastEventWas (selectCountryResponseRequestEvent1);

    // Request that the second player/client select an available country.
    final Event selectCountryRequestEvent2 = new PlayerSelectCountryRequestEvent (second.player ());
    simulateCoreCommunication (selectCountryRequestEvent2);
    verify (mockClientCommunicator).sendTo (second.client (), selectCountryRequestEvent2);
    // Make sure that the request was not sent to the first player/client.
    verify (mockClientCommunicator, never ()).sendTo (first.client (), selectCountryRequestEvent2);

    // Simulate & verify second player/client selecting a country.
    final Event selectCountryResponseRequestEvent2 = new PlayerSelectCountryResponseRequestEvent ("Test Country 2");
    simulateClientCommunication (selectCountryResponseRequestEvent2, second.client ());
    assertLastEventWas (selectCountryResponseRequestEvent2);

    // Request that the first player/client select an available country.
    final Event selectCountryRequestEvent3 = new PlayerSelectCountryRequestEvent (first.player ());
    simulateCoreCommunication (selectCountryRequestEvent3);
    verify (mockClientCommunicator).sendTo (first.client (), selectCountryRequestEvent3);
    // Make sure that the request was not sent to the second player/client.
    verify (mockClientCommunicator, never ()).sendTo (second.client (), selectCountryRequestEvent3);

    // Simulate & verify first player/client selecting a country.
    final Event selectCountryResponseRequestEvent3 = new PlayerSelectCountryResponseRequestEvent ("Test Country 3");
    simulateClientCommunication (selectCountryResponseRequestEvent3, first.client ());
    assertLastEventWas (selectCountryResponseRequestEvent3);

    // Request that the second player/client select an available country.
    final Event selectCountryRequestEvent4 = new PlayerSelectCountryRequestEvent (second.player ());
    simulateCoreCommunication (selectCountryRequestEvent4);
    verify (mockClientCommunicator).sendTo (second.client (), selectCountryRequestEvent4);
    // Make sure that the request was not sent to the first player/client.
    verify (mockClientCommunicator, never ()).sendTo (first.client (), selectCountryRequestEvent4);

    // Simulate & verify second player/client selecting a country.
    final Event selectCountryResponseRequestEvent4 = new PlayerSelectCountryResponseRequestEvent ("Test Country 4");
    simulateClientCommunication (selectCountryResponseRequestEvent4, second.client ());
    assertLastEventWas (selectCountryResponseRequestEvent4);

    // Request that the first player/client select an available country.
    final Event selectCountryRequestEvent5 = new PlayerSelectCountryRequestEvent (first.player ());
    simulateCoreCommunication (selectCountryRequestEvent5);
    verify (mockClientCommunicator).sendTo (first.client (), selectCountryRequestEvent5);
    // Make sure that the request was not sent to the second player/client.
    verify (mockClientCommunicator, never ()).sendTo (second.client (), selectCountryRequestEvent5);

    // Simulate WRONG (second) player/client selecting a country.
    final Event event = simulateClientCommunication (new PlayerSelectCountryResponseRequestEvent ("Test Country 5"),
                                                     second.client ());

    // Verify that player/client's country selection was NOT published.
    assertLastEventWas (event);
  }

  @Test
  public void testInvalidPlayerSelectCountryResponseRequestEventBecauseNoPriorRequestSentFromServer ()
  {
    // Create a game server with manual initial country assignment.
    final MultiplayerController mpc = mpcBuilder.initialCountryAssignment (InitialCountryAssignment.MANUAL)
            .build (eventBus);

    final ClientPlayerTuple first = addClientAndMockPlayerToGameServer ("Test Player 1", mpc);

    initializeCoreCommunication (first.player ());

    // Simulate player/client selecting a country BEFORE receiving a request to do so from the server.
    final Event event = simulateClientCommunication (new PlayerSelectCountryResponseRequestEvent ("Test Country 1"),
                                                     first.client ());

    // Verify that player/client's country selection was NOT published.
    assertLastEventWas (event);
  }

  @Test
  public void testPersonIdentitySetToSelfOnPlayerEvent ()
  {
    final MultiplayerController mpc = mpcBuilder.build (eventBus);

    // We can't mock the player because we need real behavior for PersonIdentity-related methods.
    final ClientPlayerTuple clientPlayer1 = addClientAndRealPlayerToGameServer ("Test Player 1", mpc);

    // Prepare to capture the instance of the PlayerJoinGameSuccessEvent that
    // was sent to player 1 regarding player 1 joining.
    //
    // Verify times 2 for player/client 1:
    // 1) sent JoinGameServerSuccessEvent about client 1 to client 1
    // 2) sent PlayerJoinGameSuccessEvent about player 1 to player/client 1 (PersonIdentity.SELF)
    //
    // We are testing #2, so obtain the last capture.
    ArgumentCaptor <Object> sentDataCaptor = ArgumentCaptor.forClass (Object.class);
    verify (mockClientCommunicator, times (2)).sendTo (eq (clientPlayer1.client ()), sentDataCaptor.capture ());
    assertTrue (sentDataCaptor.getValue () instanceof PlayerJoinGameSuccessEvent);
    final PlayerJoinGameSuccessEvent event = (PlayerJoinGameSuccessEvent) sentDataCaptor.getValue ();

    // PersonIdentity of player specified in event is set to SELF when sent to the client whose player
    // matches the player contained in the event (i.e., it's about himself).
    assertTrue (event.getPlayer ().equals (clientPlayer1.player ()));
    assertTrue ("Event: " + event + " Argument captures: " + sentDataCaptor.getAllValues (),
                event.getPlayer ().has (PersonIdentity.SELF));
  }

  @Test
  public void testPersonIdentitySetToNonSelfOnPlayerEvent ()
  {
    final MultiplayerController mpc = mpcBuilder.build (eventBus);

    // We can't mock the players because we need real behavior for PersonIdentity-related methods.
    final ClientPlayerTuple clientPlayer1 = addClientAndRealPlayerToGameServer ("Test Player 1", mpc);
    final ClientPlayerTuple clientPlayer2 = addClientAndRealPlayerToGameServer ("Test Player 2", mpc);

    // Prepare to capture the instance of the PlayerJoinGameSuccessEvent that
    // was sent to player 1 regarding player 2 joining.
    //
    // Verify times 3 for player/client 1:
    // 1) sent JoinGameServerSuccessEvent about client 1 to client 1
    // 2) sent PlayerJoinGameSuccessEvent about player 1 to player/client 1 (PersonIdentity.SELF)
    // 3) sent PlayerJoinGameSuccessEvent about player 2 to player/client 1 (PersonIdentity.NON_SELF)
    //
    // We are testing #3, so obtain the last capture.
    ArgumentCaptor <Object> sentDataCaptor = ArgumentCaptor.forClass (Object.class);
    verify (mockClientCommunicator, times (5)).sendTo (any (Remote.class), sentDataCaptor.capture ());
    assertTrue (sentDataCaptor.getValue () instanceof PlayerJoinGameSuccessEvent);
    final PlayerJoinGameSuccessEvent event = (PlayerJoinGameSuccessEvent) sentDataCaptor.getValue ();

    // PersonIdentity of player specified in event is set to NON_SELF when sent to the client whose player
    // does not match the player contained in the event (i.e., it's about someone else).
    assertTrue (event.getPlayer ().equals (clientPlayer2.player ()));
    assertTrue ("Event: " + event + " Argument captures: " + Strings.toString (sentDataCaptor.getAllValues ()),
                event.getPlayer ().has (PersonIdentity.NON_SELF));
  }

  @Test
  public void testStalePlayerPacketDataIsUpdatedOnAccess ()
  {
    final MultiplayerController mpc = mpcBuilder.build (eventBus);

    final String playerName = "TestPlayer";
    addClientAndMockPlayerToGameServer (playerName, mpc);

    final PlayerPacket player = createPlayer (playerName);
    final int armiesInHand = 5;
    final PlayerPacket updatedPlayer = createPlayer (playerName, armiesInHand);

    initializeCoreCommunication (updatedPlayer);

    // TODO ... need some mechanism for polling player data from core/server
  }

  // unit test for bug detailed in PERIL-100: https://forerunnergames.atlassian.net/browse/PERIL-100
  @Test
  public void testClientDisconnectAfterSendingPlayerJoinGameRequest ()
  {
    final MultiplayerController mpc = mpcBuilder.build (eventBus);
    final Remote client = addClientToGameServer ();
    final String playerName = "TestPlayer";
    final PlayerPacket player = createPlayer (playerName);
    simulateClientCommunication (new PlayerJoinGameRequestEvent (playerName), client);
    simulateClientDisconnection (client);
    assertFalse (mpc.isClientInServer (client));
    simulateCoreCommunication (new PlayerJoinGameSuccessEvent (player));
    verify (mockCoreCommunicator).notifyRemovePlayerFromGame (eq (player));
  }

  // convenience method for fetching a new MultiplayerControllerBuilder
  // Note: package private visibility is intended; other test classes in package should have access.
  static MultiplayerControllerBuilder builder (final ClientConnector connector,
                                               final PlayerCommunicator communicator,
                                               final CoreCommunicator coreCommunicator)
  {
    Arguments.checkIsNotNull (connector, "connector");
    Arguments.checkIsNotNull (communicator, "communicator");

    return new MultiplayerControllerBuilder (connector, communicator, coreCommunicator);
  }

  // <<<<<<<<<<<< Test helper facilities >>>>>>>>>>>>>> //

  private ClientPlayerTuple addClientAndMockPlayerToGameServer (final String playerName,
                                                                final MultiplayerController mpc)
  {
    final Remote client = addClientToGameServer ();
    return addMockPlayerToGameServer (playerName, client, mpc);
  }

  private Remote addClientToGameServer ()
  {
    final Remote client = createAndAddClient ();
    verify (mockClientCommunicator)
            .sendTo (eq (client),
                     argThat (new JoinGameServerSuccessEventMatcher (client, createDefaultServerConfig ())));

    return client;
  }

  private ClientPlayerTuple addMockPlayerToGameServer (final String playerName,
                                                       final Remote client,
                                                       final MultiplayerController mpc)
  {
    return addPlayerToGameServer (new ClientPlayerTuple (client, createPlayer (playerName)), mpc);
  }

  private ClientPlayerTuple addClientAndRealPlayerToGameServer (final String playerName,
                                                                final MultiplayerController mpc)
  {
    final Remote client = addClientToGameServer ();

    return addPlayerToGameServer (new ClientPlayerTuple (client, createPlayer (playerName)), mpc);
  }

  private ClientPlayerTuple addPlayerToGameServer (final ClientPlayerTuple clientPlayer,
                                                   final MultiplayerController mpc)
  {
    simulateClientCommunication (new PlayerJoinGameRequestEvent (clientPlayer.playerName ()), clientPlayer.client ());
    final PlayerJoinGameSuccessEvent event = new PlayerJoinGameSuccessEvent (clientPlayer.player ());
    simulateCoreCommunication (event);
    verify (mockClientCommunicator).sendTo (eq (clientPlayer.client ()),
                                            argThat (new PlayerJoinGameSuccessEventMatcher (event.getPlayer ())));
    assertTrue (mpc.isPlayerInGame (clientPlayer.player ()));

    return clientPlayer;
  }

  private PlayerPacket createPlayer (final String playerName)
  {
    return createPlayer (playerName, 0);
  }

  private PlayerPacket createPlayer (final String playerName, final int armiesInHand)
  {
    return new DefaultPlayerPacket (UUID.randomUUID (), playerName, PlayerColor.UNKNOWN.name (),
            PlayerTurnOrder.UNKNOWN.asInt (), armiesInHand);
  }

  private void initializeCoreCommunication (final PlayerPacket... players)
  {
    when (mockCoreCommunicator.fetchCurrentPlayerData ()).thenReturn (ImmutableSet.copyOf (players));

    // mock core communicator request publishing
    doAnswer (new Answer <InvocationOnMock> ()
    {
      @Override
      public InvocationOnMock answer (final InvocationOnMock invocation) throws Throwable
      {
        simulateCoreCommunication ((Event) invocation.getArguments () [1]);
        return null;
      }
    }).when (mockCoreCommunicator).publishPlayerResponseRequestEvent (any (PlayerPacket.class),
                                                                      any (ResponseRequestEvent.class));
    doAnswer (new Answer <InvocationOnMock> ()
    {
      @Override
      public InvocationOnMock answer (final InvocationOnMock invocation) throws Throwable
      {
        simulateCoreCommunication ((Event) invocation.getArguments () [1]);
        return null;
      }
    }).when (mockCoreCommunicator).publishPlayerRequestEvent (any (PlayerPacket.class), any (PlayerRequestEvent.class));
  }

  private void assertLastEventWasType (final Class <?> eventType)
  {
    assertTrue ("Expected last event was type [" + eventType.getSimpleName () + "], but was ["
            + eventHandler.lastEventType () + "] All events (newest to oldest): [" + eventHandler.getAllEvents ()
            + "].", eventHandler.lastEventWasType (eventType));
  }

  private void assertLastEventWas (final Event event)
  {
    assertEquals ("Expected last event was [" + event + "], but was [" + eventHandler.lastEvent ()
            + "] All events (newest to oldest): [" + eventHandler.getAllEvents () + "].", event,
                  eventHandler.lastEvent ());
  }

  private void assertEventFiredExactlyOnce (final Class <?> eventType)
  {
    assertTrue ("Expected event type [" + eventType.getSimpleName () + "] was fired exactly once, but was fired ["
            + eventHandler.countOf (eventType) + "] times. All events (newest to oldest): ["
            + eventHandler.getAllEvents () + "].", eventHandler.wasFiredExactlyOnce (eventType));
  }

  private void assertEventFiredExactlyOnce (final Event event)
  {
    assertTrue ("Expected event type [" + event.getClass ().getSimpleName ()
            + "] was fired exactly once, but was fired [" + eventHandler.countOf (event.getClass ())
            + "] times. All events (newest to oldest): [" + eventHandler.getAllEvents () + "].",
                eventHandler.wasFiredExactlyOnce (event));
  }

  private ClientCommunicationEvent simulateClientCommunication (final Event event, final Remote client)
  {
    final ClientCommunicationEvent clientCommunicationEvent = new ClientCommunicationEvent (event, client);

    eventBus.publish (clientCommunicationEvent);

    return clientCommunicationEvent;
  }

  private void simulateCoreCommunication (final Event event)
  {
    eventBus.publish (event);
  }

  private Remote createHost ()
  {
    return createClientWithAddress (NetworkConstants.LOCALHOST_ADDRESS);
  }

  private Remote createClient ()
  {
    return createClientWithAddress ("forerunnergames.com");
  }

  private Remote createClientWithAddress (final String address)
  {
    final int port = 1000 + clientCount;

    return new KryonetRemote (clientCount++, new InetSocketAddress (address, port));
  }

  private Remote createAndAddHost ()
  {
    return addClient (createHost ());
  }

  private Remote createAndAddClient ()
  {
    return addClient (createClient ());
  }

  private Remote createAndAddClientWithAddress (final String address)
  {
    return addClient (createClientWithAddress (address));
  }

  private Remote addClient (final Remote client)
  {
    simulateClientConnection (client);
    simulateClientCommunication (new JoinGameServerRequestEvent (), client);

    return client;
  }

  private void simulateClientConnection (final Remote client)
  {
    eventBus.publish (new ClientConnectionEvent (client));
  }

  private void simulateClientDisconnection (final Remote client)
  {
    eventBus.publish (new ClientDisconnectionEvent (client));
  }

  private ServerConfiguration createDefaultServerConfig ()
  {
    return new DefaultServerConfiguration (DEFAULT_TEST_SERVER_ADDRESS, DEFAULT_TEST_SERVER_PORT);
  }

  private class JoinGameServerSuccessEventMatcher extends TypeSafeMatcher <JoinGameServerSuccessEvent>
  {
    private final Remote client;
    private final ServerConfiguration serverConfig;

    JoinGameServerSuccessEventMatcher (final Remote client, final ServerConfiguration serverconfig)
    {
      Arguments.checkIsNotNull (client, "client");
      Arguments.checkIsNotNull (serverconfig, "serverconfig");

      this.client = client;
      this.serverConfig = serverconfig;
    }

    @Override
    protected boolean matchesSafely (final JoinGameServerSuccessEvent item)
    {
      final ServerConfiguration matchServerConfig = item.getGameServerConfiguration ();
      final ClientConfiguration matchClientConfig = item.getClientConfiguration ();
      return matchServerConfig.getServerAddress ().equals (serverConfig.getServerAddress ())
              && matchClientConfig.getClientAddress ().equals (client.getAddress ())
              && matchServerConfig.getServerTcpPort () == serverConfig.getServerTcpPort ()
              && matchClientConfig.getClientTcpPort () == client.getPort ();
    }

    @Override
    public void describeTo (final Description description)
    {
    }
  }

  private class JoinGameServerDeniedEventMatcher extends TypeSafeMatcher <JoinGameServerDeniedEvent>
  {
    private final Remote client;

    JoinGameServerDeniedEventMatcher (final Remote client)
    {
      Arguments.checkIsNotNull (client, "client");

      this.client = client;
    }

    @Override
    protected boolean matchesSafely (final JoinGameServerDeniedEvent item)
    {
      final ClientConfiguration matchClientConfig = item.getClientConfiguration ();
      return matchClientConfig.getClientAddress ().equals (client.getAddress ())
              && matchClientConfig.getClientTcpPort () == client.getPort ();
    }

    @Override
    public void describeTo (final Description arg0)
    {
    }
  }

  private class PlayerJoinGameSuccessEventMatcher extends TypeSafeMatcher <PlayerJoinGameSuccessEvent>
  {
    private final PlayerPacket player;

    PlayerJoinGameSuccessEventMatcher (final PlayerPacket player)
    {
      Arguments.checkIsNotNull (player, "player");

      this.player = player;
    }

    @Override
    protected boolean matchesSafely (final PlayerJoinGameSuccessEvent item)
    {
      return item.getPlayer ().equals (player);
    }

    @Override
    public void describeTo (final Description arg0)
    {
    }
  }

  /*
   * Configurable test builder for MultiplayerController. Returns default values if left unchanged.
   */
  static class MultiplayerControllerBuilder
  {
    private final ClientConnector connector;
    private final PlayerCommunicator communicator;
    private final CoreCommunicator coreCommunicator;
    // game configuration fields
    private final GameMode gameMode = GameMode.CLASSIC;
    private final MapMetadata mapMetadata = new DefaultMapMetadata (GameSettings.DEFAULT_CLASSIC_MODE_MAP_NAME,
            MapType.STOCK, gameMode);
    private InitialCountryAssignment initialCountryAssignment = ClassicGameRules.DEFAULT_INITIAL_COUNTRY_ASSIGNMENT;
    // game server configuration fields
    private String gameServerName = DEFAULT_TEST_GAME_SERVER_NAME;
    private GameServerType gameServerType = DEFAULT_GAME_SERVER_TYPE;
    // server configuration fields
    private String serverAddress = DEFAULT_TEST_SERVER_ADDRESS;
    private int serverPort = DEFAULT_TEST_SERVER_PORT;
    private int playerLimit = ClassicGameRules.DEFAULT_PLAYER_LIMIT;
    private int winPercent = ClassicGameRules.DEFAULT_WIN_PERCENTAGE;

    MultiplayerControllerBuilder gameServerName (final String gameServerName)
    {
      Arguments.checkIsNotNull (gameServerName, "gameServerName");

      this.gameServerName = gameServerName;
      return this;
    }

    MultiplayerControllerBuilder gameServerType (final GameServerType gameServerType)
    {
      Arguments.checkIsNotNull (gameServerType, "gameServerType");

      this.gameServerType = gameServerType;
      return this;
    }

    MultiplayerControllerBuilder serverAddress (final String serverAddress)
    {
      Arguments.checkIsNotNull (serverAddress, "serverAddress");

      this.serverAddress = serverAddress;
      return this;
    }

    MultiplayerControllerBuilder serverPort (final int serverPort)
    {
      Arguments.checkIsNotNegative (serverPort, "serverPort");
      Arguments.checkUpperInclusiveBound (serverPort, NetworkConstants.MAX_PORT, "serverPort");

      this.serverPort = serverPort;
      return this;
    }

    MultiplayerControllerBuilder playerLimit (final int playerLimit)
    {
      Arguments.checkIsNotNegative (playerLimit, "playerLimit");

      this.playerLimit = playerLimit;
      return this;
    }

    MultiplayerControllerBuilder winPercent (final int winPercent)
    {
      Arguments.checkIsNotNegative (winPercent, "winPercent");
      Arguments.checkUpperInclusiveBound (winPercent, 100, "winPercent");

      this.winPercent = winPercent;
      return this;
    }

    MultiplayerControllerBuilder initialCountryAssignment (final InitialCountryAssignment initialCountryAssignment)
    {
      Arguments.checkIsNotNull (initialCountryAssignment, "initialCountryAssignment");

      this.initialCountryAssignment = initialCountryAssignment;
      return this;
    }

    MultiplayerController build (final MBassador <Event> eventBus)
    {
      Arguments.checkIsNotNull (eventBus, "eventBus");

      final GameConfiguration gameConfig = new DefaultGameConfiguration (gameMode, playerLimit, winPercent,
              initialCountryAssignment, mapMetadata);

      final ServerConfiguration serverConfig = new DefaultServerConfiguration (serverAddress, serverPort);

      final GameServerConfiguration gameServerConfig = new DefaultGameServerConfiguration (gameServerName,
              gameServerType, gameConfig, serverConfig);

      final MultiplayerController controller = new MultiplayerController (gameServerConfig, connector, communicator,
              coreCommunicator, eventBus);

      controller.initialize ();

      return controller;
    }

    // add game mode and/or initial-country-assignment later if needed

    private MultiplayerControllerBuilder (final ClientConnector connector,
                                          final PlayerCommunicator communicator,
                                          final CoreCommunicator coreCommunicator)
    {
      this.connector = connector;
      this.communicator = communicator;
      this.coreCommunicator = coreCommunicator;
    }
  }

  private final class ClientPlayerTuple
  {
    private final Remote client;
    private final PlayerPacket player;

    ClientPlayerTuple (final Remote client, final PlayerPacket player)
    {
      Arguments.checkIsNotNull (client, "client");
      Arguments.checkIsNotNull (player, "player");

      this.client = client;
      this.player = player;
    }

    public Remote client ()
    {
      return client;
    }

    public PlayerPacket player ()
    {
      return player;
    }

    public String playerName ()
    {
      return player.getName ();
    }

    @Override
    public int hashCode ()
    {
      int result = client.hashCode ();
      result = 31 * result + player.hashCode ();
      return result;
    }

    @Override
    public boolean equals (final Object obj)
    {
      if (this == obj) return true;
      if (obj == null || getClass () != obj.getClass ()) return false;
      final ClientPlayerTuple clientPlayerTuple = (ClientPlayerTuple) obj;
      return client.equals (clientPlayerTuple.client) && player.equals (clientPlayerTuple.player);
    }

    @Override
    public String toString ()
    {
      return Strings.format ("{}: Client: {} | Player: {}", getClass ().getSimpleName (), client, player);
    }
  }
}
