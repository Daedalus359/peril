package com.forerunnergames.peril.server.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.forerunnergames.peril.core.model.rules.ClassicGameRules;
import com.forerunnergames.peril.core.model.rules.DefaultGameConfiguration;
import com.forerunnergames.peril.core.model.rules.GameConfiguration;
import com.forerunnergames.peril.core.model.rules.GameMode;
import com.forerunnergames.peril.core.model.rules.InitialCountryAssignment;
import com.forerunnergames.peril.core.shared.EventBusHandler;
import com.forerunnergames.peril.core.shared.eventbus.EventBusFactory;
import com.forerunnergames.peril.core.shared.net.DefaultGameServerConfiguration;
import com.forerunnergames.peril.core.shared.net.GameServerConfiguration;
import com.forerunnergames.peril.core.shared.net.GameServerType;
import com.forerunnergames.peril.core.shared.net.events.client.request.CreateGameServerRequestEvent;
import com.forerunnergames.peril.core.shared.net.events.client.request.JoinGameServerRequestEvent;
import com.forerunnergames.peril.core.shared.net.events.client.request.PlayerJoinGameRequestEvent;
import com.forerunnergames.peril.core.shared.net.events.client.request.response.PlayerSelectCountryResponseRequestEvent;
import com.forerunnergames.peril.core.shared.net.events.server.denied.JoinGameServerDeniedEvent;
import com.forerunnergames.peril.core.shared.net.events.server.denied.PlayerJoinGameDeniedEvent;
import com.forerunnergames.peril.core.shared.net.events.server.notification.PlayerLeaveGameEvent;
import com.forerunnergames.peril.core.shared.net.events.server.request.PlayerSelectCountryRequestEvent;
import com.forerunnergames.peril.core.shared.net.events.server.success.CreateGameServerSuccessEvent;
import com.forerunnergames.peril.core.shared.net.events.server.success.JoinGameServerSuccessEvent;
import com.forerunnergames.peril.core.shared.net.events.server.success.PlayerJoinGameSuccessEvent;
import com.forerunnergames.peril.core.shared.net.kryonet.KryonetRemote;
import com.forerunnergames.peril.core.shared.net.packets.person.PlayerPacket;
import com.forerunnergames.peril.core.shared.net.settings.NetworkSettings;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Event;
import com.forerunnergames.tools.net.Remote;
import com.forerunnergames.tools.net.client.ClientCommunicator;
import com.forerunnergames.tools.net.client.ClientConfiguration;
import com.forerunnergames.tools.net.client.ClientConnector;
import com.forerunnergames.tools.net.events.local.ClientCommunicationEvent;
import com.forerunnergames.tools.net.events.local.ClientConnectionEvent;
import com.forerunnergames.tools.net.events.local.ClientDisconnectionEvent;
import com.forerunnergames.tools.net.events.remote.origin.server.DeniedEvent;
import com.forerunnergames.tools.net.server.DefaultServerConfiguration;
import com.forerunnergames.tools.net.server.ServerConfiguration;

import java.net.InetSocketAddress;

