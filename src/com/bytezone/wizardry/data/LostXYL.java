package com.bytezone.wizardry.data;

// -----------------------------------------------------------------------------------//
public class LostXYL
// -----------------------------------------------------------------------------------//
{
  int[] value = new int[3];

  // ---------------------------------------------------------------------------------//
  public LostXYL (byte[] buffer, int offset)
  // ---------------------------------------------------------------------------------//
  {
    for (int i = 0; i < value.length; i++)
      value[i] = Utility.getSignedShort (buffer, offset + i * 2);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    if (value[0] == 0 && value[1] == 0 && value[2] == 0)
      return "";
    return String.format ("%02d / %02d / %02d", value[0], value[1], value[2]);
  }
}

// LOSTXYL  : RECORD CASE INTEGER OF
//     1:  (LOCATION : ARRAY[ 1..4] OF INTEGER);
//     2:  (POISNAMT : ARRAY[ 1..4] OF INTEGER);
//     3:  (AWARDS   : ARRAY[ 1..4] OF INTEGER);
//   END;

// LOCATION 0/0/0 means in the morgue

// LOCATION [3] <= 0 means unreachable 
// LOCATION [3] > 0 means level (also *out*)
// LOCATION [2] > 9 means north of 
// LOCATION [2] <= 9 means south of 
// LOCATION [1] > 9 means east of 
// LOCATION [1] <= 9 means west of 

// LOSTXYL.LOCATION[ 1] := MAZEX;
// LOSTXYL.LOCATION[ 2] := MAZEY;
// LOSTXYL.LOCATION[ 3] := MAZELEV;

// POISNAMT [1] contains hit points lost to poison (probably never written to disk)
