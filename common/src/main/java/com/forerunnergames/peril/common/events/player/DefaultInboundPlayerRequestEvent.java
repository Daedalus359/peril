package com.forerunnergames.peril.common.events.player;

import com.forerunnergames.peril.common.events.AbstractInternalCommunicationEvent;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;
import com.forerunnergames.tools.net.events.remote.RequestEvent;

public class DefaultInboundPlayerRequestEvent <T extends RequestEvent> extends AbstractInternalCommunicationEvent
        implements InboundPlayerRequestEvent <T>
{
  private final PlayerPacket player;
  private final T event;

  public DefaultInboundPlayerRequestEvent (final PlayerPacket player, final T event)
  {
    Arguments.checkIsNotNull (player, "player");
    Arguments.checkIsNotNull (event, "event");

    this.player = player;
    this.event = event;
  }

  @Override
  public PlayerPacket getPlayer ()
  {
    return player;
  }

  @Override
  public T getRequestEvent ()
  {
    return event;
  }

  @RequiredForNetworkSerialization
  private DefaultInboundPlayerRequestEvent ()
  {
    player = null;
    event = null;
  }
}