package com.forerunnergames.peril.common.net.events.interfaces;

import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.tools.net.events.remote.RemoteEvent;

import com.google.common.collect.ImmutableSet;

public interface PlayersEvent extends RemoteEvent
{
  ImmutableSet <PlayerPacket> getPlayers ();
}
