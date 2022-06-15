package com.bytezone.wizardry.origin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// -----------------------------------------------------------------------------------//
public class CharacterParty implements Iterable<Character>
// -----------------------------------------------------------------------------------//
{
  List<Character> characters = new ArrayList<> ();
  String slogan = "";

  // ---------------------------------------------------------------------------------//
  void add (Character character)
  // ---------------------------------------------------------------------------------//
  {
    characters.add (character);
    slogan += character.getPartialSlogan ();
    character.setParty (this);
  }

  // ---------------------------------------------------------------------------------//
  public int size ()
  // ---------------------------------------------------------------------------------//
  {
    return characters.size ();
  }

  // ---------------------------------------------------------------------------------//
  public String getName ()
  // ---------------------------------------------------------------------------------//
  {
    if (slogan.isEmpty ())
      return "";

    int pos = slogan.indexOf ('\\');
    return pos >= 0 ? slogan.substring (0, pos) : slogan;
  }

  // ---------------------------------------------------------------------------------//
  public String getMessage ()
  // ---------------------------------------------------------------------------------//
  {
    if (slogan.isEmpty ())
      return "";

    int pos = slogan.indexOf ('\\');
    return pos >= 0 ? slogan.substring (pos + 1) : "";
  }

  // ---------------------------------------------------------------------------------//
  public String getRoster ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    for (Character character : characters)
      text.append (String.format ("%3d  %-15s %s  %3d  %3d  %17s  %13s  %13s%n", character.id,
          character.name, character.getTypeString (), character.armourClass, character.hpLeft,
          character.getAttributeString (), character.getSpellsString (Character.MAGE_SPELLS),
          character.getSpellsString (Character.PRIEST_SPELLS)));

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public Iterator<Character> iterator ()
  // ---------------------------------------------------------------------------------//
  {
    return characters.iterator ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (slogan.replace ("\\", " - "));
    text.append ("\n\n");
    text.append (getRoster ());

    return text.toString ();
  }
}
