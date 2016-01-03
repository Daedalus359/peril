package com.forerunnergames.peril.common.net.events.server.notification;

import com.forerunnergames.peril.common.net.events.interfaces.PlayerEvent;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Strings;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;
import com.forerunnergames.tools.net.events.remote.origin.server.ServerNotificationEvent;

public class BeginPlayerTurnEvent implements PlayerEvent, ServerNotificationEvent
{
  private final PlayerPacket player;

  public BeginPlayerTurnEvent (final PlayerPacket player)
  {
    Arguments.checkIsNotNull (player, "player");

    this.player = player;
  }

  @Override
  public PlayerPacket getPlayer ()
  {
    return player;
  }

  @Override
  public String toString ()
  {
    return Strings.format ("{}: Player: {}", getClass ().getSimpleName (), player);
  }

  @RequiredForNetworkSerialization
  private BeginPlayerTurnEvent ()
  {
    player = null;
  }
}
