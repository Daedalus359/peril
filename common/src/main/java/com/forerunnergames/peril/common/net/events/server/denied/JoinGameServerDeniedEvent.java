package com.forerunnergames.peril.common.net.events.server.denied;

import com.forerunnergames.peril.common.net.events.server.abstracts.AbstractDeniedEvent;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Strings;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;
import com.forerunnergames.tools.net.client.ClientConfiguration;

public final class JoinGameServerDeniedEvent extends AbstractDeniedEvent <String>
{
  private final ClientConfiguration clientConfig;

  public JoinGameServerDeniedEvent (final ClientConfiguration clientConfig, final String reason)
  {
    super (reason);

    Arguments.checkIsNotNull (clientConfig, "clientConfig");

    this.clientConfig = clientConfig;
  }

  public ClientConfiguration getClientConfiguration ()
  {
    return clientConfig;
  }

  @Override
  public String toString ()
  {
    return Strings.format ("Client Configuration: {} | {}", clientConfig, super.toString ());
  }

  @RequiredForNetworkSerialization
  private JoinGameServerDeniedEvent ()
  {
    clientConfig = null;
  }
}
