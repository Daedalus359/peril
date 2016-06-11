package com.forerunnergames.peril.core.model.battle;

import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.id.Id;

public class DefaultAttackVector implements AttackVector
{
  private final Id playerId;
  private final Id sourceCountryId;
  private final Id targetCountryId;
  
  public DefaultAttackVector (final Id playerId,
                              final Id sourceCountryId,
                              final Id targetCountryId)
  {
    Arguments.checkIsNotNull (playerId, "playerId");
    Arguments.checkIsNotNull (sourceCountryId, "sourceCountryId");
    Arguments.checkIsNotNull (targetCountryId, "targetCountryId");
    
    this.playerId = playerId;
    this.sourceCountryId = sourceCountryId;
    this.targetCountryId = targetCountryId;
  }

  @Override
  public Id getPlayerId ()
  {
    return this.playerId;
  }

  @Override
  public Id getSourceCountry ()
  {
    return this.sourceCountryId;
  }

  @Override
  public Id getTargetCountry ()
  {
    return this.targetCountryId;
  }
}
