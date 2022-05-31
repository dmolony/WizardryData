package com.bytezone.wizardry.origin;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

// -----------------------------------------------------------------------------------//
public class WizardryData4 extends WizardryData
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  public WizardryData4 (WizardryDisk disk) throws FileNotFoundException, DiskFormatException
  // ---------------------------------------------------------------------------------//
  {
    super (disk);

    byte[] buffer = disk.getFileData ("SCENARIO.DATA");
    header = new Header (buffer);

    // create message lines
    messages = new MessagesV2 (disk.messageBlock);
    MessagesV2 messagesV2 = ((MessagesV2) messages);

    // add spell names
    String[] spellNames = new String[51];
    for (int i = 0; i < spellNames.length; i++)
    {
      String line = messagesV2.getMessageLine (i + 5000);
      spellNames[i] = line.startsWith ("*") ? line.substring (1) : line;
    }
    header.addSpellNames (spellNames);

    // add maze levels
    ScenarioData sd = header.get (MAZE_AREA);
    mazeLevels = new ArrayList<> (sd.totalUnits);

    int id = 0;
    for (DataBlock dataBlock : sd.dataBlocks)
    {
      MazeLevel mazeLevel = new MazeLevel (this, ++id, dataBlock);
      mazeLevels.add (mazeLevel);

      for (Special special : mazeLevel.getSpecials ())
        if (special.isMessage ())
          getMessage (special.aux[1]).addLocations (special.locations);
    }

    // add item names
    items = new TreeMap<> ();

    List<String> itemNames = new ArrayList<> ();
    for (int i = 0; i < 120; i++)
    {
      String itemNameGeneric = messagesV2.getMessageLine (i * 2 + 14000);
      String itemName = messagesV2.getMessageLine (i * 2 + 14000 + 1);

      if (itemName != null)
        items.put (i, new Item (i, itemName, itemNameGeneric));
      //      System.out.printf ("%3d  %s%n", i, itemName);
      itemNames.add (itemName);
    }

    // add characters
    sd = header.get (CHARACTER_AREA);
    characters = new ArrayList<> ();
    int ptr = sd.firstBlock * 512;

    for (int i = 0; i < 500; i++)
    {
      byte[] out = disk.decode (buffer, ptr, sd.totalBlocks);
      int len = out[0] & 0xFF;
      if (len > out.length)
        System.out.printf ("Decoded array too short: (#%3d)  %3d > %3d%n", i, len, out.length);

      Character c = new Character (i, out);
      characters.add (c);

      ptr += sd.totalBlocks;
    }

    sd = header.get (MONSTER_AREA);
    ptr = sd.firstBlock * 512;

    // add monster names
    sd = header.get (MONSTER_AREA);
    monsters = new ArrayList<> (sd.totalUnits);
    String[] monsterNames = new String[4];

    for (int i = 0; i < sd.totalUnits; i++)
    {
      byte[] out = disk.decode (buffer, ptr, sd.totalBlocks);

      for (int j = 0; j < 4; j++)
        monsterNames[j] = messagesV2.getMessageLine (i * 4 + 13000 + j);

      monsters.add (new Monster (i, monsterNames, out));

      System.out.printf ("%3d  %s%n", i, monsters.get (i).name);

      ptr += sd.totalBlocks;
    }

    rewards = new ArrayList<> ();

    buffer = disk.getFileData ("200.MONSTERS");
    images = new ArrayList<> ();

    buffer = disk.getFileData ("WERDNA.DATA");
  }
}
