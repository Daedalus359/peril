package com.forerunnergames.peril.core.model.map;

import static com.forerunnergames.tools.common.assets.AssetFluency.idOf;

import com.forerunnergames.peril.core.model.map.country.Country;
import com.forerunnergames.peril.core.model.rules.GameRules;
import com.forerunnergames.peril.core.shared.net.events.server.denied.PlayerSelectCountryInputResponseDeniedEvent;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Preconditions;
import com.forerunnergames.tools.common.Result;
import com.forerunnergames.tools.common.id.Id;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Map;

public final class PlayMapModel
{
  private final Map <Id, Country> countryIds;
  private final Map <Id, Id> countryToOwnerMap;

  public PlayMapModel (final ImmutableSet <Country> countries, final GameRules rules)
  {
    Arguments.checkIsNotNull (countries, "countries");
    Arguments.checkHasNoNullElements (countries, "countries");
    Arguments.checkIsNotNull (rules, "rules");
    Preconditions.checkIsTrue (countries.size () >= rules.getMinTotalCountryCount (),
                               "Country count is below minimum allowed!");
    Preconditions.checkIsTrue (countries.size () <= rules.getMaxTotalCountryCount (),
                               "Country count is above maximum allowed!");

    // init country id map
    final Builder <Id, Country> countryMapBuilder = ImmutableMap.builder ();
    for (final Country country : countries)
    {
      countryMapBuilder.put (country.getId (), country);
    }
    countryIds = countryMapBuilder.build ();

    countryToOwnerMap = new HashMap <> ();
  }

  public boolean existsCountryWith (final Id countryId)
  {
    Arguments.checkIsNotNull (countryId, "countryId");

    return countryIds.containsKey (countryId);
  }

  public Country countryWith (final Id countryId)
  {
    Arguments.checkIsNotNull (countryId, "countryId");
    checkValidCountryId (countryId);

    return countryIds.get (countryId);
  }

  public boolean existsCountryWith (final String countryName)
  {
    Arguments.checkIsNotNull (countryName, "countryName");

    return getCountryByName (countryName) != null;
  }

  public Country countryWith (final String countryName)
  {
    Arguments.checkIsNotNull (countryName, "countryName");

    final Country country = getCountryByName (countryName);
    if (country == null) throw new IllegalStateException ("Cannot find any country named: [" + countryName + "].");
    return country;
  }

  public boolean hasAnyUnownedCountries ()
  {
    // if the country -> owner map is less than the size of the country ID map, then there must be
    // some countries without an assigned owner.
    return countryToOwnerMap.size () < countryIds.size ();
  }

  public boolean allCountriesAreUnowned ()
  {
    return countryToOwnerMap.size () == 0;
  }

  public boolean hasAnyOwnedCountries ()
  {
    return countryToOwnerMap.size () > 0;
  }

  public boolean allCountriesAreOwned ()
  {
    return countryToOwnerMap.size () == countryIds.size ();
  }

  public boolean isCountryOwned (final Id countryId)
  {
    Arguments.checkIsNotNull (countryId, "countryId");

    return countryToOwnerMap.containsKey (countryId);
  }

  public boolean isCountryOwned (final String countryName)
  {
    Arguments.checkIsNotNull (countryName, "countryName");

    if (!existsCountryWith (countryName)) return false;
    return isCountryOwned (idOf (countryWith (countryName)));
  }

  /**
   * Assigns the Country specified by countryId to the owner specified by ownerId.
   *
   * @return success/failure Result with reason
   */
  public Result <PlayerSelectCountryInputResponseDeniedEvent.Reason> requestToAssignCountryOwner (final Id countryId,
                                                                                                  final Id ownerId)
  {
    Arguments.checkIsNotNull (countryId, "countryId");

    //@formatter:off
    if (!existsCountryWith (countryId)) return Result.failure (PlayerSelectCountryInputResponseDeniedEvent.Reason.COUNTRY_DOES_NOT_EXIST);
    if (isCountryOwned (countryId)) return Result.failure (PlayerSelectCountryInputResponseDeniedEvent.Reason.COUNTRY_ALREADY_OWNED);
    //@formatter:on

    countryToOwnerMap.put (countryId, ownerId);
    return Result.success ();
  }

  /**
   * Unassigns the Country specified by the given id from its current owner.
   *
   * @return success/failure Result
   */
  public Result <PlayerSelectCountryInputResponseDeniedEvent.Reason> requestToUnassignCountry (final Id countryId)
  {
    Arguments.checkIsNotNull (countryId, "countryId");

    //@formatter:off
    if (!existsCountryWith (countryId)) return Result.failure (PlayerSelectCountryInputResponseDeniedEvent.Reason.COUNTRY_DOES_NOT_EXIST);
    //@formatter:on

    countryToOwnerMap.remove (countryId);
    return Result.success ();
  }

  /**
   * @return the Id of the owner to which the Country specified by countryId is currently assigned.
   */
  public Id getOwnerOf (final Id countryId)
  {
    Arguments.checkIsNotNull (countryId, "countryId");
    checkValidCountryId (countryId);

    if (isCountryOwned (countryId)) return countryToOwnerMap.get (countryId);
    else throw new IllegalStateException ("Country with id [" + countryId + "] has no owner.");
  }

  public ImmutableSet <Country> getCountries ()
  {
    return ImmutableSet.copyOf (countryIds.values ());
  }

  public int getCountryCount ()
  {
    return countryIds.size ();
  }

  public boolean countryCountIs (final int n)
  {
    return getCountryCount () == n;
  }

  public boolean countryCountIsAtLeast (final int n)
  {
    return getCountryCount () >= n;
  }

  public int getOwnedCountryCount ()
  {
    return getOwnedCountries ().size ();
  }

  public boolean ownedCountryCountIs (final int n)
  {
    return getOwnedCountryCount () == n;
  }

  public boolean ownedCountryCountIsAtLeast (final int n)
  {
    return getOwnedCountryCount () >= n;
  }

  private Country getCountryByName (final String name)
  {
    assert name != null;
    assert !name.isEmpty ();

    for (final Id nextId : countryIds.keySet ())
    {
      final Country country = countryIds.get (nextId);
      if (country.hasName (name)) return country;
    }
    return null;
  }

  private ImmutableSet <Country> getOwnedCountries ()
  {
    final ImmutableSet.Builder <Country> countrySetBuilder = ImmutableSet.builder ();
    for (final Id id : countryIds.keySet ())
    {
      if (countryToOwnerMap.containsKey (id))
      {
        countrySetBuilder.add (countryIds.get (id));
      }
    }
    return countrySetBuilder.build ();
  }

  // internal convenience method for running precondition check.
  private void checkValidCountryId (final Id countryId)
  {
    Preconditions.checkIsTrue (countryIds.containsKey (countryId), "Unrecognized country id.");
  }
}
