package com.forerunnergames.peril.client.ui.screens.game.play.modes.classic;

import com.badlogic.gdx.Screen;

import com.forerunnergames.peril.client.input.MouseInput;
import com.forerunnergames.peril.client.ui.Assets;
import com.forerunnergames.peril.client.ui.screens.ScreenController;
import com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.map.actors.PlayMapActorFactory;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Classes;
import com.forerunnergames.tools.common.Event;

import net.engio.mbassy.bus.MBassador;

public final class ClassicPlayScreenFactory
{
  public static Screen create (final ScreenController screenController,
                               final MouseInput mouseInput,
                               final MBassador <Event> eventBus)
  {
    Arguments.checkIsNotNull (screenController, "screenController");
    Arguments.checkIsNotNull (mouseInput, "mouseInput");
    Arguments.checkIsNotNull (eventBus, "eventBus");

    return new ClassicPlayScreen (screenController, new PlayScreenWidgetFactory (Assets.skin, eventBus),
            PlayMapActorFactory.create (mouseInput), new PlayScreenMusic (), eventBus);
  }

  private ClassicPlayScreenFactory ()
  {
    Classes.instantiationNotAllowed ();
  }
}
