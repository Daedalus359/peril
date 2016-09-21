/*
 * Copyright © 2011 - 2013 Aaron Mahan.
 * Copyright © 2013 - 2016 Forerunner Games, LLC.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.forerunnergames.peril.common.net.events.server.success;

import com.forerunnergames.peril.common.game.PlayerColor;
import com.forerunnergames.peril.common.net.events.server.defaults.AbstractBattleResultEvent;
import com.forerunnergames.peril.common.net.packets.battle.BattleResultPacket;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;

public final class PlayerOrderAttackSuccessEvent extends AbstractBattleResultEvent
{

  public PlayerOrderAttackSuccessEvent (final BattleResultPacket result)
  {
    super (result);
  }

  @Override
  public PlayerPacket getPlayer ()
  {
    return getAttackingPlayer ();
  }

  @Override
  public String getPlayerName ()
  {
    return getAttackingPlayerName ();
  }

  @Override
  public PlayerColor getPlayerColor ()
  {
    return getAttackingPlayerColor ();
  }

  @Override
  public int getPlayerTurnOrder ()
  {
    return getAttackingPlayerTurnOrder ();
  }

  @Override
  public int getPlayerArmiesInHand ()
  {
    return getAttackingPlayerArmiesInHand ();
  }

  @Override
  public int getPlayerCardsInHand ()
  {
    return getAttackingPlayerCardsInHand ();
  }

  @RequiredForNetworkSerialization
  private PlayerOrderAttackSuccessEvent ()
  {
  }
}
