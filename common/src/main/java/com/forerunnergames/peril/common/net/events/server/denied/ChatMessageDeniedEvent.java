package com.forerunnergames.peril.common.net.events.server.denied;

import com.forerunnergames.peril.common.net.events.defaults.DefaultChatMessageEvent;
import com.forerunnergames.peril.common.net.events.interfaces.ChatMessageEvent;
import com.forerunnergames.peril.common.net.events.server.abstracts.AbstractDeniedEvent;
import com.forerunnergames.peril.common.net.messages.ChatMessage;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Author;
import com.forerunnergames.tools.common.Strings;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;

import javax.annotation.Nullable;

public final class ChatMessageDeniedEvent extends AbstractDeniedEvent <String> implements ChatMessageEvent
{
  private final ChatMessageEvent chatMessageEvent;

  public ChatMessageDeniedEvent (final ChatMessage message, final String reason)
  {
    super (reason);

    Arguments.checkIsNotNull (message, "message");

    chatMessageEvent = new DefaultChatMessageEvent (message);
  }

  @Nullable
  @Override
  public Author getAuthor ()
  {
    return chatMessageEvent.getAuthor ();
  }

  @Override
  public boolean hasAuthor ()
  {
    return chatMessageEvent.hasAuthor ();
  }

  @Override
  public ChatMessage getMessage ()
  {
    return chatMessageEvent.getMessage ();
  }

  @Override
  public String getMessageText ()
  {
    return chatMessageEvent.getMessageText ();
  }

  @Override
  public String toString ()
  {
    return Strings.format ("{} | {}", chatMessageEvent, super.toString ());
  }

  @RequiredForNetworkSerialization
  private ChatMessageDeniedEvent ()
  {
    chatMessageEvent = null;
  }
}
