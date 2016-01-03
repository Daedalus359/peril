package com.forerunnergames.peril.common.net.events.server.interfaces;

import com.forerunnergames.peril.common.net.events.interfaces.PlayerEvent;
import com.forerunnergames.tools.net.events.remote.origin.server.ResponseDeniedEvent;

public interface PlayerResponseDeniedEvent <R> extends PlayerEvent, ResponseDeniedEvent <R>
{
}
