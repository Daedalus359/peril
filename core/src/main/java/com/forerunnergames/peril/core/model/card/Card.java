package com.forerunnergames.peril.core.model.card;

import com.forerunnergames.peril.common.game.CardType;
import com.forerunnergames.tools.common.assets.Asset;

public interface Card extends Asset
{
  CardType getType ();

  boolean typeIs (final CardType type);
}
