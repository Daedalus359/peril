package com.forerunnergames.peril.common.net.events.server.denied;

import com.forerunnergames.peril.common.net.events.server.abstracts.AbstractDeniedEvent;
import com.forerunnergames.peril.common.net.events.server.denied.PlayerOccupyCountryResponseDeniedEvent.Reason;
import com.forerunnergames.peril.common.net.events.server.interfaces.PlayerResponseDeniedEvent;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;

public final class PlayerOccupyCountryResponseDeniedEvent extends AbstractDeniedEvent <Reason>
        implements PlayerResponseDeniedEvent <Reason>
{
  private final PlayerPacket player;

  public PlayerOccupyCountryResponseDeniedEvent (final PlayerPacket player, final Reason reason)
  {
    super (reason);

    this.player = player;
  }

  @Override
  public PlayerPacket getPlayer ()
  {
    return player;
  }

  public enum Reason
  {
    DELTA_ARMY_COUNT_BELOW_MIN,
    DELTA_ARMY_COUNT_EXCEEDS_MAX
  }

  @RequiredForNetworkSerialization
  private PlayerOccupyCountryResponseDeniedEvent ()
  {
    super (null);

    player = null;
  }
}
