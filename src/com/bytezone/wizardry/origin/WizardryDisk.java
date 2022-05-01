package com.bytezone.wizardry.origin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import com.bytezone.filesystem.AppleFile;
import com.bytezone.filesystem.AppleFileSystem;
import com.bytezone.filesystem.FileSystemFactory;
import com.bytezone.filesystem.FsPascal;

// -----------------------------------------------------------------------------------//
public class WizardryDisk
// -----------------------------------------------------------------------------------//
{
  FsPascal fs;

  // ---------------------------------------------------------------------------------//
  public WizardryDisk (String fileName) throws DiskFormatException, FileNotFoundException
  // ---------------------------------------------------------------------------------//
  {
    File file = new File (fileName);
    if (!file.exists () || !file.isFile ())
      throw new FileNotFoundException ("File does not exist: " + fileName);

    try
    {
      byte[] diskBuffer = Files.readAllBytes (file.toPath ());
      for (AppleFileSystem fs : FileSystemFactory.getFileSystems ("Wizardry", diskBuffer))
        if (fs instanceof FsPascal pascal)
        {
          this.fs = pascal;
          break;
        }
    }
    catch (IOException e)
    {
      throw new DiskFormatException ("Error reading file: " + fileName, e);
    }

    if (fs == null)
      throw new DiskFormatException ("Not a Pascal disk");

    if (isWizardryIVorV ())
    {
      throw new DiskFormatException ("Wizardry IV or V not supported");
    }

    if (findFile ("SCENARIO.DATA") == null || findFile ("SCENARIO.MESGS") == null)
      throw new DiskFormatException ("Not a Wizardry scenario: " + fileName);
  }

  // ---------------------------------------------------------------------------------//
  private boolean isWizardryIVorV ()
  // ---------------------------------------------------------------------------------//
  {
    // Wizardry IV or V boot code
    byte[] header = { 0x00, (byte) 0xEA, (byte) 0xA9, 0x60, (byte) 0x8D, 0x01, 0x08 };
    byte[] buffer = fs.readBlock (fs.getBlock (0));

    if (!Utility.matches (buffer, 0, header))
      return false;

    buffer = fs.readBlock (fs.getBlock (1));
    return buffer[510] == 1 && buffer[511] == 0;          // disk #1
  }

  // ---------------------------------------------------------------------------------//
  private AppleFile findFile (String fileName)
  // ---------------------------------------------------------------------------------//
  {
    for (AppleFile appleFile : fs.getFiles ())
      if (fileName.equals (appleFile.getName ()))
        return appleFile;

    return null;
  }

  // ---------------------------------------------------------------------------------//
  public byte[] getScenarioData ()
  // ---------------------------------------------------------------------------------//
  {
    AppleFile appleFile = findFile ("SCENARIO.DATA");
    if (appleFile != null)
      return appleFile.read ();

    return null;
  }

  // ---------------------------------------------------------------------------------//
  public byte[] getScenarioMessages ()
  // ---------------------------------------------------------------------------------//
  {
    AppleFile appleFile = findFile ("SCENARIO.MESGS");
    if (appleFile != null)
      return appleFile.read ();

    return null;
  }
}
