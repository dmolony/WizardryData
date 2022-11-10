package com.bytezone.wizardry.data;

// -----------------------------------------------------------------------------------//
public class LostXYL
// -----------------------------------------------------------------------------------//
{
  int[] value = new int[4];
  int offset;
  byte[] buffer;

  // ---------------------------------------------------------------------------------//
  public LostXYL (byte[] buffer, int offset)
  // ---------------------------------------------------------------------------------//
  {
    this.buffer = buffer;
    this.offset = offset;

    for (int i = 0; i < 4; i++)
      value[i] = Utility.getSignedShort (buffer, offset + i * 2);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();
    for (int i = 2; i < 8; i++)
    {
      text.append (String.format ("%02X ", buffer[offset + i]));
    }
    return text.toString ();
    //    return String.format ("%04X %04X %04X", value[1], value[2], value[3]);
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

// POISNAMT [1] contains hit points lost to poison

// AWARDS [4] contains awards bitmap
