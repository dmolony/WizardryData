package com.bytezone.wizardry.data;

import java.util.Random;

// -----------------------------------------------------------------------------------//
public class Dice
// -----------------------------------------------------------------------------------//
{
  private static final Random random = new Random ();

  public final int level;       // how many dice
  public final int faces;       // faces per die
  public final int minAdd;      // plus

  // ---------------------------------------------------------------------------------//
  public Dice (byte[] buffer, int offset)
  // ---------------------------------------------------------------------------------//
  {
    this.level = Utility.getShort (buffer, offset);
    this.faces = Utility.getShort (buffer, offset + 2);
    this.minAdd = Utility.getSignedShort (buffer, offset + 4);
  }

  // ---------------------------------------------------------------------------------//
  public Dice (int level, int faces, int minAdd)
  // ---------------------------------------------------------------------------------//
  {
    this.level = level;
    this.faces = faces;
    this.minAdd = minAdd;
  }

  // ---------------------------------------------------------------------------------//
  public int max ()
  // ---------------------------------------------------------------------------------//
  {
    return level * faces + minAdd;      // each die rolls the maximum
  }

  // ---------------------------------------------------------------------------------//
  public int min ()
  // ---------------------------------------------------------------------------------//
  {
    return level + minAdd;              // each die rolls 1
  }

  // ---------------------------------------------------------------------------------//
  public int roll ()
  // ---------------------------------------------------------------------------------//
  {
    int total = minAdd;

    if (faces > 0)
      for (int die = 0; die < level; die++)
        total += random.nextInt (faces) + 1;

    return total;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    if (level == 0)
      return "";

    if (minAdd == 0)
      return String.format ("%dd%d", level, faces);

    if (minAdd < 0)
      return String.format ("%dd%d%d", level, faces, minAdd);

    return String.format ("%dd%d+%d", level, faces, minAdd);
  }
}
