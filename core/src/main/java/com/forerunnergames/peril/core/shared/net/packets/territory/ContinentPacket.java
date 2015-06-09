package com.forerunnergames.peril.core.shared.net.packets.territory;

import com.google.common.collect.ImmutableSet;

public interface ContinentPacket extends TerritoryPacket
{
  ImmutableSet <CountryPacket> getCountries ();

  boolean hasCountry (final CountryPacket country);

  int getReinforcementBonus ();
}
