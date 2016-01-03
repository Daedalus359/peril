package com.forerunnergames.peril.common.net.events.abstracts;

import com.forerunnergames.peril.common.net.events.interfaces.MessageEvent;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Message;
import com.forerunnergames.tools.common.Strings;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;

public abstract class AbstractMessageEvent <T extends Message> implements MessageEvent <T>
{
  private final T message;

  protected AbstractMessageEvent (final T message)
  {
    Arguments.checkIsNotNull (message, "message");

    this.message = message;
  }

  @RequiredForNetworkSerialization
  protected AbstractMessageEvent ()
  {
    message = null;
  }

  @Override
  public final T getMessage ()
  {
    return message;
  }

  @Override
  public final String getMessageText ()
  {
    return message.getText ();
  }

  @Override
  public String toString ()
  {
    return Strings.format ("{}: Message: {}", getClass ().getSimpleName (), message);
  }
}
