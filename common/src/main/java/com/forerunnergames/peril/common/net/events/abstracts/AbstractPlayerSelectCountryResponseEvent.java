package com.forerunnergames.peril.common.net.events.abstracts;

import com.forerunnergames.peril.common.net.events.interfaces.PlayerSelectCountryResponseEvent;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Strings;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;

public abstract class AbstractPlayerSelectCountryResponseEvent implements PlayerSelectCountryResponseEvent
{
  private final String selectedCountryName;

  protected AbstractPlayerSelectCountryResponseEvent (final String selectedCountryName)
  {
    Arguments.checkIsNotNull (selectedCountryName, "selectedCountryName");

    this.selectedCountryName = selectedCountryName;
  }

  @RequiredForNetworkSerialization
  protected AbstractPlayerSelectCountryResponseEvent ()
  {
    selectedCountryName = null;
  }

  @Override
  public String getSelectedCountryName ()
  {
    return selectedCountryName;
  }

  @Override
  public String toString ()
  {
    return Strings.format ("{}: Selected Country Name: {}", getClass ().getSimpleName (), selectedCountryName);
  }
}
