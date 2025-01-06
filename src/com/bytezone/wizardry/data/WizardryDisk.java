package com.bytezone.wizardry.data;

import java.io.File;
import java.io.FileNotFoundException;

import com.bytezone.filesystem.AppleFile;
import com.bytezone.filesystem.AppleFileSystem;
import com.bytezone.filesystem.AppleFileSystem.FileSystemType;
import com.bytezone.filesystem.BlockReader;
import com.bytezone.filesystem.BlockReader.AddressType;
import com.bytezone.filesystem.DataRecord;
import com.bytezone.filesystem.FileSystemFactory;
import com.bytezone.filesystem.FsData;
import com.bytezone.filesystem.FsPascal;

// -----------------------------------------------------------------------------------//
public class WizardryDisk
// -----------------------------------------------------------------------------------//
{
  private String fileName;
  private FsPascal fsPascal;

  MessageBlock messageBlock;          // Wiz 4/5
  private Huffman huffman;

  private FileSystemFactory factory = new FileSystemFactory ();

  // ---------------------------------------------------------------------------------//
  public WizardryDisk (String fileName) throws DiskFormatException, FileNotFoundException
  // ---------------------------------------------------------------------------------//
  {
    this.fileName = fileName;

    File file = new File (fileName);
    if (!file.exists () || !file.isFile ())
      throw new FileNotFoundException ("File does not exist: " + fileName);

    AppleFileSystem fs = factory.getFileSystem (file.toPath ());

    if (fs.getFileSystemType () != FileSystemType.PASCAL)
      throw new DiskFormatException ("Not a Pascal disk: " + fileName);

    this.fsPascal = (FsPascal) fs;

    if (fsPascal.getVolumeName ().equals ("WIZBOOT")      // wiz4 or wiz5
        && checkWiz4or5 (fs))                             // check boot block
    {
      checkDiskSize ();

      if (findFile ("SYSTEM.RELOC") != null)
      {
        createNewDisk (file, this.fsPascal);
        huffman = new Huffman (getFileData ("ASCII.HUFF"));
        messageBlock = new MessageBlock (getFileData ("ASCII.KRN"), huffman);
      }
      else
        throw new DiskFormatException ("SYSTEM.RELOC not found: " + fileName);
    }
  }

  // ---------------------------------------------------------------------------------//
  public WizardryData getWizardryData () throws FileNotFoundException, DiskFormatException
  // ---------------------------------------------------------------------------------//
  {
    return isWizardryIVorV () ? new WizardryData4 (this) : new WizardryData1 (this);
  }

  // ---------------------------------------------------------------------------------//
  private void checkDiskSize () throws DiskFormatException
  // ---------------------------------------------------------------------------------//
  {
    if (fsPascal.getTotalBlocks () == 280)
      if (fsPascal.getVolumeTotalBlocks () == 2048)
      {
        //        System.out.println ("*** Wizardry 4 scenario disk ***");
        if (findFile ("SCENARIO.DATA") == null)
          throw new DiskFormatException ("SCENARIO.DATA not found: " + fileName);
      }
      else if (fsPascal.getVolumeTotalBlocks () == 1600)
      {
        //        System.out.println ("*** Wizardry 5 scenario disk ***");
        if (findFile ("DRAGON.DATA") == null)
          throw new DiskFormatException ("DRAGON.DATA not found: " + fileName);
      }
      else
        throw new DiskFormatException ("Unexpected disk size: " + fileName);
  }

  // ---------------------------------------------------------------------------------//
  private void createNewDisk (File file, FsPascal fs) throws DiskFormatException
  // ---------------------------------------------------------------------------------//
  {
    String fileName = file.getAbsolutePath ().toLowerCase ();
    int pos = file.getAbsolutePath ().indexOf ('.');
    char c = fileName.charAt (pos - 1);
    int requiredDisks = c == '1' ? 6 : c == 'a' ? 9 : 0;

    if (requiredDisks == 0)
      return;

    AppleFileSystem[] disks = new AppleFileSystem[requiredDisks + 1];

    byte[] buffer = new byte[fs.getVolumeTotalBlocks () * fs.getBlockSize ()];
    DataRecord dataRecord = fs.getDataRecord ();
    System.arraycopy (dataRecord.data (), dataRecord.offset (), buffer, 0,
        dataRecord.length ());

    BlockReader reader = new BlockReader ("COLLATED DISK", buffer);
    reader.setParameters (512, AddressType.BLOCK, 1, 8);
    this.fsPascal = new FsPascal (reader);

    disks[0] = this.fsPascal;     // the new logical disk
    disks[1] = fs;                // will be used as a DataDisk

    collectDataDisks (file.getAbsolutePath (), pos, disks);

    Relocator relocator = new Relocator (getFileData ("SYSTEM.RELOC"));
    relocator.createNewBuffer (disks);        // copies disks 1-6 or 1-10 into disk 0

    //    for (AppleFileSystem afs : disks)
    //    {
    //      System.out.println (afs);
    //      System.out.println ();
    //    }
  }

  // ---------------------------------------------------------------------------------//
  private void collectDataDisks (String fileName, int dotPos, AppleFileSystem[] disks)
      throws DiskFormatException
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
        throw new DiskFormatException ("Couldn't collect extra disks required");

      BlockReader reader = new BlockReader (f.toPath ());
      reader.setParameters (512, AddressType.BLOCK, 1, 8);

      disks[i] = new FsData (reader);
    }
  }

  // ---------------------------------------------------------------------------------//
  private boolean isWizardryIVorV ()
  // ---------------------------------------------------------------------------------//
  {
    return (messageBlock != null);
  }

  // ---------------------------------------------------------------------------------//
  private boolean checkWiz4or5 (AppleFileSystem fs)
  // ---------------------------------------------------------------------------------//
  {
    // Wizardry IV or V boot code
    byte[] header = { 0x00, (byte) 0xEA, (byte) 0xA9, 0x60, (byte) 0x8D, 0x01, 0x08 };
    DataRecord dataRecord = fs.getDataRecord ();

    if (!Utility.matches (dataRecord.data (), 0, header))
      return false;

    BlockReader pascalReader = fs.getBlockReader ();
    pascalReader.setParameters (512, AddressType.BLOCK, 1, 8);

    byte[] buffer = fs.readBlock (fs.getBlock (1));

    return Utility.getShort (buffer, 510) == 1;
  }

  // ---------------------------------------------------------------------------------//
  byte[] decode (byte[] buffer, int offset)
  // ---------------------------------------------------------------------------------//
  {
    return huffman.decodeMessage (buffer, offset);
  }

  // ---------------------------------------------------------------------------------//
  private AppleFile findFile (String fileName)
  // ---------------------------------------------------------------------------------//
  {
    for (AppleFile appleFile : fsPascal.getFiles ())
      if (fileName.equals (appleFile.getFileName ()))
        return appleFile;

    return null;
  }

  // ---------------------------------------------------------------------------------//
  byte[] getFileData (String fileName) throws DiskFormatException
  // ---------------------------------------------------------------------------------//
  {
    AppleFile appleFile = findFile (fileName);
    if (appleFile == null)
      throw new DiskFormatException (fileName + " not found");

    return appleFile.getDataRecord ().data ();
  }

  // ---------------------------------------------------------------------------------//
  String getFileName ()
  // ---------------------------------------------------------------------------------//
  {
    return fileName;
  }
}
