package com.forerunnergames.peril.common.net.events.server.notification;

import com.forerunnergames.peril.common.net.events.server.abstracts.AbstractArmiesChangedEvent;
import com.forerunnergames.peril.common.net.packets.territory.CountryPacket;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Strings;
import com.forerunnergames.tools.common.annotations.AllowNegative;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;

public final class CountryArmiesChangedEvent extends AbstractArmiesChangedEvent
{
  private final CountryPacket country;

  public CountryArmiesChangedEvent (final CountryPacket country, @AllowNegative final int deltaArmyCount)
  {
    super (deltaArmyCount);

    Arguments.checkIsNotNull (country, "country");

    this.country = country;
  }

  public CountryPacket getCountry ()
  {
    return country;
  }

  public String getCountryName ()
  {
    return country.getName ();
  }

  @Override
  public String toString ()
  {
    return Strings.format ("{} | Country: [{}]", super.toString (), country);
  }

  @RequiredForNetworkSerialization
  private CountryArmiesChangedEvent ()
  {
    super (0);
    country = null;
  }
}
