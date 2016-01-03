package com.forerunnergames.peril.common.net.events.server.notification;

import com.forerunnergames.peril.common.net.events.interfaces.PlayerEvent;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Strings;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;
import com.forerunnergames.tools.net.events.remote.origin.server.ServerNotificationEvent;

public final class BeginAttackPhaseEvent implements PlayerEvent, ServerNotificationEvent
{
  private final PlayerPacket currentPlayer;

  public BeginAttackPhaseEvent (final PlayerPacket currentPlayer)
  {
    Arguments.checkIsNotNull (currentPlayer, "currentPlayer");

    this.currentPlayer = currentPlayer;
  }

  @RequiredForNetworkSerialization
  public BeginAttackPhaseEvent ()
  {
    currentPlayer = null;
  }

  @Override
  public PlayerPacket getPlayer ()
  {
    return currentPlayer;
  }

  @Override
  public String toString ()
  {
    return Strings.format ("{}: Player: [{}]", getClass ().getSimpleName (), currentPlayer);
  }
}
