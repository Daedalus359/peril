package com.forerunnergames.peril.core.model.map;

import com.forerunnergames.peril.common.net.packets.territory.CountryPacket;
import com.forerunnergames.peril.core.model.map.country.CountryArmyModel;
import com.forerunnergames.peril.core.model.map.country.CountryOwnerModel;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Exceptions;
import com.forerunnergames.tools.common.Result;
import com.forerunnergames.tools.common.id.Id;

import com.google.common.collect.ImmutableSet;

public final class PlayMapStateBuilder
{
  private final PlayMapModel playMapModel;

  public PlayMapStateBuilder (final PlayMapModel playMapModel)
  {
    Arguments.checkIsNotNull (playMapModel, "playMapModel");

    this.playMapModel = playMapModel;
  }

  public OngoingCountryConfiguration forCountries (final ImmutableSet <Id> countryIds)
  {
    return new OngoingCountryConfiguration (countryIds);
  }

  public OngoingCountryConfiguration forCountries (final Iterable <Id> countryIds)
  {
    return forCountries (ImmutableSet.copyOf (countryIds));
  }

  public OngoingCountryConfiguration forCountry (final Id countryId)
  {
    return forCountries (ImmutableSet.of (countryId));
  }

  public class OngoingCountryConfiguration
  {
    private final ImmutableSet <Id> countryIds;

    private OngoingCountryConfiguration (final ImmutableSet <Id> countryIds)
    {
      assert countryIds != null;

      this.countryIds = countryIds;
    }

    public OngoingCountryConfiguration setOwner (final Id ownerId)
    {
      final CountryOwnerModel countryOwnerModel = playMapModel.getCountryOwnerModel ();
      for (final Id country : countryIds)
      {
        final CountryPacket countryPacket = playMapModel.getCountryMapGraphModel ().countryPacketWith (country);
        final Result <?> result = countryOwnerModel.requestToAssignCountryOwner (country, ownerId);
        if (result.failed ())
        {
          Exceptions.throwIllegalState ("Failed to assign country [{}] to owner [{}]: {}", countryPacket, ownerId,
                                        result.getFailureReason ());
        }
      }
      return this;
    }

    public OngoingCountryConfiguration addArmies (final int armyCount)
    {
      final CountryArmyModel countryArmyModel = playMapModel.getCountryArmyModel ();
      for (final Id country : countryIds)
      {
        final CountryPacket countryPacket = playMapModel.getCountryMapGraphModel ().countryPacketWith (country);
        final Result <?> result = countryArmyModel.requestToAddArmiesToCountry (country, armyCount);
        if (result.failed ())
        {
          Exceptions.throwIllegalState ("Failed to add {} armies to country [{}]: {}", armyCount, countryPacket,
                                        result.getFailureReason ());
        }
      }
      return this;
    }
  }
}