package com.bytezone.wizardry.origin;

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
      int c = (char) getChar () & 0xFF;
      text.append (switch (c)
      {
        case 0x09 -> " OF ";
        case 0x0A -> "POTION";
        case 0x0B -> "*POTION";
        default -> (char) c;
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