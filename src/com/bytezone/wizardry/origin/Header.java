package com.bytezone.wizardry.origin;

import java.util.ArrayList;
import java.util.List;

// -----------------------------------------------------------------------------------//
public class Header
// -----------------------------------------------------------------------------------//
{
  static public final String[] typeText =
      { "header", "maze", "monsters", "rewards", "items", "characters", "images", "char levels" };
  static public final String[] scenarioNames = { "PROVING GROUNDS OF THE MAD OVERLORD!",
      "THE KNIGHT OF DIAMONDS", "THE LEGACY OF LLYLGAMYN", "THE RETURN OF WERDNA" };
  static final String[] spell012Text = { "Generic", "Person", "Group" };

  List<ScenarioData> scenarioData = new ArrayList<> (typeText.length);
  String scenarioName;
  int scenarioId;

  String[] race = new String[6];                // NORACE .. HOBBIT
  String[] characterClass = new String[8];      // FIGHTER .. NINJA
  String[] status = new String[8];              // OK .. LOST
  String[] align = new String[4];               // UNALIGN .. EVIL

  int[] spellhsh = new int[51];
  int[] spellgrp = new int[51];
  int[] spell012 = new int[51];

  String[] spellName = new String[51];

  int[][] totalSpells = new int[2][8];

  List<WizardryFont> fonts = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  public Header (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    scenarioName = Utility.getPascalString (buffer, 0);

    for (int i = 0; i < scenarioNames.length; i++)
      if (scenarioNames[i].equals (scenarioName))
      {
        scenarioId = i + 1;
        break;
      }

    for (int i = 0; i < typeText.length; i++)
      scenarioData.add (new ScenarioData (buffer, i));

    int offset = 64 + 42;
    for (int i = 0; i < race.length; i++)
      race[i] = Utility.getPascalString (buffer, offset + i * 10);

    offset += race.length * 10;
    for (int i = 0; i < characterClass.length; i++)
      characterClass[i] = Utility.getPascalString (buffer, offset + i * 10);

    offset += characterClass.length * 10;
    for (int i = 0; i < status.length; i++)
      status[i] = Utility.getPascalString (buffer, offset + i * 10);

    offset += status.length * 10;
    for (int i = 0; i < align.length; i++)
      align[i] = Utility.getPascalString (buffer, offset + i * 10);

    offset += align.length * 10;
    for (int i = 0; i < spellhsh.length; i++)
      spellhsh[i] = Utility.getSignedShort (buffer, offset + i * 2);

    offset += spellhsh.length * 2;

    int row = 0;
    int col = 1;
    int lastVal = 1;

    int count = 0;
    loop: while (true)
    {
      int val = Utility.getShort (buffer, offset);
      offset += 2;

      for (int j = 0; j < 5; j++)
      {
        int level = val & 0x07;           // 3 bits
        spellgrp[count] = level;

        if (level > 0)
        {
          if (level > lastVal)
          {
            ++col;
            lastVal = level;
          }
          else if (level < lastVal)
          {
            ++row;
            col = 1;
            lastVal = level;
          }
          totalSpells[row][0]++;
          totalSpells[row][col]++;
        }

        if (++count >= spellgrp.length)
          break loop;

        val >>>= 3;
      }
    }

    count = 0;
    loop2: while (true)
    {
      int val = Utility.getShort (buffer, offset);
      offset += 2;

      for (int j = 0; j < 8; j++)
      {
        spell012[count] = val & 0x03;           // 2 bits
        if (++count >= spell012.length)
          break loop2;
        val >>>= 2;
      }
    }

    offset = 512;

    if (scenarioId < 3)
    {
      fonts.add (new WizardryFont ("Alphabet", buffer, 512, 512));
      fonts.add (new WizardryFont ("Graphics", buffer, 1024, 512));
      fonts.add (new WizardryFont ("Unknown", buffer, 1536, 512));    // probably not a font

      offset = 2048;
    }

    count = 1;
    for (row = 0; row < 2; row++)
    {
      int ptr = offset;

      for (int i = 0; i < totalSpells[row][0]; i++)
      {
        String spell = Utility.getDelimitedString (buffer, ptr, (byte) 0x0D);
        ptr += spell.length () + 1;
        spellName[count++] = spell.charAt (0) == '*' ? spell.substring (1) : spell;
      }

      offset += 512;
    }
  }

  // ---------------------------------------------------------------------------------//
  public ScenarioData get (int index)
  // ---------------------------------------------------------------------------------//
  {
    return scenarioData.get (index);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Scenario # ........ %d%n", scenarioId));
    text.append (String.format ("Scenario name ..... %s%n", scenarioName));

    text.append ("\n");
    for (ScenarioData sd : scenarioData)
    {
      text.append (sd);
      text.append ("\n");
    }

    text.append ("\nRace:\n");
    for (String s : race)
      text.append ("  " + s + "\n");

    text.append ("\nClass:\n");
    for (String s : characterClass)
      text.append ("  " + s + "\n");

    text.append ("\nStatus:\n");
    for (String s : status)
      text.append ("  " + s + "\n");

    text.append ("\nAlignment:\n");
    for (String s : align)
      text.append ("  " + s + "\n");

    String[] head = { "Mage spells   ", "Priest spells " };
    text.append ("\n");
    for (int row = 0; row < 2; row++)
    {
      text.append (head[row]);
      for (int col = 1; col < totalSpells[row].length; col++)
        text.append (String.format ("%d ", totalSpells[row][col]));
      text.append ("\n");
    }

    int lastLevel = 0;
    text.append ("\n");
    for (int i = 1; i < spellhsh.length; i++)
    {
      if (spellgrp[i] < lastLevel)
        text.append ("\n");
      text.append (String.format ("%2d  %-12s  %d  %s%n", i, spellName[i], spellgrp[i],
          spell012Text[spell012[i]]));
      lastLevel = spellgrp[i];
    }

    Utility.trim (text);

    return text.toString ();
  }

  /*
  TEXP = ARRAY[ FIGHTER..NINJA] OF ARRAY[ 0..12] OF TWIZLONG;
  TBCD = ARRAY[ 0..13] OF INTEGER;
  TSPEL012 = (GENERIC, PERSON, GROUP);
  TZSCN = (ZZERO, ZMAZE, ZENEMY, ZREWARD, ZOBJECT, ZCHAR, ZSPCCHRS, ZEXP);
  
  TSCNTOC = RECORD
         GAMENAME : STRING[ 40];
         RECPER2B : ARRAY[ ZZERO..ZEXP] OF INTEGER;
         RECPERDK : ARRAY[ ZZERO..ZEXP] OF INTEGER;
         UNUSEDXX : ARRAY[ ZZERO..ZEXP] OF INTEGER;
         BLOFF    : ARRAY[ ZZERO..ZEXP] OF INTEGER;
         RACE     : ARRAY[ NORACE..HOBBIT]         OF STRING[ 9];
         CLASS    : PACKED ARRAY[ FIGHTER..NINJA]  OF STRING[ 9];
         STATUS   : ARRAY[ OK..LOST]               OF STRING[ 8];
         ALIGN    : PACKED ARRAY[ UNALIGN..EVIL]   OF STRING[ 9];
         SPELLHSH : PACKED ARRAY[ 0..50] OF INTEGER;
         SPELLGRP : PACKED ARRAY[ 0..50] OF 0..7;
         SPELL012 : PACKED ARRAY[ 0..50] OF TSPEL012;     (GENERIC, PERSON, GROUP)
       END;
      */
}
