package com.forerunnergames.peril.common.net.events.server.abstracts;

import com.forerunnergames.tools.common.Strings;
import com.forerunnergames.tools.common.annotations.AllowNegative;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;
import com.forerunnergames.tools.net.events.remote.origin.server.ServerNotificationEvent;

public class AbstractArmiesChangedEvent implements ServerNotificationEvent
{
  private final int deltaArmyCount;

  /**
   * @param deltaArmyCount
   *          army change delta value; negative values are ALLOWED
   */
  public AbstractArmiesChangedEvent (@AllowNegative final int deltaArmyCount)
  {
    this.deltaArmyCount = deltaArmyCount;
  }

  public int getDeltaArmyCount ()
  {
    return deltaArmyCount;
  }

  @Override
  public String toString ()
  {
    return Strings.format ("{}: Army Count Delta: {}", getClass ().getSimpleName (), deltaArmyCount);
  }

  @RequiredForNetworkSerialization
  private AbstractArmiesChangedEvent ()
  {
    deltaArmyCount = 0;
  }
}
