package com.forerunnergames.peril.client.ui.screens.menus.multiplayer.modes.classic.loading;

import com.forerunnergames.peril.client.events.CreateGameServerDeniedEvent;
import com.forerunnergames.peril.client.events.CreateGameServerRequestEvent;
import com.forerunnergames.peril.client.events.CreateGameServerSuccessEvent;
import com.forerunnergames.peril.common.game.GameConfiguration;
import com.forerunnergames.peril.common.net.DefaultGameServerConfiguration;
import com.forerunnergames.peril.common.net.GameServerType;
import com.forerunnergames.peril.common.settings.NetworkSettings;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Event;
import com.forerunnergames.tools.common.Preconditions;
import com.forerunnergames.tools.common.Strings;
import com.forerunnergames.tools.net.NetworkConstants;
import com.forerunnergames.tools.net.server.DefaultServerConfiguration;

import javax.annotation.Nullable;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Listener (references = References.Strong)
public final class DefaultCreateGameServerHandler implements CreateGameServerHandler
{
  private static final Logger log = LoggerFactory.getLogger (DefaultCreateGameServerHandler.class);
  private final JoinGameServerHandler joinGameServerHandler;
  private final MBassador <Event> eventBus;
  @Nullable
  private String playerName = null;
  @Nullable
  private CreateGameServerListener listener = null;
  private boolean createGameIsInProgress = false;

  public DefaultCreateGameServerHandler (final JoinGameServerHandler joinGameServerHandler,
                                         final MBassador <Event> eventBus)
  {
    Arguments.checkIsNotNull (joinGameServerHandler, "joinGameHandler");
    Arguments.checkIsNotNull (eventBus, "eventBus");

    this.joinGameServerHandler = joinGameServerHandler;
    this.eventBus = eventBus;
  }

  @Override
  public void create (final String serverName,
                      final GameConfiguration gameConfig,
                      final String playerName,
                      final CreateGameServerListener listener)
  {
    Arguments.checkIsNotNull (serverName, "serverName");
    Arguments.checkIsNotNull (gameConfig, "gameConfig");
    Arguments.checkIsNotNull (playerName, "playerName");
    Arguments.checkIsNotNull (listener, "listener");

    this.playerName = playerName;
    this.listener = listener;

    eventBus.subscribe (this);

    final CreateGameServerRequestEvent event = new CreateGameServerRequestEvent (new DefaultGameServerConfiguration (
            serverName, GameServerType.HOST_AND_PLAY, gameConfig,
            new DefaultServerConfiguration (NetworkConstants.LOCALHOST_ADDRESS, NetworkSettings.DEFAULT_TCP_PORT)));

    log.info ("Attempting to create game server... [{}]", event);

    assert listener != null;
    listener.onCreateStart (event.getGameServerConfiguration (), playerName);

    eventBus.publishAsync (event);

    createGameIsInProgress = true;
  }

  @Handler
  void onEvent (final CreateGameServerSuccessEvent event)
  {
    Arguments.checkIsNotNull (event, "event");
    Preconditions.checkIsTrue (createGameIsInProgress, Strings.format ("{}#create has not been called first.",
                                                                       CreateGameServerHandler.class.getSimpleName ()));

    log.trace ("Event received [{}].", event);
    log.info ("Successfully created game server: [{}]", event);

    assert listener != null;
    listener.onCreateFinish (event.getGameServerConfiguration ());

    // Attempt to join the created game.
    // When this call returns, JoinGameServerHandler will be subscribed.
    joinGameServerHandler.join (playerName, NetworkConstants.LOCALHOST_ADDRESS, listener);

    // Don't unsubscribe until we're already subscribed in the JoinGameServerHandler.
    eventBus.unsubscribe (this);

    createGameIsInProgress = false;
  }

  @Handler
  void onEvent (final CreateGameServerDeniedEvent event)
  {
    Arguments.checkIsNotNull (event, "event");
    Preconditions.checkIsTrue (createGameIsInProgress, Strings.format ("{}#create has not been called first.",
                                                                       CreateGameServerHandler.class.getSimpleName ()));

    log.trace ("Event received [{}].", event);
    log.error ("Could not create game server: [{}]", event);

    eventBus.unsubscribe (this);

    createGameIsInProgress = false;

    assert listener != null;
    listener.onCreateFailure (event.getGameServerConfiguration (), event.getReason ());
  }
}
