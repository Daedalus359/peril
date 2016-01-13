package com.forerunnergames.peril.common.net.events.server.interfaces;

import com.forerunnergames.tools.net.events.remote.origin.server.ResponseDeniedEvent;

public interface PlayerResponseDeniedEvent <R> extends ResponseDeniedEvent <R>, PlayerDeniedEvent <R>
{
}
