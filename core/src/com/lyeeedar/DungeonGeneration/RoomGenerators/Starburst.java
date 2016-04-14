package com.lyeeedar.DungeonGeneration.RoomGenerators;

import java.util.Random;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.lyeeedar.DungeonGeneration.Data.Symbol;
import com.lyeeedar.DungeonGeneration.RoomGenerators.AbstractRoomGenerator;
import com.lyeeedar.Util.Array2D;
import org.jetbrains.annotations.NotNull;

public class Starburst extends AbstractRoomGenerator
{

	/*
	 * Accept values for y and x (considered as the endpoints of lines) between
	 * 0 and 40, and return an angle in degrees (divided by two).  -LM-
	 *
	 * This table's input and output need some processing:
	 *
	 * Because this table gives degrees for a whole circle, up to radius 20, its
	 * origin is at (x,y) = (20, 20).  Therefore, the input code needs to find
	 * the origin grid (where the lines being compared come from), and then map
	 * it to table grid 20,20.  Do not, however, actually try to compare the
	 * angle of a line that begins and ends at the origin with any other line -
	 * it is impossible mathematically, and the table will return the value "255".
	 *
	 * The output of this table also needs to be massaged, in order to avoid the
	 * discontinuity at 0/180 degrees.  This can be done by:
	 *   rotate = 90 - first value
	 *   this rotates the first input to the 90 degree line)
	 *   tmp = ABS(second value + rotate) % 180
	 *   diff = ABS(90 - tmp) = the angular difference (divided by two) between
	 *   the first and second values.
	 *
	 * Note that grids diagonal to the origin have unique angles.
	 */
	private static final int[][] get_angle_to_grid =
	{
	  {  68,  67,  66,  65,  64,  63,  62,  62,  60,  59,  58,  57,  56,  55,  53,  52,  51,  49,  48,  46,  45,  44,  42,  41,  39,  38,  37,  35,  34,  33,  32,  31,  30,  28,  28,  27,  26,  25,  24,  24,  23 },
	  {  69,  68,  67,  66,  65,  64,  63,  62,  61,  60,  59,  58,  56,  55,  54,  52,  51,  49,  48,  47,  45,  43,  42,  41,  39,  38,  36,  35,  34,  32,  31,  30,  29,  28,  27,  26,  25,  24,  24,  23,  22 },
	  {  69,  69,  68,  67,  66,  65,  64,  63,  62,  61,  60,  58,  57,  56,  54,  53,  51,  50,  48,  47,  45,  43,  42,  40,  39,  37,  36,  34,  33,  32,  30,  29,  28,  27,  26,  25,  24,  24,  23,  22,  21 },
	  {  70,  69,  69,  68,  67,  66,  65,  64,  63,  61,  60,  59,  58,  56,  55,  53,  52,  50,  48,  47,  45,  43,  42,  40,  38,  37,  35,  34,  32,  31,  30,  29,  27,  26,  25,  24,  24,  23,  22,  21,  20 },
	  {  71,  70,  69,  69,  68,  67,  66,  65,  63,  62,  61,  60,  58,  57,  55,  54,  52,  50,  49,  47,  45,  43,  41,  40,  38,  36,  35,  33,  32,  30,  29,  28,  27,  25,  24,  24,  23,  22,  21,  20,  19 },
	  {  72,  71,  70,  69,  69,  68,  67,  65,  64,  63,  62,  60,  59,  58,  56,  54,  52,  51,  49,  47,  45,  43,  41,  39,  38,  36,  34,  32,  31,  30,  28,  27,  26,  25,  24,  23,  22,  21,  20,  19,  18 },
	  {  73,  72,  71,  70,  69,  69,  68,  66,  65,  64,  63,  61,  60,  58,  57,  55,  53,  51,  49,  47,  45,  43,  41,  39,  37,  35,  33,  32,  30,  29,  27,  26,  25,  24,  23,  22,  21,  20,  19,  18,  17 },
	  {  73,  73,  72,  71,  70,  70,  69,  68,  66,  65,  64,  62,  61,  59,  57,  56,  54,  51,  49,  47,  45,  43,  41,  39,  36,  34,  33,  31,  29,  28,  26,  25,  24,  23,  21,  20,  20,  19,  18,  17,  17 },
	  {  75,  74,  73,  72,  72,  71,  70,  69,  68,  66,  65,  63,  62,  60,  58,  56,  54,  52,  50,  47,  45,  43,  40,  38,  36,  34,  32,  30,  28,  27,  25,  24,  23,  21,  20,  19,  18,  18,  17,  16,  15 },
	  {  76,  75,  74,  74,  73,  72,  71,  70,  69,  68,  66,  65,  63,  61,  59,  57,  55,  53,  50,  48,  45,  42,  40,  37,  35,  33,  31,  29,  27,  25,  24,  23,  21,  20,  19,  18,  17,  16,  16,  15,  14 },
	  {  77,  76,  75,  75,  74,  73,  72,  71,  70,  69,  68,  66,  64,  62,  60,  58,  56,  53,  51,  48,  45,  42,  39,  37,  34,  32,  30,  28,  26,  24,  23,  21,  20,  19,  18,  17,  16,  15,  15,  14,  13 },
	  {  78,  77,  77,  76,  75,  75,  74,  73,  72,  70,  69,  68,  66,  64,  62,  60,  57,  54,  51,  48,  45,  42,  39,  36,  33,  30,  28,  26,  24,  23,  21,  20,  18,  17,  16,  15,  15,  14,  13,  13,  12 },
	  {  79,  79,  78,  77,  77,  76,  75,  74,  73,  72,  71,  69,  68,  66,  63,  61,  58,  55,  52,  49,  45,  41,  38,  35,  32,  29,  27,  24,  23,  21,  19,  18,  17,  16,  15,  14,  13,  13,  12,  11,  11 },
	  {  80,  80,  79,  79,  78,  77,  77,  76,  75,  74,  73,  71,  69,  68,  65,  63,  60,  57,  53,  49,  45,  41,  37,  33,  30,  27,  25,  23,  21,  19,  17,  16,  15,  14,  13,  13,  12,  11,  11,  10,  10 },
	  {  82,  81,  81,  80,  80,  79,  78,  78,  77,  76,  75,  73,  72,  70,  68,  65,  62,  58,  54,  50,  45,  40,  36,  32,  28,  25,  23,  20,  18,  17,  15,  14,  13,  12,  12,  11,  10,  10,   9,   9,   8 },
	  {  83,  83,  82,  82,  81,  81,  80,  79,  79,  78,  77,  75,  74,  72,  70,  68,  64,  60,  56,  51,  45,  39,  34,  30,  26,  23,  20,  18,  16,  15,  13,  12,  11,  11,  10,   9,   9,   8,   8,   7,   7 },
	  {  84,  84,  84,  83,  83,  83,  82,  81,  81,  80,  79,  78,  77,  75,  73,  71,  68,  63,  58,  52,  45,  38,  32,  27,  23,  19,  17,  15,  13,  12,  11,  10,   9,   9,   8,   7,   7,   7,   6,   6,   6 },
	  {  86,  86,  85,  85,  85,  84,  84,  84,  83,  82,  82,  81,  80,  78,  77,  75,  72,  68,  62,  54,  45,  36,  28,  23,  18,  15,  13,  12,  10,   9,   8,   8,   7,   6,   6,   6,   5,   5,   5,   4,   4 },
	  {  87,  87,  87,  87,  86,  86,  86,  86,  85,  85,  84,  84,  83,  82,  81,  79,  77,  73,  68,  58,  45,  32,  23,  17,  13,  11,   9,   8,   7,   6,   6,   5,   5,   4,   4,   4,   4,   3,   3,   3,   3 },
	  {  89,  88,  88,  88,  88,  88,  88,  88,  88,  87,  87,  87,  86,  86,  85,  84,  83,  81,  77,  68,  45,  23,  13,   9,   7,   6,   5,   4,   4,   3,   3,   3,   2,   2,   2,   2,   2,   2,   2,   2,   1 },
	  {  90,  90,  90,  90,  90,  90,  90,  90,  90,  90,  90,  90,  90,  90,  90,  90,  90,  90,  90,  90, 255,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0 },
	  {  91,  92,  92,  92,  92,  92,  92,  92,  92,  93,  93,  93,  94,  94,  95,  96,  97,  99, 103, 113, 135, 158, 167, 171, 173, 174, 175, 176, 176, 177, 177, 177, 178, 178, 178, 178, 178, 178, 178, 178, 179 },
	  {  93,  93,  93,  93,  94,  94,  94,  94,  95,  95,  96,  96,  97,  98,  99, 101, 103, 107, 113, 122, 135, 148, 158, 163, 167, 169, 171, 172, 173, 174, 174, 175, 175, 176, 176, 176, 176, 177, 177, 177, 177 },
	  {  94,  94,  95,  95,  95,  96,  96,  96,  97,  98,  98,  99, 100, 102, 103, 105, 108, 113, 118, 126, 135, 144, 152, 158, 162, 165, 167, 168, 170, 171, 172, 172, 173, 174, 174, 174, 175, 175, 175, 176, 176 },
	  {  96,  96,  96,  97,  97,  97,  98,  99,  99, 100, 101, 102, 103, 105, 107, 109, 113, 117, 122, 128, 135, 142, 148, 153, 158, 161, 163, 165, 167, 168, 169, 170, 171, 171, 172, 173, 173, 173, 174, 174, 174 },
	  {  97,  97,  98,  98,  99,  99, 100, 101, 101, 102, 103, 105, 106, 108, 110, 113, 116, 120, 124, 129, 135, 141, 146, 150, 154, 158, 160, 162, 164, 165, 167, 168, 169, 169, 170, 171, 171, 172, 172, 173, 173 },
	  {  98,  99,  99, 100, 100, 101, 102, 102, 103, 104, 105, 107, 108, 110, 113, 115, 118, 122, 126, 130, 135, 140, 144, 148, 152, 155, 158, 160, 162, 163, 165, 166, 167, 168, 168, 169, 170, 170, 171, 171, 172 },
	  { 100, 100, 101, 101, 102, 103, 103, 104, 105, 106, 107, 109, 111, 113, 115, 117, 120, 123, 127, 131, 135, 139, 143, 147, 150, 153, 155, 158, 159, 161, 163, 164, 165, 166, 167, 167, 168, 169, 169, 170, 170 },
	  { 101, 101, 102, 103, 103, 104, 105, 106, 107, 108, 109, 111, 113, 114, 117, 119, 122, 125, 128, 131, 135, 139, 142, 145, 148, 151, 153, 156, 158, 159, 161, 162, 163, 164, 165, 166, 167, 167, 168, 169, 169 },
	  { 102, 103, 103, 104, 105, 105, 106, 107, 108, 110, 111, 113, 114, 116, 118, 120, 123, 126, 129, 132, 135, 138, 141, 144, 147, 150, 152, 154, 156, 158, 159, 160, 162, 163, 164, 165, 165, 166, 167, 167, 168 },
	  { 103, 104, 105, 105, 106, 107, 108, 109, 110, 111, 113, 114, 116, 118, 120, 122, 124, 127, 129, 132, 135, 138, 141, 143, 146, 148, 150, 152, 154, 156, 158, 159, 160, 161, 162, 163, 164, 165, 165, 166, 167 },
	  { 104, 105, 106, 106, 107, 108, 109, 110, 111, 113, 114, 115, 117, 119, 121, 123, 125, 127, 130, 132, 135, 138, 140, 143, 145, 147, 149, 151, 153, 155, 156, 158, 159, 160, 161, 162, 163, 164, 164, 165, 166 },
	  { 105, 106, 107, 108, 108, 109, 110, 111, 113, 114, 115, 117, 118, 120, 122, 124, 126, 128, 130, 133, 135, 137, 140, 142, 144, 146, 148, 150, 152, 153, 155, 156, 158, 159, 160, 161, 162, 162, 163, 164, 165 },
	  { 107, 107, 108, 109, 110, 110, 111, 113, 114, 115, 116, 118, 119, 121, 123, 124, 126, 129, 131, 133, 135, 137, 139, 141, 144, 146, 147, 149, 151, 152, 154, 155, 156, 158, 159, 160, 160, 161, 162, 163, 163 },
	  { 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 119, 120, 122, 123, 125, 127, 129, 131, 133, 135, 137, 139, 141, 143, 145, 147, 148, 150, 151, 153, 154, 155, 156, 158, 159, 159, 160, 161, 162, 163 },
	  { 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 120, 121, 122, 124, 126, 128, 129, 131, 133, 135, 137, 139, 141, 142, 144, 146, 148, 149, 150, 152, 153, 154, 155, 157, 158, 159, 159, 160, 161, 162 },
	  { 109, 110, 111, 112, 113, 114, 114, 115, 117, 118, 119, 120, 122, 123, 125, 126, 128, 130, 131, 133, 135, 137, 139, 140, 142, 144, 145, 147, 148, 150, 151, 152, 153, 155, 156, 157, 158, 159, 159, 160, 161 },
	  { 110, 111, 112, 113, 114, 114, 115, 116, 117, 119, 120, 121, 122, 124, 125, 127, 128, 130, 132, 133, 135, 137, 138, 140, 142, 143, 145, 146, 148, 149, 150, 151, 153, 154, 155, 156, 157, 158, 159, 159, 160 },
	  { 111, 112, 113, 114, 114, 115, 116, 117, 118, 119, 120, 122, 123, 124, 126, 127, 129, 130, 132, 133, 135, 137, 138, 140, 141, 143, 144, 146, 147, 148, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159, 159 },
	  { 112, 113, 114, 114, 115, 116, 117, 118, 119, 120, 121, 122, 124, 125, 126, 128, 129, 131, 132, 133, 135, 137, 138, 139, 141, 142, 144, 145, 146, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159 },
	  { 113, 114, 114, 115, 116, 117, 118, 118, 120, 121, 122, 123, 124, 125, 127, 128, 129, 131, 132, 134, 135, 136, 138, 139, 141, 142, 143, 145, 146, 147, 148, 149, 150, 152, 152, 153, 154, 155, 156, 157, 158 }
	};

