package com.bytezone.wizardry.data;

import static com.bytezone.wizardry.data.Utility.getPascalString;
import static com.bytezone.wizardry.data.Utility.getShort;
import static com.bytezone.wizardry.data.Utility.getSignedShort;
import static com.bytezone.wizardry.data.Utility.getWizLong;
import static com.bytezone.wizardry.data.Utility.trimComma;;

// -----------------------------------------------------------------------------------//
public class Monster
// -----------------------------------------------------------------------------------//
{
  public final int id;
  public final String name;                         //   0
  public final String namePlural;                   //  16
  public final String genericName;                  //  32
  public final String genericNamePlural;            //  48
  public final int image;                           //  64
  public final Dice groupSize;                      //  66  
  public final Dice hitPoints;                      //  72 
  public final int monsterClass;                    //  78
  public final int armourClass;                     //  80          
  public final int damageDiceSize;                  //  82  recsn
  public final Dice[] damageDice = new Dice[7];     //  84  
  public final long experiencePoints;               // 126  wizlong
  public final int drain;                           // 132                    
  public final int regen;                           // 134  hit points healed per turn
  public final int rewardWandering;                 // 136  reward index outside lair
  public final int rewardLair;                      // 138  reward index inside lair
  public final int partnerId;                       // 140  
  public final int partnerOdds;                     // 142  partner %
  public final int mageSpells;                      // 144  spell level?
  public final int priestSpells;                    // 146  spell level?
  public final int unique;                          // 148
  public final int breathe;                         // 150  index into breathValues
  public final int unaffect;                        // 152
  public final int resistance;                      // 154  wepvsty3
  public final int properties;                      // 156  sppc

  public final String damageDiceText;
  private final String[] breathValues =
      { "None", "Fire", "Frost", "Poison", "Level drain", "Stoning", "Magic" };

  // Scenario #1 values
  private static int[] experience = {                                     //
      55, 235, 415, 230, 380, 620, 840, 520, 550, 350,                    // 00-09
      475, 515, 920, 600, 735, 520, 795, 780, 990, 795,                   // 10-19
      1360, 1320, 1275, 680, 960, 600, 755, 1120, 2075, 870,              // 20-29
      960, 600, 1120, 2435, 1080, 2280, 975, 875, 1135, 1200,             // 30-39
      620, 740, 1460, 1245, 960, 1405, 1040, 1220, 1520, 1000,            // 40-49
      960, 2340, 2160, 2395, 790, 1140, 1235, 1790, 1720, 2240,           // 50-59
      1475, 1540, 1720, 1900, 1240, 1220, 1020, 20435, 5100, 3515,        // 60-69
      2115, 2920, 2060, 2140, 1400, 1640, 1280, 4450, 42840, 3300,        // 70-79
      40875, 5000, 3300, 2395, 1935, 1600, 3330, 44090, 40840, 5200,      // 80-89
      4155, 3000, 9200, 3160, 7460, 7320, 15880, 1600, 2200, 1000,        // 90-99
      1900                                                                // 100 
  };

  // ---------------------------------------------------------------------------------//
  public Monster (int id, String[] names, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    this.id = id;

    genericName = names[0];
    genericNamePlural = names[1];
    name = names[2];
    namePlural = names[3];

    image = -1;
    experiencePoints = 0;
    rewardWandering = 0;
    rewardLair = 0;
    partnerId = 0;
    partnerOdds = 0;
    unique = 0;

    groupSize = new Dice (buffer, 1);
    hitPoints = new Dice (buffer, 7);
    monsterClass = getShort (buffer, 13);
    armourClass = getSignedShort (buffer, 15);

    damageDiceSize = buffer[17];                               // number of dice
    String dd = "";
    for (int i = 0, ptr = 19; i < 7; i++, ptr += 6)
    {
      if (buffer[ptr] == 0)
        break;
      damageDice[i] = new Dice (buffer, ptr);
      dd += damageDice[i] + ", ";
    }
    if (dd.length () > 0)
      damageDiceText = dd.substring (0, dd.length () - 2);
    else
      damageDiceText = "";

    drain = getShort (buffer, 61);
    regen = getShort (buffer, 63);
    mageSpells = getShort (buffer, 65);
    priestSpells = getShort (buffer, 67);
    breathe = getShort (buffer, 69);
    unaffect = getShort (buffer, 71);

    resistance = getShort (buffer, 73);      // bit flags
    properties = getShort (buffer, 75);      // bit flags
  }

  // ---------------------------------------------------------------------------------//
  public Monster (int id, DataBlock dataBlock)
  // ---------------------------------------------------------------------------------//
  {
    this.id = id;

    byte[] buffer = dataBlock.buffer;
    int offset = dataBlock.offset;

    genericName = getPascalString (buffer, offset);
    genericNamePlural = getPascalString (buffer, offset + 16);
    name = getPascalString (buffer, offset + 32);
    namePlural = getPascalString (buffer, offset + 48);

    image = getShort (buffer, offset + 64);
    groupSize = new Dice (buffer, offset + 66);
    hitPoints = new Dice (buffer, offset + 72);

    monsterClass = getShort (buffer, offset + 78);
    armourClass = getSignedShort (buffer, offset + 80);

    damageDiceSize = getShort (buffer, offset + 82);       // 0-7
    StringBuilder dd = new StringBuilder ();
    for (int i = 0; i < damageDiceSize; i++)
    {
      damageDice[i] = new Dice (buffer, offset + 84 + i * 6);
      dd.append (damageDice[i].toString () + ", ");
    }

    trimComma (dd);
    damageDiceText = dd.toString ();

    long exp = getWizLong (buffer, offset + 126);

    drain = getShort (buffer, offset + 132);
    regen = getShort (buffer, offset + 134);

    rewardWandering = getShort (buffer, offset + 136);    // gold rewards index
    rewardLair = getShort (buffer, offset + 138);         // chest rewards index

    partnerId = getShort (buffer, offset + 140);
    partnerOdds = getShort (buffer, offset + 142);

    mageSpells = getShort (buffer, offset + 144);         // spell level
    priestSpells = getShort (buffer, offset + 146);       // spell level

    unique = getSignedShort (buffer, offset + 148);
    breathe = getShort (buffer, offset + 150);
    unaffect = getShort (buffer, offset + 152);

    resistance = getShort (buffer, offset + 154);         // flags
    properties = getShort (buffer, offset + 156);         // flags

    experiencePoints = exp == 0 ? getExperienceTotal () : exp;
  }

  // ---------------------------------------------------------------------------------//
  public String getBreatheEffect ()
  // ---------------------------------------------------------------------------------//
  {
    return breathValues[breathe];
  }

  // ---------------------------------------------------------------------------------//
  public int getGroupSize (MazeLevel mazeLevel)
  // ---------------------------------------------------------------------------------//
  {
    return mazeLevel.validateGroupSize (groupSize.roll ());
  }

  // ---------------------------------------------------------------------------------//
  private long getExperienceTotal ()
  // ---------------------------------------------------------------------------------//
  {
    int expHitPoints = hitPoints.faces * hitPoints.level * (breathe == 0 ? 20 : 40);
    int expAc = 40 * (11 - armourClass);
    int expMage = getBonus (35, mageSpells);
    int expPriest = getBonus (35, priestSpells);
    int expDrain = getBonus (200, drain);
    int expHeal = getBonus (90, regen);
    int expDamage = damageDiceSize <= 1 ? 0 : getBonus (30, damageDiceSize);
    int expUnaffect = unaffect == 0 ? 0 : getBonus (40, (unaffect / 10 + 1));

    int expFlags1 = getBonus (35, Integer.bitCount (resistance & 0x7E));      // 6 bits
    int expFlags2 = getBonus (40, Integer.bitCount (properties & 0x7F));      // 7 bits

    return expHitPoints + expAc + expMage + expPriest + expDrain + expHeal + expDamage
        + expUnaffect + expFlags1 + expFlags2;
  }

  // ---------------------------------------------------------------------------------//
  private int getBonus (int base, int multiplier)
  // ---------------------------------------------------------------------------------//
  {
    if (multiplier == 0)
      return 0;

    int total = base;
    while (multiplier > 1)
    {
      int part = total % 10000;   // get the last 4 digits

      multiplier--;
      total += total;             // double the value

      if (part >= 5000)           // mimics the wizardry bug
        total += 10000;           // yay, free points
    }

    return total;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()       // used by ComboBox
  // ---------------------------------------------------------------------------------//
  {
    return name;
  }
}

/*
 *  TENEMY = RECORD
        NAMEUNK  : STRING[ 15];
        NAMEUNKS : STRING[ 15];
        NAME     : STRING[ 15];
        NAMES    : STRING[ 15];
        PIC      : INTEGER;
        CALC1    : TWIZLONG;      // this should be THPREC
        HPREC    : THPREC;
        CLASS    : INTEGER;
        AC       : INTEGER;
        RECSN    : INTEGER;
        RECS     : ARRAY[ 1..7] OF THPREC;
        EXPAMT   : TWIZLONG;
        DRAINAMT : INTEGER;
        HEALPTS  : INTEGER;
        REWARD1  : INTEGER;
        REWARD2  : INTEGER;
        ENMYTEAM : INTEGER;
        TEAMPERC : INTEGER;
        MAGSPELS : INTEGER;
        PRISPELS : INTEGER;
        UNIQUE   : INTEGER;
        BREATHE  : INTEGER;
        UNAFFCT  : INTEGER;       // magic spells resistance %
        WEPVSTY3 : PACKED ARRAY[ 0..15] OF BOOLEAN;
        SPPC     : PACKED ARRAY[ 0..15] OF BOOLEAN;
      END;
 */
