package com.forerunnergames.peril.core.shared.event;

import com.forerunnergames.tools.common.id.Id;
import com.forerunnergames.tools.net.events.local.LocalEvent;

public interface InternalCommunicationEvent extends LocalEvent
{
  Id getEventId ();
}
