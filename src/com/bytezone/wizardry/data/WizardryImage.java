package com.bytezone.wizardry.data;

// -----------------------------------------------------------------------------------//
public class WizardryImage
{
  public final byte[] buffer;
  public final int offset;
  public final int scenarioId;

  // ---------------------------------------------------------------------------------//
  public WizardryImage (int id, DataBlock dataBlock, int scenarioId)
  // ---------------------------------------------------------------------------------//
  {
    buffer = dataBlock.buffer;
    offset = dataBlock.offset;
    this.scenarioId = scenarioId;
  }
}
