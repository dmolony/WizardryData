package com.bytezone.wizardry.origin;

// -----------------------------------------------------------------------------------//
public class MessagesV2 extends Messages
// -----------------------------------------------------------------------------------//
{
  MessageBlock messageBlock;

  // ---------------------------------------------------------------------------------//
  public MessagesV2 (MessageBlock messageBlock)
  // ---------------------------------------------------------------------------------//
  {
    this.messageBlock = messageBlock;

    //    for (MessageDataBlock mdb : messageBlock)
    //      System.out.println (mdb.getText ());
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public Message getMessage (int id)
  // ---------------------------------------------------------------------------------//
  {
    return null;
  }
}
