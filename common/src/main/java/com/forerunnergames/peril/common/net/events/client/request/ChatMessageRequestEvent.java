package com.forerunnergames.peril.common.net.events.client.request;

import com.forerunnergames.peril.common.net.events.abstracts.AbstractMessageEvent;
import com.forerunnergames.peril.common.net.events.interfaces.ChatMessageEvent;
import com.forerunnergames.peril.common.net.messages.ChatMessage;
import com.forerunnergames.tools.common.Author;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;
import com.forerunnergames.tools.net.events.remote.origin.client.ClientRequestEvent;

import javax.annotation.Nullable;

public final class ChatMessageRequestEvent extends AbstractMessageEvent <ChatMessage> implements ChatMessageEvent,
        ClientRequestEvent
{
  public ChatMessageRequestEvent (final ChatMessage message)
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
  private ChatMessageRequestEvent ()
  {
  }
}
