package com.forerunnergames.peril.client.ui.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.g2d.Batch;

import com.forerunnergames.peril.client.input.MouseInput;
import com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.ClassicModePlayScreen;
import com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.ClassicModePlayScreenWidgetFactory;
import com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.map.CountryCounterFactory;
import com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.map.actors.DefaultPlayMapActorFactory;
import com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.map.actors.PlayMapActorFactory;
import com.forerunnergames.peril.client.ui.screens.game.play.modes.peril.PerilModePlayScreen;
import com.forerunnergames.peril.client.ui.screens.menus.MenuScreenWidgetFactory;
import com.forerunnergames.peril.client.ui.screens.menus.main.MainMenuScreen;
import com.forerunnergames.peril.client.ui.screens.menus.multiplayer.modes.MultiplayerGameModesMenuScreen;
import com.forerunnergames.peril.client.ui.screens.menus.multiplayer.modes.classic.MultiplayerClassicGameModeMenuScreen;
import com.forerunnergames.peril.client.ui.screens.menus.multiplayer.modes.classic.creategame.MultiplayerClassicGameModeCreateGameMenuScreen;
import com.forerunnergames.peril.client.ui.screens.menus.multiplayer.modes.classic.joingame.MultiplayerClassicGameModeJoinGameMenuScreen;
import com.forerunnergames.peril.client.ui.screens.menus.multiplayer.modes.classic.loading.DefaultJoinGameServerHandler;
import com.forerunnergames.peril.client.ui.screens.menus.multiplayer.modes.classic.loading.JoinGameServerHandler;
import com.forerunnergames.peril.client.ui.screens.menus.multiplayer.modes.classic.loading.LoadingScreen;
import com.forerunnergames.peril.client.ui.screens.menus.multiplayer.modes.classic.loading.LoadingScreenWidgetFactory;
import com.forerunnergames.peril.client.ui.screens.menus.multiplayer.modes.peril.MultiplayerPerilGameModeMenuScreen;
import com.forerunnergames.peril.common.game.GameMode;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Event;

import net.engio.mbassy.bus.MBassador;

public final class ScreenFactory
{
  private final ScreenChanger screenChanger;
  private final ScreenSize screenSize;
  private final MouseInput mouseInput;
  private final Cursor normalCursor;
  private final MBassador <Event> eventBus;
  private final Batch batch;
  private final MenuScreenWidgetFactory menuScreenWidgetFactory;
  private final JoinGameServerHandler joinGameServerHandler;
  private final AssetManager assetManager;
  private final PlayMapActorFactory playMapActorFactory;

  public ScreenFactory (final ScreenChanger screenChanger,
                        final ScreenSize screenSize,
                        final MouseInput mouseInput,
                        final Batch batch,
                        final AssetManager assetManager,
                        final MBassador <Event> eventBus)
  {
    Arguments.checkIsNotNull (screenChanger, "screenChanger");
    Arguments.checkIsNotNull (screenSize, "screenSize");
    Arguments.checkIsNotNull (mouseInput, "mouseInput");
    Arguments.checkIsNotNull (batch, "batch");
    Arguments.checkIsNotNull (assetManager, "assetManager");
    Arguments.checkIsNotNull (eventBus, "eventBus");

    this.screenChanger = screenChanger;
    this.screenSize = screenSize;
    this.mouseInput = mouseInput;
    this.batch = batch;
    this.assetManager = assetManager;
    this.eventBus = eventBus;
    menuScreenWidgetFactory = new MenuScreenWidgetFactory (assetManager);
    joinGameServerHandler = new DefaultJoinGameServerHandler (eventBus);
    playMapActorFactory = new DefaultPlayMapActorFactory (assetManager, screenSize, mouseInput, eventBus);
    normalCursor = menuScreenWidgetFactory.createNormalCursor ();
  }

  public Screen create (final ScreenId screenId)
  {
    Arguments.checkIsNotNull (screenId, "screenId");

    switch (screenId)
    {
      case LOADING:
      {
        return new LoadingScreen (new LoadingScreenWidgetFactory (assetManager), playMapActorFactory, screenChanger,
                screenSize, mouseInput, normalCursor, batch, assetManager, eventBus);
      }
      case MAIN_MENU:
      {
        return new MainMenuScreen (menuScreenWidgetFactory, screenChanger, screenSize, normalCursor, batch);
      }
      case MULTIPLAYER_GAME_MODES_MENU:
      {
        return new MultiplayerGameModesMenuScreen (menuScreenWidgetFactory, screenChanger, screenSize, normalCursor,
                batch);
      }
      case MULTIPLAYER_CLASSIC_GAME_MODE_MENU:
      {
        return new MultiplayerClassicGameModeMenuScreen (menuScreenWidgetFactory, screenChanger, screenSize,
                normalCursor, batch);
      }
      case MULTIPLAYER_PERIL_GAME_MODE_MENU:
      {
        return new MultiplayerPerilGameModeMenuScreen (menuScreenWidgetFactory, screenChanger, screenSize, normalCursor,
                batch);
      }
      case MULTIPLAYER_CLASSIC_GAME_MODE_CREATE_GAME_MENU:
      {
        return new MultiplayerClassicGameModeCreateGameMenuScreen (menuScreenWidgetFactory, screenChanger, screenSize,
                normalCursor, batch, CountryCounterFactory.create (GameMode.CLASSIC), eventBus);
      }
      case MULTIPLAYER_CLASSIC_GAME_MODE_JOIN_GAME_MENU:
      {
        return new MultiplayerClassicGameModeJoinGameMenuScreen (menuScreenWidgetFactory, screenChanger, screenSize,
                normalCursor, batch, joinGameServerHandler, eventBus);
      }
      case PLAY_CLASSIC:
      {
        return new ClassicModePlayScreen (new ClassicModePlayScreenWidgetFactory (assetManager, playMapActorFactory),
                screenChanger, screenSize, mouseInput, normalCursor, batch, eventBus);
      }
      case PLAY_PERIL:
      {
        return new PerilModePlayScreen (screenChanger, screenSize, mouseInput, normalCursor, batch, assetManager, eventBus);
      }
      default:
      {
        throw new IllegalStateException ("Unknown " + ScreenId.class.getSimpleName () + " [" + screenId + "].");
      }
    }
  }
}
