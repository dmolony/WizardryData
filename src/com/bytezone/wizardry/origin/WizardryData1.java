package com.bytezone.wizardry.origin;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.TreeMap;

// -----------------------------------------------------------------------------------//
public class WizardryData1 extends WizardryData
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  public WizardryData1 (WizardryDisk disk) throws FileNotFoundException, DiskFormatException
  // ---------------------------------------------------------------------------------//
  {
    super (disk);

    byte[] buffer = disk.getFileData ("SCENARIO.DATA");
    header = new Header (buffer);

    // create message lines (must happen before maze levels are added)
    byte[] messageBuffer = disk.getFileData ("SCENARIO.MESGS");
    messages = new MessagesV1 (messageBuffer, getScenarioId ());

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

    // add characters
    sd = header.get (CHARACTER_AREA);
    characters = new ArrayList<> (sd.totalUnits);

    id = 0;
    for (DataBlock dataBlock : sd.dataBlocks)
      try
      {
        characters.add (new Character (id++, dataBlock, getScenarioId ()));
      }
      catch (InvalidCharacterException e)
      {
        continue;
      }

    // add monsters
    sd = header.get (MONSTER_AREA);
    monsters = new ArrayList<> (sd.totalUnits);

    id = 0;
    for (DataBlock dataBlock : sd.dataBlocks)
      monsters.add (new Monster (id++, dataBlock));

    // add items
    sd = header.get (ITEM_AREA);
    items = new TreeMap<> ();

    id = 0;
    for (DataBlock dataBlock : sd.dataBlocks)
      items.put (id, new Item (id++, dataBlock));

    // add rewards
    sd = header.get (TREASURE_TABLE_AREA);
    rewards = new ArrayList<> (sd.totalUnits);

    id = 0;
    for (DataBlock dataBlock : sd.dataBlocks)
      rewards.add (new Reward (id++, dataBlock));

    // add images
    sd = header.get (IMAGE_AREA);
    images = new ArrayList<> (sd.totalUnits);

    id = 0;
    for (DataBlock dataBlock : sd.dataBlocks)
      images.add (new WizardryImage (id++, dataBlock, getScenarioId ()));

    if (false)
    {
      int[] imageTotals = new int[images.size ()];
      for (Monster monster : monsters)
        imageTotals[monster.image]++;

      for (int i = 0; i < imageTotals.length; i++)
        System.out.printf ("%2d  %2d%n", i, imageTotals[i]);
    }

    if (false)
      for (int i = 0; i < 10; i++)
        histogram (i);

    if (false)
      for (int i = 0; i < mazeLevels.size (); i++)
        mazeLevels.get (i).showOdds ();

  }
}
