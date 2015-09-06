package com.forerunnergames.peril.common.net.events.interfaces;

import com.forerunnergames.tools.common.Event;
import com.forerunnergames.tools.common.Message;

public interface MessageEvent <T extends Message> extends Event
{
  T getMessage ();

  String getMessageText ();
}