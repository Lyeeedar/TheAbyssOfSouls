package com.lyeeedar.DungeonGeneration.Data

import com.exp4j.Helpers.EquationHelper
import com.lyeeedar.Enums
import com.lyeeedar.Util.Array2D
import java.util.*

/**
 * Created by Philip on 08-Apr-16.
 */

class SymbolicRoom()
{
	lateinit var contents: Array2D<Symbol?>
	val width: Int
		get() = contents.xSize
	val height: Int
		get() = contents.ySize

	var x: Int = 0
	var y: Int = 0

	var placement: Enums.Direction = Enums.Direction.CENTER

	fun fill(ran: Random, data: SymbolicRoomData)
	{
		placement = data.placement

		x = data.x
		y = data.y

		data.ran = ran
		val w = data.widthVal
		val h = data.heightVal

		contents = Array2D<Symbol>(w, h)

		if (data.generator != null)
		{
			// generate the room
		}
		else
		{
			for (x in 0..w-1)
			{
				for (y in 0..h-1)
				{
					contents[x, y] = data.symbolMap[data.contents[x, y]].copy()
				}
			}
		}
	}
}
