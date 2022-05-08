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

  public final String name;
  public final String password;
  public final boolean inMaze;
  public final Race race;
  public final CharacterClass characterClass;
  public final int age;
  public final CharacterStatus status;
  public final Alignment alignment;
  public final int[] attributes = new int[6];      // 0:18
  public final int[] luckSkill = new int[5];       // 0:31

  public final long gold;

  public final int possessionsCount;
  public final List<Possession> possessions = new ArrayList<> (MAX_POSSESSIONS);

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

  // ---------------------------------------------------------------------------------//
  public Character (int id, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    name = Utility.getPascalString (buffer, 1);
    password = Utility.getPascalString (buffer, 17);        // slogan of some kind

    inMaze = false;
    race = Race.NORACE;
    characterClass = CharacterClass.BISHOP;
    age = 0;
    status = CharacterStatus.OK;
    alignment = Alignment.UNALIGN;
    gold = 0;
    possessionsCount = Utility.getShort (buffer, 59);
    experience = 0;
    maxlevac = 0;
    charlev = 0;
    hpLeft = Utility.getShort (buffer, 135);
    hpMax = Utility.getShort (buffer, 137);
    mysteryBit = false;
    hpCalCmd = 0;
    armourClass = Utility.getSignedShort (buffer, 39);
    healPts = 0;
    crithitm = false;
    swingCount = 0;
    hpdamrc = new Dice (0, 0, 0);
    awards = "???";

    for (int i = 0; i < possessionsCount; i++)
    {
      int itemNo = Utility.getShort (buffer, 67 + i * 8);
      Possession p = new Possession (itemNo, true, false, true);
      possessions.add (p);
    }

    int idd = Utility.getShort (buffer, 53);
    //    System.out.println (idd);
  }

  // ---------------------------------------------------------------------------------//
  public Character (int id, DataBlock dataBlock, int scenarioId) throws InvalidCharacterException
  // ---------------------------------------------------------------------------------//
  {
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

    long attr = Utility.getLong (buffer, offset + 44);
    for (int i = 0; i < 6; i++)
    {
      attributes[i] = (int) (attr & 0x1F);
      attr >>>= i == 2 ? 6 : 5;
    }

    // luck/skill
    //    System.out.println (HexFormatter.formatNoHeader (buffer, offset + 48, 4));

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

    awards = getAwardString (buffer, offset);
  }

  // ---------------------------------------------------------------------------------//
  private String getAwardString (byte[] buffer, int offset)
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    int awards = Utility.getShort (buffer, offset + 206);

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
