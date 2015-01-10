package com.forerunnergames.peril.core.shared.net.messages;

import com.forerunnergames.tools.common.Author;
import com.forerunnergames.tools.common.Message;

import javax.annotation.Nullable;

public interface ChatMessage extends Message
{
  @Nullable
  public Author getAuthor ();

  public boolean hasAuthor ();
}
