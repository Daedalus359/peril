package com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.map.converters.coordinatetoname.screen;

import com.badlogic.gdx.math.Vector2;

import com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.map.converters.coordinatetocoordinate.ScreenToPlayMapCoordinateConverter;
import com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.map.converters.coordinatetoname.playmap.PlayMapCoordinateToTerritoryNameConverter;
import com.forerunnergames.peril.core.model.map.territory.TerritoryName;
import com.forerunnergames.tools.common.Arguments;

public abstract class AbstractScreenCoordinateToTerritoryNameConverter <T extends TerritoryName> implements
        ScreenCoordinateToTerritoryNameConverter <T>
{
  private final ScreenToPlayMapCoordinateConverter screenToPlayMapCoordinateConverter;
  private final PlayMapCoordinateToTerritoryNameConverter <T> playMapCoordinateToTerritoryNameConverter;

  protected AbstractScreenCoordinateToTerritoryNameConverter (final ScreenToPlayMapCoordinateConverter screenToPlayMapCoordinateConverter,
                                                              final PlayMapCoordinateToTerritoryNameConverter <T> playMapCoordinateToTerritoryNameConverter)
  {
    Arguments.checkIsNotNull (screenToPlayMapCoordinateConverter, "screenToPlayMapCoordinateConverter");
    Arguments.checkIsNotNull (playMapCoordinateToTerritoryNameConverter, "playMapCoordinateToTerritoryNameConverter");

    this.screenToPlayMapCoordinateConverter = screenToPlayMapCoordinateConverter;
    this.playMapCoordinateToTerritoryNameConverter = playMapCoordinateToTerritoryNameConverter;
  }

  @Override
  public T convert (final Vector2 screenCoordinate)
  {
    Arguments.checkIsNotNull (screenCoordinate, "screenCoordinate");

    return playMapCoordinateToTerritoryNameConverter.convert (screenToPlayMapCoordinateConverter
            .convert (screenCoordinate));
  }
}
