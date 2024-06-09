package com.bytezone.wizardry.data;

import static com.bytezone.wizardry.data.Utility.getPascalString;
import static com.bytezone.wizardry.data.Utility.getShort;
import static com.bytezone.wizardry.data.Utility.getSignedShort;
import static com.bytezone.wizardry.data.Utility.getWizLong;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.wizardry.data.WizardryData.Alignment;
import com.bytezone.wizardry.data.WizardryData.CharacterClass;
import com.bytezone.wizardry.data.WizardryData.CharacterStatus;
import com.bytezone.wizardry.data.WizardryData.Race;

// -----------------------------------------------------------------------------------//
public class Character
// -----------------------------------------------------------------------------------//
{
  private static int MAX_POSSESSIONS = 8;
  public static int MAGE_SPELLS = 0;
  public static int PRIEST_SPELLS = 1;
  private static char[] awardsText = ">!$#&*<?BCPKODG@".toCharArray ();

  public int id;

  public final String name;
  public final String password;
  public final int inMaze;
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

  //  public final int nextCharacter;
  public final long experience;

  public final int maxlevac;                       // max level armour class?
  public final int charlev;                        // character level?
  public final int hpLeft;
  public final int hpMax;

  public boolean mysteryBit;                      // first bit in spellsKnown
  public final boolean[] spellsKnown = new boolean[50];
  public final int[][] spellAllowance = new int[2][7];

  public final int hpCalCmd;
  public final int armourClass;
  public final int healPts;

  public final boolean crithitm;
  public final int swingCount;
  public final Dice hpdamrc;                        // +184

  boolean[][] wepvsty2 = new boolean[2][14];        // +190      4 bytes
  boolean[][] wepvsty3 = new boolean[2][7];         // +194      4 bytes
  boolean[] wepvstyp = new boolean[14];             // +198      2 bytes

  public final int[] wep2 = new int[2];
  public final int[] wep3 = new int[2];
  public final int wep1;

  public final String wepVs2;
  public final String wepVs3;
  public final String wepVs1;

  public Location lostXYL;                          // +200      location

  public final String awards;                       // +206

  //  public int unknown1;    // 35,939 if in party  0x8C63  - or 3,171
  //  public int unknown2;    // 43,107 if in party  0xA863  - or    99
  //  public int unknown3;    // if == id then character belongs to a party (or == 0?)
  //  public int unknown4;
  //  public int unknown5;

  public int nextCharacterId;    // if == id then character has a party name only
  CharacterParty party;
  String partialSlogan;

  // ---------------------------------------------------------------------------------//
  public Character (int id, DataBlock dataBlock, int scenarioId)
      throws InvalidCharacterException
  // ---------------------------------------------------------------------------------//
  {
    this.id = id;

    byte[] buffer = dataBlock.buffer;
    int offset = dataBlock.offset;

    int nameLength = buffer[offset] & 0xFF;
    if (nameLength < 1 || nameLength > 15)
      throw new InvalidCharacterException ("Name too long");

    name = getPascalString (buffer, offset);

    password = getPascalString (buffer, offset + 16);
    inMaze = getShort (buffer, offset + 32);
    race = WizardryData.Race.values ()[getShort (buffer, offset + 34)];
    characterClass =
        WizardryData.CharacterClass.values ()[getShort (buffer, offset + 36)];
    age = getShort (buffer, offset + 38);

    status = CharacterStatus.values ()[getShort (buffer, offset + 40)];
    alignment = WizardryData.Alignment.values ()[getShort (buffer, offset + 42)];

    if (name.isEmpty () || ("UNSET".equals (name) && status == CharacterStatus.LOST))
      throw new InvalidCharacterException ("Character is UNSET");

    // basic attributes
    int attr1 = getShort (buffer, offset + 44);
    int attr2 = getShort (buffer, offset + 46);

    attributes[0] = attr1 & 0x001F;
    attributes[1] = (attr1 & 0x03E0) >>> 5;
    attributes[2] = (attr1 & 0x7C00) >>> 10;
    attributes[3] = attr2 & 0x001F;
    attributes[4] = (attr2 & 0x03E0) >>> 5;
    attributes[5] = (attr2 & 0x7C00) >>> 10;

    // saving throws
    attr1 = getShort (buffer, offset + 48);
    attr2 = getShort (buffer, offset + 50);

    saveVs[0] = attr1 & 0x001F;
    saveVs[1] = (attr1 & 0x03E0) >>> 5;
    saveVs[2] = (attr1 & 0x7C00) >>> 10;
    saveVs[3] = attr2 & 0x001F;
    saveVs[4] = (attr2 & 0x03E0) >>> 5;

    gold = getWizLong (buffer, offset + 52);
    possessionsCount = getShort (buffer, offset + 58);      // 0-8

    for (int i = 0; i < possessionsCount; i++)
    {
      boolean equipped = getShort (buffer, offset + 60 + i * 8) != 0;
      boolean cursed = getShort (buffer, offset + 62 + i * 8) != 0;
      boolean identified = getShort (buffer, offset + 64 + i * 8) != 0;
      int itemId = getShort (buffer, offset + 66 + i * 8);

      if (scenarioId == 3 && itemId >= 1000)
        itemId -= 1000;             // why?

      possessions.add (new Possession (itemId, equipped, cursed, identified));
    }

    nextCharacterId = -1;                                         // not used
    experience = getWizLong (buffer, offset + 124);

    maxlevac = getShort (buffer, offset + 130);
    charlev = getShort (buffer, offset + 132);
    hpLeft = getShort (buffer, offset + 134);
    hpMax = getShort (buffer, offset + 136);

    checkKnownSpells (buffer, offset + 138);

    hpCalCmd = getSignedShort (buffer, offset + 174);
    armourClass = getSignedShort (buffer, offset + 176);
    healPts = getShort (buffer, offset + 178);

    crithitm = getShort (buffer, offset + 180) != 0;
    swingCount = getShort (buffer, offset + 182);
    hpdamrc = new Dice (buffer, offset + 184);

    // 190 - 193 = wepvsty2 PACKED ARRAY[ 0..1, 0..13] OF BOOLEAN
    // 194 - 197 = wepvsty3 PACKED ARRAY[ 0..1, 0..6] OF BOOLEAN
    // 197 - 199 = wepvstyp PACKED ARRAY[ 0..13] OF BOOLEAN

    wep2[0] = getShort (buffer, offset + 190);
    wep2[1] = getShort (buffer, offset + 192);
    wep3[0] = getShort (buffer, offset + 194);
    wep3[1] = getShort (buffer, offset + 196);
    wep1 = getShort (buffer, offset + 198);

    wepVs2 = String.format ("%04X %04X", wep2[0], wep2[1]);       // protection
    wepVs3 = String.format ("%04X %04X", wep3[0], wep3[1]);       // resistance
    wepVs1 = String.format ("%04X", wep1);                        // purposed

    //    System.out.printf ("%-15s %s %s %s%n", name, wepVs1, wepVs2, wepVs3);

    lostXYL = new Location (buffer, offset + 200);
    awards = getAwardString (buffer, offset + 206);
  }

  // ---------------------------------------------------------------------------------//
  public Character (int id, byte[] buffer)          // Wizardry IV
  // ---------------------------------------------------------------------------------//
  {
    this.id = id;

    name = getPascalString (buffer, 1);
    password = "";
    partialSlogan = buffer[17] == 0 ? "" : HexFormatter.getPascalString (buffer, 17);

    inMaze = getShort (buffer, 33);
    race = WizardryData.Race.values ()[getShort (buffer, 35)];
    characterClass = WizardryData.CharacterClass.values ()[getShort (buffer, 37)];
    age = 0;
    armourClass = getSignedShort (buffer, 39);

    status = WizardryData.CharacterStatus.values ()[getShort (buffer, 41)];
    alignment = WizardryData.Alignment.values ()[getShort (buffer, 43)];

    int attr1 = getShort (buffer, 45);
    int attr2 = getShort (buffer, 47);

    attributes[0] = attr1 & 0x001F;
    attributes[1] = (attr1 & 0x03E0) >>> 5;
    attributes[2] = (attr1 & 0x7C00) >>> 10;
    attributes[3] = attr2 & 0x001F;
    attributes[4] = (attr2 & 0x03E0) >>> 5;
    attributes[5] = (attr2 & 0x7C00) >>> 10;

    gold = 0;

    //    unknown1 = getShort (buffer, 49);     // was saveVs (4 bytes)
    //    unknown2 = getShort (buffer, 51);
    //    unknown3 = getShort (buffer, 53);     // was gold (6 bytes)
    //    unknown4 = getShort (buffer, 55);
    //    unknown5 = getShort (buffer, 57);

    // this is certainly wrong
    wep2[0] = getShort (buffer, 49);
    wep2[1] = getShort (buffer, 51);
    wep3[0] = getShort (buffer, 53);
    wep3[1] = getShort (buffer, 55);
    wep1 = getShort (buffer, 57);

    wepVs2 = String.format ("%04X %04X", wep2[0], wep2[1]);       // protection
    wepVs3 = String.format ("%04X %04X", wep3[0], wep3[1]);       // resistance
    wepVs1 = String.format ("%04X", wep1);                        // purposed

    possessionsCount = getShort (buffer, 59);

    // what is in 61:66? could it be lostxyl?
    //    System.out.println (Utility.getHexString (buffer, 61, 6));

    for (int i = 0; i < possessionsCount; i++)
    {
      int itemNo = getShort (buffer, 67 + i * 8);
      Possession p = new Possession (itemNo, false, false, true);
      possessions.add (p);
    }

    experience = 0;                                 // not used
    nextCharacterId = getShort (buffer, 125);       // this is part of possessions[7]

    maxlevac = getShort (buffer, 131);
    charlev = getShort (buffer, 133);
    hpLeft = getShort (buffer, 135);
    hpMax = getShort (buffer, 137);

    checkKnownSpells (buffer, 139);

    hpCalCmd = getSignedShort (buffer, 175);
    //    armourClass = getSignedShort (buffer, 177);   // see offset 39
    healPts = getShort (buffer, 179);

    crithitm = getShort (buffer, 181) == 1;
    swingCount = getShort (buffer, 183);
    hpdamrc = new Dice (buffer, 185);

    awards = "";        // buffer is too short
  }

  // ---------------------------------------------------------------------------------//
  private void checkKnownSpells (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    for (int i = 0; i < 7; i++)
    {
      spellAllowance[MAGE_SPELLS][i] = getShort (buffer, ptr + 8 + i * 2);
      spellAllowance[PRIEST_SPELLS][i] = getShort (buffer, ptr + 22 + i * 2);
    }

    int val = buffer[ptr];
    int bit = 0;
    mysteryBit = ((val >>> bit++) & 0x01) == 1;           // knows all spells?

    for (int i = 0; i < WizardryData.spells.length; i++)
    {
      if (bit == 8)
      {
        val = buffer[++ptr];
        bit = 0;
      }
      spellsKnown[i] = ((val >>> bit++) & 0x01) == 1;
    }
  }

  // ---------------------------------------------------------------------------------//
  private String getAwardString (byte[] buffer, int offset)
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    int awards = getShort (buffer, offset);

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
  void setParty (CharacterParty party)
  // ---------------------------------------------------------------------------------//
  {
    this.party = party;
  }

  // ---------------------------------------------------------------------------------//
  boolean isInParty ()
  // ---------------------------------------------------------------------------------//
  {
    return party != null;
  }

  // ---------------------------------------------------------------------------------//
  String getPartialSlogan ()
  // ---------------------------------------------------------------------------------//
  {
    return partialSlogan;
  }

  // ---------------------------------------------------------------------------------//
  public CharacterParty getParty ()
  // ---------------------------------------------------------------------------------//
  {
    return party;
  }

  // ---------------------------------------------------------------------------------//
  public boolean isLost ()
  // ---------------------------------------------------------------------------------//
  {
    return lostXYL != null && lostXYL.getLevel () != 0;
  }

  // ---------------------------------------------------------------------------------//
  public String getAttributeString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    for (int i = 0; i < attributes.length; i++)
      text.append (String.format ("%02d/", attributes[i]));
    text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  public String getSpellsString (int which)
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    int total = 0;
    for (int i = 0; i < spellAllowance[which].length; i++)
      total += spellAllowance[which][i];

    if (total == 0)
      return "";

    for (int i = 0; i < spellAllowance[which].length; i++)
      text.append (String.format ("%d/", spellAllowance[which][i]));
    text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  public String getTypeString ()
  // ---------------------------------------------------------------------------------//
  {
    return String.format ("%1.1s-%3.3s", alignment, characterClass);
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
