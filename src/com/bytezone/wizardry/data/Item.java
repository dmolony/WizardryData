package com.bytezone.wizardry.data;

import static com.bytezone.wizardry.data.Utility.getPascalString;
import static com.bytezone.wizardry.data.Utility.getShort;
import static com.bytezone.wizardry.data.Utility.getSignedShort;
import static com.bytezone.wizardry.data.Utility.getWizLong;

import com.bytezone.wizardry.data.WizardryData.Alignment;
import com.bytezone.wizardry.data.WizardryData.ObjectType;

// -----------------------------------------------------------------------------------//
public class Item
// -----------------------------------------------------------------------------------//
{
  public final int id;
  public final String name;
  public final String nameGeneric;

  public final ObjectType type;
  public final Alignment alignment;
  public final boolean cursed;
  public final int special;
  public final int changeTo;
  public final int changeChance;
  public final long price;
  public final int boltac;
  public final int spellPwr;
  public final int classUseFlags;

  public final int healPts;
  public final int wepvsty2Flags;             // protection
  public final int wepvsty3Flags;             // resistance
  public final int armourClass;
  public final int wephitmd;
  public final Dice wephpdam;

  public final int xtraSwing;
  public final boolean crithitm;
  public final int wepvstyFlags;              // purposed

  // ---------------------------------------------------------------------------------//
  public Item (String[] names, byte[] buffer, int id)
  // ---------------------------------------------------------------------------------//
  {
    this.id = id;
    this.name = names[1];
    this.nameGeneric = names[0];

    type = ObjectType.values ()[buffer[1]];
    alignment = Alignment.values ()[buffer[3]];
    cursed = getSignedShort (buffer, 5) == -1;
    special = getSignedShort (buffer, 7);
    changeTo = getShort (buffer, 9);            // decay #
    changeChance = getShort (buffer, 11);
    price = getWizLong (buffer, 13);
    boltac = getSignedShort (buffer, 19);
    spellPwr = getShort (buffer, 21);
    classUseFlags = getShort (buffer, 23);       // 8 flags

    healPts = getSignedShort (buffer, 25);
    wepvsty2Flags = getShort (buffer, 27);       // protection
    wepvsty3Flags = getShort (buffer, 29);       // resistance
    armourClass = getSignedShort (buffer, 31);
    wephitmd = getSignedShort (buffer, 33);
    wephpdam = new Dice (buffer, 35);

    xtraSwing = getShort (buffer, 41);
    crithitm = getShort (buffer, 43) == 1;       // boolean
    wepvstyFlags = getShort (buffer, 45);        // purposed
  }

  // ---------------------------------------------------------------------------------//
  public Item (int id, DataBlock dataBlock)
  // ---------------------------------------------------------------------------------//
  {
    this.id = id;

    byte[] buffer = dataBlock.buffer;
    int offset = dataBlock.offset;

    name = getPascalString (buffer, offset);
    nameGeneric = getPascalString (buffer, offset + 16);
    type = ObjectType.values ()[buffer[offset + 32]];
    alignment = Alignment.values ()[buffer[offset + 34]];
    cursed = getSignedShort (buffer, offset + 36) == -1;
    special = getSignedShort (buffer, offset + 38);
    changeTo = getShort (buffer, offset + 40);            // decay #
    changeChance = getShort (buffer, offset + 42);
    price = getWizLong (buffer, offset + 44);
    boltac = getSignedShort (buffer, offset + 50);
    spellPwr = getShort (buffer, offset + 52);
    classUseFlags = getShort (buffer, offset + 54);       // 8 flags

    healPts = getSignedShort (buffer, offset + 56);
    wepvsty2Flags = getShort (buffer, offset + 58);       // 0..13 - protection
    wepvsty3Flags = getShort (buffer, offset + 60);       // 0..6  - resistance
    armourClass = getSignedShort (buffer, offset + 62);
    wephitmd = getSignedShort (buffer, offset + 64);
    wephpdam = new Dice (buffer, offset + 66);            // Dice
    xtraSwing = getShort (buffer, offset + 72);
    crithitm = getShort (buffer, offset + 74) == 1;       // boolean
    wepvstyFlags = getShort (buffer, offset + 76);        // 0..13 - purposed
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return name;
  }
}

/*
 *  TOBJREC = RECORD
        NAME     : STRING[ 15];
        NAMEUNK  : STRING[ 15];
        OBJTYPE  : TOBJTYPE;
        ALIGN    : TALIGN;
        CURSED   : BOOLEAN;
        SPECIAL  : INTEGER;
        CHANGETO : INTEGER;
        CHGCHANC : INTEGER;
        PRICE    : TWIZLONG;
        BOLTACXX : INTEGER;
        SPELLPWR : INTEGER;
        CLASSUSE : PACKED ARRAY[ TCLASS] OF BOOLEAN;
        HEALPTS  : INTEGER;
        WEPVSTY2 : PACKED ARRAY[ 0..15] OF BOOLEAN;       // protection
        WEPVSTY3 : PACKED ARRAY[ 0..15] OF BOOLEAN;       // resistance
        ARMORMOD : INTEGER;
        WEPHITMD : INTEGER;
        WEPHPDAM : THPREC;
        XTRASWNG : INTEGER;
        CRITHITM : BOOLEAN;
        WEPVSTYP : PACKED ARRAY[ 0..13] OF BOOLEAN;       // purposed
      END;
 */