import net.engio.mbassy.bus.MBassador;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MultiplayerControllerTest
{
  private static final String DEFAULT_TEST_GAME_SERVER_NAME = "test-server";
  private static final GameServerType DEFAULT_GAME_SERVER_TYPE = GameServerType.DEDICATED;
  private static final String DEFAULT_TEST_SERVER_ADDRESS = "server@test";
  private static final int DEFAULT_TEST_SERVER_PORT = 8888;
  private static final EventBusHandler eventHandler = new EventBusHandler ();
  private final ClientConnector mockConnector = mock (ClientConnector.class);
  private final ClientCommunicator mockClientCommunicator = mock (ClientCommunicator.class);
  private final MultiplayerControllerBuilder mpcBuilder = builder (mockConnector,
                                                                   new PlayerCommunicator (mockClientCommunicator));
  private int clientCount = 0;
  private MBassador <Event> eventBus;

  @Before
  public void setup ()
  {
    eventBus = EventBusFactory.create ();
    eventHandler.clearEvents ();
    eventHandler.subscribe (eventBus);
  }

  @After
  public void tearDown ()
  {
    eventHandler.unsubscribe (eventBus);
    eventHandler.clearEvents ();
  }

  @Test
  public void testSuccessfulHostClientCreateGameServer ()
  {
    final MultiplayerController mpc = mpcBuilder.gameServerType (GameServerType.HOST_AND_PLAY).build (eventBus);

    final Remote host = createHost ();
    connect (host);

    final GameServerConfiguration gameServerConfig = new DefaultGameServerConfiguration (DEFAULT_TEST_GAME_SERVER_NAME,
            GameServerType.HOST_AND_PLAY, mpc.getGameConfiguration (), createDefaultServerConfig ());
    communicateEventFromClient (new CreateGameServerRequestEvent (gameServerConfig), host);

    final BaseMatcher <CreateGameServerSuccessEvent> successEventMatcher = new BaseMatcher <CreateGameServerSuccessEvent> ()
    {
      @Override
      public boolean matches (final Object arg0)
      {
        assertThat (arg0, instanceOf (CreateGameServerSuccessEvent.class));
        final CreateGameServerSuccessEvent matchEvent = (CreateGameServerSuccessEvent) arg0;
        final GameServerConfiguration matchGameServerConfig = matchEvent.getGameServerConfiguration ();
        final ClientConfiguration matchClientConfig = matchEvent.getClientConfiguration ();
        return matchGameServerConfig.getServerAddress ().equals (gameServerConfig.getServerAddress ())
                && matchClientConfig.getClientAddress ().equals (host.getAddress ())
                && matchGameServerConfig.getServerTcpPort () == gameServerConfig.getServerTcpPort ()
                && matchClientConfig.getClientTcpPort () == host.getPort ()
                && matchGameServerConfig.getGameServerName ().equals (gameServerConfig.getGameServerName ())
                && matchGameServerConfig.getGameServerType () == gameServerConfig.getGameServerType ()
                && matchGameServerConfig.getGameMode () == gameServerConfig.getGameMode ()
                && matchGameServerConfig.getPlayerLimit () == gameServerConfig.getPlayerLimit ()
                && matchGameServerConfig.getWinPercentage () == gameServerConfig.getWinPercentage ()
                && matchGameServerConfig.getInitialCountryAssignment () == gameServerConfig
                        .getInitialCountryAssignment ();
      }

      @Override
      public void describeTo (final Description arg0)
      {
      }
    };
    verify (mockClientCommunicator, only ()).sendTo (eq (host), argThat (successEventMatcher));
  }

  @Test
  public void testSuccessfulClientJoinGameServer ()
  {
    mpcBuilder.build (eventBus);

    final Remote client = createClient ();
    connect (client);

    final ServerConfiguration serverConfig = createDefaultServerConfig ();
    eventBus.publish (new ClientCommunicationEvent (new JoinGameServerRequestEvent (serverConfig), client));

    final BaseMatcher <JoinGameServerSuccessEvent> successEventMatcher = new BaseMatcher <JoinGameServerSuccessEvent> ()
    {
      @Override
      public boolean matches (final Object arg0)
      {
        assertThat (arg0, instanceOf (JoinGameServerSuccessEvent.class));
        final JoinGameServerSuccessEvent matchEvent = (JoinGameServerSuccessEvent) arg0;
        final ServerConfiguration matchServerConfig = matchEvent.getGameServerConfiguration ();
        final ClientConfiguration matchClientConfig = matchEvent.getClientConfiguration ();
        return matchServerConfig.getServerAddress ().equals (serverConfig.getServerAddress ())
                && matchClientConfig.getClientAddress ().equals (client.getAddress ())
                && matchServerConfig.getServerTcpPort () == serverConfig.getServerTcpPort ()
                && matchClientConfig.getClientTcpPort () == client.getPort ();
      }

      @Override
      public void describeTo (final Description arg0)
      {
      }
    };
    verify (mockClientCommunicator).sendTo (eq (client), argThat (successEventMatcher));
  }

  @Test
  public void testClientJoinRequestBeforeHostDenied ()
  {
    mpcBuilder.gameServerType (GameServerType.HOST_AND_PLAY).build (eventBus);

    final Remote client = createClient ();
    connect (client);

    final ServerConfiguration serverConfig = createDefaultServerConfig ();
    eventBus.publish (new ClientCommunicationEvent (new JoinGameServerRequestEvent (serverConfig), client));

    final BaseMatcher <JoinGameServerDeniedEvent> denialEventMatcher = new BaseMatcher <JoinGameServerDeniedEvent> ()
    {
      @Override
      public boolean matches (final Object arg0)
      {
        assertThat (arg0, instanceOf (JoinGameServerDeniedEvent.class));
        final JoinGameServerDeniedEvent matchEvent = (JoinGameServerDeniedEvent) arg0;
        final ServerConfiguration matchServerConfig = matchEvent.getServerConfiguration ();
        final ClientConfiguration matchClientConfig = matchEvent.getClientConfiguration ();
        return matchServerConfig.getServerAddress ().equals (serverConfig.getServerAddress ())
                && matchClientConfig.getClientAddress ().equals (client.getAddress ())
                && matchServerConfig.getServerTcpPort () == serverConfig.getServerTcpPort ()
                && matchClientConfig.getClientTcpPort () == client.getPort ();
      }

      @Override
      public void describeTo (final Description arg0)
      {
      }
    };
    verify (mockClientCommunicator, only ()).sendTo (eq (client), argThat (denialEventMatcher));
    verify (mockConnector, only ()).disconnect (eq (client));
  }

  @Test
  public void testValidPlayerJoinGameRequestPublished ()
  {
    mpcBuilder.build (eventBus);

    final Remote client = addClient ();
    verify (mockClientCommunicator, only ()).sendTo (eq (client), isA (JoinGameServerSuccessEvent.class));

    final String playerName = "Test Player 1";
    final Event event = new PlayerJoinGameRequestEvent (playerName);
    communicateEventFromClient (event, client);
    assertLastEventWas (event);
  }

  @Test
  public void testIgnorePlayerJoinGameRequestBeforeJoiningGameServer ()
  {
    mpcBuilder.build (eventBus);

    // Connect client to server, but do not join client to game server.
    final Remote client = createClient ();
    connect (client);

    // Simulate bad request.
    final Event event = communicateEventFromClient (new PlayerJoinGameRequestEvent ("Test Player 1"), client);

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
    final Remote client = joinClientToGameServer ();

    final String playerName = "Test-Player-0";
    communicateEventFromClient (new PlayerJoinGameRequestEvent (playerName), client);

    final PlayerPacket mockPacket = mock (PlayerPacket.class);
    when (mockPacket.getName ()).thenReturn (playerName);
    // make up a reason... doesn't have to be true :)
    final PlayerJoinGameDeniedEvent.Reason reason = PlayerJoinGameDeniedEvent.Reason.DUPLICATE_ID;
    final DeniedEvent <PlayerJoinGameDeniedEvent.Reason> deniedEvent = new PlayerJoinGameDeniedEvent (playerName,
            reason);
    eventBus.publish (deniedEvent);
    verify (mockClientCommunicator).sendTo (eq (client), eq (deniedEvent));
    assertFalse (mpc.isPlayerInGame (mockPacket));
  }

  @Test
  public void testPlayerLeaveGame ()
  {
    final MultiplayerController mpc = mpcBuilder.build (eventBus);
    final ClientPlayerTuple clientPlayer = addClientAndMockPlayerToGameServer ("Test Player 1", mpc);

    eventBus.publish (new ClientDisconnectionEvent (clientPlayer.client ()));

    // make sure nothing was sent to the disconnecting player
    verify (mockClientCommunicator, never ()).sendTo (eq (clientPlayer.client ()), isA (PlayerLeaveGameEvent.class));
    assertLastEventWasType (PlayerLeaveGameEvent.class);
    assertFalse (mpc.isPlayerInGame (clientPlayer.player ()));
  }

  @Test
  public void testValidPlayerSelectCountryResponseRequestEvent ()
  {
    // Create a game server with manual initial country assignment.
    final MultiplayerController mpc = mpcBuilder.initialCountryAssignment (InitialCountryAssignment.MANUAL)
            .build (eventBus);

    final ClientPlayerTuple clientPlayer = addClientAndMockPlayerToGameServer ("Test Player 1", mpc);

    // Request that the player/client select an available country.
    eventBus.publish (new PlayerSelectCountryRequestEvent (clientPlayer.player ()));
    verify (mockClientCommunicator).sendTo (eq (clientPlayer.client ()), isA (PlayerSelectCountryRequestEvent.class));

    // Simulate player/client selecting a country.
    final Event event = new PlayerSelectCountryResponseRequestEvent ("Test Country 1");
    communicateEventFromClient (event, clientPlayer.client ());

    // Verify that player/client's country selection was published.
    assertLastEventWas (event);
    assertEventFiredExactlyOnce (PlayerSelectCountryResponseRequestEvent.class);
  }

  @Test
  public void testInvalidPlayerSelectCountryResponseRequestEventIgnoredBecauseClientIsNotAPlayer ()
  {
    // Create a game server with manual initial country assignment.
    mpcBuilder.initialCountryAssignment (InitialCountryAssignment.MANUAL).build (eventBus);

    final Remote client = joinClientToGameServer ();
    final PlayerPacket player = mock (PlayerPacket.class);
    when (player.getName ()).thenReturn ("Test Player 1");

    // Simulate player/client selecting a country.
    final Event event = communicateEventFromClient (new PlayerSelectCountryResponseRequestEvent ("Test Country 1"),
                                                    client);

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

    // Request that the player/client select an available country.
    eventBus.publish (new PlayerSelectCountryRequestEvent (first.player ()));
    verify (mockClientCommunicator).sendTo (eq (first.client ()), isA (PlayerSelectCountryRequestEvent.class));

    // Simulate WRONG player/client selecting a country.
    final Event event = communicateEventFromClient (new PlayerSelectCountryResponseRequestEvent ("Test Country 1"),
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

    // Request that the first player/client select an available country.
    final Event selectCountryRequestEvent1 = new PlayerSelectCountryRequestEvent (first.player ());
    eventBus.publish (selectCountryRequestEvent1);
    verify (mockClientCommunicator).sendTo (first.client (), selectCountryRequestEvent1);
    // Make sure that the request was not sent to the second player/client.
    verify (mockClientCommunicator, never ()).sendTo (second.client (), selectCountryRequestEvent1);

    // Simulate & verify first player/client selecting a country.
    final Event selectCountryResponseRequestEvent1 = new PlayerSelectCountryResponseRequestEvent ("Test Country 1");
    communicateEventFromClient (selectCountryResponseRequestEvent1, first.client ());
    assertLastEventWas (selectCountryResponseRequestEvent1);

    // Request that the second player/client select an available country.
    final Event selectCountryRequestEvent2 = new PlayerSelectCountryRequestEvent (second.player ());
    eventBus.publish (selectCountryRequestEvent2);
    verify (mockClientCommunicator).sendTo (second.client (), selectCountryRequestEvent2);
    // Make sure that the request was not sent to the first player/client.
    verify (mockClientCommunicator, never ()).sendTo (first.client (), selectCountryRequestEvent2);

    // Simulate & verify second player/client selecting a country.
    final Event selectCountryResponseRequestEvent2 = new PlayerSelectCountryResponseRequestEvent ("Test Country 2");
    communicateEventFromClient (selectCountryResponseRequestEvent2, second.client ());
    assertLastEventWas (selectCountryResponseRequestEvent2);

    // Request that the first player/client select an available country.
    final Event selectCountryRequestEvent3 = new PlayerSelectCountryRequestEvent (first.player ());
    eventBus.publish (selectCountryRequestEvent3);
    verify (mockClientCommunicator).sendTo (first.client (), selectCountryRequestEvent3);
    // Make sure that the request was not sent to the second player/client.
    verify (mockClientCommunicator, never ()).sendTo (second.client (), selectCountryRequestEvent3);

    // Simulate & verify first player/client selecting a country.
    final Event selectCountryResponseRequestEvent3 = new PlayerSelectCountryResponseRequestEvent ("Test Country 3");
    communicateEventFromClient (selectCountryResponseRequestEvent3, first.client ());
    assertLastEventWas (selectCountryResponseRequestEvent3);

    // Request that the second player/client select an available country.
    final Event selectCountryRequestEvent4 = new PlayerSelectCountryRequestEvent (second.player ());
    eventBus.publish (selectCountryRequestEvent4);
    verify (mockClientCommunicator).sendTo (second.client (), selectCountryRequestEvent4);
    // Make sure that the request was not sent to the first player/client.
    verify (mockClientCommunicator, never ()).sendTo (first.client (), selectCountryRequestEvent4);

    // Simulate & verify second player/client selecting a country.
    final Event selectCountryResponseRequestEvent4 = new PlayerSelectCountryResponseRequestEvent ("Test Country 4");
    communicateEventFromClient (selectCountryResponseRequestEvent4, second.client ());
    assertLastEventWas (selectCountryResponseRequestEvent4);

    // Request that the first player/client select an available country.
    final Event selectCountryRequestEvent5 = new PlayerSelectCountryRequestEvent (first.player ());
    eventBus.publish (selectCountryRequestEvent5);
    verify (mockClientCommunicator).sendTo (first.client (), selectCountryRequestEvent5);
    // Make sure that the request was not sent to the second player/client.
    verify (mockClientCommunicator, never ()).sendTo (second.client (), selectCountryRequestEvent5);

    // Simulate WRONG (second) player/client selecting a country.
    final Event event = communicateEventFromClient (new PlayerSelectCountryResponseRequestEvent ("Test Country 5"),
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

    // Simulate player/client selecting a country BEFORE receiving a request to do so from the server.
    final Event event = communicateEventFromClient (new PlayerSelectCountryResponseRequestEvent ("Test Country 1"),
                                                    first.client ());

    // Verify that player/client's country selection was NOT published.
    assertLastEventWas (event);
  }

  // <<<<<<<<<<<< Test helper facilities >>>>>>>>>>>>>> //

  // convenience method for fetching a new MultiplayerControllerBuilder
  // Note: package private visibility is intended; other test classes in package should have access.
  static MultiplayerControllerBuilder builder (final ClientConnector connector, final PlayerCommunicator communicator)
  {
    Arguments.checkIsNotNull (connector, "connector");
    Arguments.checkIsNotNull (communicator, "communicator");

    return new MultiplayerControllerBuilder (connector, communicator);
  }

  private ClientPlayerTuple addClientAndMockPlayerToGameServer (final String playerName,
                                                                final MultiplayerController mpc)
  {
    final Remote client = joinClientToGameServer ();
    final PlayerPacket player = addMockPlayerToGameWithName (playerName, client, mpc);

    return new ClientPlayerTuple (client, player);
  }

  private Remote joinClientToGameServer ()
  {
    final Remote client = addClient ();
    verify (mockClientCommunicator).sendTo (eq (client), isA (JoinGameServerSuccessEvent.class));

    return client;
  }

  private PlayerPacket addMockPlayerToGameWithName (final String playerName,
                                                    final Remote client,
                                                    final MultiplayerController mpc)
  {
    final PlayerPacket mockPlayerPacket = mock (PlayerPacket.class);
    when (mockPlayerPacket.getName ()).thenReturn (playerName);
    when (mockPlayerPacket.toString ()).thenReturn (playerName);
    communicateEventFromClient (new PlayerJoinGameRequestEvent (playerName), client);
    eventBus.publish (new PlayerJoinGameSuccessEvent (mockPlayerPacket));
    verify (mockClientCommunicator).sendTo (eq (client), isA (PlayerJoinGameSuccessEvent.class));
    assertTrue (mpc.isPlayerInGame (mockPlayerPacket));

    return mockPlayerPacket;
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

  private ClientCommunicationEvent communicateEventFromClient (final Event event, final Remote client)
  {
    final ClientCommunicationEvent clientCommunicationEvent = new ClientCommunicationEvent (event, client);

    eventBus.publish (clientCommunicationEvent);

    return clientCommunicationEvent;
  }

  private Remote createHost ()
  {
    return createClientWith (NetworkSettings.LOCALHOST_ADDRESS);
  }

  private Remote createClient ()
  {
    return createClientWith ("forerunnergames.com");
  }

  private Remote createClientWith (final String address)
  {
    Arguments.checkIsNotNull (address, "address");

    final int port = 1000 + clientCount;
    return new KryonetRemote (clientCount++, new InetSocketAddress (address, port));
  }

  private void connect (final Remote client)
  {
    Arguments.checkIsNotNull (client, "client");

    eventBus.publish (new ClientConnectionEvent (client));
  }

  private ServerConfiguration createDefaultServerConfig ()
  {
    return new DefaultServerConfiguration (DEFAULT_TEST_SERVER_ADDRESS, DEFAULT_TEST_SERVER_PORT);
  }

  private Remote addClient ()
  {
    final Remote client = createClient ();
    addClient (client);
    return client;
  }

  private void addClient (final Remote client)
  {
    connect (client);
    eventBus.publish (new ClientCommunicationEvent (new JoinGameServerRequestEvent (createDefaultServerConfig ()),
            client));
  }

  /*
   * Configurable test builder for MultiplayerController. Returns default values if left unchanged.
   */
  static class MultiplayerControllerBuilder
  {
    private final ClientConnector connector;
    private final PlayerCommunicator communicator;
    // game configuration fields
    private final GameMode gameMode = GameMode.CLASSIC;
    private InitialCountryAssignment initialCountryAssignment = ClassicGameRules.DEFAULT_INITIAL_COUNTRY_ASSIGNMENT;
    // game server configuration fields
    private String gameServerName = DEFAULT_TEST_GAME_SERVER_NAME;
    private GameServerType gameServerType = DEFAULT_GAME_SERVER_TYPE;
    // server configuration fields
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

    MultiplayerControllerBuilder serverPort (final int serverPort)
    {
      Arguments.checkIsNotNegative (serverPort, "serverPort");
      Arguments.checkUpperInclusiveBound (serverPort, NetworkSettings.MAX_PORT_VALUE, "serverPort");

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

      final GameConfiguration config = new DefaultGameConfiguration (gameMode, playerLimit, winPercent,
              initialCountryAssignment);
      final MultiplayerController controller = new MultiplayerController (gameServerName, gameServerType, serverPort,
              config, connector, communicator, eventBus);
      controller.initialize ();
      return controller;
    }

    // add game mode and/or initial-country-assignment later if needed

    private MultiplayerControllerBuilder (final ClientConnector connector, final PlayerCommunicator communicator)
    {
      this.connector = connector;
      this.communicator = communicator;
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
      return String.format ("%1$s: Client: %2$s | Player %3$s", getClass ().getSimpleName (), client, player);
    }
  }
}
