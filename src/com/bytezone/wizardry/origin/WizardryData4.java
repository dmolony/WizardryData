package com.bytezone.wizardry.origin;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.TreeMap;

// -----------------------------------------------------------------------------------//
public class WizardryData4 extends WizardryData
// -----------------------------------------------------------------------------------//
{
  private static final int CHARACTER_RECORD_LENGTH = 99;
  private static final int ITEM_RECORD_LENGTH = 33;
  private static final int MONSTER_RECORD_LENGTH = 33;

  // ---------------------------------------------------------------------------------//
  public WizardryData4 (WizardryDisk disk) throws FileNotFoundException, DiskFormatException
  // ---------------------------------------------------------------------------------//
  {
    super (disk);

    byte[] buffer = disk.getFileData ("SCENARIO.DATA");
    header = new Header (buffer);

    // create message lines
    messages = new MessagesV2 (disk.messageBlock);
    MessagesV2 messagesV2 = ((MessagesV2) messages);

    // add spell names
    String[] spellNames = new String[51];
    for (int i = 0; i < spellNames.length; i++)
    {
      String line = messagesV2.getMessageLine (i + 5000);
      spellNames[i] = line.startsWith ("*") ? line.substring (1) : line;
    }
    header.addSpellNames (spellNames);

    // add maze levels
    ScenarioData sd = header.get (MAZE_AREA);
    mazeLevels = new ArrayList<> (sd.totalUnits);

    int id = 0;
    for (DataBlock dataBlock : sd.dataBlocks)
    {
      MazeLevel mazeLevel = new MazeLevel (this, ++id, dataBlock);
      mazeLevels.add (mazeLevel);

      for (Special special : mazeLevel.getSpecials ())
        if (special.isMessage ())
          getMessage (special.aux[1]).addLocations (special.locations);
    }

    // add monster names
    sd = header.get (MONSTER_AREA);
    monsters = new ArrayList<> (sd.totalUnits);
    String[] monsterNames = new String[4];
    for (int i = 0; i < 120; i++)
    {
      for (int j = 0; j < 4; j++)
        monsterNames[j] = messagesV2.getMessageLine (i * 4 + 13000 + j);

      if (monsterNames[0] != null)
        monsters.add (new Monster (i, monsterNames));
    }

    // add item names
    items = new TreeMap<> ();

    for (int i = 0; i < 120; i++)
    {
      String itemNameGeneric = messagesV2.getMessageLine (i * 2 + 14000);
      String itemName = messagesV2.getMessageLine (i * 2 + 14000 + 1);

      if (itemName != null)
        items.put (i, new Item (i, itemName, itemNameGeneric));
    }

    // add characters
    characters = new ArrayList<> ();
    int ptr = 1024;

    for (int i = 0; i < 500; i++)
    {
      byte[] out = disk.decode (buffer, ptr, CHARACTER_RECORD_LENGTH);
      int len = out[0] & 0xFF;
      // if (len > out.length)
      //   System.out.printf ("Decoded array too short: (#%3d)  %3d > %3d%n", i, len, out.length);

      Character c = new Character (i, out);
      characters.add (c);

      ptr += CHARACTER_RECORD_LENGTH;
    }

    //    listTeam ();

    //        System.out.println (ptr);     // 50524

    ptr = 66048;
    int count = 0;
    while (true)
    {
      if (buffer[ptr] != (byte) 0xD8)
        break;
      ++count;
      System.out.println (HexFormatter.formatNoHeader (buffer, ptr, ITEM_RECORD_LENGTH + 1));
      System.out.println ();
      byte[] out = disk.decode (buffer, ptr, ITEM_RECORD_LENGTH);
      System.out.println (HexFormatter.formatNoHeader (out));
      System.out.println ();
      ptr += ITEM_RECORD_LENGTH;
    }
    System.out.println (count);

    rewards = new ArrayList<> ();

    buffer = disk.getFileData ("200.MONSTERS");
    images = new ArrayList<> ();

    buffer = disk.getFileData ("WERDNA.DATA");
  }

  // ---------------------------------------------------------------------------------//
  private void listTeam ()
  // ---------------------------------------------------------------------------------//
  {
    int[][] team1 = //
        { //
            { 0, 240, 241, 242, 20, 35 },//
            { 1, 243, 244, 245, 21, 36 },//
            { 2, 246, 247, 248, 40, 55 },//
            { 3, 249, 250, 251, 41, 56 },//
            { 4, 252, 253, 254, 60, 75 },//
            { 5, 255, 256, 257, 61, 76 },//
            { 6, 258, 259, 260, 80, 95 },//
            { 7, 261, 262, 263, 81, 96 },//
            { 8, 264, 265, 266, 100, 115 },//
            { 9, 267, 268, 269, 101, 116 },//
            { 10, 270, 271, 272, 120, 135 },//
            { 11, 273, 274, 275, 121, 136 },//
            { 12, 276, 277, 278, 140, 155 },//
            { 13, 279, 280, 281, 141, 156 },//
            { 14, 282, 283, 284, 160, 175 },//
            { 15, 285, 286, 287, 161, 176 },//
            { 16, 288, 289, 290, 180, 195 },//
            { 17, 291, 292, 293, 181, 196 },//
            { 18, 294, 295, 296, 200, 215 },//
            { 19, 297, 298, 299, 201, 216 },//

            { 354, 355, 356, 357, 358, 359 },//
            { 360, 361, 362, 363, 364, 365 },//
            { 366, 367, 368, 369, 370, 371 },//
            { 390, 391, 392, 393, 394, 395 },//
            { 396, 397, 398, 399, 400, 401 },//
            { 402, 403, 404, 405, 406, 407 },//
            { 408, 409, 410, 411 },//

            { 412, 413, 414, 415, 416, 417 },//
            { 418, 419, 420, 421, 422, 423 },//
            { 424, 425, 426, 427, 428, 429 },//
            { 430, 431, 432, 433, 434, 435 },//
            { 436, 437, 438, 439, 440, 441 },//
            { 442, 443, 444, 445, 446, 447 },//
            { 448, 449, 450, 451, 452, 453 },//
            { 454, 455, 456, 457, 458, 459 },//
            { 460, 461, 462, 463, 464, 465 },//
            { 466, 467, 468, 469, 470, 471 },//
            { 472 },//
            { 473 },//
            { 474, 475, 476, 477, 478, 479 },//
            { 480, 481, 482, 483, 484, 485 },//
            { 486 }, //
            { 487, 488, 489, 490, 491, 492 },//
            { 499 },//

            { 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39 },//
            { 350, 351, 352, 353 },
            { 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 57, 58, 59 },//
            { 372 },//
            { 373 },//
            { 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 77, 78, 79 },//
            { 374 },//
            { 375 },//
            { 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 97, 98, 99 },//
            { 376 },//
            { 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 117, 118, 119 },
            { 377 },//
            { 378 },//
            { 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 137, 138, 139 },
            { 379, 380, 381, 382 },//
            { 383, 389 },//
            { 384, 385, 386, 387, 388 },
            { 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 157, 158, 159 },
            { 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 177, 178, 179 },
            { 182, 183, 184, 185, 186, 187, 188, 189, 190, 191, 192, 193, 194, 197, 198, 199 },
            { 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 217, 218, 219, 220,
                221 },
            { 222, 223, 224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 237, 238,
                239, },
            { 300, 301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 312, 313, 314, 315, 316,
                317, 318, 319, 320, 321, 322, 323, 324, 325, 326, 327, 328, 329, 330, 331, 332, 333,
                334, 335, 336, 337, 338, 339, 340, 341, 342, 343, 344, 345, 346, 347, 348, 349 },
            { 493, 494, 495, 496, 497, 498 },//
        };

    int[] total = new int[500];
    list (team1, total);

    System.out.println ();
    for (int i = 0; i < total.length; i++)
      if (total[i] == 0)
        System.out.println (i);
  }

  // ---------------------------------------------------------------------------------//
  void list (int[][] team, int[] total)
  // ---------------------------------------------------------------------------------//
  {
    for (int i = 0; i < team.length; i++)
    {
      for (int j = 0; j < team[i].length; j++)
      {
        int id = team[i][j];
        total[id]++;
        assert total[id] == 1;
        Character c = characters.get (id);
        System.out.println (c.getText ());
      }
      System.out.println ();
    }
  }
}
