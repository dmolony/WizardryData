package com.bytezone.wizardry.origin;

// -----------------------------------------------------------------------------------//
public class Image
// -----------------------------------------------------------------------------------//
{
  public final byte[] buffer;
  public final int offset;
  public final int scenarioId;

  // ---------------------------------------------------------------------------------//
  public Image (int id, DataBlock dataBlock, int scenarioId)
  // ---------------------------------------------------------------------------------//
  {
    buffer = dataBlock.buffer;
    offset = dataBlock.offset;
    this.scenarioId = scenarioId;
  }
}
