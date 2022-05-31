package com.bytezone.wizardry.origin;

import java.io.FileNotFoundException;

// -----------------------------------------------------------------------------------//
public class Wizardry
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  private Wizardry ()
  // ---------------------------------------------------------------------------------//
  {
  }

  // ---------------------------------------------------------------------------------//
  public static WizardryData getWizardryData (String fileName)
      throws DiskFormatException, FileNotFoundException
  // ---------------------------------------------------------------------------------//
  {
    WizardryDisk disk = new WizardryDisk (fileName);

    if (disk == null)
      throw new DiskFormatException ("Not a Wizardry disk: " + fileName);

    if (disk.isWizardryIVorV ())
      return new WizardryData4 (disk);

    return new WizardryData1 (disk);
  }
}
