package com.forerunnergames.peril.core.shared.net.events.client.request;

import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;
import com.forerunnergames.tools.net.events.RequestEvent;

public final class PlayerJoinGameRequestEvent implements RequestEvent
{
  private final String playerName;

  public PlayerJoinGameRequestEvent (final String playerName)
  {
    Arguments.checkIsNotNull (playerName, "playerName");

    this.playerName = playerName;
  }

  public String getPlayerName ()
  {
    return playerName;
  }

  @Override
  public String toString ()
  {
    return String.format ("%1$s: Player name: %2$s", ((Object) this).getClass ().getSimpleName (), playerName);
  }

  @RequiredForNetworkSerialization
  private PlayerJoinGameRequestEvent ()
  {
    playerName = null;
  }
}