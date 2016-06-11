package com.forerunnergames.peril.common.net.events.server.defaults;

import com.forerunnergames.peril.common.net.events.server.interfaces.PlayerDeniedEvent;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Strings;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;

public abstract class AbstractPlayerDeniedEvent <T> extends AbstractPlayerEvent implements PlayerDeniedEvent <T>
{
  private final T reason;

  public AbstractPlayerDeniedEvent (final PlayerPacket player, final T reason)
  {
    super (player);

    Arguments.checkIsNotNull (reason, "reason");

    this.reason = reason;
  }

  @Override
  public T getReason ()
  {
    return reason;
  }

  @Override
  public String toString ()
  {
    return Strings.format ("{} | Reason: {}", super.toString (), reason);
  }

  @RequiredForNetworkSerialization
  protected AbstractPlayerDeniedEvent ()
  {
    reason = null;
  }
}
