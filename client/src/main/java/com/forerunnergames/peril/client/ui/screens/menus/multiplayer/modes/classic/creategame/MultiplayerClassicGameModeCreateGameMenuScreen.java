/*
 * Copyright © 2011 - 2013 Aaron Mahan.
 * Copyright © 2013 - 2016 Forerunner Games, LLC.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.forerunnergames.peril.client.ui.screens.menus.multiplayer.modes.classic.creategame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import com.forerunnergames.peril.client.events.CreateGameEvent;
import com.forerunnergames.peril.client.input.MouseInput;
import com.forerunnergames.peril.client.settings.InputSettings;
import com.forerunnergames.peril.client.ui.screens.ScreenChanger;
import com.forerunnergames.peril.client.ui.screens.ScreenId;
import com.forerunnergames.peril.client.ui.screens.ScreenSize;
import com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.playmap.countrycounter.CountryCounter;
import com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.playmap.io.loaders.ClientMapMetadataLoaderFactory;
import com.forerunnergames.peril.client.ui.screens.menus.AbstractMenuScreen;
import com.forerunnergames.peril.client.ui.screens.menus.MenuScreenWidgetFactory;
import com.forerunnergames.peril.client.ui.widgets.dialogs.Dialog;
import com.forerunnergames.peril.common.game.DefaultGameConfiguration;
import com.forerunnergames.peril.common.game.GameConfiguration;
import com.forerunnergames.peril.common.game.GameMode;
import com.forerunnergames.peril.common.game.InitialCountryAssignment;
import com.forerunnergames.peril.common.game.rules.ClassicGameRules;
import com.forerunnergames.peril.common.game.rules.GameRules;
import com.forerunnergames.peril.common.game.rules.GameRulesFactory;
import com.forerunnergames.peril.common.map.MapMetadata;
import com.forerunnergames.peril.common.map.MapType;
import com.forerunnergames.peril.common.map.PlayMapLoadingException;
import com.forerunnergames.peril.common.map.io.MapMetadataLoader;
import com.forerunnergames.peril.common.settings.GameSettings;
import com.forerunnergames.peril.common.settings.NetworkSettings;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.DefaultMessage;
import com.forerunnergames.tools.common.Event;
import com.forerunnergames.tools.common.LetterCase;
import com.forerunnergames.tools.common.Maths;
import com.forerunnergames.tools.common.Strings;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;

import java.util.Iterator;
import java.util.Set;

import javax.annotation.Nullable;

import net.engio.mbassy.bus.MBassador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MultiplayerClassicGameModeCreateGameMenuScreen extends AbstractMenuScreen
{
  private static final Logger log = LoggerFactory.getLogger (MultiplayerClassicGameModeCreateGameMenuScreen.class);
  private static final String TITLE_TEXT = "CREATE MULTIPLAYER GAME";
  private static final String SUBTITLE_TEXT = "CLASSIC MODE";
  private static final String FORWARD_BUTTON_TEXT = "CREATE GAME";
  private static final String SERVER_NAME_SETTING_LABEL_TEXT = "Title";
  private static final String PLAYERS_SETTING_LABEL_TEXT = "Players";
  private static final String SPECTATORS_SETTING_SETTING_LABEL_TEXT = "Spectators";
  private static final String MAP_SETTING_LABEL_TEXT = "Map";
  private static final String WIN_PERCENT_SETTING_LABEL_TEXT = "Win Percent";
  private static final String INITIAL_COUNTRY_ASSIGNMENT_SETTING_LABEL_TEXT = "Initial Countries";
  private static final MapMetadataLoader MAPS_LOADER = new ClientMapMetadataLoaderFactory (GameMode.CLASSIC)
          .create (MapType.STOCK, MapType.CUSTOM);
  private static final int WIN_PERCENT_INCREMENT = 5;
  private final MenuScreenWidgetFactory widgetFactory;
  private final Dialog errorDialog;
  private final TextField playerNameTextField;
  private final TextField clanNameTextField;
  private final TextField serverNameTextField;
  private final CheckBox clanNameCheckBox;
  private final SelectBox <Integer> winPercentSelectBox;
  private final SelectBox <String> initialCountryAssignmentSelectBox;
  private final SelectBox <Integer> spectatorLimitSelectBox;
  private final Label playerLimitLabel;
  private final Label mapNameLabel;
  private final ImageButton customizePlayersButton;
  private final ImageButton customizeMapButton;
  private final CountryCounter countryCounter;
  private final Label playerSettingsSectionTitleLabel;
  private final Label playerNameSettingLabel;
  private final Label clanTagSettingLabel;
  private final Label gameSettingsSectionTitleLabel;
  private final Label serverNameSettingLabel;
  private final Label playerLimitSettingLabel;
  private final Label spectatorLimitSettingLabel;
  private final Label mapSettingLabel;
  private final Label winPercentSettingLabel;
  private final Label initialCountryAssignmentSettingLabel;
  private final Button forwardButton;
  private Set <MapMetadata> maps;
  private int totalCountryCount;
  @Nullable
  private Iterator <MapMetadata> mapIterator = null;
  private MapMetadata currentMap = MapMetadata.NULL;
  private boolean isFirstTimeOnScreen = true;

  public MultiplayerClassicGameModeCreateGameMenuScreen (final MenuScreenWidgetFactory widgetFactory,
                                                         final ScreenChanger screenChanger,
                                                         final ScreenSize screenSize,
                                                         final MouseInput mouseInput,
                                                         final Batch batch,
                                                         final CountryCounter countryCounter,
                                                         final MBassador <Event> eventBus)

  {
    super (widgetFactory, screenChanger, screenSize, mouseInput, batch, eventBus);

    Arguments.checkIsNotNull (widgetFactory, "widgetFactory");
    Arguments.checkIsNotNull (countryCounter, "countryCounter");

    this.widgetFactory = widgetFactory;
    this.countryCounter = countryCounter;

    errorDialog = createErrorDialog ();

    addTitle (TITLE_TEXT, Align.bottomLeft, 40);
    addSubTitle (SUBTITLE_TEXT);

    playerNameTextField = widgetFactory.createPlayerNameTextField ();
    clanNameTextField = widgetFactory.createClanNameTextField ();
    serverNameTextField = widgetFactory.createServerNameTextField ();

    clanNameCheckBox = widgetFactory.createClanNameCheckBox (new ChangeListener ()
    {
      @Override
      public void changed (final ChangeEvent event, final Actor actor)
      {
        clanNameTextField.setText (clanNameCheckBox.isChecked () ? clanNameTextField.getText () : "");
        clanNameTextField.setDisabled (!clanNameCheckBox.isChecked ());
      }
    });

    clanNameCheckBox.setChecked (!clanNameTextField.getText ().isEmpty ());
    clanNameTextField.setDisabled (!clanNameCheckBox.isChecked ());

    // @formatter:off
    playerLimitLabel = widgetFactory.createPlayerLimitLabel (String.valueOf (InputSettings.INITIAL_CLASSIC_MODE_PLAYER_LIMIT));
    maps = loadMaps ();
    currentMap = findMapOrFirstMap (InputSettings.INITIAL_CLASSIC_MODE_MAP_NAME);
    mapNameLabel = widgetFactory.createMapNameLabel (asMapNameLabelText (currentMap));
    totalCountryCount = calculateCurrentMapTotalCountryCount ();
    // @formatter:on

    customizePlayersButton = widgetFactory.createCustomizePlayersButton (new ClickListener (Input.Buttons.LEFT)
    {
      @Override
      public void clicked (final InputEvent event, final float x, final float y)
      {
        // TODO Show Players Dialog.

        if (Integer.valueOf (playerLimitLabel.getText ().toString ()) < ClassicGameRules.MAX_PLAYERS)
        {
          playerLimitLabel.setText (String.valueOf (Integer.valueOf (playerLimitLabel.getText ().toString ()) + 1));
        }
        else
        {
          playerLimitLabel.setText (String.valueOf (ClassicGameRules.MIN_PLAYER_LIMIT));
        }

        updateWinPercentSelectBoxItems ();
      }
    });

    customizeMapButton = widgetFactory.createCustomizeMapButton (new ClickListener (Input.Buttons.LEFT)
    {
      @Override
      public void clicked (final InputEvent event, final float x, final float y)
      {
        // TODO Show Map Dialog.

        currentMap = nextMap ();
        mapNameLabel.setText (asMapNameLabelText (currentMap));
        totalCountryCount = calculateCurrentMapTotalCountryCount ();
        updateWinPercentSelectBoxItems ();
      }
    });

    // @formatter:off
    spectatorLimitSelectBox = widgetFactory.createSpectatorsSelectBox ();
    final Array <Integer> spectatorLimits = new Array <> (GameSettings.MAX_SPECTATORS - GameSettings.MIN_SPECTATORS + 1);
    for (int i = GameSettings.MIN_SPECTATORS; i <= GameSettings.MAX_SPECTATORS; ++i)
    {
      spectatorLimits.add (i);
    }
    spectatorLimitSelectBox.setItems (spectatorLimits);
    spectatorLimitSelectBox.setSelected (InputSettings.INITIAL_SPECTATOR_LIMIT);
    // @formatter:off

    // @formatter:on
    initialCountryAssignmentSelectBox = widgetFactory.createInitialCountryAssignmentSelectBox ();
    final Array <String> initialCountryAssignments = new Array <> (InitialCountryAssignment.count ());
    for (final InitialCountryAssignment initialCountryAssignment : InitialCountryAssignment.values ())
    {
      initialCountryAssignments.add (Strings.toProperCase (initialCountryAssignment.name ()));
    }
    initialCountryAssignmentSelectBox.setItems (initialCountryAssignments);
    initialCountryAssignmentSelectBox.setSelected (Strings
            .toProperCase (InputSettings.INITIAL_CLASSIC_MODE_COUNTRY_ASSIGNMENT.name ()));
    // @formatter:on

    winPercentSelectBox = widgetFactory.createWinPercentSelectBox ();
    updateWinPercentSelectBoxItems ();
    selectInitialWinPercentItem ();

    // @formatter:off
    playerSettingsSectionTitleLabel = widgetFactory.createPlayerSettingsSectionTitleLabel ();
    playerNameSettingLabel = widgetFactory.createPlayerNameSettingLabel ();
    clanTagSettingLabel = widgetFactory.createClanTagSettingLabel ();
    gameSettingsSectionTitleLabel = widgetFactory.createGameSettingsSectionTitleLabel ();
    serverNameSettingLabel = widgetFactory.createMenuSettingLabel (SERVER_NAME_SETTING_LABEL_TEXT);
    playerLimitSettingLabel = widgetFactory.createMenuSettingLabel (PLAYERS_SETTING_LABEL_TEXT);
    spectatorLimitSettingLabel = widgetFactory.createMenuSettingLabel (SPECTATORS_SETTING_SETTING_LABEL_TEXT);
    mapSettingLabel = widgetFactory.createMenuSettingLabel (MAP_SETTING_LABEL_TEXT);
    winPercentSettingLabel = widgetFactory.createMenuSettingLabel (WIN_PERCENT_SETTING_LABEL_TEXT);
    initialCountryAssignmentSettingLabel = widgetFactory.createMenuSettingLabel (INITIAL_COUNTRY_ASSIGNMENT_SETTING_LABEL_TEXT);
    // @formatter:on

    final VerticalGroup verticalGroup = new VerticalGroup ();
    verticalGroup.align (Align.topLeft);

    final Table playerSettingsTable = new Table ().top ().left ();
    playerSettingsTable.add ().height (23);
    playerSettingsTable.row ();
    playerSettingsTable.add (playerSettingsSectionTitleLabel).size (540, 40).fill ().padLeft (60).left ();

    playerSettingsTable.row ();

    final Table playerNameTable = new Table ();
    playerNameTable.add (playerNameSettingLabel).size (150, 40).fill ().padLeft (90).left ().spaceRight (10);
    playerNameTable.add (playerNameTextField).size (270, 28).fill ().left ().spaceLeft (10);
    playerSettingsTable.add (playerNameTable).left ();

    playerSettingsTable.row ();

    final Table clanTable = new Table ();
    clanTable.add (clanTagSettingLabel).size (150, 40).fill ().padLeft (90).left ().spaceRight (10);
    clanTable.add (clanNameCheckBox).size (20, 20).fill ().left ().spaceLeft (10).spaceRight (8);
    clanTable.add (clanNameTextField).size (80, 28).fill ().left ().spaceLeft (8);
    playerSettingsTable.add (clanTable).left ();

    verticalGroup.addActor (playerSettingsTable);

    final Table gameSettingsTable = new Table ().top ().left ();
    gameSettingsTable.row ();
    gameSettingsTable.add ().height (20);
    gameSettingsTable.row ();
    gameSettingsTable.add (gameSettingsSectionTitleLabel).size (540, 40).fill ().padLeft (60).left ();

    gameSettingsTable.row ();

    final Table serverNameTable = new Table ();
    serverNameTable.add (serverNameSettingLabel).size (150, 40).fill ().padLeft (90).left ().spaceRight (10);
    serverNameTable.add (serverNameTextField).size (270, 28).fill ().left ().spaceLeft (10);
    gameSettingsTable.add (serverNameTable).left ();

    gameSettingsTable.row ();

    final Table mapTable = new Table ();
    mapTable.add (mapSettingLabel).size (150, 40).fill ().padLeft (90).left ().spaceRight (10);
    mapTable.add (mapNameLabel).size (238, 28).fill ().left ().spaceLeft (10).spaceRight (4);
    mapTable.add (customizeMapButton).size (28, 28).fill ().left ().spaceLeft (4);
    gameSettingsTable.add (mapTable).left ();

    gameSettingsTable.row ();

    final Table playersTable = new Table ();
    playersTable.add (playerLimitSettingLabel).size (150, 40).fill ().padLeft (90).left ().spaceRight (10);
    playersTable.add (playerLimitLabel).size (76, 28).fill ().left ().spaceLeft (10).spaceRight (4);
    playersTable.add (customizePlayersButton).size (28, 28).fill ().left ().spaceLeft (4);
    gameSettingsTable.add (playersTable).left ();

    gameSettingsTable.row ();

    final Table spectatorsTable = new Table ();
    spectatorsTable.add (spectatorLimitSettingLabel).size (150, 40).fill ().padLeft (90).left ().spaceRight (10);
    spectatorsTable.add (spectatorLimitSelectBox).size (108, 28).fill ().left ().spaceLeft (10);
    gameSettingsTable.add (spectatorsTable).left ();

    gameSettingsTable.row ();

    final Table winPercentTable = new Table ();
    winPercentTable.add (winPercentSettingLabel).size (150, 40).fill ().padLeft (90).left ().spaceRight (10);
    winPercentTable.add (winPercentSelectBox).size (108, 28).fill ().left ().spaceLeft (10);
    gameSettingsTable.add (winPercentTable).left ();

    gameSettingsTable.row ();

    // @formatter:off
    final Table initialCountryAssignmentTable = new Table ();
    initialCountryAssignmentTable.add (initialCountryAssignmentSettingLabel).size (150, 40).fill ().padLeft (90).left ().spaceRight (10);
    initialCountryAssignmentTable.add (initialCountryAssignmentSelectBox).size (108, 28).fill ().left ().spaceLeft (10);
    gameSettingsTable.add (initialCountryAssignmentTable).left ();
    // @formatter:on

    verticalGroup.addActor (gameSettingsTable);

    addContent (verticalGroup);

    addBackButton (new ClickListener (Input.Buttons.LEFT)
    {
      @Override
      public void clicked (final InputEvent event, final float x, final float y)
      {
        contractMenuBar (new Runnable ()
        {
          @Override
          public void run ()
          {
            toScreen (ScreenId.MULTIPLAYER_CLASSIC_GAME_MODE_MENU);
          }
        });
      }
    });

    forwardButton = addForwardButton (FORWARD_BUTTON_TEXT, new ChangeListener ()
    {
      @Override
      public void changed (final ChangeEvent event, final Actor actor)
      {
        if (currentMap.equals (MapMetadata.NULL))
        {
          errorDialog.setMessage (new DefaultMessage ("Please select a valid map before continuing."));
          errorDialog.show ();
          return;
        }

        final String playerName = playerNameTextField.getText ();

        if (!GameSettings.isValidPlayerNameWithoutClanTag (playerName))
        {
          errorDialog.setMessage (new DefaultMessage (Strings
                  .format ("Invalid player name: \'{}\'\n\nValid player name rules:\n\n{}", playerName,
                           GameSettings.VALID_PLAYER_NAME_DESCRIPTION)));
          errorDialog.show ();
          return;
        }

        final String clanName = clanNameTextField.getText ();

        if (!clanNameTextField.isDisabled () && !GameSettings.isValidClanName (clanName))
        {
          errorDialog.setMessage (new DefaultMessage (Strings
                  .format ("Invalid clan tag: \'{}\'\n\nValid clan tag rules:\n\n{}", clanName,
                           GameSettings.VALID_CLAN_NAME_DESCRIPTION)));
          errorDialog.show ();
          return;
        }

        final String playerNameWithOptionalClanTag = GameSettings.getPlayerNameWithOptionalClanTag (playerName,
                                                                                                    clanName);
        final int playerLimit = Integer.valueOf (playerLimitLabel.getText ().toString ());
        final int spectatorLimit = spectatorLimitSelectBox.getSelected ();
        final int winPercent = winPercentSelectBox.getSelected ();
        final InitialCountryAssignment initialCountryAssignment = InitialCountryAssignment.valueOf (Strings
                .toCase (initialCountryAssignmentSelectBox.getSelected (), LetterCase.UPPER));
        final GameRules gameRules = GameRulesFactory.create (GameMode.CLASSIC, playerLimit, winPercent,
                                                             totalCountryCount, initialCountryAssignment);
        final GameConfiguration gameConfig = new DefaultGameConfiguration (GameMode.CLASSIC, playerLimit,
                spectatorLimit, winPercent, initialCountryAssignment, currentMap, gameRules);
        final String serverName = serverNameTextField.getText ();

        if (!NetworkSettings.isValidServerName (serverName))
        {
          errorDialog.setMessage (new DefaultMessage (Strings
                  .format ("Invalid server name: \'{}\'\n\nValid server name rules:\n\n{}", serverName,
                           NetworkSettings.VALID_SERVER_NAME_DESCRIPTION)));
          errorDialog.show ();
          return;
        }

        toScreen (ScreenId.MENU_TO_PLAY_LOADING);

        // The menu-to-play loading screen is now active & can therefore receive events.

        publishAsync (new CreateGameEvent (serverName, gameConfig, playerNameWithOptionalClanTag));
      }
    });
  }

  @Override
  public void show ()
  {
    super.show ();

    expandMenuBar ();

    mapIterator = null;
    maps = loadMaps ();
    currentMap = findMapOrFirstMap (InputSettings.INITIAL_CLASSIC_MODE_MAP_NAME);
    mapNameLabel.setText (asMapNameLabelText (currentMap));
    totalCountryCount = calculateCurrentMapTotalCountryCount ();
    updateWinPercentSelectBoxItems ();

    // @formatter:off
    playerNameTextField.setStyle (widgetFactory.createPlayerNameTextFieldStyle ());
    clanNameTextField.setStyle (widgetFactory.createClanNameTextFieldStyle ());
    serverNameTextField.setStyle (widgetFactory.createServerNameTextFieldStyle ());
    clanNameCheckBox.setStyle (widgetFactory.createClanNameCheckBoxStyle ());
    final SelectBox.SelectBoxStyle winPercentSelectBoxStyle = widgetFactory.createWinPercentSelectBoxStyle ();
    winPercentSelectBox.setStyle (winPercentSelectBoxStyle);
    winPercentSelectBox.getScrollPane ().setStyle (winPercentSelectBoxStyle.scrollStyle);
    winPercentSelectBox.getList ().setStyle (winPercentSelectBoxStyle.listStyle);
    final SelectBox.SelectBoxStyle spectatorLimitSelectBoxStyle = widgetFactory.createSpectatorLimitSelectBoxStyle ();
    spectatorLimitSelectBox.setStyle (spectatorLimitSelectBoxStyle);
    spectatorLimitSelectBox.getScrollPane ().setStyle (spectatorLimitSelectBoxStyle.scrollStyle);
    spectatorLimitSelectBox.getList ().setStyle (spectatorLimitSelectBoxStyle.listStyle);
    final SelectBox.SelectBoxStyle initialCountryAssignmentSelectBoxStyle = widgetFactory.createInitialCountryAssignmentSelectBoxStyle ();
    initialCountryAssignmentSelectBox.setStyle (initialCountryAssignmentSelectBoxStyle);
    initialCountryAssignmentSelectBox.getScrollPane ().setStyle (initialCountryAssignmentSelectBoxStyle.scrollStyle);
    initialCountryAssignmentSelectBox.getList ().setStyle (initialCountryAssignmentSelectBoxStyle.listStyle);
    playerLimitLabel.setStyle (widgetFactory.createPlayerLimitLabelStyle ());
    mapNameLabel.setStyle (widgetFactory.createMapNameLabelStyle ());
    customizePlayersButton.setStyle (widgetFactory.createCustomizePlayersButtonStyle ());
    customizeMapButton.setStyle (widgetFactory.createCustomizeMapButtonStyle ());
    playerSettingsSectionTitleLabel.setStyle (widgetFactory.createPlayerSettingsSectionTitleLabelStyle ());
    playerNameSettingLabel.setStyle (widgetFactory.createPlayerNameSettingLabelStyle ());
    clanTagSettingLabel.setStyle (widgetFactory.createClanTagSettingLabelStyle ());
    gameSettingsSectionTitleLabel.setStyle (widgetFactory.createGameSettingsSectionTitleLabelStyle ());
    serverNameSettingLabel.setStyle (widgetFactory.createMenuSettingLabelStyle ());
    playerLimitSettingLabel.setStyle (widgetFactory.createMenuSettingLabelStyle ());
    spectatorLimitSettingLabel.setStyle (widgetFactory.createMenuSettingLabelStyle ());
    mapSettingLabel.setStyle (widgetFactory.createMenuSettingLabelStyle ());
    winPercentSettingLabel.setStyle (widgetFactory.createMenuSettingLabelStyle ());
    initialCountryAssignmentSettingLabel.setStyle (widgetFactory.createMenuSettingLabelStyle ());
    // @formatter:on

    if (isFirstTimeOnScreen && InputSettings.AUTO_CREATE_MULTIPLAYER_GAME)
    {
      // Execute next frame because a screen transition is still in progress.
      Gdx.app.postRunnable (new Runnable ()
      {
        @Override
        public void run ()
        {
          forwardButton.toggle ();
        }
      });
      isFirstTimeOnScreen = false;
    }
  }

  @Override
  protected boolean onEscape ()
  {
    if (!super.onEscape ())
    {
      contractMenuBar (new Runnable ()
      {
        @Override
        public void run ()
        {
          toScreen (ScreenId.MULTIPLAYER_CLASSIC_GAME_MODE_MENU);
        }
      });
    }
    return true;
  }

  private static String asMapNameLabelText (final MapMetadata map)
  {
    return Strings.toProperCase (map.getName ());
  }

  private ImmutableSet <MapMetadata> loadMaps ()
  {
    try
    {
      return MAPS_LOADER.load ();
    }
    catch (final PlayMapLoadingException e)
    {
      final String errorMessage = Strings
              .format ("There was a problem loading map data.\n\nProblem:\n\n{}\n\nDetails\n\n{}", Throwables
                      .getRootCause (e).getMessage (), Strings.toString (e));

      log.error (errorMessage);

      errorDialog.setMessage (new DefaultMessage (errorMessage));
      errorDialog.show ();

      return ImmutableSet.of (MapMetadata.NULL);
    }
  }

  private int calculateCurrentMapTotalCountryCount ()
  {
    try
    {
      return countryCounter.count (currentMap);
    }
    catch (final PlayMapLoadingException e)
    {
      final String errorMessage = Strings
              .format ("Could not read country data for {} map \'{}\'.\n\nProblem:\n\n{}\n\nDetails\n\n{}", currentMap
                      .getType ().name ().toLowerCase (), Strings.toProperCase (currentMap.getName ()), Throwables
                      .getRootCause (e).getMessage (), Strings.toString (e));

      log.error (errorMessage);

      errorDialog.setMessage (new DefaultMessage (errorMessage));
      errorDialog.show ();

      return ClassicGameRules.DEFAULT_TOTAL_COUNTRY_COUNT;
    }
  }

  private void updateWinPercentSelectBoxItems ()
  {
    if (currentMap.equals (MapMetadata.NULL))
    {
      winPercentSelectBox.setItems (ClassicGameRules.MAX_WIN_PERCENTAGE);
      return;
    }

    // @formatter:off
    final GameRules gameRules = new ClassicGameRules.Builder ()
            .totalCountryCount (totalCountryCount)
            .playerLimit (Integer.valueOf (playerLimitLabel.getText ().toString ()))
            .winPercentage (ClassicGameRules.MAX_WIN_PERCENTAGE)
            .initialCountryAssignment (InitialCountryAssignment.valueOf (initialCountryAssignmentSelectBox.getSelected ().toUpperCase ()))
            .build ();

    final Array <Integer> winPercentCounts =
            new Array <> (gameRules.getMaxWinPercentage () - gameRules.getMinWinPercentage () + 1);

    for (int i = Maths.nextHigherMultiple (gameRules.getMinWinPercentage (), WIN_PERCENT_INCREMENT);
         i <= gameRules.getMaxWinPercentage (); i += WIN_PERCENT_INCREMENT)
    {
      winPercentCounts.add (i);
    }
    // @formatter:on

    winPercentSelectBox.setItems (winPercentCounts);
  }

  private void selectInitialWinPercentItem ()
  {
    winPercentSelectBox.setSelected (InputSettings.INITIAL_CLASSIC_MODE_WIN_PERCENT);

    if (winPercentSelectBox.getItems ().contains (InputSettings.INITIAL_CLASSIC_MODE_WIN_PERCENT, true)) return;

    errorDialog
            .setMessage (new DefaultMessage (
                    Strings.format ("{} % is not a valid win percent for {} players on {} map: \'{}\'.\n\nPlease check your settings file.",
                                    InputSettings.INITIAL_CLASSIC_MODE_WIN_PERCENT, playerLimitLabel.getText ()
                                            .toString (), currentMap.getType ().name ().toLowerCase (), Strings
                                            .toProperCase (currentMap.getName ()))));

    errorDialog.show ();
  }

  private MapMetadata nextMap ()
  {
    if (mapIterator == null || !mapIterator.hasNext ()) mapIterator = maps.iterator ();

    if (!mapIterator.hasNext ())
    {
      errorDialog.setMessage (new DefaultMessage (Strings.format ("No maps could be found.")));
      errorDialog.show ();
      return MapMetadata.NULL;
    }

    return mapIterator.next ();
  }

  private MapMetadata findMapOrFirstMap (final String mapName)
  {
    final MapMetadata firstMap = nextMap ();
    MapMetadata map = firstMap;

    while (!map.getName ().equalsIgnoreCase (InputSettings.INITIAL_CLASSIC_MODE_MAP_NAME))
    {
      map = nextMap ();

      if (map.equals (firstMap))
      {
        errorDialog.setMessage (new DefaultMessage (Strings
                .format ("Could not find any map named \'{}\'.\n\nPlease check your settings file. ", mapName)));
        errorDialog.show ();

        mapIterator = null;

        return nextMap ();
      }
    }

    return map;
  }
}
