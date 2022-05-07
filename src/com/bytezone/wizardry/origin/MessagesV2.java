package com.bytezone.wizardry.origin;

import java.util.List;

// -----------------------------------------------------------------------------------//
public class MessagesV2 extends Messages
// -----------------------------------------------------------------------------------//
{
  MessageBlock messageBlock;

  String[] spellNames = new String[51];

  String[][] monsterNames = new String[120][4];
  String[][] itemNames = new String[120][2];

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

    for (int i = 0; i < monsterNames.length; i++)
    {
      monsterNames[i][2] = messageBlock.getMessageLine (i * 4 + 13000);
      monsterNames[i][3] = messageBlock.getMessageLine (i * 4 + 13000 + 1);
      monsterNames[i][0] = messageBlock.getMessageLine (i * 4 + 13000 + 2);
      monsterNames[i][1] = messageBlock.getMessageLine (i * 4 + 13000 + 3);
    }

    for (int i = 0; i < itemNames.length; i++)
    {
      itemNames[i][1] = messageBlock.getMessageLine (i * 2 + 14000);
      itemNames[i][0] = messageBlock.getMessageLine (i * 2 + 14000 + 1);
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public Message getMessage (int id)
  // ---------------------------------------------------------------------------------//
  {
    return messages.get (id);
  }
}
