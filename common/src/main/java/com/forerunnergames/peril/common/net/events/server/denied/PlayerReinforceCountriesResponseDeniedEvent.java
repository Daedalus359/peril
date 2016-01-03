package com.forerunnergames.peril.common.net.events.server.denied;

import com.forerunnergames.peril.common.net.events.interfaces.PlayerEvent;
import com.forerunnergames.peril.common.net.events.server.interfaces.CountryArmyChangeDeniedEvent;
import com.forerunnergames.peril.common.net.events.server.interfaces.CountryArmyChangeDeniedEvent.Reason;
import com.forerunnergames.peril.common.net.events.server.interfaces.PlayerResponseDeniedEvent;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.tools.common.Strings;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;

public final class PlayerReinforceCountriesResponseDeniedEvent implements CountryArmyChangeDeniedEvent, PlayerEvent,
        PlayerResponseDeniedEvent <Reason>
{
  private final PlayerPacket player;
  private final Reason reason;

  public PlayerReinforceCountriesResponseDeniedEvent (final PlayerPacket player, final Reason reason)
  {
    this.player = player;
    this.reason = reason;
  }

  @Override
  public PlayerPacket getPlayer ()
  {
    return player;
  }

  @Override
  public Reason getReason ()
  {
    return reason;
  }

  @Override
  public String toString ()
  {
    return Strings.format ("{}: Player: [{}] | Reason: [{}]", getClass ().getSimpleName (), player.getName (),
                           reason.toString ());
  }

  @RequiredForNetworkSerialization
  private PlayerReinforceCountriesResponseDeniedEvent ()
  {
    player = null;
    reason = null;
  }
}
