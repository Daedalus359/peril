package com.forerunnergames.peril.common.net.events.client.request.response;

import com.forerunnergames.peril.common.net.events.server.request.PlayerAttackOrderRequestEvent;
import com.forerunnergames.tools.net.events.remote.origin.client.ResponseRequestEvent;
import com.forerunnergames.tools.net.events.remote.origin.server.ServerRequestEvent;

public final class PlayerRetreatOrderResponseRequestEvent implements ResponseRequestEvent
{
  @Override
  public Class <? extends ServerRequestEvent> getRequestType ()
  {
    return PlayerAttackOrderRequestEvent.class;
  }

  @Override
  public String toString ()
  {
    return this.getClass ().getSimpleName ();
  }
}
