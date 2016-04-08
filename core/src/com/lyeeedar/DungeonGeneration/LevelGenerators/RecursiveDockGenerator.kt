package com.lyeeedar.DungeonGeneration.LevelGenerators

import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.DungeonGeneration.Data.SymbolicRoomData
import com.lyeeedar.Enums
import com.lyeeedar.Level.Level
import com.lyeeedar.Util.Point
import java.util.*

/**
 * Created by Philip on 08-Apr-16.
 */

class RecursiveDockGenerator(): AbstractLevelGenerator()
{
	var minPadding: Int = 1
	var maxPadding: Int = 2

	override fun parse(xml: XmlReader.Element)
	{

	}

	override fun generate()
	{

	}

	fun partition()
	{
		val min = Point(0, 0)
		val max = Point(width, height)

		// place edge rooms
		val edges = EnumMap<Enums.Direction, com.badlogic.gdx.utils.Array<SymbolicRoomData>>(Enums.Direction::class.java)

		val itr = toBePlacedRooms.iterator()
		while (itr.hasNext())
		{
			val room = itr.next()
			if (room.placement != Enums.Direction.CENTER)
			{
				itr.remove()

				var block = edges[room.placement]
				if (block == null)
				{
					block = com.badlogic.gdx.utils.Array<SymbolicRoomData>()
					edges.put(room.placement, block)
				}

				block.add(room)
			}
		}

		for (dir in Enums.Direction.CardinalValues)
		{
			val block = edges[dir] ?: continue

			val vertical = dir.x != 0
			var totalSize = 0

			for (room in block)
			{
				room.ran = ran
				totalSize += if (vertical) room.heightVal else room.widthVal

				if (max.x - min.x >= room.widthVal + minPadding || max.y - min.y >= room.heightVal + minPadding)
				{
					// not enough space
					return
				}
			}
		}

		// launch recursive phase
	}
}