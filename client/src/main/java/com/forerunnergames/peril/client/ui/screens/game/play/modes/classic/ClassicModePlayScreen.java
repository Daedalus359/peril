package com.forerunnergames.peril.client.ui.screens.game.play.modes.classic;

import static com.forerunnergames.peril.common.net.events.EventFluency.countriesFrom;
import static com.forerunnergames.peril.common.net.events.EventFluency.deltaArmyCountFrom;
import static com.forerunnergames.peril.common.net.events.EventFluency.hasAuthorFrom;
import static com.forerunnergames.peril.common.net.events.EventFluency.playerColorFrom;
import static com.forerunnergames.peril.common.net.events.EventFluency.playerFrom;
import static com.forerunnergames.peril.common.net.events.EventFluency.selectedCountryNameFrom;
import static com.forerunnergames.peril.common.net.events.EventFluency.withAuthorNameFrom;
import static com.forerunnergames.peril.common.net.events.EventFluency.withCountryNameFrom;
import static com.forerunnergames.peril.common.net.events.EventFluency.withMessageFrom;
import static com.forerunnergames.peril.common.net.events.EventFluency.withMessageTextFrom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.forerunnergames.peril.client.events.DefaultStatusMessageEvent;
import com.forerunnergames.peril.client.events.PlayGameEvent;
import com.forerunnergames.peril.client.events.QuitGameEvent;
import com.forerunnergames.peril.client.events.StatusMessageEvent;
import com.forerunnergames.peril.client.events.StatusMessageEventFactory;
import com.forerunnergames.peril.client.input.GdxKeyRepeatListenerAdapter;
import com.forerunnergames.peril.client.input.GdxKeyRepeatSystem;
import com.forerunnergames.peril.client.input.MouseInput;
import com.forerunnergames.peril.client.messages.DefaultStatusMessage;
import com.forerunnergames.peril.client.messages.StatusMessage;
import com.forerunnergames.peril.client.settings.GraphicsSettings;
import com.forerunnergames.peril.client.settings.InputSettings;
import com.forerunnergames.peril.client.settings.PlayMapSettings;
import com.forerunnergames.peril.client.ui.screens.ScreenChanger;
import com.forerunnergames.peril.client.ui.screens.ScreenId;
import com.forerunnergames.peril.client.ui.screens.ScreenShaker;
import com.forerunnergames.peril.client.ui.screens.ScreenSize;
import com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.debug.DebugInputProcessor;
import com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.debug.DebugPackets;
import com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.map.actors.CountryActor;
import com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.map.actors.PlayMapActor;
import com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.map.images.CountryPrimaryImageState;
import com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.widgets.AbstractBattlePopupListener;
import com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.widgets.AttackPopup;
import com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.widgets.BattleOutcome;
import com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.widgets.ClassicModePlayScreenWidgetFactory;
import com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.widgets.DefendPopup;
import com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.widgets.OccupationPopup;
import com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.widgets.PlayerBox;
import com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.widgets.ReinforcementPopup;
import com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.widgets.SideBar;
import com.forerunnergames.peril.client.ui.widgets.messagebox.MessageBox;
import com.forerunnergames.peril.client.ui.widgets.popup.OkPopup;
import com.forerunnergames.peril.client.ui.widgets.popup.Popup;
import com.forerunnergames.peril.client.ui.widgets.popup.PopupListener;
import com.forerunnergames.peril.client.ui.widgets.popup.PopupListenerAdapter;
import com.forerunnergames.peril.common.game.DieFaceValue;
import com.forerunnergames.peril.common.net.events.server.notification.CountryArmiesChangedEvent;
import com.forerunnergames.peril.common.net.events.server.notification.DeterminePlayerTurnOrderCompleteEvent;
import com.forerunnergames.peril.common.net.events.server.notification.PlayerCountryAssignmentCompleteEvent;
import com.forerunnergames.peril.common.net.events.server.notification.PlayerLeaveGameEvent;
import com.forerunnergames.peril.common.net.events.server.success.ChatMessageSuccessEvent;
import com.forerunnergames.peril.common.net.events.server.success.PlayerJoinGameSuccessEvent;
import com.forerunnergames.peril.common.net.events.server.success.PlayerSelectCountryResponseSuccessEvent;
import com.forerunnergames.peril.common.net.messages.ChatMessage;
import com.forerunnergames.peril.common.net.messages.DefaultChatMessage;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.peril.common.net.packets.territory.CountryPacket;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.DefaultMessage;
import com.forerunnergames.tools.common.Event;
import com.forerunnergames.tools.common.LetterCase;
import com.forerunnergames.tools.common.Randomness;
import com.forerunnergames.tools.common.Strings;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.listener.Handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClassicModePlayScreen extends InputAdapter implements Screen
{
  private static final Logger log = LoggerFactory.getLogger (ClassicModePlayScreen.class);
  private final ClassicModePlayScreenWidgetFactory widgetFactory;
  private final ScreenChanger screenChanger;
  private final MouseInput mouseInput;
  private final Cursor normalCursor;
  private final MBassador <Event> eventBus;
  private final Stage stage;
  private final Image backgroundImage;
  private final MessageBox <StatusMessage> statusBox;
  private final MessageBox <ChatMessage> chatBox;
  private final PlayerBox playerBox;
  private final SideBar sideBar;
  private final InputProcessor inputProcessor;
  private final GdxKeyRepeatSystem keyRepeat;
  private final OccupationPopup occupationPopup;
  private final ReinforcementPopup reinforcementPopup;
  private final AttackPopup attackPopup;
  private final DefendPopup defendPopup;
  private final OkPopup battleResultPopup;
  private final Popup quitPopup;
  private final Vector2 tempPosition = new Vector2 ();
  private final BattleOutcome tempOutcome = new BattleOutcome ();
  private final Cell <Actor> playMapActorCell;
  private final ScreenShaker screenShaker;
  private final DebugInputProcessor debugInputProcessor;
  private Sound battleSingleExplosionSound;
  private PlayMapActor playMapActor = PlayMapActor.NULL_PLAY_MAP_ACTOR;

  public ClassicModePlayScreen (final ClassicModePlayScreenWidgetFactory widgetFactory,
                                final ScreenChanger screenChanger,
                                final ScreenSize screenSize,
                                final MouseInput mouseInput,
                                final Batch batch,
                                final MBassador <Event> eventBus)
  {
    Arguments.checkIsNotNull (widgetFactory, "widgetFactory");
    Arguments.checkIsNotNull (screenChanger, "screenChanger");
    Arguments.checkIsNotNull (screenSize, "screenSize");
    Arguments.checkIsNotNull (mouseInput, "mouseInput");
    Arguments.checkIsNotNull (batch, "batch");
    Arguments.checkIsNotNull (eventBus, "eventBus");

    this.widgetFactory = widgetFactory;
    this.screenChanger = screenChanger;
    this.mouseInput = mouseInput;
    this.eventBus = eventBus;

    normalCursor = widgetFactory.createNormalCursor ();
    backgroundImage = widgetFactory.createBackgroundImage ();
    statusBox = widgetFactory.createStatusBox ();
    chatBox = widgetFactory.createChatBox (eventBus);
    playerBox = widgetFactory.createPlayerBox ();
    sideBar = widgetFactory.createSideBar (eventBus);

    final Stack rootStack = new Stack ();
    rootStack.setFillParent (true);
    rootStack.add (backgroundImage);

    final Table playMapAndSideBarTable = new Table ();
    playMapActorCell = playMapAndSideBarTable.add (playMapActor.asActor ())
            .size (PlayMapSettings.ACTUAL_WIDTH, PlayMapSettings.ACTUAL_HEIGHT).padRight (16);
    playMapAndSideBarTable.add (sideBar).top ();

    final Table foregroundTable = new Table ().pad (12);
    foregroundTable.add (playMapAndSideBarTable).colspan (3);
    foregroundTable.row ().expandY ().padTop (16 + 2);
    foregroundTable.add (statusBox.asActor ()).width (714).height (252 - 2 - 2).padRight (16).padBottom (2);
    foregroundTable.add (chatBox.asActor ()).width (714).height (252 - 2).padRight (16);
    foregroundTable.add (playerBox.asActor ()).width (436).height (252 - 2 - 2).padBottom (2);

    rootStack.add (foregroundTable);

    final Camera camera = new OrthographicCamera (screenSize.actualWidth (), screenSize.actualHeight ());
    final Viewport viewport = new ScalingViewport (GraphicsSettings.VIEWPORT_SCALING, screenSize.referenceWidth (),
            screenSize.referenceHeight (), camera);

    screenShaker = new ScreenShaker (viewport, screenSize);

    stage = new Stage (viewport, batch);

    occupationPopup = widgetFactory.createOccupationPopup (stage, eventBus, new OccupationPopupListener ());
    reinforcementPopup = widgetFactory.createReinforcementPopup (stage, eventBus, new ReinforcementPopupListener ());
    attackPopup = widgetFactory.createAttackPopup (stage, eventBus, new DefaultAttackPopupListener ());
    defendPopup = widgetFactory.createDefendPopup (stage, eventBus, new DefendPopupListener ());
    battleResultPopup = widgetFactory.createBattleResultPopup (stage, new BattleResultPopupListener ());
    quitPopup = widgetFactory.createQuitPopup (stage, new QuitPopupListener ());

    stage.addActor (rootStack);

    stage.addCaptureListener (new InputListener ()
    {
      @Override
      public boolean keyDown (final InputEvent event, final int keycode)
      {
        switch (keycode)
        {
          case Input.Keys.ESCAPE:
          {
            if (!attackPopup.isShown () && !reinforcementPopup.isShown ()) quitPopup.show ();

            return false;
          }
          default:
          {
            return false;
          }
        }
      }
    });

    stage.addListener (new ClickListener ()
    {
      @Override
      public boolean touchDown (final InputEvent event,
                                final float x,
                                final float y,
                                final int pointer,
                                final int button)
      {
        stage.setKeyboardFocus (event.getTarget ());

        return false;
      }
    });

    final InputProcessor preInputProcessor = new InputAdapter ()
    {
      @Override
      public boolean touchDown (final int screenX, final int screenY, final int pointer, final int button)
      {
        stage.setKeyboardFocus (null);

        return false;
      }
    };

    keyRepeat = new GdxKeyRepeatSystem (Gdx.input, new GdxKeyRepeatListenerAdapter ()
    {
      @Override
      public void keyDownRepeating (final int keyCode)
      {
        occupationPopup.keyDownRepeating (keyCode);
        reinforcementPopup.keyDownRepeating (keyCode);
      }
    });

    keyRepeat.setKeyRepeatRate (Input.Keys.LEFT, 50);
    keyRepeat.setKeyRepeatRate (Input.Keys.RIGHT, 50);
    keyRepeat.setKeyRepeatRate (Input.Keys.UP, 50);
    keyRepeat.setKeyRepeatRate (Input.Keys.DOWN, 50);
    keyRepeat.setKeyRepeat (Input.Keys.LEFT, true);
    keyRepeat.setKeyRepeat (Input.Keys.RIGHT, true);
    keyRepeat.setKeyRepeat (Input.Keys.UP, true);
    keyRepeat.setKeyRepeat (Input.Keys.DOWN, true);
    keyRepeat.setKeyRepeat (Input.Keys.BACKSPACE, true);
    keyRepeat.setKeyRepeat (Input.Keys.FORWARD_DEL, true);

    debugInputProcessor = new DebugInputProcessor (mouseInput, playMapActor, statusBox, chatBox, playerBox,
            occupationPopup, reinforcementPopup, attackPopup, defendPopup, eventBus);

    inputProcessor = new InputMultiplexer (preInputProcessor, stage, this, debugInputProcessor);

    battleSingleExplosionSound = widgetFactory.createBattleSingleExplosionSound ();
  }

  @Override
  public void show ()
  {
    showCursor ();

    eventBus.subscribe (this);

    Gdx.input.setInputProcessor (inputProcessor);

    stage.mouseMoved (mouseInput.x (), mouseInput.y ());
    playMapActor.mouseMoved (mouseInput.position ());

    backgroundImage.setDrawable (widgetFactory.createBackgroundImageDrawable ());
    statusBox.refreshAssets ();
    chatBox.refreshAssets ();
    playerBox.refreshAssets ();
    sideBar.refreshAssets ();
    occupationPopup.refreshAssets ();
    reinforcementPopup.refreshAssets ();
    attackPopup.refreshAssets ();
    defendPopup.refreshAssets ();
    battleResultPopup.refreshAssets ();
    quitPopup.refreshAssets ();
    battleSingleExplosionSound = widgetFactory.createBattleSingleExplosionSound ();
  }

  @Override
  public void render (final float delta)
  {
    Gdx.gl.glClearColor (0, 0, 0, 1);
    Gdx.gl.glClear (GL20.GL_COLOR_BUFFER_BIT);

    keyRepeat.update ();
    stage.act (delta);
    occupationPopup.update (delta);
    reinforcementPopup.update (delta);
    attackPopup.update (delta);
    defendPopup.update (delta);
    battleResultPopup.update (delta);
    quitPopup.update (delta);
    screenShaker.update (delta);
    stage.draw ();
  }

  @Override
  public void resize (final int width, final int height)
  {
    stage.getViewport ().update (width, height, true);
    stage.getViewport ().setScreenPosition (InputSettings.ACTUAL_INPUT_SPACE_TO_ACTUAL_SCREEN_SPACE_TRANSLATION_X,
                                            InputSettings.ACTUAL_INPUT_SPACE_TO_ACTUAL_SCREEN_SPACE_TRANSLATION_Y);
  }

  @Override
  public void pause ()
  {
  }

  @Override
  public void resume ()
  {
  }

  @Override
  public void hide ()
  {
    eventBus.unsubscribe (this);

    stage.unfocusAll ();

    Gdx.input.setInputProcessor (null);

    hideCursor ();

    chatBox.clear ();
    statusBox.clear ();
    playerBox.clear ();
    debugInputProcessor.reset ();
    clearPlayMapActor ();
    occupationPopup.hide ();
    reinforcementPopup.hide ();
    attackPopup.hide ();
    defendPopup.hide ();
    battleResultPopup.hide ();
    quitPopup.hide ();

    battleSingleExplosionSound.stop ();
  }

  @Override
  public void dispose ()
  {
    eventBus.unsubscribe (this);
    stage.dispose ();
  }

  @Override
  public boolean touchDown (final int screenX, final int screenY, final int pointer, final int button)
  {
    playMapActor.touchDown (tempPosition.set (screenX, screenY), button);

    return false;
  }

  @Override
  public boolean touchUp (final int screenX, final int screenY, final int pointer, final int button)
  {
    playMapActor.touchUp (tempPosition.set (screenX, screenY));

    return false;
  }

  @Override
  public boolean mouseMoved (final int screenX, final int screenY)
  {
    playMapActor.mouseMoved (tempPosition.set (screenX, screenY));

    return false;
  }

  @Handler
  void onEvent (final PlayGameEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.trace ("Event received [{}].", event);

    Gdx.app.postRunnable (new Runnable ()
    {
      @Override
      public void run ()
      {
        updatePlayMapActor (event.getPlayMapActor ());
        playerBox.setPlayers (event.getPlayersInGame ());
      }
    });
  }

  @Handler
  void onEvent (final StatusMessageEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.trace ("Event received [{}].", event);

    Gdx.app.postRunnable (new Runnable ()
    {
      @Override
      public void run ()
      {
        statusBox.addMessage (withMessageFrom (event));
        statusBox.showLastMessage ();
      }
    });
  }

  @Handler
  void onEvent (final ChatMessageSuccessEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.trace ("Event received [{}].", event);

    Gdx.app.postRunnable (new Runnable ()
    {
      @Override
      public void run ()
      {
        log.debug ("Event received [{}].", event);

        if (!hasAuthorFrom (event)) return;

        chatBox.addMessage (new DefaultChatMessage (withAuthorNameFrom (event) + ": " + withMessageTextFrom (event)));
        chatBox.showLastMessage ();
      }
    });
  }

  @Handler
  void onEvent (final PlayerJoinGameSuccessEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.trace ("Event received [{}].", event);

    Gdx.app.postRunnable (new Runnable ()
    {
      @Override
      public void run ()
      {
        playerBox.addPlayer (playerFrom (event));
      }
    });
  }

  @Handler
  void onEvent (final PlayerLeaveGameEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    Gdx.app.postRunnable (new Runnable ()
    {
      @Override
      public void run ()
      {
        log.trace ("Event received [{}].", event);

        playerBox.setPlayers (event.getPlayers ());
      }
    });
  }

  @Handler
  void onEvent (final CountryArmiesChangedEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.trace ("Event received [{}].", event);

    Gdx.app.postRunnable (new Runnable ()
    {
      @Override
      public void run ()
      {
        playMapActor.changeArmiesBy (deltaArmyCountFrom (event), withCountryNameFrom (event));
      }
    });
  }

  @Handler
  void onEvent (final DeterminePlayerTurnOrderCompleteEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.trace ("Event received [{}].", event);

    Gdx.app.postRunnable (new Runnable ()
    {
      @Override
      public void run ()
      {
        playerBox.setPlayers (event.getOrderedPlayers ());
      }
    });
  }

  @Handler
  void onEvent (final PlayerSelectCountryResponseSuccessEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.trace ("Event received [{}].", event);

    Gdx.app.postRunnable (new Runnable ()
    {
      @Override
      public void run ()
      {
        playMapActor.setCountryState (selectedCountryNameFrom (event), CountryPrimaryImageState
                .valueOf (Strings.toCase (playerColorFrom (event), LetterCase.UPPER)));
      }
    });
  }

  @Handler
  void onEvent (final PlayerCountryAssignmentCompleteEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.trace ("Event received [{}].", event);

    Gdx.app.postRunnable (new Runnable ()
    {
      @Override
      public void run ()
      {
        for (final CountryPacket country : countriesFrom (event))
        {
          final CountryPrimaryImageState state = CountryPrimaryImageState
                  .valueOf (Strings.toCase (event.getOwnerColor (country), LetterCase.UPPER));

          if (playMapActor.currentPrimaryImageStateOfCountryIs (state, country.getName ())) continue;

          playMapActor.setCountryState (country.getName (), state);
        }
      }
    });
  }

  @Handler
  void onEvent (final QuitGameEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.trace ("Event received [{}].", event);

    Gdx.app.postRunnable (new Runnable ()
    {
      @Override
      public void run ()
      {
        screenChanger.toScreen (ScreenId.PLAY_TO_MENU_LOADING);
      }
    });
  }

  private static void hideCursor ()
  {
    Gdx.graphics.setCursor (null);
  }

  private void showCursor ()
  {
    Gdx.graphics.setCursor (normalCursor);
  }

  private void updatePlayMapActor (final PlayMapActor playMapActor)
  {
    this.playMapActor = playMapActor;
    playMapActorCell.setActor (this.playMapActor.asActor ());
    debugInputProcessor.setPlayMapActor (this.playMapActor);
  }

  private void clearPlayMapActor ()
  {
    playMapActor.reset ();
    playMapActorCell.clearActor ();
    widgetFactory.destroyPlayMapActor (playMapActor.getMapMetadata ());
    playMapActor = PlayMapActor.NULL_PLAY_MAP_ACTOR;
    playMapActorCell.setActor (playMapActor.asActor ());
    debugInputProcessor.setPlayMapActor (playMapActor);
  }

  private void playBattleEffects (final int attackingCountryDeltaArmies, final int defendingCountryDeltaArmies)
  {
    if (attackingCountryDeltaArmies == 0 && defendingCountryDeltaArmies == 0) return;

    battleSingleExplosionSound.play ();
    screenShaker.shake ();

    if (attackingCountryDeltaArmies == -2 || defendingCountryDeltaArmies == -2)
    {
      Timer.schedule (new Timer.Task ()
      {
        @Override
        public void run ()
        {
          battleSingleExplosionSound.play ();
          screenShaker.shake ();
        }
      }, 0.25f);
    }
  }

  private final class OccupationPopupListener implements PopupListener
  {
    @Override
    public void onSubmit ()
    {
      final int deltaArmies = occupationPopup.getDeltaArmies ();
      final String sourceCountryName = occupationPopup.getSourceCountryName ();
      final String destinationCountryName = occupationPopup.getDestinationCountryName ();

      // TODO Production: Remove
      eventBus.publish (new DefaultStatusMessageEvent (
              new DefaultStatusMessage ("You occupied " + destinationCountryName + " with "
                      + Strings.pluralize (deltaArmies, "army", "armies") + " from " + sourceCountryName + "."),
              ImmutableSet.<PlayerPacket> of ()));

      // TODO Production: Remove
      eventBus.publish (new CountryArmiesChangedEvent (DebugPackets.from (sourceCountryName), -deltaArmies));

      // TODO Production: Remove
      eventBus.publish (new CountryArmiesChangedEvent (DebugPackets.from (destinationCountryName), deltaArmies));

      // TODO: Production: Publish event (OccupyCountryRequestEvent?)
    }

    @Override
    public void onShow ()
    {
      playMapActor.disable ();
    }

    @Override
    public void onHide ()
    {
      if (quitPopup.isShown ()) return;

      playMapActor.enable (mouseInput.position ());
    }
  }

  private final class ReinforcementPopupListener implements PopupListener
  {
    @Override
    public void onSubmit ()
    {
      final int deltaArmies = reinforcementPopup.getDeltaArmies ();
      final String sourceCountryName = reinforcementPopup.getSourceCountryName ();
      final String destinationCountryName = reinforcementPopup.getDestinationCountryName ();

      // TODO Production: Remove
      eventBus.publish (new DefaultStatusMessageEvent (
              new DefaultStatusMessage ("You reinforced " + destinationCountryName + " with "
                      + Strings.pluralize (deltaArmies, "army", "armies") + " from " + sourceCountryName + "."),
              ImmutableSet.<PlayerPacket> of ()));

      // TODO Production: Remove
      eventBus.publish (new CountryArmiesChangedEvent (DebugPackets.from (sourceCountryName), -deltaArmies));

      // TODO Production: Remove
      eventBus.publish (new CountryArmiesChangedEvent (DebugPackets.from (destinationCountryName), deltaArmies));

      // TODO: Production: Publish event (OccupyCountryRequestEvent?)
    }

    @Override
    public void onShow ()
    {
      playMapActor.disable ();
    }

    @Override
    public void onHide ()
    {
      if (quitPopup.isShown ()) return;

      playMapActor.enable (mouseInput.position ());
    }
  }

  private final class DefaultAttackPopupListener extends AbstractBattlePopupListener implements AttackPopupListener
  {
    @Override
    public void onBattle ()
    {
      final ImmutableList.Builder <DieFaceValue> attackerDieFaceValuesBuilder = ImmutableList.builder ();

      for (int i = 0; i < attackPopup.getActiveAttackerDieCount (); ++i)
      {
        attackerDieFaceValuesBuilder.add (Randomness.getRandomElementFrom (DieFaceValue.values ()));
      }

      final ImmutableList <DieFaceValue> attackerDieFaceValues = attackerDieFaceValuesBuilder.build ();

      attackPopup.rollAttackerDice (attackerDieFaceValues);

      final ImmutableList.Builder <DieFaceValue> defenderDieFaceValuesBuilder = ImmutableList.builder ();

      for (int i = 0; i < attackPopup.getActiveDefenderDieCount (); ++i)
      {
        defenderDieFaceValuesBuilder.add (Randomness.getRandomElementFrom (DieFaceValue.values ()));
      }

      final ImmutableList <DieFaceValue> defenderDieFaceValues = defenderDieFaceValuesBuilder.build ();

      attackPopup.rollDefenderDice (defenderDieFaceValues);

      tempOutcome.set (attackPopup.determineOutcome (attackerDieFaceValues, defenderDieFaceValues));

      final String attackingCountryName = tempOutcome.getAttackingCountryName ();
      final String defendingCountryName = tempOutcome.getDefendingCountryName ();
      final String defendingPlayerName = tempOutcome.getDefendingPlayerName ();
      final int attackingCountryDeltaArmies = tempOutcome.getAttackingCountryDeltaArmies ();
      final int defendingCountryDeltaArmies = tempOutcome.getDefendingCountryDeltaArmies ();

      playBattleEffects (attackingCountryDeltaArmies, defendingCountryDeltaArmies);

      // TODO Production: Remove
      if (attackingCountryDeltaArmies != 0)
      {
        final CountryPacket attackingCountry = DebugPackets.from (attackingCountryName);
        eventBus.publish (new CountryArmiesChangedEvent (attackingCountry, attackingCountryDeltaArmies));
      }

      // TODO Production: Remove
      if (defendingCountryDeltaArmies != 0)
      {
        final CountryPacket defendingCountry = DebugPackets.from (defendingCountryName);
        eventBus.publish (new CountryArmiesChangedEvent (defendingCountry, defendingCountryDeltaArmies));
      }

      // TODO Production: Remove
      eventBus.publish (StatusMessageEventFactory
              .create (Strings.format ("You attacked {} in {} from {}, destroying {} & losing {}!", defendingPlayerName,
                                       defendingCountryName, attackingCountryName,
                                       Strings.pluralize (Math.abs (defendingCountryDeltaArmies), "army", "armies"),
                                       Strings.pluralize (Math.abs (attackingCountryDeltaArmies), "army", "armies")),
                       ImmutableSet.<PlayerPacket> of ()));
    }

    @Override
    public void onAttackerWinFinal ()
    {
      // @formatter:off
      final String attackingCountryName = attackPopup.getAttackingCountryName ();
      final String defendingCountryName = attackPopup.getDefendingCountryName ();
      final CountryPrimaryImageState attackingCountryPrimaryImageState = playMapActor.getCurrentPrimaryImageStateOf (attackingCountryName);
      final int totalArmies = attackPopup.getAttackingCountryArmies ();
      final int minDestinationArmies = attackPopup.getActiveAttackerDieCount ();
      final int maxDestinationArmies = totalArmies - 1;
      final CountryActor sourceCountryActor = attackPopup.getAttackingCountryActor ();
      final CountryActor destinationCountryActor = attackPopup.getDefendingCountryActor ();
      // @formatter:on

      // TODO Production: Remove
      if (attackingCountryPrimaryImageState != null)
      {
        attackPopup.getDefendingCountryActor ().changePrimaryStateTo (attackingCountryPrimaryImageState);
        playMapActor.setCountryState (defendingCountryName, attackingCountryPrimaryImageState);
      }

      attackPopup.hide ();

      occupationPopup.show (minDestinationArmies, maxDestinationArmies, sourceCountryActor, destinationCountryActor,
                            totalArmies);

      battleResultPopup.setTitle ("Victory");
      battleResultPopup.setMessage (new DefaultMessage (
              "General, you conquered " + defendingCountryName + "!\nWe must now occupy it quickly."));
      battleResultPopup.show ();

      // TODO Production: Remove
      eventBus.publish (StatusMessageEventFactory.create (
                                                          Strings.format ("You conquered {}!",
                                                                          attackPopup.getDefendingCountryName ()),
                                                          ImmutableSet.<PlayerPacket> of ()));
    }

    @Override
    public void onAttackerLoseFinal ()
    {
      attackPopup.hide ();

      battleResultPopup.setTitle ("Defeat");
      battleResultPopup.setMessage (new DefaultMessage (
              "General, we have failed to conquer " + attackPopup.getDefendingCountryName () + "."));
      battleResultPopup.show ();

      // TODO Production: Remove
      eventBus.publish (StatusMessageEventFactory.create (
                                                          Strings.format ("You failed to conquer {}.",
                                                                          attackPopup.getDefendingCountryName ()),
                                                          ImmutableSet.<PlayerPacket> of ()));
    }

    @Override
    public void onRetreat ()
    {
      // TODO Production: Remove
      eventBus.publish (StatusMessageEventFactory
              .create (Strings.format ("You stopped attacking {} in {} from {}.", attackPopup.getDefendingPlayerName (),
                                       attackPopup.getDefendingCountryName (), attackPopup.getAttackingCountryName ()),
                       ImmutableSet.<PlayerPacket> of ()));
    }

    @Override
    public void onShow ()
    {
      playMapActor.disable ();

      eventBus.publish (StatusMessageEventFactory
              .create (Strings.format ("You are preparing to attack {} in {} from {}.",
                                       attackPopup.getDefendingPlayerName (), attackPopup.getDefendingCountryName (),
                                       attackPopup.getAttackingCountryName ()),
                       ImmutableSet.<PlayerPacket> of ()));

      attackPopup.startBattle ();
    }

    @Override
    public void onHide ()
    {
    }
  }

  private final class DefendPopupListener extends AbstractBattlePopupListener
  {
    @Override
    public void onBattle ()
    {
      final ImmutableList.Builder <DieFaceValue> defenderDieFaceValuesBuilder = ImmutableList.builder ();

      for (int i = 0; i < defendPopup.getActiveDefenderDieCount (); ++i)
      {
        defenderDieFaceValuesBuilder.add (Randomness.getRandomElementFrom (DieFaceValue.values ()));
      }

      final ImmutableList <DieFaceValue> defenderDieFaceValues = defenderDieFaceValuesBuilder.build ();

      defendPopup.rollDefenderDice (defenderDieFaceValues);

      final ImmutableList.Builder <DieFaceValue> attackerDieFaceValuesBuilder = ImmutableList.builder ();

      for (int i = 0; i < defendPopup.getActiveAttackerDieCount (); ++i)
      {
        attackerDieFaceValuesBuilder.add (Randomness.getRandomElementFrom (DieFaceValue.values ()));
      }

      final ImmutableList <DieFaceValue> attackerDieFaceValues = attackerDieFaceValuesBuilder.build ();

      defendPopup.rollAttackerDice (attackerDieFaceValues);

      tempOutcome.set (defendPopup.determineOutcome (attackerDieFaceValues, defenderDieFaceValues));

      final String attackingCountryName = tempOutcome.getAttackingCountryName ();
      final String defendingCountryName = tempOutcome.getDefendingCountryName ();
      final String attackingPlayerName = tempOutcome.getAttackingPlayerName ();
      final int attackingCountryDeltaArmies = tempOutcome.getAttackingCountryDeltaArmies ();
      final int defendingCountryDeltaArmies = tempOutcome.getDefendingCountryDeltaArmies ();

      playBattleEffects (attackingCountryDeltaArmies, defendingCountryDeltaArmies);

      // TODO Production: Remove
      final CountryPacket attackingCountryPacket = DebugPackets.from (attackingCountryName);
      eventBus.publish (new CountryArmiesChangedEvent (attackingCountryPacket, attackingCountryDeltaArmies));

      // TODO Production: Remove
      final CountryPacket defendingCountryPacket = DebugPackets.from (defendingCountryName);
      eventBus.publish (new CountryArmiesChangedEvent (defendingCountryPacket, defendingCountryDeltaArmies));

      // TODO Production: Remove
      eventBus.publish (StatusMessageEventFactory
              .create (Strings.format ("You defended {} against {} in {}, destroying {} & losing {}!",
                                       defendingCountryName, attackingPlayerName, attackingCountryName,
                                       Strings.pluralize (Math.abs (attackingCountryDeltaArmies), "army", "armies"),
                                       Strings.pluralize (Math.abs (defendingCountryDeltaArmies), "army", "armies")),
                       ImmutableSet.<PlayerPacket> of ()));
    }

    @Override
    public void onAttackerWinFinal ()
    {
      defendPopup.hide ();

      battleResultPopup.setTitle ("Defeat");
      battleResultPopup.setMessage (new DefaultMessage ("General, we have failed to defend "
              + defendPopup.getDefendingCountryName () + ".\nThe enemy has taken it."));
      battleResultPopup.show ();

      // TODO Production: Remove
      eventBus.publish (StatusMessageEventFactory.create (
                                                          Strings.format ("You were defeated in {} & it has been conquered by {}!",
                                                                          defendPopup.getDefendingCountryName (),
                                                                          defendPopup.getAttackingPlayerName ()),
                                                          ImmutableSet.<PlayerPacket> of ()));
    }

    @Override
    public void onAttackerLoseFinal ()
    {
      defendPopup.hide ();

      battleResultPopup.setTitle ("Victory");
      battleResultPopup.setMessage (new DefaultMessage ("General, we have successfully protected "
              + defendPopup.getDefendingCountryName () + " from the enemy!"));
      battleResultPopup.show ();

      // TODO Production: Remove
      eventBus.publish (StatusMessageEventFactory
              .create (Strings.format ("You defeated {} in {}!", defendPopup.getAttackingPlayerName (),
                                       defendPopup.getAttackingCountryName ()),
                       ImmutableSet.<PlayerPacket> of ()));
    }

    @Override
    public void onShow ()
    {
      playMapActor.disable ();

      eventBus.publish (StatusMessageEventFactory
              .create (Strings.format ("You are preparing to defend {} against {} in {}.",
                                       defendPopup.getDefendingCountryName (), defendPopup.getAttackingPlayerName (),
                                       defendPopup.getAttackingCountryName ()),
                       ImmutableSet.<PlayerPacket> of ()));

      defendPopup.startBattle ();
    }

    @Override
    public void onHide ()
    {
    }
  }

  private final class QuitPopupListener implements PopupListener
  {
    @Override
    public void onSubmit ()
    {
      screenChanger.toScreen (ScreenId.PLAY_TO_MENU_LOADING);
      eventBus.publishAsync (new QuitGameEvent ());
    }

    @Override
    public void onShow ()
    {
      playMapActor.disable ();
    }

    @Override
    public void onHide ()
    {
      if (defendPopup.isShown () || occupationPopup.isShown () || battleResultPopup.isShown ()) return;

      playMapActor.enable (mouseInput.position ());
    }
  }

  private final class BattleResultPopupListener extends PopupListenerAdapter
  {
    @Override
    public void onShow ()
    {
      if (quitPopup.isShown ())
      {
        quitPopup.hide (null);
        quitPopup.show (null);
      }

      if (occupationPopup.isShown ()) occupationPopup.disableInput ();
    }

    @Override
    public void onHide ()
    {
      if (occupationPopup.isShown ())
      {
        occupationPopup.enableInput ();
        return;
      }

      playMapActor.enable (mouseInput.position ());
    }
  }
}
