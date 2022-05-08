package com.bytezone.wizardry.origin;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// -----------------------------------------------------------------------------------//
public class WizardryData
// -----------------------------------------------------------------------------------//
{
  private static final int CHARACTER_RECORD_LENGTH = 0x63;

  public static final String[] monsterClass = { "Fighter", "Mage", "Priest", "Thief", "Midget",
      "Giant", "Mythical", "Dragon", "Animal", "Were", "Undead", "Demon", "Insect", "Enchanted" };
  public static final String[] resistance =
      { "No elements", "Fire", "Cold", "Poison", "Level drain", "Stoning", "Magic" };
  public static final String[] characterClass =
      { "Fighter", "Mage", "Priest", "Thief", "Bishop", "Samurai", "Lord", "Ninja" };
  public static final String[] property =
      { "Stone", "Poison", "Paralyze", "Auto Kill", "Be slept", "Run", "Call for help" };
  public static String[] trapType = { "Trapless chest", "Poison needle", "Gas bomb", "Bolt",
      "Teleporter", "Anti-mage", "Anti-priest", "Alarm" };
  public static String[] trapType3 =
      { "Crossbow bolt", "Exploding box", "Splinters", "Blades", "Stunner" };

  public static final String[] spells = { "Halito", "Mogref", "Katino", "Dumapic", "Dilto", "Sopic",
      "Mahalito", "Molito", "Morlis", "Dalto", "Lahalito", "Mamorlis", "Makanito", "Madalto",
      "Lakanito", "Zilwan", "Masopic", "Haman", "Malor", "Mahaman", "Tiltowait",

      "Kalki", "Dios", "Badios", "Milwa", "Porfic", "Matu", "Calfo", "Manifo", "Montino", "Lomilwa",
      "Dialko", "Latumapic", "Bamatu", "Dial", "Badial", "Latumofis", "Maporfic", "Dialma",
      "Badialma", "Litokan", "Kandi", "Di", "Badi", "Lorto", "Madi", "Mabadi", "Loktofeit",
      "Malikto", "Kadorto" };

  static final int HEADER_AREA = 0;
  static final int MAZE_AREA = 1;
  static final int MONSTER_AREA = 2;
  static final int TREASURE_TABLE_AREA = 3;
  static final int ITEM_AREA = 4;
  static final int CHARACTER_AREA = 5;
  static final int IMAGE_AREA = 6;
  static final int EXPERIENCE_AREA = 7;

  private Messages messages;
  private Header header;
  private String fileName;

  private List<Monster> monsters;
  private Map<Integer, Item> items;
  private List<MazeLevel> mazeLevels;
  private List<Reward> rewards;
  private List<WizardryImage> images;
  private List<Character> characters;

  public enum Square
  {
    NORMAL, STAIRS, PIT, CHUTE, SPINNER, DARK, TRANSFER, OUCHY, BUTTONZ, ROCKWATE, FIZZLE, SCNMSG,
    ENCOUNTE
  }

  public enum Direction
  {
    NORTH, SOUTH, EAST, WEST
  }

  public enum ObjectType
  {
    WEAPON, ARMOR, SHIELD, HELMET, GAUNTLET, SPECIAL, MISC
  }

  public enum Race
  {
    NORACE, HUMAN, ELF, DWARF, GNOME, HOBBIT
  }

  public enum Alignment
  {
    UNALIGN, GOOD, NEUTRAL, EVIL
  }

  public enum CharacterStatus
  {
    OK, AFRAID, ASLEEP, PLYZE, STONED, DEAD, ASHES, LOST
  }

  public enum Attribute
  {
    STRENGTH, IQ, PIETY, VITALITY, AGILITY, LUCK
  }

  public enum CharacterClass
  {
    FIGHTER, MAGE, PRIEST, THIEF, BISHOP, SAMURAI, LORD, NINJA
  }

  public enum Status
  {
    OK, AFRAID, ASLEEP, PLYZE, STONED, DEAD, ASHES, LOST
  }

  // ---------------------------------------------------------------------------------//
  public WizardryData (String diskFileName) throws DiskFormatException, FileNotFoundException
  // ---------------------------------------------------------------------------------//
  {
    File file = new File (diskFileName);
    if (!file.exists () || !file.isFile ())
    {
      System.out.println ("File does not exist: " + diskFileName);
      return;
    }

    WizardryDisk disk = new WizardryDisk (diskFileName);
    if (disk == null)
      throw new DiskFormatException ("Not a Wizardry disk: " + diskFileName);

    this.fileName = diskFileName;
    byte[] buffer = disk.getFileData ("SCENARIO.DATA");
    header = new Header (buffer);

    switch (getScenarioId ())
    {
      case 1:
      case 2:
      case 3:
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
            if (special.square == Square.SCNMSG && special.aux[2] <= 13)
              getMessage (special.aux[1]).addLocations (special.locations); // force message creation
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

        break;

      case 4:
      case 5:
        // create message lines (must happen before maze levels are added)
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
        sd = header.get (MAZE_AREA);
        mazeLevels = new ArrayList<> (sd.totalUnits);

        id = 0;
        for (DataBlock dataBlock : sd.dataBlocks)
        {
          MazeLevel mazeLevel = new MazeLevel (this, ++id, dataBlock);
          mazeLevels.add (mazeLevel);

          for (Special special : mazeLevel.getSpecials ())
            if (special.square == Square.SCNMSG && special.aux[2] <= 13)
              getMessage (special.aux[1]).addLocations (special.locations);
        }

        // add monster names
        sd = header.get (MONSTER_AREA);
        monsters = new ArrayList<> (sd.totalUnits);
        String[] monsterNames = new String[4];
        for (int i = 0; i < 120; i++)
        {
          for (int j = 0; j < 4; j++)
            monsterNames[j] = messagesV2.getMessageLine (i * 4 + 13000 + j);

          if (monsterNames[0] != null)
            monsters.add (new Monster (i, monsterNames));
        }

        // add item names
        items = new TreeMap<> ();

        for (int i = 0; i < 120; i++)
        {
          String itemNameGeneric = messagesV2.getMessageLine (i * 2 + 14000);
          String itemName = messagesV2.getMessageLine (i * 2 + 14000 + 1);

          if (itemName != null)
            items.put (i, new Item (i, itemName, itemNameGeneric));
        }

        // add characters
        characters = new ArrayList<> ();
        int ptr = 1024;

        for (int i = 0; i < 500; i++)
        {
          byte[] out = disk.decode (buffer, ptr, CHARACTER_RECORD_LENGTH);
          int len = out[0] & 0xFF;

          if (out[1] > 0)       // name length
          {
            Character c = new Character (i, out);
            characters.add (c);
            //            System.out.println (c.name);
            //            System.out.println (HexFormatter.formatNoHeader (out, 33, len - 33));
          }
          ptr += CHARACTER_RECORD_LENGTH;
        }

        rewards = new ArrayList<> ();

        buffer = disk.getFileData ("200.MONSTERS");
        images = new ArrayList<> ();

        buffer = disk.getFileData ("WERDNA.DATA");
        break;

      default:
        System.out.println ("Unknown scenario id");
    }
  }

  // ---------------------------------------------------------------------------------//
  public String getFileName ()
  // ---------------------------------------------------------------------------------//
  {
    return fileName;
  }

  // ---------------------------------------------------------------------------------//
  public Special getSpecial (Location location)
  // ---------------------------------------------------------------------------------//
  {
    return getCell (location).getSpecial ();
  }

  // ---------------------------------------------------------------------------------//
  MazeCell getCell (Location location)
  // ---------------------------------------------------------------------------------//
  {
    MazeLevel mazeLevel = mazeLevels.get (location.getLevel () - 1);
    return mazeLevel.getMazeCell (location);
  }

  // ---------------------------------------------------------------------------------//
  private void histogram (int level)
  // ---------------------------------------------------------------------------------//
  {
    System.out.println ("+--------------------------------------------+");
    System.out.printf ("|                  Level %2d                  |%n", level + 1);
    System.out.println ("+--------------------------------------------+");

    int[] totals = new int[monsters.size ()];
    MazeLevel mazeLevel = mazeLevels.get (level);

    int tests = 1_000_000;
    for (int i = 0; i < tests; i++)
      totals[mazeLevel.getRandomMonster ()]++;

    float percentTotal = 0;
    int grandTotal = 0;

    for (int i = 0; i < totals.length; i++)
      if (totals[i] > 0)
      {
        float percent = totals[i] * 100 / (float) tests;
        System.out.printf ("%3d  %-20s %6.2f%%   %,9d%n", i, monsters.get (i).name, percent,
            totals[i]);
        percentTotal += percent;
        grandTotal += totals[i];
      }

    System.out.println ("                           ------   ---------");
    System.out.printf ("                           %6.2f %,11d%n%n", percentTotal, grandTotal);
  }

  // ---------------------------------------------------------------------------------//
  public int getScenarioId ()
  // ---------------------------------------------------------------------------------//
  {
    return header.scenarioId;
  }

  // ---------------------------------------------------------------------------------//
  public String getScenarioName ()
  // ---------------------------------------------------------------------------------//
  {
    return header.scenarioName;
  }

  // ---------------------------------------------------------------------------------//
  public Header getHeader ()
  // ---------------------------------------------------------------------------------//
  {
    return header;
  }

  // ---------------------------------------------------------------------------------//
  public List<MazeLevel> getMazeLevels ()
  // ---------------------------------------------------------------------------------//
  {
    return mazeLevels;
  }

  // ---------------------------------------------------------------------------------//
  public List<Character> getCharacters ()
  // ---------------------------------------------------------------------------------//
  {
    return characters;
  }

  // ---------------------------------------------------------------------------------//
  public Character getCharacter (int id)
  // ---------------------------------------------------------------------------------//
  {
    return characters.get (id);
  }

  // ---------------------------------------------------------------------------------//
  public List<Monster> getMonsters ()
  // ---------------------------------------------------------------------------------//
  {
    return monsters;
  }

  // ---------------------------------------------------------------------------------//
  public Monster getMonster (int id)
  // ---------------------------------------------------------------------------------//
  {
    if (id < 0 || id >= monsters.size ())
    {
      System.out.println ("Monster out of range: " + id);
      return null;
    }

    return monsters.get (id);
  }

  // ---------------------------------------------------------------------------------//
  public Map<Integer, Item> getItems ()
  // ---------------------------------------------------------------------------------//
  {
    return items;
  }

  // ---------------------------------------------------------------------------------//
  public Item getItem (int id)
  // ---------------------------------------------------------------------------------//
  {
    if (id >= 0 && id < items.size ())
      return items.get (id);

    System.out.printf ("Item %d out of range%n", id);
    return null;
  }

  // ---------------------------------------------------------------------------------//
  public String getItemName (int id)
  // ---------------------------------------------------------------------------------//
  {
    if (id >= 0 && id < items.size ())
      return items.get (id).name;

    return "** No such item **";
  }

  // ---------------------------------------------------------------------------------//
  public Trade getItemTrade (int itemNo)
  // ---------------------------------------------------------------------------------//
  {
    itemNo *= -1;
    itemNo -= 20000;

    return new Trade (itemNo / 100, itemNo % 100);
  }

  // ---------------------------------------------------------------------------------//
  public List<Reward> getRewards ()
  // ---------------------------------------------------------------------------------//
  {
    return rewards;
  }

  // ---------------------------------------------------------------------------------//
  public List<WizardryFont> getFonts ()
  // ---------------------------------------------------------------------------------//
  {
    return header.fonts;
  }

  // ---------------------------------------------------------------------------------//
  public List<WizardryImage> getImages ()
  // ---------------------------------------------------------------------------------//
  {
    return images;
  }

  // ---------------------------------------------------------------------------------//
  public WizardryImage getImage (int id)
  // ---------------------------------------------------------------------------------//
  {
    if (id >= 0 && id < images.size ())
      return images.get (id);

    System.out.printf ("Image %d out of range%n", id);
    return null;
  }

  // ---------------------------------------------------------------------------------//
  public String getMessageText (int id)
  // ---------------------------------------------------------------------------------//
  {
    if (messages.getMessage (id) == null)
    {
      System.out.println ("Message out of range: " + id);
      return null;
    }

    return messages.getMessage (id).getText ();
  }

  // ---------------------------------------------------------------------------------//
  public Message getMessage (int id)
  // ---------------------------------------------------------------------------------//
  {
    return messages.getMessage (id);
  }

  // ---------------------------------------------------------------------------------//
  public Messages getMessages ()
  // ---------------------------------------------------------------------------------//
  {
    return messages;
  }

  // ---------------------------------------------------------------------------------//
  public record Trade (int item1, int item2)
  // ---------------------------------------------------------------------------------//
  {
  }
}
