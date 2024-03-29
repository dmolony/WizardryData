package com.bytezone.wizardry.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Useful web sites:
// https://datadrivengamer.blogspot.com/2019/08/the-not-so-basic-mechanics-of-wizardry.html
// -----------------------------------------------------------------------------------//
public abstract class WizardryData
// -----------------------------------------------------------------------------------//
{
  public static final String[] monsterClass =
      { "Fighter", "Mage", "Priest", "Thief", "Midget", "Giant", "Mythical", "Dragon",
          "Animal", "Were", "Undead", "Demon", "Insect", "Enchanted" };
  public static final String[] resistance =
      { "No elements", "Fire", "Cold", "Poison", "Level drain", "Stoning", "Magic" };
  public static final String[] characterClass =
      { "Fighter", "Mage", "Priest", "Thief", "Bishop", "Samurai", "Lord", "Ninja" };
  public static final String[] property =
      { "Stone", "Poison", "Paralyze", "Auto Kill", "Be slept", "Run", "Call for help" };
  public static String[] trapType = { "Trapless chest", "Poison needle", "Gas bomb",
      "Bolt", "Teleporter", "Anti-mage", "Anti-priest", "Alarm" };
  public static String[] trapType3 =
      { "Crossbow bolt", "Exploding box", "Splinters", "Blades", "Stunner" };

  public static final String[] spells =
      { "Halito", "Mogref", "Katino", "Dumapic", "Dilto", "Sopic", "Mahalito", "Molito",
          "Morlis", "Dalto", "Lahalito", "Mamorlis", "Makanito", "Madalto", "Lakanito",
          "Zilwan", "Masopic", "Haman", "Malor", "Mahaman", "Tiltowait",

          "Kalki", "Dios", "Badios", "Milwa", "Porfic", "Matu", "Calfo", "Manifo",
          "Montino", "Lomilwa", "Dialko", "Latumapic", "Bamatu", "Dial", "Badial",
          "Latumofis", "Maporfic", "Dialma", "Badialma", "Litokan", "Kandi", "Di", "Badi",
          "Lorto", "Madi", "Mabadi", "Loktofeit", "Malikto", "Kadorto" };

  static final int HEADER_AREA = 0;
  static final int MAZE_AREA = 1;
  static final int MONSTER_AREA = 2;
  static final int TREASURE_TABLE_AREA = 3;
  static final int ITEM_AREA = 4;
  static final int CHARACTER_AREA = 5;
  static final int IMAGE_AREA = 6;
  static final int EXPERIENCE_AREA = 7;

  protected Messages messages;
  protected Header header;
  private WizardryDisk disk;

  protected List<Monster> monsters;
  protected Map<Integer, Item> items;
  protected List<MazeLevel> mazeLevels;
  protected List<Reward> rewards;
  protected List<WizardryImage> images;
  protected List<Character> characters;
  protected List<Location> teleportLocations = new ArrayList<> ();
  protected List<Location> lostCharacterLocations = new ArrayList<> ();

  public enum Square
  {
    NORMAL, STAIRS, PIT, CHUTE, SPINNER, DARK, TRANSFER, OUCHY, BUTTONZ, ROCKWATE, FIZZLE,
    SCNMSG, ENCOUNTE
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
  public WizardryData (WizardryDisk disk) throws DiskFormatException
  // ---------------------------------------------------------------------------------//
  {
    this.disk = disk;
  }

  // ---------------------------------------------------------------------------------//
  public String getFileName ()
  // ---------------------------------------------------------------------------------//
  {
    return disk.getFileName ();
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
  public boolean isTeleportTarget (Location checkLocation)
  // ---------------------------------------------------------------------------------//
  {
    for (Location location : teleportLocations)
      if (location.equals (checkLocation))
        return true;

    return false;
  }

  // ---------------------------------------------------------------------------------//
  public boolean hasLostCharacter (Location checkLocation)
  // ---------------------------------------------------------------------------------//
  {
    for (Location location : lostCharacterLocations)
      if (location.equals (checkLocation))
        return true;

    return false;
  }

  // ---------------------------------------------------------------------------------//
  public List<Character> getLostCharacters (Location checkLocation)
  // ---------------------------------------------------------------------------------//
  {
    List<Character> lostCharacters = new ArrayList<> ();

    for (Character character : characters)
      if (character.isLost () && character.lostXYL.equals (checkLocation))
        lostCharacters.add (character);

    return lostCharacters;
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
    return items.get (id);
  }

  // ---------------------------------------------------------------------------------//
  public String getItemName (int id)
  // ---------------------------------------------------------------------------------//
  {
    Item item = items.get (id);
    if (item != null)
      return item.name;

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
  protected void addTeleportTargetLocation (Location teleportLocation)
  // ---------------------------------------------------------------------------------//
  {
    for (Location location : teleportLocations)
      if (location.equals (teleportLocation))
        return;

    teleportLocations.add (teleportLocation);
  }

  // ---------------------------------------------------------------------------------//
  protected void addLostCharacterLocation (Location lostLocation)
  // ---------------------------------------------------------------------------------//
  {
    for (Location location : lostCharacterLocations)
      if (location.equals (lostLocation))
        return;

    lostCharacterLocations.add (lostLocation);
  }

  // ---------------------------------------------------------------------------------//
  public record Trade (int item1, int item2)
  // ---------------------------------------------------------------------------------//
  {
  }
}
