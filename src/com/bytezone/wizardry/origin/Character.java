package com.bytezone.wizardry.origin;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.wizardry.origin.WizardryData.Alignment;
import com.bytezone.wizardry.origin.WizardryData.CharacterClass;
import com.bytezone.wizardry.origin.WizardryData.CharacterStatus;
import com.bytezone.wizardry.origin.WizardryData.Race;

// -----------------------------------------------------------------------------------//
public class Character
// -----------------------------------------------------------------------------------//
{
  private static int MAX_POSSESSIONS = 8;
  private static char[] awardsText = ">!$#&*<?BCPKODG@".toCharArray ();

  public int id;

  public final String name;
  public final String password;
  public final boolean inMaze;
  public final Race race;
  public final CharacterClass characterClass;
  public final int age;
  public final CharacterStatus status;
  public final Alignment alignment;
  public final int[] attributes = new int[6];      // 0:18
  public final int[] saveVs = new int[5];          // 0:31

  public final long gold;

  public final int possessionsCount;
  public final List<Possession> possessions = new ArrayList<> (MAX_POSSESSIONS);

  public final int nextCharacter;
  public final long experience;

  public final int maxlevac;                       // max level armour class?
  public final int charlev;                        // character level?
  public final int hpLeft;
  public final int hpMax;

  public final boolean mysteryBit;                 // first bit in spellsKnown
  public final boolean[] spellsKnown = new boolean[50];
  public final int[] mageSpells = new int[7];
  public final int[] priestSpells = new int[7];

  public final int hpCalCmd;
  public final int armourClass;
  public final int healPts;

  public final boolean crithitm;
  public final int swingCount;
  public final Dice hpdamrc;                        // +184

  boolean[][] wepvsty2 = new boolean[2][14];        // +190      4 bytes?
  boolean[][] wepvsty3 = new boolean[2][7];         //           2 bytes?
  boolean[] wepvstyp = new boolean[14];             //           2 bytes?

  LostXYL lostXYL;                                  // + 206      awards?

  public final String awards;

  int unknown1;
  int unknown2;
  int unknown3;
  int unknown4;
  int unknown5;

  // ---------------------------------------------------------------------------------//
  public Character (int id, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    this.id = id;

    name = Utility.getPascalString (buffer, 1);
    password = Utility.getPascalString (buffer, 17);        // slogan of some kind

    inMaze = Utility.getShort (buffer, 33) != 0;
    race = WizardryData.Race.values ()[Utility.getShort (buffer, 35)];
    characterClass = WizardryData.CharacterClass.values ()[Utility.getShort (buffer, 37)];
    age = 0;
    armourClass = Utility.getSignedShort (buffer, 39);

    status = WizardryData.CharacterStatus.values ()[Utility.getShort (buffer, 41)];
    alignment = WizardryData.Alignment.values ()[Utility.getShort (buffer, 43)];

    int attr1 = Utility.getShort (buffer, 45);
    int attr2 = Utility.getShort (buffer, 47);

    attributes[0] = attr1 & 0x001F;
    attributes[1] = (attr1 & 0x03E0) >>> 5;
    attributes[2] = attr1 & (0x7C00) >>> 10;
    attributes[3] = attr2 & 0x001F;
    attributes[4] = attr2 & (0x03E0) >>> 5;
    attributes[5] = attr2 & (0x7C00) >>> 10;

    gold = 0;

    unknown1 = Utility.getShort (buffer, 49);     // was luck/skill (4 bytes)
    unknown2 = Utility.getShort (buffer, 51);
    unknown3 = Utility.getShort (buffer, 53);     // was gold (6 bytes)
    unknown4 = Utility.getShort (buffer, 55);
    unknown5 = Utility.getShort (buffer, 57);

    possessionsCount = Utility.getShort (buffer, 59);

    for (int i = 0; i < possessionsCount; i++)
    {
      //      boolean equipped = Utility.getShort (buffer, 61 + i * 8) == 1;
      //      boolean cursed = Utility.getShort (buffer, 63 + i * 8) == 1;
      //      boolean identified = Utility.getShort (buffer, 65 + i * 8) == 1;
      int itemNo = Utility.getShort (buffer, 67 + i * 8);
      Possession p = new Possession (itemNo, false, false, true);
      //      Possession p = new Possession (itemNo, equipped, cursed, identified);
      possessions.add (p);
    }

    experience = 0;                                       // not used
    nextCharacter = Utility.getShort (buffer, 125);

    maxlevac = Utility.getShort (buffer, 131);
    charlev = Utility.getShort (buffer, 133);
    hpLeft = Utility.getShort (buffer, 135);
    hpMax = Utility.getShort (buffer, 137);

    mysteryBit = (buffer[139] & 0x01) == 1;
    int index = -1;                         // skip mystery bit
    for (int i = 139; i < 146; i++)
      for (int bit = 0; bit < 8; bit++)
      {
        if (((buffer[i] >>> bit) & 0x01) != 0)
          if (index >= 0)
            spellsKnown[index] = true;

        if (++index >= WizardryData.spells.length)
          break;
      }

    for (int i = 0; i < 7; i++)
    {
      mageSpells[i] = Utility.getShort (buffer, 147 + i * 2);
      priestSpells[i] = Utility.getShort (buffer, 161 + i * 2);
    }

    hpCalCmd = Utility.getSignedShort (buffer, 175);
    //    armourClass = Utility.getSignedShort (buffer, 177);   // see offset 39
    healPts = Utility.getShort (buffer, 179);

    crithitm = Utility.getShort (buffer, 181) == 1;
    swingCount = Utility.getShort (buffer, 183);
    hpdamrc = new Dice (buffer, 185);

    awards = "";        // buffer is too short
  }

  // ---------------------------------------------------------------------------------//
  public Character (int id, DataBlock dataBlock, int scenarioId) throws InvalidCharacterException
  // ---------------------------------------------------------------------------------//
  {
    this.id = id;

    byte[] buffer = dataBlock.buffer;
    int offset = dataBlock.offset;

    int nameLength = buffer[offset] & 0xFF;
    if (nameLength < 1 || nameLength > 15)
      throw new InvalidCharacterException ("Name too long");

    name = Utility.getPascalString (buffer, offset);
    if (name.isEmpty () || ("UNSET".equals (name) && buffer[offset + 40] == 0x07))   // 7 = LOST
      throw new InvalidCharacterException ("Character is UNSET");

    password = Utility.getPascalString (buffer, offset + 16);
    inMaze = Utility.getShort (buffer, offset + 32) != 0;
    race = WizardryData.Race.values ()[Utility.getShort (buffer, offset + 34)];
    characterClass = WizardryData.CharacterClass.values ()[Utility.getShort (buffer, offset + 36)];
    age = Utility.getShort (buffer, offset + 38);

    status = WizardryData.CharacterStatus.values ()[Utility.getShort (buffer, offset + 40)];
    alignment = WizardryData.Alignment.values ()[Utility.getShort (buffer, offset + 42)];

    // basic attributes
    int attr1 = Utility.getShort (buffer, offset + 44);
    int attr2 = Utility.getShort (buffer, offset + 46);

    attributes[0] = attr1 & 0x001F;
    attributes[1] = (attr1 & 0x03E0) >>> 5;
    attributes[2] = attr1 & (0x7C00) >>> 10;
    attributes[3] = attr2 & 0x001F;
    attributes[4] = attr2 & (0x03E0) >>> 5;
    attributes[5] = attr2 & (0x7C00) >>> 10;

    // saving throws
    attr1 = Utility.getShort (buffer, offset + 48);
    attr2 = Utility.getShort (buffer, offset + 50);

    saveVs[0] = attr1 & 0x001F;
    saveVs[1] = (attr1 & 0x03E0) >>> 5;
    saveVs[2] = attr1 & (0x7C00) >>> 10;
    saveVs[3] = attr2 & 0x001F;
    saveVs[4] = attr2 & (0x03E0) >>> 5;

    gold = Utility.getWizLong (buffer, offset + 52);
    possessionsCount = Utility.getShort (buffer, offset + 58);      // 0-8

    for (int i = 0; i < possessionsCount; i++)
    {
      boolean equipped = Utility.getShort (buffer, offset + 60 + i * 8) == 1;
      boolean cursed = Utility.getShort (buffer, offset + 62 + i * 8) == 1;
      boolean identified = Utility.getShort (buffer, offset + 64 + i * 8) == 1;

      int itemId = Utility.getShort (buffer, offset + 66 + i * 8);
      if (scenarioId == 3 && itemId >= 1000)
        itemId -= 1000;             // why?

      possessions.add (new Possession (itemId, equipped, cursed, identified));
    }

    nextCharacter = -1;                                         // not used
    experience = Utility.getWizLong (buffer, offset + 124);

    maxlevac = Utility.getShort (buffer, offset + 130);
    charlev = Utility.getShort (buffer, offset + 132);
    hpLeft = Utility.getShort (buffer, offset + 134);
    hpMax = Utility.getShort (buffer, offset + 136);

    mysteryBit = (buffer[offset + 138] & 0x01) == 1;
    int index = -1;                         // skip mystery bit
    for (int i = 138; i < 145; i++)
      for (int bit = 0; bit < 8; bit++)
      {
        if (((buffer[offset + i] >>> bit) & 0x01) != 0)
          if (index >= 0)
            spellsKnown[index] = true;

        if (++index >= WizardryData.spells.length)
          break;
      }

    for (int i = 0; i < 7; i++)
    {
      mageSpells[i] = Utility.getShort (buffer, offset + 146 + i * 2);
      priestSpells[i] = Utility.getShort (buffer, offset + 160 + i * 2);
    }

    hpCalCmd = Utility.getSignedShort (buffer, offset + 174);
    armourClass = Utility.getSignedShort (buffer, offset + 176);
    healPts = Utility.getShort (buffer, offset + 178);

    crithitm = Utility.getShort (buffer, offset + 180) == 1;
    swingCount = Utility.getShort (buffer, offset + 182);
    hpdamrc = new Dice (buffer, offset + 184);

    awards = getAwardString (buffer, offset + 206);
  }

  // ---------------------------------------------------------------------------------//
  private String getAwardString (byte[] buffer, int offset)
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    int awards = Utility.getShort (buffer, offset);

    for (int i = 0; i < 16; i++)
    {
      if ((awards & 0x01) != 0)
        text.append (awardsText[i]);
      awards >>>= 1;
    }

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  public String getRegenerationSign ()
  // ---------------------------------------------------------------------------------//
  {
    return healPts < 0 ? "-" : healPts == 0 ? " " : "+";
  }

  // ---------------------------------------------------------------------------------//
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    String type = String.format ("%1.1s-%3.3s", alignment, characterClass);
    String attr = String.format ("%2d %2d %2d %2d %2d %2d", attributes[0], attributes[1],
        attributes[2], attributes[3], attributes[4], attributes[5]);

    return String.format ("%3d  %-15s  %-15s  %s %4d %4d  %5d %5d %4d %4d %4d  %s", id, name,
        password, type, armourClass, hpMax, unknown1, unknown2, unknown3, unknown4, unknown5, attr);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return name;
  }

  //-----------------------------------------------------------------------------------//
  public record Possession (int id, boolean equipped, boolean cursed, boolean identified)
  //-----------------------------------------------------------------------------------//
  {
  }
}
