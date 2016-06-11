package com.forerunnergames.peril.common.net.events.server.defaults;

import com.forerunnergames.peril.common.net.packets.battle.BattleActorPacket;
import com.forerunnergames.peril.common.net.packets.battle.BattleResultPacket;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Strings;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;
import com.forerunnergames.tools.net.events.remote.origin.server.ServerNotificationEvent;

public abstract class AbstractAttackResultEvent extends AbstractPlayerEvent implements ServerNotificationEvent
{
  final BattleResultPacket battleResult;

  public AbstractAttackResultEvent (final PlayerPacket player, final BattleResultPacket battleResult)
  {
    super (player);

    Arguments.checkIsNotNull (battleResult, "battleResult");

    this.battleResult = battleResult;
  }

  public BattleResultPacket getBattleResult ()
  {
    return battleResult;
  }

  public BattleActorPacket getAttacker ()
  {
    return battleResult.getAttacker ();
  }

  public BattleActorPacket getDefender ()
  {
    return battleResult.getDefender ();
  }

  @Override
  public String toString ()
  {
    return Strings.format ("{} | BattleResult: [{}]", super.toString (), this.battleResult);
  }

  @RequiredForNetworkSerialization
  protected AbstractAttackResultEvent ()
  {
    battleResult = null;
  }
}
