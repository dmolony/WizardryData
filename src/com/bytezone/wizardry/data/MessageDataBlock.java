package com.bytezone.wizardry.data;

import java.util.ArrayList;
import java.util.List;

// -----------------------------------------------------------------------------------//
class MessageDataBlock
// -----------------------------------------------------------------------------------//
{
  final int firstMessageNo;
  final int lastMessageNo;

  private final int groupCount;
  private final List<Message> messages = new ArrayList<> ();

  private final Huffman huffman;
  private byte[] buffer;

  // ---------------------------------------------------------------------------------//
  MessageDataBlock (String name, byte[] buffer, int firstMessageNo, Huffman huffman)
  // ---------------------------------------------------------------------------------//
  {
    this.buffer = buffer;

    this.firstMessageNo = firstMessageNo;
    this.huffman = huffman;

    int ptr = 0x1FF;          // last byte in block
    groupCount = buffer[ptr--] & 0xFF;

    int currentMessageNo = firstMessageNo;
    int totalMessageBytes = firstMessageNo == 1 ? 4 : 0;

    for (int i = 0, max = groupCount - 1; i < groupCount; i++, max--)
    {
      int huffBytes = buffer[ptr];

      for (int j = 0; j < huffBytes; j++)
      {
        int messageLength = buffer[ptr - j - 1] & 0xFF;
        totalMessageBytes += messageLength;
        Message message = new Message (currentMessageNo + j,
            totalMessageBytes - messageLength, messageLength);
        messages.add (message);
      }

      ptr -= huffBytes;
      currentMessageNo += huffBytes;

      ptr--;

      if (max > 0)
      {
        byte gap = buffer[ptr--];
        int skip = gap & 0xFF;

        if ((gap & 0x80) != 0)          // is high bit set?
        {
          gap &= 0x7F;
          int gap2 = buffer[ptr--] & 0xFF;
          skip = gap * 256 + gap2;
        }

        skip--;
        currentMessageNo += skip;
      }
    }

    if (currentMessageNo > firstMessageNo)
      lastMessageNo = currentMessageNo - 1;
    else
      lastMessageNo = firstMessageNo;
  }

  // ---------------------------------------------------------------------------------//
  String getText (int messageNo)
  // ---------------------------------------------------------------------------------//
  {
    for (Message message : messages)
      if (message.msgNo == messageNo)
        return message.text;

    return null;
  }

  // ---------------------------------------------------------------------------------//
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    if (messages.size () == 0)
      return "No Messages";

    if (huffman == null)
      return toString ();

    StringBuilder text = new StringBuilder ();
    int lastMessageNo = messages.get (0).msgNo - 1;

    for (Message message : messages)
    {
      if (message.msgNo != lastMessageNo + 1)
        text.append ("\n");

      lastMessageNo = message.msgNo;
      text.append (String.format ("%5d  %s%n", message.msgNo, message.text));
    }

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    for (Message message : messages)
    {
      text.append (message);
      text.append ("\n");
    }

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  class Message
  // ---------------------------------------------------------------------------------//
  {
    final int msgNo;
    final int offset;
    final int length;
    final String text;

    public Message (int msgNo, int offset, int length)
    {
      this.msgNo = msgNo;
      this.offset = offset;
      this.length = length;

      byte[] returnMessage = new byte[length];
      System.arraycopy (buffer, offset, returnMessage, 0, length);

      text = huffman.decodeMessage (returnMessage);
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();

      String data = HexFormatter.format (buffer, offset, length);
      text.append (String.format ("%5d: %03X  %02X : %s", msgNo, offset, length, data));

      return text.toString ();
    }
  }
}