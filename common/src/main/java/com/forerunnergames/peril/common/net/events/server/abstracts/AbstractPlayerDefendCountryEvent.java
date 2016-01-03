package com.forerunnergames.peril.common.net.events.server.abstracts;

import com.forerunnergames.peril.common.net.events.interfaces.PlayerEvent;
import com.forerunnergames.peril.common.net.packets.battle.BattleActorPacket;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.peril.common.net.packets.territory.CountryPacket;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;
import com.forerunnergames.tools.net.events.remote.origin.server.ServerEvent;

public abstract class AbstractPlayerDefendCountryEvent implements PlayerEvent, ServerEvent
{
  private final PlayerPacket defendingPlayer;
  private final CountryPacket defendingCountry;
  private final BattleActorPacket attackerData;

  public AbstractPlayerDefendCountryEvent (final PlayerPacket defendingPlayer,
                                           final CountryPacket defendingCountry,
                                           final BattleActorPacket attackerData)
  {
    Arguments.checkIsNotNull (defendingPlayer, "defendingPlayer");
    Arguments.checkIsNotNull (defendingCountry, "defendingCountry");
    Arguments.checkIsNotNull (attackerData, "attackerData");

    this.defendingPlayer = defendingPlayer;
    this.defendingCountry = defendingCountry;
    this.attackerData = attackerData;
  }

  @Override
  public PlayerPacket getPlayer ()
  {
    return defendingPlayer;
  }

  public CountryPacket getCountry ()
  {
    return defendingCountry;
  }

  public BattleActorPacket getAttackerData ()
  {
    return attackerData;
  }

  @RequiredForNetworkSerialization
  private AbstractPlayerDefendCountryEvent ()
  {
    defendingPlayer = null;
    defendingCountry = null;
    attackerData = null;
  }
}
