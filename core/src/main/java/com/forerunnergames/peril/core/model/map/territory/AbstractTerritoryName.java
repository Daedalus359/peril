package com.forerunnergames.peril.core.model.map.territory;

import com.forerunnergames.tools.common.Arguments;

public abstract class AbstractTerritoryName implements TerritoryName
{
  private final String name;

  protected AbstractTerritoryName (final String name)
  {
    Arguments.checkIsNotNull (name, "name");

    this.name = name;
  }

  @Override
  public String asString ()
  {
    return name;
  }

  @Override
  public boolean is (final String name)
  {
    Arguments.checkIsNotNull (name, "name");

    return this.name.equals (name);
  }

  @Override
  public boolean isNot (final String name)
  {
    return !is (name);
  }

  @Override
  public boolean isUnknown ()
  {
    return name.isEmpty ();
  }

  @Override
  public int hashCode ()
  {
    return name.hashCode ();
  }

  @Override
  public boolean equals (final Object o)
  {
    if (this == o) return true;
    if (o == null || getClass () != o.getClass ()) return false;

    final AbstractTerritoryName that = (AbstractTerritoryName) o;

    return name.equals (that.name);
  }

  @Override
  public int compareTo (final TerritoryName territoryName)
  {
    return name.compareTo (territoryName.asString ());
  }

  @Override
  public String toString ()
  {
    return String.format ("%1$s: %2$s", getClass ().getSimpleName (), name);
  }
}
