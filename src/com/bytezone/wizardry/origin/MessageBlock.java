package com.bytezone.wizardry.origin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// -----------------------------------------------------------------------------------//
class MessageBlock implements Iterable<MessageDataBlock>
// -----------------------------------------------------------------------------------//
{
  private final int indexOffset;
  private final int indexLength;
  private String text;

  private final List<MessageDataBlock> messageDataBlocks = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  MessageBlock (byte[] buffer, Huffman huffman)
  // ---------------------------------------------------------------------------------//
  {
    indexOffset = Utility.getShort (buffer, 0);
    indexLength = Utility.getShort (buffer, 2);

    int ptr = indexOffset * 512;

    for (int i = 0, max = indexLength / 2; i < max; i++)
    {
      int firstMessageNo = Utility.getShort (buffer, ptr + i * 2);
      byte[] data = new byte[512];
      System.arraycopy (buffer, i * 512, data, 0, data.length);
      MessageDataBlock messageDataBlock =
          new MessageDataBlock (" Message " + firstMessageNo, data, firstMessageNo, huffman);
      messageDataBlocks.add (messageDataBlock);
    }
  }

  // ---------------------------------------------------------------------------------//
  public String getMessageText (int messageNo)
  // ---------------------------------------------------------------------------------//
  {
    for (int i = 0; i < messageDataBlocks.size (); i++)
    {
      MessageDataBlock messageDataBlock = messageDataBlocks.get (i);
      if (messageDataBlock.lastMessageNo >= messageNo)
        return messageDataBlock.getText (messageNo);
    }

    return null;
  }

  // ---------------------------------------------------------------------------------//
  public String getMessageLine (int messageNo)
  // ---------------------------------------------------------------------------------//
  {
    for (MessageDataBlock messageDataBlock : messageDataBlocks)
    {
      if (messageNo > messageDataBlock.lastMessageNo)
        continue;
      if (messageNo < messageDataBlock.firstMessageNo)
        break;

      return messageDataBlock.getText (messageNo);
    }

    return "No message";
  }

  // ---------------------------------------------------------------------------------//
  public List<String> getMessageLines (int messageNo)
  // ---------------------------------------------------------------------------------//
  {
    List<String> lines = new ArrayList<> ();

    for (MessageDataBlock messageDataBlock : messageDataBlocks)
    {
      if (messageNo > messageDataBlock.lastMessageNo)
        continue;
      if (messageNo < messageDataBlock.firstMessageNo)
        break;

      while (true)
      {
        String message = messageDataBlock.getText (messageNo++);
        if (message == null)
          break;
        lines.add (message);
      }
    }

    return lines;
  }

  // ---------------------------------------------------------------------------------//
  //  public byte[] getMessage (int messageNo)
  //  // ---------------------------------------------------------------------------------//
  //  {
  //    for (int i = 0; i < messageDataBlocks.size (); i++)
  //    {
  //      MessageDataBlock messageDataBlock = messageDataBlocks.get (i);
  //      if (messageDataBlock.firstMessageNo > messageNo)
  //        return messageDataBlocks.get (i - 1).getMessage (messageNo);
  //    }
  //
  //    return null;
  //  }

  // ---------------------------------------------------------------------------------//
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    if (text != null)
      return text;

    StringBuilder text = new StringBuilder ();

    for (MessageDataBlock mdb : messageDataBlocks)
    {
      text.append (mdb);
      text.append ("\n");
    }

    this.text = text.toString ();

    return this.text;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public Iterator<MessageDataBlock> iterator ()
  // ---------------------------------------------------------------------------------//
  {
    return messageDataBlocks.iterator ();
  }
}