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

import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Strings;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;
import com.forerunnergames.tools.net.events.remote.origin.server.SuccessEvent;

public final class PlayerJoinGameSuccessEvent implements SuccessEvent
{
  private final PlayerPacket player;

  public PlayerJoinGameSuccessEvent (final PlayerPacket player)
  {
    Arguments.checkIsNotNull (player, "player");

    this.player = player;
  }

  public PlayerPacket getPlayer ()
  {
    return player;
  }

  public String getPlayerName ()
  {
    return player.getName ();
  }

  public String getPlayerColor ()
  {
    return player.getColor ();
  }

  public int getPlayerTurnOrder ()
  {
    return player.getTurnOrder ();
  }

  @Override
  public String toString ()
  {
    return Strings.format ("{}: Player: {}", getClass ().getSimpleName (), player);
  }

  @RequiredForNetworkSerialization
  private PlayerJoinGameSuccessEvent ()
  {
    player = null;
  }
}
