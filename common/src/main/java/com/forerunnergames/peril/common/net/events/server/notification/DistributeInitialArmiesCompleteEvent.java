package com.forerunnergames.peril.common.net.events.server.notification;

import com.forerunnergames.peril.common.net.events.interfaces.PlayersEvent;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Strings;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;
import com.forerunnergames.tools.net.events.remote.origin.server.ServerNotificationEvent;

import com.google.common.collect.ImmutableSet;

public final class DistributeInitialArmiesCompleteEvent implements PlayersEvent, ServerNotificationEvent
{
  private final ImmutableSet <PlayerPacket> players;

  public DistributeInitialArmiesCompleteEvent (final ImmutableSet <PlayerPacket> players)
  {
    Arguments.checkIsNotNull (players, "players");
    Arguments.checkHasNoNullElements (players, "players");

    this.players = players;
  }

  @Override
  public ImmutableSet <PlayerPacket> getPlayers ()
  {
    return players;
  }

  @Override
  public String toString ()
  {
    return Strings.format ("{}: Players: {}", getClass ().getSimpleName (), players);
  }

  @RequiredForNetworkSerialization
  private DistributeInitialArmiesCompleteEvent ()
  {
    players = null;
  }
}
