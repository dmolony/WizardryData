package com.bytezone.wizardry.data;

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
    ScenarioData scenarioData = header.get (MAZE_AREA);
    mazeLevels = new ArrayList<> (scenarioData.totalUnits);

    int id = 0;
    for (DataBlock dataBlock : scenarioData.dataBlocks)
    {
      MazeLevel mazeLevel = new MazeLevel (this, ++id, dataBlock);
      mazeLevels.add (mazeLevel);

      for (Special special : mazeLevel.getSpecials ())
      {
        if (special.isMessage ())
          getMessage (special.aux[1]).addLocations (special.locations);

        // add teleport targets only if the teleport is actually used
        if (special.is (Square.TRANSFER) && special.locations.size () > 0)
          addTeleportTargetLocation (new Location (special.getAux ()));
      }
    }

    // add characters
    scenarioData = header.get (CHARACTER_AREA);
    characters = new ArrayList<> (scenarioData.totalUnits);

    id = 0;
    for (DataBlock dataBlock : scenarioData.dataBlocks)
      try
      {
        Character character = new Character (id++, dataBlock, getScenarioId ());
        characters.add (character);
        if (character.isLost ())
          addLostCharacterLocation (character.lostXYL);
      }
      catch (InvalidCharacterException e)
      {
        continue;
      }

    // add monsters
    scenarioData = header.get (MONSTER_AREA);
    monsters = new ArrayList<> (scenarioData.totalUnits);

    id = 0;
    for (DataBlock dataBlock : scenarioData.dataBlocks)
      monsters.add (new Monster (id++, dataBlock));

    // add items
    scenarioData = header.get (ITEM_AREA);
    items = new TreeMap<> ();

    id = 0;
    for (DataBlock dataBlock : scenarioData.dataBlocks)
      items.put (id, new Item (id++, dataBlock));

    // add rewards
    scenarioData = header.get (TREASURE_TABLE_AREA);
    rewards = new ArrayList<> (scenarioData.totalUnits);

    id = 0;
    for (DataBlock dataBlock : scenarioData.dataBlocks)
      rewards.add (new Reward (id++, dataBlock, getScenarioId ()));

    // add images
    scenarioData = header.get (IMAGE_AREA);
    images = new ArrayList<> (scenarioData.totalUnits);

    id = 0;
    for (DataBlock dataBlock : scenarioData.dataBlocks)
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
