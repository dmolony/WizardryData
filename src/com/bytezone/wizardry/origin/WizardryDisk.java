package com.bytezone.wizardry.origin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import com.bytezone.filesystem.AppleFile;
import com.bytezone.filesystem.AppleFileSystem;
import com.bytezone.filesystem.FileSystemFactory;
import com.bytezone.filesystem.FsData;
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

    if (findFile ("SCENARIO.DATA") == null)// || findFile ("SCENARIO.MESGS") == null)
      throw new DiskFormatException ("Not a Wizardry scenario: " + fileName);

    if (isWizardryIVorV ())
    {
      createNewDisk (file, fs);
      //      throw new DiskFormatException ("Wizardry IV or V not supported");
    }
  }

  // ---------------------------------------------------------------------------------//
  private void createNewDisk (File file, FsPascal fs) throws DiskFormatException
  // ---------------------------------------------------------------------------------//
  {
    String fileName = file.getAbsolutePath ().toLowerCase ();
    int pos = file.getAbsolutePath ().indexOf ('.');
    char c = fileName.charAt (pos - 1);
    int requiredDisks = c == '1' ? 6 : c == 'a' ? 10 : 0;

    if (requiredDisks == 0)
      return;

    AppleFileSystem[] disks = new AppleFileSystem[requiredDisks];

    byte[] buffer = new byte[fs.getVolumeTotalBlocks () * fs.getBlockSize ()];
    System.arraycopy (fs.getBuffer (), fs.getOffset (), buffer, 0, fs.getBuffer ().length);
    disks[0] = new FsPascal ("Wiz4", buffer, fs.getBlockReader ());
    disks[1] = fs;           // will be used as a DataDisk
    this.fs = (FsPascal) disks[0];

    if (!collectDataDisks (file.getAbsolutePath (), pos, disks))
      throw new DiskFormatException ("Couldn't collect extra disks required");

    relocate (disks);
  }

  // ---------------------------------------------------------------------------------//
  private void relocate (AppleFileSystem[] disks) throws DiskFormatException
  // ---------------------------------------------------------------------------------//
  {
    AppleFile appleFile = findFile ("SYSTEM.RELOC");
    if (appleFile == null)
      throw new DiskFormatException ("SYSTEM.RELOC not found");

    Relocator relocator = new Relocator (appleFile.read ());
    relocator.createNewBuffer (disks);
  }

  // ---------------------------------------------------------------------------------//
  private boolean collectDataDisks (String fileName, int dotPos, AppleFileSystem[] disks)
  // ---------------------------------------------------------------------------------//
  {
    char c = fileName.charAt (dotPos - 1);
    String suffix = fileName.substring (dotPos + 1);

    for (int i = 2; i < disks.length; i++)
    {
      String old = new String (c + "." + suffix);
      String rep = new String ((char) (c + i - 1) + "." + suffix);

      File f = new File (fileName.replace (old, rep));
      if (!f.exists () || !f.isFile ())
        return false;

      try
      {
        byte[] diskBuffer = Files.readAllBytes (f.toPath ());
        disks[i] = new FsData ("WizData" + i, diskBuffer, disks[0].getBlockReader ());
      }
      catch (IOException e)
      {
        e.printStackTrace ();
      }
    }

    return true;
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

    return Utility.getShort (buffer, 510) == 1;
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
  public byte[] getFileData (String fileName) throws DiskFormatException
  // ---------------------------------------------------------------------------------//
  {
    AppleFile appleFile = findFile (fileName);
    if (appleFile == null)
      throw new DiskFormatException (fileName + " not found");

    return appleFile.read ();
  }

  // ---------------------------------------------------------------------------------//
  public MessageBlock getScenarioMessages4 () throws DiskFormatException
  // ---------------------------------------------------------------------------------//
  {
    return new MessageBlock (getFileData ("ASCII.KRN"), new Huffman (getFileData ("ASCII.HUFF")));
  }
}
