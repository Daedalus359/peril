package com.forerunnergames.peril.core.model.battle;

import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.id.Id;
import com.forerunnergames.tools.common.id.IdGenerator;

public final class AttackOrder
{
  private final Id attackOrderId = IdGenerator.generateUniqueId ();
  private final Id playerId;
  private final Id sourceCountry;
  private final Id targetCountry;
  private final int dieCount;

  AttackOrder (final Id playerId, final Id sourceCountry, final Id targetCountry, final int dieCount)
  {
    Arguments.checkIsNotNull (playerId, "playerId");
    Arguments.checkIsNotNull (sourceCountry, "sourceCountry");
    Arguments.checkIsNotNull (targetCountry, "targetCountry");
    Arguments.checkIsNotNegative (dieCount, "dieCount");

    this.playerId = playerId;
    this.sourceCountry = sourceCountry;
    this.targetCountry = targetCountry;
    this.dieCount = dieCount;
  }

  Id getId ()
  {
    return attackOrderId;
  }

  public Id getPlayerId ()
  {
    return playerId;
  }

  public Id getSourceCountry ()
  {
    return sourceCountry;
  }

  public Id getTargetCountry ()
  {
    return targetCountry;
  }

  public int getDieCount ()
  {
    return dieCount;
  }

  @Override
  public boolean equals (final Object obj)
  {
    if (!(obj instanceof AttackOrder)) return false;
    return ((AttackOrder) obj).attackOrderId.equals (attackOrderId);
  }

  @Override
  public int hashCode ()
  {
    return attackOrderId.hashCode ();
  }
}
