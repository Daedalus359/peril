package com.forerunnergames.peril.common.net.events.server.success;

import com.forerunnergames.peril.common.net.events.interfaces.PlayerEvent;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Strings;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;
import com.forerunnergames.tools.net.events.remote.origin.server.SuccessEvent;

public final class PlayerJoinGameSuccessEvent implements PlayerEvent, SuccessEvent
{
  private final PlayerPacket player;

  public PlayerJoinGameSuccessEvent (final PlayerPacket player)
  {
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
