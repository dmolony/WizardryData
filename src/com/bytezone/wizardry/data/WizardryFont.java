package com.bytezone.wizardry.data;

// -----------------------------------------------------------------------------------//
public class WizardryFont
{
  private static int nextId = 1;

  public final String name;
  public final byte[] buffer;
  public final int offset;
  public final int id = nextId++;

  // ---------------------------------------------------------------------------------//
  public WizardryFont (String name, byte[] buffer, int offset, int length)
  // ---------------------------------------------------------------------------------//
  {
    this.name = name;
    this.buffer = buffer;
    this.offset = offset;
  }

  // ---------------------------------------------------------------------------------//
  public int id ()
  // ---------------------------------------------------------------------------------//
  {
    return id;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return name;
  }
}
