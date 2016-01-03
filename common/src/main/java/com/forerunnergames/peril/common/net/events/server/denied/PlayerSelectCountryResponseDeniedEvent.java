package com.forerunnergames.peril.common.net.events.server.denied;

import com.forerunnergames.peril.common.net.events.abstracts.AbstractPlayerSelectCountryResponseEvent;
import com.forerunnergames.peril.common.net.events.server.interfaces.CountryOwnerChangeDeniedEvent;
import com.forerunnergames.peril.common.net.events.server.interfaces.CountryOwnerChangeDeniedEvent.Reason;
import com.forerunnergames.peril.common.net.events.server.interfaces.PlayerResponseDeniedEvent;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;

public final class PlayerSelectCountryResponseDeniedEvent extends AbstractPlayerSelectCountryResponseEvent
        implements CountryOwnerChangeDeniedEvent, PlayerResponseDeniedEvent <Reason>
{
  private final PlayerPacket player;
  private final Reason reason;

  public PlayerSelectCountryResponseDeniedEvent (final PlayerPacket player,
                                                 final String selectedCountryName,
                                                 final Reason reason)
  {
    super (selectedCountryName);

    Arguments.checkIsNotNull (player, "player");
    Arguments.checkIsNotNull (reason, "reason");

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

  @RequiredForNetworkSerialization
  private PlayerSelectCountryResponseDeniedEvent ()
  {
    super (null);
    player = null;
    reason = null;
  }
}
