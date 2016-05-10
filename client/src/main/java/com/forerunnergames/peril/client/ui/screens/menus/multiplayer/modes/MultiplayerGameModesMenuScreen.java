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

package com.forerunnergames.peril.client.ui.screens.menus.multiplayer.modes;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import com.forerunnergames.peril.client.ui.screens.ScreenChanger;
import com.forerunnergames.peril.client.ui.screens.ScreenId;
import com.forerunnergames.peril.client.ui.screens.ScreenSize;
import com.forerunnergames.peril.client.ui.screens.menus.AbstractMenuScreen;
import com.forerunnergames.peril.client.ui.screens.menus.MenuScreenWidgetFactory;

public final class MultiplayerGameModesMenuScreen extends AbstractMenuScreen
{
  public MultiplayerGameModesMenuScreen (final MenuScreenWidgetFactory widgetFactory,
                                         final ScreenChanger screenChanger,
                                         final ScreenSize screenSize,
                                         final Batch batch)
  {
    super (widgetFactory, screenChanger, screenSize, batch);

    addTitle ("MULTIPLAYER", Align.bottomLeft, 40);
    addSubTitle ("GAME MODES");

    addMenuChoiceSpacer (22);

    addMenuChoice ("CLASSIC", new ClickListener (Input.Buttons.LEFT)
    {
      @Override
      public void clicked (final InputEvent event, final float x, final float y)
      {
        toScreen (ScreenId.MULTIPLAYER_CLASSIC_GAME_MODE_MENU);
      }
    });

    addMenuChoiceSpacer (10);

    addMenuChoice ("PERIL", new ClickListener (Input.Buttons.LEFT)
    {
      @Override
      public void clicked (final InputEvent event, final float x, final float y)
      {
        // TODO Production: Go to MultiplayerPerilGameModeMenuScreen.
        //toScreen (ScreenId.MULTIPLAYER_PERIL_GAME_MODE_MENU);
      }
    });

    addBackButton (new ClickListener (Input.Buttons.LEFT)
    {
      @Override
      public void clicked (final InputEvent event, final float x, final float y)
      {
        toScreen (ScreenId.MAIN_MENU);
      }
    });
  }

  @Override
  protected void onEscape ()
  {
    toScreen (ScreenId.MAIN_MENU);
  }
}
