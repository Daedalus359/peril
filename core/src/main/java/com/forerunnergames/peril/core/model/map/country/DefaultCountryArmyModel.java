package com.forerunnergames.peril.core.model.map.country;

import com.forerunnergames.peril.common.game.rules.GameRules;
import com.forerunnergames.peril.common.net.events.server.interfaces.CountryArmyChangeDeniedEvent.Reason;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Preconditions;
import com.forerunnergames.tools.common.Result;
import com.forerunnergames.tools.common.Strings;
import com.forerunnergames.tools.common.id.Id;

public final class DefaultCountryArmyModel implements CountryArmyModel
{
  private final CountryMapGraphModel countryMapGraphModel;
  private final GameRules rules;

  public DefaultCountryArmyModel (final CountryMapGraphModel countryMapGraphModel, final GameRules rules)
  {
    Arguments.checkIsNotNull (countryMapGraphModel, "countryMapGraphModel");
    Arguments.checkIsNotNull (rules, "rules");

    this.countryMapGraphModel = countryMapGraphModel;
    this.rules = rules;
  }

  @Override
  public Result <Reason> requestToAddArmiesToCountry (final Id countryId, final int armyCount)
  {
    Arguments.checkIsNotNull (countryId, "countryId");
    Arguments.checkIsNotNegative (armyCount, "armyCount");

    final Country country = countryMapGraphModel.countryWith (countryId);
    if (country.getArmyCount () + armyCount > rules.getMaxArmiesOnCountry ())
    {
      return Result.failure (Reason.COUNTRY_ARMY_COUNT_OVERFLOW);
    }

    country.addArmies (armyCount);

    return Result.success ();
  }

  @Override
  public Result <Reason> requestToRemoveArmiesFromCountry (final Id countryId, final int armyCount)
  {
    Arguments.checkIsNotNull (countryId, "countryId");
    Arguments.checkIsNotNegative (armyCount, "armyCount");

    final Country country = countryMapGraphModel.countryWith (countryId);
    if (country.getArmyCount () - armyCount > rules.getMinArmiesOnCountry ())
    {
      return Result.failure (Reason.COUNTRY_ARMY_COUNT_UNDERFLOW);
    }

    country.removeArmies (armyCount);

    return Result.success ();
  }

  @Override
  public int getArmyCountFor (final Id countryId)
  {
    Arguments.checkIsNotNull (countryId, "countryId");
    Preconditions.checkIsTrue (countryMapGraphModel.existsCountryWith (countryId),
                               Strings.format ("No country with id [{}] exists.", countryId));

    return countryMapGraphModel.countryWith (countryId).getArmyCount ();
  }

  @Override
  public boolean armyCountIsAtLeast (final int minArmyCount, final Id countryId)
  {
    Arguments.checkIsNotNegative (minArmyCount, "minArmyCount");
    Arguments.checkIsNotNull (countryId, "countryId");

    return getArmyCountFor (countryId) >= minArmyCount;
  }
}