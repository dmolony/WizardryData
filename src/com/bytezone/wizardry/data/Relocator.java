package com.bytezone.wizardry.data;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.filesystem.AppleBlock.BlockType;
import com.bytezone.filesystem.AppleFileSystem;
import com.bytezone.filesystem.FsPascal;

// -----------------------------------------------------------------------------------//
public class Relocator
// -----------------------------------------------------------------------------------//
{
  private final int checkByte;
  private final List<DiskRecord> diskRecords = new ArrayList<> ();

  private final int[] diskBlocks = new int[0x800];        // disk #
  private final int[] diskOffsets = new int[0x800];       // physical block

  // ---------------------------------------------------------------------------------//
  public Relocator (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    checkByte = Utility.getShort (buffer, 0);       // no of blocks?

    int ptr = 2;            // skip checkByte

    while (buffer[ptr] != 0)
    {
      DiskRecord diskRecord = new DiskRecord (buffer, ptr);
      diskRecords.add (diskRecord);
      ptr += diskRecord.size ();
    }

    for (DiskRecord diskRecord : diskRecords)
      for (DiskSegment diskSegment : diskRecord.diskSegments)
      {
        int lo = diskSegment.logicalBlock;
        int hi = diskSegment.logicalBlock + diskSegment.segmentLength;

        for (int i = lo, count = 0; i < hi; i++, count++)
        {
          diskBlocks[i] = diskRecord.diskNumber;
          diskOffsets[i] = diskSegment.physicalBlock + count;
        }
      }
  }

  // ---------------------------------------------------------------------------------//
  public void createNewBuffer (AppleFileSystem[] dataDisks)
  // ---------------------------------------------------------------------------------//
  {
    FsPascal master = (FsPascal) dataDisks[0];

    for (int logicalBlock = 0; logicalBlock < diskBlocks.length; logicalBlock++)
    {
      int diskNo = diskBlocks[logicalBlock];
      if (diskNo > 0)
      {
        AppleFileSystem disk = dataDisks[diskNo];
        byte[] temp =
            disk.readBlock (disk.getBlock (diskOffsets[logicalBlock], BlockType.OS_DATA));
        master.writeBlock (master.getBlock (logicalBlock, BlockType.OS_DATA), temp);
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append ("Pascal Relocator\n\n");
    text.append (String.format ("Check byte..... %04X%n%n", checkByte));

    for (DiskRecord diskRecord : diskRecords)
    {
      text.append (diskRecord);
      text.append ("\n");
    }

    List<String> lines = new ArrayList<> ();
    String heading = " Logical   Size  Disk  Physical";
    String underline = "---------  ----  ----  ---------";

    int first = 0;
    int lastDisk = diskBlocks[0];
    int lastOffset = diskOffsets[0];

    for (int i = 0; i < diskBlocks.length; i++)
    {
      if (diskBlocks[i] != lastDisk || diskOffsets[i] != lastOffset + i - first)
      {
        int size = i - first;
        if (lastDisk > 0)
          lines.add (String.format ("%03X - %03X   %03X    %d   %03X - %03X", first,
              i - 1, size, lastDisk, lastOffset, lastOffset + size - 1));
        else
          lines.add (String.format ("%03X - %03X   %03X", first, i - 1, size));

        first = i;
        lastDisk = diskBlocks[i];
        lastOffset = diskOffsets[i];
      }
    }

    if (lastDisk > 0)
    {
      int max = diskBlocks.length;
      int size = max - first;
      lines.add (String.format ("%03X - %03X   %03X    %d   %03X - %03X", first, max - 1,
          size, lastDisk, lastOffset, lastOffset + size - 1));
    }

    for (int i = lines.size () - 1; i >= 0; i--)
    {
      String line = lines.get (i);
      if (line.length () > 20)
        break;
      lines.remove (i);
    }

    text.append (String.format ("   %s        %s%n   %s       %s%n", heading, heading,
        underline, underline));
    int offset = (lines.size () + 1) / 2;
    int pairs = lines.size () / 2;

    for (int i = 0; i < pairs; i++)
      text.append (
          String.format ("   %-35s    %s%n", lines.get (i), lines.get (i + offset)));

    if (offset != pairs)
      text.append (String.format ("   %s%n", lines.get (pairs)));

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private class DiskRecord
  // ---------------------------------------------------------------------------------//
  {
    int diskNumber;
    int totDiskSegments;
    List<DiskSegment> diskSegments;

    public DiskRecord (byte[] buffer, int ptr)
    {
      diskNumber = Utility.getShort (buffer, ptr);
      //      totDiskSegments = Utility.intValue (buffer[ptr + 2], buffer[ptr + 4]);
      totDiskSegments = Utility.getShort (buffer, ptr + 2);
      diskSegments = new ArrayList<> (totDiskSegments);

      ptr += 4;
      for (int i = 0; i < totDiskSegments; i++)
      {
        diskSegments.add (new DiskSegment (buffer, ptr));
        ptr += 6;
      }
    }

    int size ()
    {
      return 4 + diskSegments.size () * 6;
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();

      text.append (String.format ("Disk number.... %04X%n", diskNumber));
      text.append (String.format ("Segments....... %04X%n%n", totDiskSegments));
      text.append (String.format (" Seg    Physical       Logical     Size    Gap%n"));
      text.append (String.format (" ---   -----------   -----------   ----   ----%n"));

      int count = 1;
      int last = 0;
      int skip = 0;

      for (DiskSegment diskSegment : diskSegments)
      {
        if (diskSegment.logicalBlock > last)
        {
          int end = diskSegment.logicalBlock - 1;
          skip = end - last + 1;
        }
        last = diskSegment.logicalBlock + diskSegment.segmentLength;
        text.append (String.format ("  %02X  %s   %04X%n", count++, diskSegment, skip));
      }

      return text.toString ();
    }
  }

  // ---------------------------------------------------------------------------------//
  private class DiskSegment
  // ---------------------------------------------------------------------------------//
  {
    int logicalBlock;
    int physicalBlock;
    int segmentLength;

    public DiskSegment (byte[] buffer, int ptr)
    {
      logicalBlock = Utility.getShort (buffer, ptr);
      physicalBlock = Utility.getShort (buffer, ptr + 2);
      segmentLength = Utility.getShort (buffer, ptr + 4);
    }

    @Override
    public String toString ()
    {
      return String.format (" %04X - %04X   %04X - %04X   %04X", physicalBlock,
          (physicalBlock + segmentLength - 1), logicalBlock,
          (logicalBlock + segmentLength - 1), segmentLength);
    }
  }
}