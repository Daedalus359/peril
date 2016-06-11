package com.forerunnergames.peril.core.model.battle;

import com.forerunnergames.tools.common.id.Id;

public interface AttackVector
{
  Id getPlayerId ();

  Id getSourceCountry ();

  Id getTargetCountry ();
}
