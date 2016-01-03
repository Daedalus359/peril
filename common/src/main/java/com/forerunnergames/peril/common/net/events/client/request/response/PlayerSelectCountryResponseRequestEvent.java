package com.forerunnergames.peril.common.net.events.client.request.response;

import com.forerunnergames.peril.common.net.events.abstracts.AbstractPlayerSelectCountryResponseEvent;
import com.forerunnergames.peril.common.net.events.server.request.PlayerSelectCountryRequestEvent;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;
import com.forerunnergames.tools.net.events.remote.origin.client.ResponseRequestEvent;
import com.forerunnergames.tools.net.events.remote.origin.server.ServerRequestEvent;

public final class PlayerSelectCountryResponseRequestEvent extends AbstractPlayerSelectCountryResponseEvent
        implements ResponseRequestEvent
{
  public PlayerSelectCountryResponseRequestEvent (final String selectedCountryName)
  {
    super (selectedCountryName);
  }

  @Override
  public Class <? extends ServerRequestEvent> getRequestType ()
  {
    return PlayerSelectCountryRequestEvent.class;
  }

  @RequiredForNetworkSerialization
  private PlayerSelectCountryResponseRequestEvent ()
  {
    super (null);
  }
}
