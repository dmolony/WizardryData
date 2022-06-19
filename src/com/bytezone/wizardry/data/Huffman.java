package com.bytezone.wizardry.data;

import java.util.ArrayList;
import java.util.List;

// Based on a pascal routine by Tom Ewers

// link for possible display algorithm:
// http://stackoverflow.com/questions/14184655/set-position-for-drawing-binary-tree

// -----------------------------------------------------------------------------------//
class Huffman
// -----------------------------------------------------------------------------------//
{
  private static final byte[] mask = { 2, 1 };              // bits: 10 or 01
  private static final int[] nodeOffset = { 512, 256 };     // offset to left/right nodes

  private byte[] buffer;

  private byte depth;
  private int msgPtr;
  private byte currentByte;
  private byte[] message;

  // ---------------------------------------------------------------------------------//
  Huffman (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    this.buffer = buffer;
  }

  // ---------------------------------------------------------------------------------//
  byte[] decodeMessageOld (byte[] buffer, int offset, int length)
  // ---------------------------------------------------------------------------------//
  {
    this.message = buffer;
    List<Byte> decoded = new ArrayList<> ();
    int retPtr = 0;
    int max = offset + length;

    depth = 0;
    msgPtr = offset;
    currentByte = 0;

    while (msgPtr < max)
      decoded.add (getChar ());

    byte[] returnBuffer = new byte[decoded.size ()];
    for (byte b : decoded)
      returnBuffer[retPtr++] = b;

    return returnBuffer;
  }

  // ---------------------------------------------------------------------------------//
  byte[] decodeMessage (byte[] buffer, int offset)
  // ---------------------------------------------------------------------------------//
  {
    this.message = buffer;

    depth = 0;
    msgPtr = offset;
    currentByte = 0;

    int size = (getChar () & 0xFF) + 1;
    byte[] returnBuffer = new byte[size];
    returnBuffer[0] = (byte) size;
    int ptr = 1;

    while (ptr < size)
      returnBuffer[ptr++] = getChar ();

    return returnBuffer;
  }

  // ---------------------------------------------------------------------------------//
  String decodeMessage (byte[] message)
  // ---------------------------------------------------------------------------------//
  {
    this.message = message;

    depth = 0;
    msgPtr = 0;
    currentByte = 0;

    int len = getChar () & 0xFF;
    StringBuilder text = new StringBuilder ();
    for (int i = 0; i < len; i++)
    {
      int c = getChar () & 0xFF;
      text.append (switch (c)
      {
        case 0x09 -> " OF ";
        case 0x0A -> "POTION";
        case 0x0B -> "STAFF";
        default -> c < 32 ? '?' : (char) c;
      });
    }

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private byte getChar ()
  // ---------------------------------------------------------------------------------//
  {
    int treePtr = 0;                            // start at the root

    while (true)
    {
      if ((depth++ & 0x07) == 0)                // every 8th bit...
        currentByte = message[msgPtr++];        // ...get a new byte

      int currentBit = currentByte & 0x01;      // extract the next bit to process
      currentByte >>= 1;                        // and remove it from the current byte

      // use currentBit to determine whether to use the left or right node
      byte nodeValue = buffer[treePtr + nodeOffset[currentBit]];

      // if the node is a leaf, return its contents
      if ((buffer[treePtr] & mask[currentBit]) != 0)
        return nodeValue;

      // else continue traversal
      treePtr = nodeValue & 0xFF;
    }
  }
}