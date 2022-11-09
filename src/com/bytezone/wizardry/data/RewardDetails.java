package com.bytezone.wizardry.data;

// -----------------------------------------------------------------------------------//
public class RewardDetails
// -----------------------------------------------------------------------------------//
{
  public final int rewardPct;
  public final int type;
  public final int scenarioId;

  public final GoldReward goldReward;       // if type == 0
  public final ItemReward itemReward;       // if type == 1

  // ---------------------------------------------------------------------------------//
  public RewardDetails (byte[] buffer, int offset, int scenarioId)
  // ---------------------------------------------------------------------------------//
  {
    this.scenarioId = scenarioId;
    rewardPct = Utility.getShort (buffer, offset);
    type = Utility.getShort (buffer, offset + 2);

    if (type == 0)
    {
      Dice goldDice = new Dice (buffer, offset + 4);
      int base = Utility.getShort (buffer, offset + 10);
      Dice goldDice2 = new Dice (buffer, offset + 12);

      goldReward = new GoldReward (goldDice, base, goldDice2);
      itemReward = null;
    }
    else
    {
      int itemNo = Utility.getSignedShort (buffer, offset + 4);
      int size = Utility.getShort (buffer, offset + 6);
      int max = Utility.getShort (buffer, offset + 8);
      int range = Utility.getShort (buffer, offset + 10);
      int odds = Utility.getSignedShort (buffer, offset + 12);

      itemReward = new ItemReward (itemNo, size, max, range, odds, scenarioId);
      goldReward = null;
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("%3d%%  %d  ", rewardPct, type));

    if (type == 0)
      text.append (String.format ("%s %3d %s  min %d  max %d", goldReward.dice1, goldReward.base,
          goldReward.dice2, goldReward.getMin (), goldReward.getMax ()));
    else
      text.append (String.format ("%3d %3d %3d %3d %3d%%  max %d", itemReward.min, itemReward.size,
          itemReward.max, itemReward.range, itemReward.odds, itemReward.getMax ()));

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  public record GoldReward (Dice dice1, int base, Dice dice2)
  // ---------------------------------------------------------------------------------//
  {
    public int getMin ()
    {
      return dice1.min () * dice2.min ();
    }

    public int getMax ()
    {
      return dice1.max () * base * dice2.max ();
    }

    public String getRange ()
    {
      return getMin () + " : " + getMax ();
    }
  }

  // ---------------------------------------------------------------------------------//
  public record ItemReward (int min, int size, int max, int range, int odds, int scenarioId)
  // ---------------------------------------------------------------------------------//
  {
    public int getMin ()
    {
      if (scenarioId == 1)
        return range == 0 ? min : min + 2;

      return min;
    }

    public int getMax ()
    {
      if (scenarioId == 1)
        return range == 0 ? min : min + range + 1;

      return min + size * max + range;
    }
  }
}

//  FUNCTION CALCULAT( TRIES:  INTEGER;  (* P010D1B *)
//                     AVEAMT: INTEGER;
//                     MINADD: INTEGER) : INTEGER;
//
//  VAR
//       TOTAL : INTEGER;
//     
//  BEGIN
//    TOTAL := MINADD;
//    WHILE TRIES > 0 DO
//      BEGIN
//        TOTAL := TOTAL + (RANDOM MOD AVEAMT) + 1;
//        TRIES := TRIES - 1
//      END;
//    CALCULAT := TOTAL
// END;  (* CALCULAT *)

//  ITEMINDX := REWARDM.REWDCALC.ITEM.MININDX +
//     (CALCULATE( 1, REWARDM.REWDCALC.ITEM.RANGE, 1)) +
//     (REWARDM.REWDCALC.ITEM.MFACTOR * CHARIIII);
