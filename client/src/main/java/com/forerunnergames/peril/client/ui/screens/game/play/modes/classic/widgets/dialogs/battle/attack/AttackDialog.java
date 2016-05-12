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

package com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.widgets.dialogs.battle.attack;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import com.forerunnergames.peril.client.ui.screens.ScreenShaker;
import com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.widgets.dialogs.battle.AbstractBattleDialog;
import com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.widgets.dialogs.battle.BattleDialogWidgetFactory;
import com.forerunnergames.peril.client.ui.widgets.dialogs.KeyListener;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Event;

import net.engio.mbassy.bus.MBassador;

public final class AttackDialog extends AbstractBattleDialog
{
  private static final String TITLE_TEXT = "Attack";
  private static final String RETREAT_BUTTON_TEXT = "Retreat";
  private final AttackDialogListener listener;
  private TextButton quitButton;

  public AttackDialog (final BattleDialogWidgetFactory widgetFactory,
                      final Stage stage,
                      final ScreenShaker screenShaker,
                      final AttackDialogListener listener,
                      final MBassador <Event> eventBus)
  {
    super (widgetFactory, new AttackDialogDiceFactory (widgetFactory), TITLE_TEXT, stage, screenShaker, listener, eventBus);

    Arguments.checkIsNotNull (listener, "listener");

    this.listener = listener;
  }

  @Override
  protected void addButtons ()
  {
    quitButton = addTextButton (RETREAT_BUTTON_TEXT, DialogAction.HIDE, new ChangeListener ()
    {
      @Override
      public void changed (final ChangeEvent event, final Actor actor)
      {
        listener.onRetreat ();
        stopBattle ();
      }
    });
  }

  @Override
  protected void addKeys ()
  {
    addKey (Input.Keys.ESCAPE, DialogAction.HIDE, new KeyListener ()
    {
      @Override
      public void keyDown ()
      {
        quitButton.toggle ();
      }
    });
  }

  @Override
  protected void setDiceTouchable (final boolean areTouchable)
  {
    setAttackerDiceTouchable (areTouchable);
    setDefenderDiceTouchable (false);
  }
}
