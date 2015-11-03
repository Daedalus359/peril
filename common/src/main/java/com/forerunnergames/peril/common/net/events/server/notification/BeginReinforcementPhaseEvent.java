package com.forerunnergames.peril.common.net.events.server.notification;

import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;
import com.forerunnergames.tools.net.events.remote.origin.server.ServerNotificationEvent;

public final class BeginReinforcementPhaseEvent implements ServerNotificationEvent
{
  private final PlayerPacket currentPlayer;

  public BeginReinforcementPhaseEvent (final PlayerPacket currentPlayer)
  {
    Arguments.checkIsNotNull (currentPlayer, "currentPlayer");
    
    this.currentPlayer = currentPlayer;
  }

  public PlayerPacket getCurrentPlayer ()
  {
    return currentPlayer;
  }

  @RequiredForNetworkSerialization
  private BeginReinforcementPhaseEvent ()
  {
    currentPlayer = null;
  }
}