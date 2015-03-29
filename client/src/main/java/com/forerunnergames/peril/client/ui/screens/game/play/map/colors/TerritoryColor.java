package com.forerunnergames.peril.client.ui.screens.game.play.map.colors;

import com.forerunnergames.tools.common.color.RgbaColorComponent;

public interface TerritoryColor <T extends RgbaColorComponent>
{
  T getComponent ();

  int hashCode ();

  boolean equals (final Object o);
}
