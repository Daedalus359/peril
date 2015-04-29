package com.forerunnergames.peril.client.ui.screens.menus.multiplayer.modes;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import com.forerunnergames.peril.client.ui.screens.ScreenChanger;
import com.forerunnergames.peril.client.ui.screens.ScreenId;
import com.forerunnergames.peril.client.ui.screens.ScreenSize;
import com.forerunnergames.peril.client.ui.screens.menus.AbstractMenuScreen;
import com.forerunnergames.peril.client.ui.screens.menus.MenuScreenWidgetFactory;

public final class MultiplayerGameModesMenuScreen extends AbstractMenuScreen
{
  public MultiplayerGameModesMenuScreen (final MenuScreenWidgetFactory widgetFactory,
                                         final ScreenChanger screenChanger,
                                         final ScreenSize screenSize)
  {
    super (widgetFactory, screenChanger, screenSize);
  }

  @Override
  protected void addTitle ()
  {
    addTitleWithSubtitle ("MULTIPLAYER", "GAME MODES");
  }

  @Override
  protected void addMenuChoices ()
  {
    addMenuChoice ("CLASSIC", new ChangeListener ()
    {
      @Override
      public void changed (final ChangeEvent event, final Actor actor)
      {
        toScreen (ScreenId.MULTIPLAYER_CLASSIC_GAME_MODE_MENU);
      }
    });

    addMenuChoice ("PERIL", new ChangeListener ()
    {
      @Override
      public void changed (final ChangeEvent event, final Actor actor)
      {
        toScreen (ScreenId.MULTIPLAYER_PERIL_GAME_MODE_MENU);
      }
    });
  }

  @Override
  protected void onEscape ()
  {
    toScreen (ScreenId.MAIN_MENU);
  }

  @Override
  protected void addButtons ()
  {
    addBackButton (new ChangeListener ()
    {
      @Override
      public void changed (final ChangeEvent event, final Actor actor)
      {
        toScreen (ScreenId.MAIN_MENU);
      }
    });
  }
}
