package com.forerunnergames.peril.common.net.events.server.success;

import com.forerunnergames.peril.common.net.events.server.defaults.AbstractPlayerEvent;
import com.forerunnergames.peril.common.net.events.server.interfaces.PlayerResponseSuccessEvent;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.peril.common.net.packets.territory.CountryPacket;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Strings;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;

public final class PlayerBeginAttackResponseSuccessEvent extends AbstractPlayerEvent
        implements PlayerResponseSuccessEvent
{
  private final CountryPacket attackerCountry;
  private final CountryPacket defenderCountry;
  private final PlayerPacket attacker;
  private final PlayerPacket defender;
  private final int minAttackerDieCount;
  private final int maxAttackerDieCount;

  public PlayerBeginAttackResponseSuccessEvent (final CountryPacket attackerCountry,
                                                final CountryPacket defenderCountry,
                                                final PlayerPacket attacker,
                                                final PlayerPacket defender,
                                                final int minAttackerDieCount,
                                                final int maxAttackerDieCount)
  {
    super (attacker);

    Arguments.checkIsNotNull (attackerCountry, "attackerCountry");
    Arguments.checkIsNotNull (defenderCountry, "defenderCountry");
    Arguments.checkIsNotNull (defender, "defender");
    Arguments.checkIsNotNegative (minAttackerDieCount, "minAttackerDieCount");
    Arguments.checkIsNotNegative (maxAttackerDieCount, "maxAttackerDieCount");

    this.attackerCountry = attackerCountry;
    this.defenderCountry = defenderCountry;
    this.attacker = attacker;
    this.defender = defender;
    this.minAttackerDieCount = minAttackerDieCount;
    this.maxAttackerDieCount = maxAttackerDieCount;
  }

  public CountryPacket getAttackerCountry ()
  {
    return attackerCountry;
  }

  public CountryPacket getDefenderCountry ()
  {
    return defenderCountry;
  }

  public PlayerPacket getAttacker ()
  {
    return attacker;
  }

  public PlayerPacket getDefender ()
  {
    return defender;
  }

  public int getMinAttackerDieCount ()
  {
    return minAttackerDieCount;
  }

  public int getMaxAttackerDieCount ()
  {
    return maxAttackerDieCount;
  }

  @Override
  public String toString ()
  {
    return Strings.format (
                           "{} | AttackerCountry: [{}] | DefenderCountry: [{}] | Attacker: [{}] | Defender: [{}] | MinAttackerDieCount: [{}] | MaxAttackerDieCount: [{}]",
                           super.toString (), attackerCountry, defenderCountry, attacker, defender, minAttackerDieCount,
                           maxAttackerDieCount);
  }

  @RequiredForNetworkSerialization
  private PlayerBeginAttackResponseSuccessEvent ()
  {
    attackerCountry = null;
    defenderCountry = null;
    attacker = null;
    defender = null;
    minAttackerDieCount = 0;
    maxAttackerDieCount = 0;
  }
}
