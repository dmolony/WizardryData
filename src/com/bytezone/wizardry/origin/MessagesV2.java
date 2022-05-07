package com.bytezone.wizardry.origin;

import java.util.List;

// -----------------------------------------------------------------------------------//
public class MessagesV2 extends Messages
// -----------------------------------------------------------------------------------//
{
  MessageBlock messageBlock;

  String[] spellNames = new String[51];

  // ---------------------------------------------------------------------------------//
  public MessagesV2 (MessageBlock messageBlock)
  // ---------------------------------------------------------------------------------//
  {
    this.messageBlock = messageBlock;

    for (int i = 15000; i < 28000; i += 50)
    {
      List<String> lines = messageBlock.getMessageLines (i);
      if (lines != null && lines.size () > 0)
      {
        Message message = new Message ((i - 15000) / 50);

        for (String line : lines)
          message.addLine (line);

        messages.put (message.getId (), message);
      }
    }

    for (int i = 0; i < spellNames.length; i++)
    {
      String line = messageBlock.getMessageLine (i + 5000);
      spellNames[i] = line.startsWith ("*") ? line.substring (1) : line;
    }
  }

  // ---------------------------------------------------------------------------------//
  public String getMessageLine (int messageNo)
  // ---------------------------------------------------------------------------------//
  {
    return messageBlock.getMessageLine (messageNo);
  }

  // ---------------------------------------------------------------------------------//
  public List<String> getMessageLines (int messageNo)
  // ---------------------------------------------------------------------------------//
  {
    return messageBlock.getMessageLines (messageNo);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public Message getMessage (int id)
  // ---------------------------------------------------------------------------------//
  {
    return messages.get (id);
  }
}
