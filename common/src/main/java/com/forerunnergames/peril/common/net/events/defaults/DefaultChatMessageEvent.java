package com.forerunnergames.peril.common.net.events.defaults;

import com.forerunnergames.peril.common.net.events.abstracts.AbstractMessageEvent;
import com.forerunnergames.peril.common.net.events.interfaces.ChatMessageEvent;
import com.forerunnergames.peril.common.net.messages.ChatMessage;
import com.forerunnergames.tools.common.Author;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;

import javax.annotation.Nullable;

/**
 * This class exists for delegation use with ChatMessageEvent's that can't extend AbstractMessageEvent because they are
 * already extending another event class.
 */
public final class DefaultChatMessageEvent extends AbstractMessageEvent <ChatMessage> implements ChatMessageEvent
{
  public DefaultChatMessageEvent (final ChatMessage message)
  {
    super (message);
  }

  @Nullable
  @Override
  public Author getAuthor ()
  {
    return getMessage ().getAuthor ();
  }

  @Override
  public boolean hasAuthor ()
  {
    return getMessage ().hasAuthor ();
  }

  @RequiredForNetworkSerialization
  private DefaultChatMessageEvent ()
  {
  }
}
