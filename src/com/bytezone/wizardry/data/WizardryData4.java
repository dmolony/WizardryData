package com.bytezone.wizardry.data;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

// -----------------------------------------------------------------------------------//
public class WizardryData4 extends WizardryData
// -----------------------------------------------------------------------------------//
{
  private List<CharacterParty> parties = new ArrayList<> ();

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

    // add items
    sd = header.get (ITEM_AREA);
    int ptr = sd.firstBlock * 512;

    items = new TreeMap<> ();
    String[] itemNames = new String[2];

    for (int i = 0; i < sd.totalUnits; i++)
    {
      byte[] out = disk.decode (buffer, ptr);

      for (int j = 0; j < itemNames.length; j++)
      {
        itemNames[j] = messagesV2.getMessageLine (i * 2 + 14000 + j);
        if (itemNames[j] == null)
          itemNames[j] = "Broken Item";
      }

      items.put (i, new Item (itemNames, out, i));

      ptr += sd.totalBlocks;      // uncompressed record length
    }

    // add characters
    sd = header.get (CHARACTER_AREA);
    characters = new ArrayList<> ();
    ptr = sd.firstBlock * 512;

    for (int i = 0; i < 500; i++)
    {
      byte[] out = disk.decode (buffer, ptr);

      Character c = new Character (i, out);
      characters.add (c);

      ptr += sd.totalBlocks;      // uncompressed record length
    }

    linkParties ();

    // add monsters
    sd = header.get (MONSTER_AREA);
    ptr = sd.firstBlock * 512;

    monsters = new ArrayList<> (sd.totalUnits);
    String[] monsterNames = new String[4];

    for (int i = 0; i < sd.totalUnits; i++)
    {
      byte[] out = disk.decode (buffer, ptr);

      for (int j = 0; j < 4; j++)
        monsterNames[j] = messagesV2.getMessageLine (i * 4 + 13000 + j);

      monsters.add (new Monster (i, monsterNames, out));

      ptr += sd.totalBlocks;      // uncompressed record length
    }

    rewards = new ArrayList<> ();

    buffer = disk.getFileData ("200.MONSTERS");
    images = new ArrayList<> ();

    buffer = disk.getFileData ("WERDNA.DATA");
  }

  // ---------------------------------------------------------------------------------//
  private void linkParties ()
  // ---------------------------------------------------------------------------------//
  {
    for (Character character : characters)
    {
      if (character.isInParty () || character.name.isEmpty ())
        continue;

      CharacterParty party = new CharacterParty ();
      parties.add (party);
      link (character, party);
    }
  }

  // ---------------------------------------------------------------------------------//
  private void link (Character character, CharacterParty party)
  // ---------------------------------------------------------------------------------//
  {
    if (character.isInParty ())
      return;

    party.add (character);

    if (character.nextCharacterId > 0)
      link (characters.get (character.nextCharacterId), party);
  }
}
