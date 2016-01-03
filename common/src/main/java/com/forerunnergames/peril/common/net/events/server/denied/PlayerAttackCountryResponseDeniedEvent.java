package com.forerunnergames.peril.common.net.events.server.denied;

import com.forerunnergames.peril.common.net.events.server.abstracts.AbstractDeniedEvent;
import com.forerunnergames.peril.common.net.events.server.denied.PlayerAttackCountryResponseDeniedEvent.Reason;
import com.forerunnergames.peril.common.net.events.server.interfaces.PlayerResponseDeniedEvent;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;

public class PlayerAttackCountryResponseDeniedEvent extends AbstractDeniedEvent <Reason> implements
        PlayerResponseDeniedEvent <Reason>
{
  private final PlayerPacket player;

  public PlayerAttackCountryResponseDeniedEvent (final PlayerPacket player, final Reason reason)
  {
    super (reason);

    this.player = player;
  }

  public enum Reason
  {
    SOURCE_COUNTRY_DOES_NOT_EXIST,
    TARGET_COUNTRY_DOES_NOT_EXIST,
    NOT_OWNER_OF_SOURCE_COUNTRY,
    ALREADY_OWNER_OF_TARGET_COUNTRY,
    INSUFFICIENT_ARMY_COUNT,
    INVALID_DIE_COUNT,
    COUNTRIES_NOT_ADJACENT;
  }

  @Override
  public PlayerPacket getPlayer ()
  {
    return player;
  }

  @RequiredForNetworkSerialization
  private PlayerAttackCountryResponseDeniedEvent ()
  {
    super (null);

    player = null;
  }
}
