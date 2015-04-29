package com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.map.actors;

import com.badlogic.gdx.math.Vector2;

import com.forerunnergames.peril.client.settings.PlayMapSettings;
import com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.map.data.CountryImageData;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Classes;

public final class CountryArmyTextActorFactory
{
  private static final Vector2 tempPosition = new Vector2 ();

  public static CountryArmyTextActor create (final CountryImageData countryImageData)
  {
    Arguments.checkIsNotNull (countryImageData, "countryImageData");

    final CountryArmyTextActor countryArmyTextActor = new CountryArmyTextActor ();

    tempPosition.set (countryImageData.getReferenceTextUpperLeft ());
    tempPosition.y = PlayMapSettings.REFERENCE_HEIGHT - tempPosition.y;
    tempPosition.scl (PlayMapSettings.REFERENCE_PLAY_MAP_SPACE_TO_ACTUAL_PLAY_MAP_SPACE_SCALING);

    countryArmyTextActor.setCircleTopLeft (tempPosition);
    countryArmyTextActor.setCircleSize (PlayMapSettings.COUNTRY_ARMY_CIRCLE_SIZE_ACTUAL_PLAY_MAP_SPACE);
    countryArmyTextActor.setName (countryImageData.getName ());

    return countryArmyTextActor;
  }

  private CountryArmyTextActorFactory ()
  {
    Classes.instantiationNotAllowed ();
  }
}