package com.forerunnergames.peril.core.shared.map;

import com.forerunnergames.peril.core.model.rules.GameMode;

final class NullMapMetadata extends AbstractMapMetadata
{
  @Override
  public String getName ()
  {
    return "";
  }

  @Override
  public MapType getType ()
  {
    return MapType.STOCK;
  }

  @Override
  public GameMode getMode ()
  {
    return GameMode.CLASSIC;
  }
}
