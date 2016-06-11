package com.forerunnergames.peril.common.net.events.server.notification;

import com.forerunnergames.peril.common.net.events.server.defaults.AbstractAttackResultEvent;
import com.forerunnergames.peril.common.net.packets.battle.BattleResultPacket;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;

public final class PlayerAttackDefeatEvent extends AbstractAttackResultEvent
{
  public PlayerAttackDefeatEvent (final PlayerPacket player, final BattleResultPacket battleResult)
  {
    super (player, battleResult);
  }

  @RequiredForNetworkSerialization
  private PlayerAttackDefeatEvent ()
  {
  }
}
