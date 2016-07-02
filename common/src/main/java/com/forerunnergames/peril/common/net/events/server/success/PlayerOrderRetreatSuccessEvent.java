package com.forerunnergames.peril.common.net.events.server.success;

import com.forerunnergames.peril.common.net.events.server.defaults.AbstractPlayerEvent;
import com.forerunnergames.peril.common.net.events.server.interfaces.PlayerSuccessEvent;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.peril.common.net.packets.territory.CountryPacket;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;

public class PlayerOrderRetreatSuccessEvent extends AbstractPlayerEvent implements PlayerSuccessEvent
{
  private final CountryPacket attackingCountry;
  private final CountryPacket defendingCountry;

  public PlayerOrderRetreatSuccessEvent (final PlayerPacket player,
                                         final CountryPacket attackingCountry,
                                         final CountryPacket defendingCountry)
  {
    super (player);

    Arguments.checkIsNotNull (attackingCountry, "attackingCountry");
    Arguments.checkIsNotNull (defendingCountry, "defendingCountry");

    this.attackingCountry = attackingCountry;
    this.defendingCountry = defendingCountry;
  }

  public CountryPacket getAttackingCountry ()
  {
    return attackingCountry;
  }

  public int getAttackingCountryArmyCount ()
  {
    return attackingCountry.getArmyCount ();
  }

  public CountryPacket getDefendingCountry ()
  {
    return getDefendingCountry ();
  }

  public int getDefendingCountryArmyCount ()
  {
    return defendingCountry.getArmyCount ();
  }

  @RequiredForNetworkSerialization
  private PlayerOrderRetreatSuccessEvent ()
  {
    attackingCountry = null;
    defendingCountry = null;
  }
}
