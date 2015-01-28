package com.forerunnergames.peril.client.ui.screens.game.play.map.loaders;

import com.forerunnergames.peril.client.ui.screens.game.play.map.colors.CountryColor;
import com.forerunnergames.peril.core.model.map.country.CountryName;
import com.forerunnergames.tools.common.Arguments;

public final class CountryColorToNameLoader extends AbstractTerritoryColorToNameLoader <CountryColor, CountryName>
{
  @Override
  protected CountryColor createTerritoryColor (final int colorComponentValue)
  {
    Arguments.checkIsNotNull (colorComponentValue, "colorComponentValue");

    return new CountryColor (colorComponentValue);
  }

  @Override
  protected CountryName createTerritoryName (final String nameValue)
  {
    Arguments.checkIsNotNull (nameValue, "nameValue");

    return new CountryName (nameValue);
  }
}