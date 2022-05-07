package com.bytezone.wizardry.origin;

import java.util.ArrayList;
import java.util.List;

// -----------------------------------------------------------------------------------//
class MessageBlock
// -----------------------------------------------------------------------------------//
{
  private final int indexOffset;
  private final int indexLength;

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
    String messageLine;

    for (MessageDataBlock messageDataBlock : messageDataBlocks)
    {
      if (messageNo > messageDataBlock.lastMessageNo)
        continue;
      if (messageNo < messageDataBlock.firstMessageNo)
        break;

      while ((messageLine = messageDataBlock.getText (messageNo)) != null)
      {
        lines.add (messageLine);
        ++messageNo;
      }
    }

    return lines;
  }
}