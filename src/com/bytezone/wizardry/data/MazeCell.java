package com.bytezone.wizardry.data;

import static com.bytezone.wizardry.data.Walls.EAST;
import static com.bytezone.wizardry.data.Walls.NORTH;
import static com.bytezone.wizardry.data.Walls.SOUTH;
import static com.bytezone.wizardry.data.Walls.WEST;

import com.bytezone.wizardry.data.Walls.Wall;
import com.bytezone.wizardry.data.WizardryData.Direction;

// -----------------------------------------------------------------------------------//
public class MazeCell
// -----------------------------------------------------------------------------------//
{
  public static final int CELL_SIZE = 40;
  public static final int INSET = 5;

  private Location location;
  private Walls walls;
  private Special special;
  private boolean lair;

  // ---------------------------------------------------------------------------------//
  public MazeCell (Location location, Walls walls, boolean lair)
  // ---------------------------------------------------------------------------------//
  {
    this.location = location;
    this.lair = lair;
    this.walls = walls;
  }

  // ---------------------------------------------------------------------------------//
  public void addExtra (Special special)
  // ---------------------------------------------------------------------------------//
  {
    this.special = special;
  }

  // ---------------------------------------------------------------------------------//
  public Walls getWalls ()
  // ---------------------------------------------------------------------------------//
  {
    return walls;
  }

  // ---------------------------------------------------------------------------------//
  public Special getSpecial ()
  // ---------------------------------------------------------------------------------//
  {
    return special;
  }

  // ---------------------------------------------------------------------------------//
  public boolean isLair ()
  // ---------------------------------------------------------------------------------//
  {
    return lair;
  }

  // ---------------------------------------------------------------------------------//
  public Location getLocation ()
  // ---------------------------------------------------------------------------------//
  {
    return location;
  }

  // ---------------------------------------------------------------------------------//
  public Wall getLeftWall (Direction direction)
  // ---------------------------------------------------------------------------------//
  {
    return switch (direction)
    {
      case NORTH -> walls.wall (WEST);
      case SOUTH -> walls.wall (EAST);
      case EAST -> walls.wall (NORTH);
      case WEST -> walls.wall (SOUTH);
    };
  }

  // ---------------------------------------------------------------------------------//
  public Wall getRightWall (Direction direction)
  // ---------------------------------------------------------------------------------//
  {
    return switch (direction)
    {
      case NORTH -> walls.wall (EAST);
      case SOUTH -> walls.wall (WEST);
      case EAST -> walls.wall (SOUTH);
      case WEST -> walls.wall (NORTH);
    };
  }

  // ---------------------------------------------------------------------------------//
  public Wall getCentreWall (Direction direction)
  // ---------------------------------------------------------------------------------//
  {
    return switch (direction)
    {
      case NORTH -> walls.wall (NORTH);
      case SOUTH -> walls.wall (SOUTH);
      case EAST -> walls.wall (EAST);
      case WEST -> walls.wall (WEST);
    };
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    if (special == null)
      return String.format ("%s %s %s", location, walls, lair);

    return String.format ("%s %s %s %s", location, walls, lair, special);
  }
}
