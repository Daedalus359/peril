package com.forerunnergames.peril.common.events.player;

import com.forerunnergames.peril.common.events.AbstractInternalCommunicationEvent;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.tools.common.Arguments;

public final class InternalPlayerLeaveGameEvent extends AbstractInternalCommunicationEvent
{
  private final PlayerPacket player;

  public InternalPlayerLeaveGameEvent (final PlayerPacket player)
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
}