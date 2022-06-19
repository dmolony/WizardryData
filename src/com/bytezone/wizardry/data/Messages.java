package com.bytezone.wizardry.data;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

// -----------------------------------------------------------------------------------//
public abstract class Messages
// -----------------------------------------------------------------------------------//
{
  protected Map<Integer, Message> messages = new TreeMap<> ();

  // ---------------------------------------------------------------------------------//
  public Collection<Message> getMessages ()
  // ---------------------------------------------------------------------------------//
  {
    return messages.values ();
  }

  // ---------------------------------------------------------------------------------//
  public abstract Message getMessage (int id);
  // ---------------------------------------------------------------------------------//
}
