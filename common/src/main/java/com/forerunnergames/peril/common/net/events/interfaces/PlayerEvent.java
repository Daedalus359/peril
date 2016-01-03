package com.forerunnergames.peril.common.net.events.interfaces;

import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.tools.net.events.remote.RemoteEvent;

public interface PlayerEvent extends RemoteEvent
{
  PlayerPacket getPlayer ();
}
