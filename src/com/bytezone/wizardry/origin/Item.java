package com.bytezone.wizardry.origin;

import com.bytezone.wizardry.origin.WizardryData.Alignment;
import com.bytezone.wizardry.origin.WizardryData.ObjectType;

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
  public final int healPts;
  public final int armourClass;
  public final int wephitmd;
  public final Dice wephpdam;
  public final int xtraSwing;
  public final boolean crithitm;

  public final int classUseFlags;
  public final int wepvsty2Flags;
  public final int wepvsty3Flags;
  public final int wepvstyFlags;

  // ---------------------------------------------------------------------------------//
  public Item (String[] names, byte[] buffer, int id)
  // ---------------------------------------------------------------------------------//
  {
    this.id = id;
    this.name = names[1];
    this.nameGeneric = names[0];

    type = ObjectType.values ()[buffer[1]];
    alignment = Alignment.values ()[buffer[3]];
    cursed = Utility.getSignedShort (buffer, 5) == -1;
    special = Utility.getSignedShort (buffer, 7);
    changeTo = Utility.getShort (buffer, 9);            // decay #
    changeChance = Utility.getShort (buffer, 11);
    price = Utility.getWizLong (buffer, 13);
    boltac = Utility.getSignedShort (buffer, 19);
    spellPwr = Utility.getShort (buffer, 21);
    classUseFlags = Utility.getShort (buffer, 23);       // 8 flags

    healPts = Utility.getSignedShort (buffer, 25);
    wepvsty2Flags = Utility.getShort (buffer, 27);       // 16 flags
    wepvsty3Flags = Utility.getShort (buffer, 29);       // 16 flags
    armourClass = Utility.getSignedShort (buffer, 31);
    wephitmd = Utility.getSignedShort (buffer, 33);
    wephpdam = new Dice (buffer, 35);

    xtraSwing = Utility.getShort (buffer, 41);
    crithitm = Utility.getShort (buffer, 43) == 1;       // boolean
    wepvstyFlags = Utility.getShort (buffer, 45);        // 14 flags
  }

  // ---------------------------------------------------------------------------------//
  public Item (int id, DataBlock dataBlock)
  // ---------------------------------------------------------------------------------//
  {
    this.id = id;

    byte[] buffer = dataBlock.buffer;
    int offset = dataBlock.offset;

    name = Utility.getPascalString (buffer, offset);
    nameGeneric = Utility.getPascalString (buffer, offset + 16);
    type = ObjectType.values ()[buffer[offset + 32]];
    alignment = Alignment.values ()[buffer[offset + 34]];
    cursed = Utility.getSignedShort (buffer, offset + 36) == -1;
    special = Utility.getSignedShort (buffer, offset + 38);
    changeTo = Utility.getShort (buffer, offset + 40);            // decay #
    changeChance = Utility.getShort (buffer, offset + 42);
    price = Utility.getWizLong (buffer, offset + 44);
    boltac = Utility.getSignedShort (buffer, offset + 50);
    spellPwr = Utility.getShort (buffer, offset + 52);
    classUseFlags = Utility.getShort (buffer, offset + 54);       // 8 flags

    healPts = Utility.getSignedShort (buffer, offset + 56);
    wepvsty2Flags = Utility.getShort (buffer, offset + 58);       // 16 flags
    wepvsty3Flags = Utility.getShort (buffer, offset + 60);       // 16 flags
    armourClass = Utility.getSignedShort (buffer, offset + 62);
    wephitmd = Utility.getSignedShort (buffer, offset + 64);
    wephpdam = new Dice (buffer, offset + 66);                    // Dice
    xtraSwing = Utility.getShort (buffer, offset + 72);
    crithitm = Utility.getShort (buffer, offset + 74) == 1;       // boolean
    wepvstyFlags = Utility.getShort (buffer, offset + 76);        // 14 flags
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return name;
  }
}
