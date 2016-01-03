package com.forerunnergames.peril.common.net.events.server.success;

import com.forerunnergames.peril.common.net.events.server.abstracts.AbstractPlayerDefendCountryEvent;
import com.forerunnergames.peril.common.net.events.server.interfaces.PlayerResponseSuccessEvent;
import com.forerunnergames.peril.common.net.packets.battle.BattleActorPacket;
import com.forerunnergames.peril.common.net.packets.defaults.DefaultBattleActorPacket;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.peril.common.net.packets.territory.CountryPacket;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;

public final class PlayerDefendCountryResponseSuccessEvent extends AbstractPlayerDefendCountryEvent
        implements PlayerResponseSuccessEvent
{
  private final BattleActorPacket defenderData;

  public PlayerDefendCountryResponseSuccessEvent (final PlayerPacket defendingPlayer,
                                                  final CountryPacket defendingCountry,
                                                  final int defenderDieCount,
                                                  final BattleActorPacket attackerData)
  {
    super (defendingPlayer, defendingCountry, attackerData);

    defenderData = new DefaultBattleActorPacket (defendingPlayer, defendingCountry, defenderDieCount);
  }

  public BattleActorPacket getDefenderData ()
  {
    return defenderData;
  }

  @RequiredForNetworkSerialization
  private PlayerDefendCountryResponseSuccessEvent ()
  {
    super (null, null, null);

    defenderData = null;
  }
}