	public Starburst( )
	{
		super( false );
	}


	/*
	 * Mark a starburst shape in the dungeon with the CAVE_TEMP flag, given the
	 * coordinates of a section of the dungeon in "box" format. -LM-, -DG-
	 *
	 * Starburst are made in three steps:
	 * 1: Choose a box size-dependant number of arcs.  Large starburts need to
	 *    look less granular and alter their shape more often, so they need
	 *    more arcs.
	 * 2: For each of the arcs, calculate the portion of the full circle it
	 *    includes, and its maximum effect range (how far in that direction
	 *    we can change features in).  This depends on starburst size, shape, and
	 *    the maximum effect range of the previous arc.
	 * 3: Use the table "get_angle_to_grid" to supply angles to each grid in
	 *    the room.  If the distance to that grid is not greater than the
	 *    maximum effect range that applies at that angle, change the feature
	 *    if appropriate (this depends on feature type).
	 *
	 * Usage notes:
	 * - This function uses a table that cannot handle distances larger than
	 *   20, so it calculates a distance conversion factor for larger starbursts.
	 * - This function is not good at handling starbursts much longer along one axis
	 *   than the other.
	 * This function doesn't mark any grid in the perimeter of the given box.
	 *
	 */
	@Override
	public void process( @NotNull Array2D<Symbol> grid, @NotNull ObjectMap<Character, Symbol> symbolMap, @NotNull Random ran )
	{
		int y0, x0, y, x, ny, nx;
		int i;
		int size;
		int dist, max_dist, dist_conv, dist_check;
		int height, width, arc_dist;
		int degree_first, center_of_arc, degree;

		/* Special variant starburst.  Discovered by accident. */
		boolean make_cloverleaf = false;

		/* Holds first degree of arc, maximum effect distance in arc. */
		int[][] arc = new int[45][2];

		/* Number (max 45) of arcs. */
		int arc_num;

		/* Get room height and width. */
		width = grid.getXSize();
		height = grid.getYSize();

		Symbol wall = symbolMap.get( '#' );
		Symbol floor = symbolMap.get( '.' );

		// reset to all wall
		for (x = 0; x < width; x++)
		{
			for (y = 0; y < height; y++)
			{
				grid.getArray()[x][y] = wall.copy();
			}
		}

		/* Note the "size" */
		size = 2 + (width + height) / 22;

		/* Get a shrinkage ratio for large starbursts, as table is limited. */
		if ((width > 40) || (height > 40))
		{
			if (width > height) dist_conv = 1 + (10 * width  / 40);
			else                dist_conv = 1 + (10 * height / 40);
		}
		else dist_conv = 10;

		/* Make a cloverleaf starburst sometimes.  (discovered by accident) */
		if (height > 10 && ran.nextInt(20) == 0)
		{
			arc_num = 12;
			make_cloverleaf = true;
		}

		/* Usually, we make a normal starburst. */
		else
		{
			/* Ask for a reasonable number of arcs. */
			arc_num = 8 + (height * width / 80);
			arc_num = (arc_num - 3) + ran.nextInt(6);;
			if (arc_num < 8) arc_num = 8;
			if (arc_num > 45) arc_num = 45;
		}


		/* Get the center of the starburst. */
		y0 = height / 2;
		x0 = width  / 2;

		/* Start out at zero degrees. */
		degree_first = 0;


		/* Determine the start degrees and expansion distance for each arc. */
		for (i = 0; i < arc_num; i++)
		{
			/* Get the first degree for this arc (using 180-degree circles). */
			arc[i][0] = degree_first;

			/* Get a slightly randomized start degree for the next arc. */
			degree_first += 180 / arc_num;

			/* Do not entirely leave the usual range */
			if (degree_first < 180 * (i+1) / arc_num)
			{
				degree_first = 180 * (i+1) / arc_num;
			}
			if (degree_first > (180 + arc_num) * (i+1) / arc_num)
			{
				degree_first = (180 + arc_num) * (i+1) / arc_num;
			}

			/* Get the center of the arc (convert from 180 to 360 circle). */
			center_of_arc = degree_first + arc[i][0];

			/* Get arc distance from the horizontal (0 and 180 degrees) */
			if      (center_of_arc <=  90) arc_dist = center_of_arc;
			else if (center_of_arc >= 270) arc_dist = Math.abs(center_of_arc - 360);
			else                           arc_dist = Math.abs(center_of_arc - 180);

			/* Special case -- Handle cloverleafs */
			if ((arc_dist == 45) && (make_cloverleaf)) dist = 0;

			/*
			 * Usual case -- Calculate distance to expand outwards.  Pay more
			 * attention to width near the horizontal, more attention to height
			 * near the vertical.
			 */
			else dist = ((height * arc_dist) + (width * (90 - arc_dist))) / 90;

			/* Randomize distance (should never be greater than radius) */
			arc[i][1] = dist >= 4 ? ran.nextInt(dist/4)+(dist/4) : dist;

			/* Keep variability under control (except in special cases). */
			if ((dist != 0) && (i != 0))
			{
				int diff = arc[i][1] - arc[i-1][1];

				if (Math.abs(diff) > size)
				{
					if (diff > 0)	arc[i][1] = arc[i-1][1] + size;
					else arc[i][1] = arc[i-1][1] - size;
				}
			}
		}

		/* Neaten up final arc of circle by comparing it to the first. */
		{
			int diff = arc[arc_num - 1][1] - arc[0][1];

			if (Math.abs(diff) > size)
			{
				if (diff > 0)	arc[arc_num - 1][1] = arc[0][1] + size;
				else arc[arc_num - 1][1] = arc[0][1] - size;
			}
		}

		/* Precalculate check distance. */
		dist_check = 21 * dist_conv / 10;

		/* Change grids between (and not including) the edges. */
		for (y = 1; y < height; y++)
		{
			for (x = 1; x < width; x++)
			{

				/* Get distance to grid. */
				dist = (int)Vector2.dst(y0, x0, y, x);

				/* Look at the grid if within check distance. */
				if (dist < dist_check)
				{
					/* Convert and reorient grid for table access. */
					ny = 20 + 10 * (y - y0) / dist_conv;
					nx = 20 + 10 * (x - x0) / dist_conv;

					/* Illegal table access is bad. */
					if ((ny < 0) || (ny > 40) || (nx < 0) || (nx > 40))  continue;

					/* Get angle to current grid. */
					degree = get_angle_to_grid[ny][nx];

					/* Scan arcs to find the one that applies here. */
					for (i = arc_num - 1; i >= 0; i--)
					{
						if (arc[i][0] <= degree)
						{
							max_dist = arc[i][1];

							/* Must be within effect range. */
							if (max_dist >= dist)
							{
								/* Mark the grid */
								grid.getArray()[x][y] = floor.copy();
							}

							/* Arc found.  End search */
							break;
						}
					}
				}
			}
		}
	}





	@Override
	public void parse(Element xml)
	{
	}
}
