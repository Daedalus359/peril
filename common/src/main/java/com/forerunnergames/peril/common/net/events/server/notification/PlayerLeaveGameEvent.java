package com.forerunnergames.peril.common.net.events.server.notification;

import com.forerunnergames.peril.common.net.events.interfaces.PlayerEvent;
import com.forerunnergames.peril.common.net.events.interfaces.PlayersEvent;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Strings;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;
import com.forerunnergames.tools.net.events.remote.origin.server.ServerNotificationEvent;

import com.google.common.collect.ImmutableSet;

public final class PlayerLeaveGameEvent implements PlayerEvent, PlayersEvent, ServerNotificationEvent
{
  private final PlayerPacket player;
  private final ImmutableSet <PlayerPacket> players;

  public PlayerLeaveGameEvent (final PlayerPacket leavingPlayer, final ImmutableSet <PlayerPacket> remainingPlayers)
  {
    Arguments.checkIsNotNull (leavingPlayer, "leavingPlayer");
    Arguments.checkIsNotNull (remainingPlayers, "remainingPlayers");
    Arguments.checkHasNoNullElements (remainingPlayers, "remainingPlayers");

    this.player = leavingPlayer;
    this.players = remainingPlayers;
  }

  @Override
  public PlayerPacket getPlayer ()
  {
    return player;
  }

  @Override
  public ImmutableSet <PlayerPacket> getPlayers ()
  {
    return players;
  }

  public String getPlayerName ()
  {
    return player.getName ();
  }

  @Override
  public String toString ()
  {
    return Strings.format ("{}: Player Who Left: {} | Remaining Players: {}", getClass ().getSimpleName (), player,
                           players);
  }

  @RequiredForNetworkSerialization
  private PlayerLeaveGameEvent ()
  {
    player = null;
    players = null;
  }
}
