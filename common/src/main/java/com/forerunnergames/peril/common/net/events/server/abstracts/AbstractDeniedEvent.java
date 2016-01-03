package com.forerunnergames.peril.common.net.events.server.abstracts;

import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Strings;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;
import com.forerunnergames.tools.net.events.remote.origin.server.DeniedEvent;

public abstract class AbstractDeniedEvent <T> implements DeniedEvent <T>
{
  private final T reason;

  protected AbstractDeniedEvent (final T reason)
  {
    Arguments.checkIsNotNull (reason, "reason");

    this.reason = reason;
  }

  @RequiredForNetworkSerialization
  protected AbstractDeniedEvent ()
  {
    reason = null;
  }

  @Override
  public final T getReason ()
  {
    return reason;
  }

  @Override
  public String toString ()
  {
    return Strings.format ("{}: Reason for denial: {}", getClass ().getSimpleName (), reason);
  }
}
