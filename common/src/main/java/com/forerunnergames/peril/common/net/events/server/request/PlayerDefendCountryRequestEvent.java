package com.forerunnergames.peril.common.net.events.server.request;

import com.forerunnergames.peril.common.net.events.server.abstracts.AbstractPlayerDefendCountryEvent;
import com.forerunnergames.peril.common.net.events.server.interfaces.PlayerInputRequestEvent;
import com.forerunnergames.peril.common.net.packets.battle.BattleActorPacket;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.peril.common.net.packets.territory.CountryPacket;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;

public final class PlayerDefendCountryRequestEvent extends AbstractPlayerDefendCountryEvent
        implements PlayerInputRequestEvent
{
  public PlayerDefendCountryRequestEvent (final PlayerPacket defendingPlayer,
                                          final CountryPacket defendingCountry,
                                          final BattleActorPacket attackerData)
  {
    super (defendingPlayer, defendingCountry, attackerData);
  }

  @RequiredForNetworkSerialization
  private PlayerDefendCountryRequestEvent ()
  {
    super (null, null, null);
  }
}
