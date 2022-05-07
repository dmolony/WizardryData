package com.bytezone.wizardry.origin;

// -----------------------------------------------------------------------------------//
public class Monster
// -----------------------------------------------------------------------------------//
{
  public final int id;
  public final String name;                                //   0
  public final String namePlural;                          //  16
  public final String genericName;                         //  32
  public final String genericNamePlural;                   //  48
  public final int image;                                  //  64
  public final Dice groupSize;                             //  66  
  public final Dice hitPoints;                             //  72 
  public final int monsterClass;                           //  78
  public final int armourClass;                            //  80          
  public final int damageDiceSize;                         //  82  recsn
  public final Dice[] damageDice = new Dice[7];            //  84  
  public final long experiencePoints;                      // 126  wizlong
  public final int drain;                                  // 132                    
  public final int regen;                                  // 134  hit points healed per turn
  public final int rewardWandering;                        // 136  reward index outside lair
  public final int rewardLair;                             // 138  reward index inside lair
  public final int partnerId;                              // 140  
  public final int partnerOdds;                            // 142  partner %
  public final int mageSpells;                             // 144  spell level?
  public final int priestSpells;                           // 146  spell level?
  public final int unique;                                 // 148
  public final int breathe;                                // 150  index into breathValues
  public final int unaffect;                               // 152
  public final int resistance;                             // 154
  public final int properties;                             // 156

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
  public Monster (int id, String[] names)
  // ---------------------------------------------------------------------------------//
  {
    this.id = id;

    genericName = names[0];
    genericNamePlural = names[1];
    name = names[2];
    namePlural = names[3];

    Dice noDice = new Dice (1, 1, 0);

    image = -1;
    groupSize = noDice;
    hitPoints = noDice;
    monsterClass = 0;
    armourClass = 0;
    damageDiceSize = 0;
    experiencePoints = 0;
    drain = 0;
    regen = 0;
    rewardWandering = 0;
    rewardLair = 0;
    partnerId = 0;
    partnerOdds = 0;
    mageSpells = 0;
    priestSpells = 0;
    unique = 0;
    breathe = 0;
    unaffect = 0;
    resistance = 0;
    properties = 0;
    damageDiceText = "";
  }

  // ---------------------------------------------------------------------------------//
  public Monster (int id, DataBlock dataBlock)
  // ---------------------------------------------------------------------------------//
  {
    this.id = id;

    byte[] buffer = dataBlock.buffer;
    int offset = dataBlock.offset;

    genericName = Utility.getPascalString (buffer, offset);
    genericNamePlural = Utility.getPascalString (buffer, offset + 16);
    name = Utility.getPascalString (buffer, offset + 32);
    namePlural = Utility.getPascalString (buffer, offset + 48);

    image = Utility.getShort (buffer, offset + 64);
    groupSize = new Dice (buffer, offset + 66);
    hitPoints = new Dice (buffer, offset + 72);

    monsterClass = Utility.getShort (buffer, offset + 78);
    armourClass = Utility.getSignedShort (buffer, offset + 80);

    damageDiceSize = Utility.getShort (buffer, offset + 82);       // 0-7
    StringBuilder dd = new StringBuilder ();
    for (int i = 0; i < damageDiceSize; i++)
    {
      damageDice[i] = new Dice (buffer, offset + 84 + i * 6);
      dd.append (damageDice[i].toString () + ", ");
    }
    Utility.trimComma (dd);
    damageDiceText = dd.toString ();

    long exp = Utility.getWizLong (buffer, offset + 126);
    experiencePoints = exp == 0 ? experience[id] : exp;

    drain = Utility.getShort (buffer, offset + 132);
    regen = Utility.getShort (buffer, offset + 134);

    rewardWandering = Utility.getShort (buffer, offset + 136);    // gold rewards index
    rewardLair = Utility.getShort (buffer, offset + 138);         // chest rewards index

    partnerId = Utility.getShort (buffer, offset + 140);
    partnerOdds = Utility.getShort (buffer, offset + 142);

    mageSpells = Utility.getShort (buffer, offset + 144);         // spell level
    priestSpells = Utility.getShort (buffer, offset + 146);       // spell level

    unique = Utility.getSignedShort (buffer, offset + 148);
    breathe = Utility.getShort (buffer, offset + 150);
    unaffect = Utility.getShort (buffer, offset + 152);

    resistance = Utility.getShort (buffer, offset + 154);         // flags
    properties = Utility.getShort (buffer, offset + 156);         // flags
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
  @Override
  public String toString ()       // used by ComboBox
  // ---------------------------------------------------------------------------------//
  {
    return name;
  }
}
