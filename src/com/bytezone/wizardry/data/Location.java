package com.bytezone.wizardry.data;

import com.bytezone.wizardry.data.WizardryData.Direction;

// -----------------------------------------------------------------------------------//
public class Location
// -----------------------------------------------------------------------------------//
{
  private int level;
  private int row;
  private int column;

  // ---------------------------------------------------------------------------------//
  public Location (int level, int column, int row)
  // ---------------------------------------------------------------------------------//
  {
    if (column > 19)
      column = 19;
    if (column < 0)
      column = 0;
    if (row > 19)
      row = 19;
    if (row < 0)
      row = 0;

    this.level = level;
    this.row = row;
    this.column = column;
  }

  // ---------------------------------------------------------------------------------//
  public Location (byte[] buffer, int offset)     // used ONLY for LostXYL
  // ---------------------------------------------------------------------------------//
  {
    int[] value = new int[3];

    for (int i = 0; i < value.length; i++)
      value[i] = Utility.getSignedShort (buffer, offset + i * 2);

    column = value[0];
    row = value[1];
    level = value[2];
  }

  // ---------------------------------------------------------------------------------//
  public Location (int[] aux)
  // ---------------------------------------------------------------------------------//
  {
    this.level = aux[0];
    this.row = aux[1];
    this.column = aux[2];
  }

  // ---------------------------------------------------------------------------------//
  public Location (Location copy)
  // ---------------------------------------------------------------------------------//
  {
    this.level = copy.level;
    this.row = copy.row;
    this.column = copy.column;
  }

  // ---------------------------------------------------------------------------------//
  public boolean matches (Location other)
  // ---------------------------------------------------------------------------------//
  {
    return level == other.level && row == other.row && column == other.column;
  }

  // ---------------------------------------------------------------------------------//
  public int getLevel ()
  // ---------------------------------------------------------------------------------//
  {
    return level;
  }

  // ---------------------------------------------------------------------------------//
  public int getRow ()
  // ---------------------------------------------------------------------------------//
  {
    return row;
  }

  // ---------------------------------------------------------------------------------//
  public int getColumn ()
  // ---------------------------------------------------------------------------------//
  {
    return column;
  }

  // ---------------------------------------------------------------------------------//
  public void set (int row, int column)
  // ---------------------------------------------------------------------------------//
  {
    this.row = row;
    this.column = column;
  }

  // ---------------------------------------------------------------------------------//
  public void move (Direction direction)
  // ---------------------------------------------------------------------------------//
  {
    switch (direction)
    {
      case SOUTH:
        row--;
        if (row < 0)
          row = 19;
        break;

      case NORTH:
        row++;
        if (row > 19)
          row = 0;
        break;

      case EAST:
        column++;
        if (column > 19)
          column = 0;
        break;

      case WEST:
        column--;
        if (column < 0)
          column = 19;
        break;
    }
  }

  // ---------------------------------------------------------------------------------//
  public boolean equals (Location location)
  // ---------------------------------------------------------------------------------//
  {
    return this.level == location.level && this.row == location.row
        && this.column == location.column;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    if (level == 0)
      return "Castle";
    return String.format ("L:%02d  N:%02d  E:%02d", level, row, column);
  }
}
