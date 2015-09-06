package com.forerunnergames.peril.common.net.events.client.request;

import com.forerunnergames.peril.common.net.events.defaults.DefaultCommandMessageEvent;
import com.forerunnergames.peril.common.net.events.interfaces.CommandMessageEvent;
import com.forerunnergames.peril.common.net.messages.CommandMessage;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;
import com.forerunnergames.tools.net.events.remote.origin.client.ClientRequestEvent;

public final class CommandMessageRequestEvent implements CommandMessageEvent, ClientRequestEvent
{
  private final CommandMessageEvent event;

  public CommandMessageRequestEvent (final CommandMessage message)
  {
    Arguments.checkIsNotNull (message, "message");

    event = new DefaultCommandMessageEvent (message);
  }

  @Override
  public CommandMessage getMessage ()
  {
    return event.getMessage ();
  }

  @Override
  public String getMessageText ()
  {
    return event.getMessageText ();
  }

  @Override
  public String toString ()
  {
    return String.format ("%1$s: %2$s", getClass ().getSimpleName (), event);
  }

  @RequiredForNetworkSerialization
  private CommandMessageRequestEvent ()
  {
    event = null;
  }
}