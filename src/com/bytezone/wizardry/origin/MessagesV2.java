package com.bytezone.wizardry.origin;

import java.util.List;

// -----------------------------------------------------------------------------------//
public class MessagesV2 extends Messages
// -----------------------------------------------------------------------------------//
{
  MessageBlock messageBlock;

  String[] spellNames = new String[51];

  String[] monsterNamesGeneric = new String[120];
  String[] monsterNamesGenericPlural = new String[120];
  String[] monsterNames = new String[120];
  String[] monsterNamesPlural = new String[120];

  String[] itemNamesGeneric = new String[120];
  String[] itemNames = new String[120];

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
      monsterNamesGeneric[i] = messageBlock.getMessageLine (i * 4 + 13000);
      monsterNamesGenericPlural[i] = messageBlock.getMessageLine (i * 4 + 13000 + 1);
      monsterNames[i] = messageBlock.getMessageLine (i * 4 + 13000 + 2);
      monsterNamesPlural[i] = messageBlock.getMessageLine (i * 4 + 13000 + 3);

      System.out.printf ("%3d  %5d  %-15s %-15s %-15s %-15s%n", i, 13000 + i * 4,
          monsterNamesGeneric[i], monsterNamesGenericPlural[i], monsterNames[i],
          monsterNamesPlural[i]);
    }

    for (int i = 0; i < itemNames.length; i++)
    {
      itemNamesGeneric[i] = messageBlock.getMessageLine (i * 2 + 14000);
      itemNames[i] = messageBlock.getMessageLine (i * 2 + 14000 + 1);

      System.out.printf ("%3d  %5d  %-15s %s%n", i, 14000 + i * 2, itemNamesGeneric[i],
          itemNames[i]);
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
