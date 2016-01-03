package com.forerunnergames.peril.common.net.events.server.notification;

import com.forerunnergames.peril.common.net.events.interfaces.PlayerEvent;
import com.forerunnergames.peril.common.net.events.server.abstracts.AbstractArmiesChangedEvent;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.annotations.AllowNegative;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;

public final class PlayerArmiesChangedEvent extends AbstractArmiesChangedEvent implements PlayerEvent
{
  private final PlayerPacket player;

  public PlayerArmiesChangedEvent (final PlayerPacket player, @AllowNegative final int deltaArmyCount)
  {
    super (deltaArmyCount);

    Arguments.checkIsNotNull (player, "player");

    this.player = player;
  }

  @Override
  public PlayerPacket getPlayer ()
  {
    return player;
  }

  public String getPlayerName ()
  {
    return player.getName ();
  }

  @RequiredForNetworkSerialization
  private PlayerArmiesChangedEvent ()
  {
    super (0);
    player = null;
  }
}
