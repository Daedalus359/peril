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

package com.forerunnergames.peril.common.net.events.server.request;

import com.forerunnergames.peril.common.net.events.server.defaults.AbstractPlayerEvent;
import com.forerunnergames.peril.common.net.events.server.interfaces.PlayerInputRequestEvent;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.peril.common.net.packets.territory.CountryPacket;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Strings;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;

public final class PlayerAttackOrderRequestEvent extends AbstractPlayerEvent implements PlayerInputRequestEvent
{
  private final PlayerPacket defendingPlayer;
  private final CountryPacket attackingCountry;
  private final CountryPacket defendingCountry;
  private final int minValidDieCount;
  private final int maxValidDieCount;

  public PlayerAttackOrderRequestEvent (final PlayerPacket attackingPlayer,
                                          final PlayerPacket defendingPlayer,
                                          final CountryPacket attackingCountry,
                                          final CountryPacket defendingCountry,
                                          final int minValidDieCount,
                                          final int maxValidDieCount)
  {
    super (attackingPlayer);

    Arguments.checkIsNotNull (defendingPlayer, "defendingPlayer");
    Arguments.checkIsNotNull (attackingCountry, "attackingCountry");
    Arguments.checkIsNotNull (defendingCountry, "defendingCountry");
    Arguments.checkIsNotNegative (minValidDieCount, "minValidDieCount");
    Arguments.checkIsNotNegative (maxValidDieCount, "maxValidDieCount");

    this.defendingPlayer = defendingPlayer;
    this.attackingCountry = attackingCountry;
    this.defendingCountry = defendingCountry;
    this.minValidDieCount = minValidDieCount;
    this.maxValidDieCount = maxValidDieCount;
  }

  public PlayerPacket getDefendingPlayer ()
  {
    return defendingPlayer;
  }

  public CountryPacket getAttackingCountry ()
  {
    return attackingCountry;
  }

  public CountryPacket getDefendingCountry ()
  {
    return defendingCountry;
  }

  public int getMinValidDieCount ()
  {
    return minValidDieCount;
  }

  public int getMaxValidDieCount ()
  {
    return maxValidDieCount;
  }

  @Override
  public String toString ()
  {
    return Strings.format (
                           "{} | DefendingPlayer: [{}] | AttackingCountry: [{}] | DefendingCountry: [{}] | MinValidDieCount: [{}] | MaxValidDieCount: [{}]",
                           super.toString (), defendingPlayer, attackingCountry, defendingCountry, minValidDieCount,
                           maxValidDieCount);
  }

  @RequiredForNetworkSerialization
  private PlayerAttackOrderRequestEvent ()
  {
    defendingPlayer = null;
    attackingCountry = null;
    defendingCountry = null;
    minValidDieCount = -1;
    maxValidDieCount = -1;
  }
}
