package com.bytezone.wizardry.origin;

import java.util.Random;

// -----------------------------------------------------------------------------------//
public class EnemyOdds
// -----------------------------------------------------------------------------------//
{
  private static final Random random = new Random ();

  public final int minEnemy;       // first monster
  public final int rangeSize;        // range size
  public final int extraRangeOdds;       // extra range odds
  public final int totExtraRanges;        // extra ranges
  public final int extraRangeOffset;       // range offset

  // ---------------------------------------------------------------------------------//
  public EnemyOdds (byte[] buffer, int offset)
  // ---------------------------------------------------------------------------------//
  {
    minEnemy = Utility.getShort (buffer, offset);
    extraRangeOffset = Utility.getShort (buffer, offset + 2);
    totExtraRanges = Utility.getShort (buffer, offset + 4);
    rangeSize = Utility.getShort (buffer, offset + 6);
    extraRangeOdds = Utility.getSignedShort (buffer, offset + 8);
  }

  // ---------------------------------------------------------------------------------//
  public int getRandomMonster ()
  // ---------------------------------------------------------------------------------//
  {
    // decide which range to use
    int encounterCalc = 0;
    while (random.nextInt (100) < extraRangeOdds && encounterCalc < totExtraRanges)
      ++encounterCalc;

    return minEnemy + random.nextInt (rangeSize) + extraRangeOffset * encounterCalc;
  }

  // ---------------------------------------------------------------------------------//
  public double[] getOdds ()
  // ---------------------------------------------------------------------------------//
  {
    double[] oddsTable = new double[totExtraRanges + 1];

    int min = minEnemy;
    int max = minEnemy + rangeSize - 1;

    double worse = extraRangeOdds / 100.0;

    double odds;
    double oddsLeft = 1.0;
    double total = 0.0;

    for (int i = 0; i <= totExtraRanges; i++)
    {
      odds = oddsLeft * (1 - worse);
      oddsLeft *= worse;
      total += odds;

      if (i == totExtraRanges)         // last line, so combine both fields
      {
        oddsTable[i] = odds + oddsLeft;
        //  System.out.printf ("%2d  %2d:%2d  %12.8f%n", i + 1, min, max, (odds + oddsLeft) * 100);
        total += oddsLeft;
      }
      else
      {
        oddsTable[i] = odds;
        //  System.out.printf ("%2d  %2d:%2d  %12.8f%n", i + 1, min, max, odds * 100);
      }

      min += extraRangeOffset;
      max += extraRangeOffset;
    }

    //    System.out.println ("           ------------");
    //    System.out.printf ("           %12.8f%n", total * 100);

    return oddsTable;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return String.format ("%3d  %3d  %3d  %3d  %3d", minEnemy, extraRangeOffset, totExtraRanges,
        rangeSize, extraRangeOdds);
  }
}
