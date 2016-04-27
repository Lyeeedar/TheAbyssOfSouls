package com.lyeeedar.DungeonGeneration.LevelGenerators

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.badlogic.gdx.utils.reflect.ClassReflection
import com.exp4j.Helpers.EquationHelper
import com.lyeeedar.Components.EntityLoader
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.name
import com.lyeeedar.Components.tile
import com.lyeeedar.DungeonGeneration.Data.Symbol
import com.lyeeedar.DungeonGeneration.Data.SymbolicLevelData
import com.lyeeedar.DungeonGeneration.Data.SymbolicRoom
import com.lyeeedar.DungeonGeneration.Data.SymbolicRoomData
import com.lyeeedar.Level.Level
import com.lyeeedar.Level.Room
import com.lyeeedar.Level.Tile
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.Array2D
import java.util.*

/**
 * Created by Philip on 08-Apr-16.
 */

abstract class AbstractLevelGenerator()
{
	lateinit var levelData: SymbolicLevelData
	var chosenRooms: com.badlogic.gdx.utils.Array<SymbolicRoomData> = com.badlogic.gdx.utils.Array()
	var toBePlacedRooms: com.badlogic.gdx.utils.Array<SymbolicRoomData> = com.badlogic.gdx.utils.Array()
	var placedRooms: com.badlogic.gdx.utils.Array<SymbolicRoomData> = com.badlogic.gdx.utils.Array()
	var rooms:  com.badlogic.gdx.utils.Array<SymbolicRoom> = com.badlogic.gdx.utils.Array()
	lateinit var ran: Random

	lateinit var contents: Array2D<Symbol>
	val width: Int
		get() = contents.xSize
	val height: Int
		get() = contents.ySize

	abstract fun parse(xml: XmlReader.Element)
	abstract fun generate() // should be able to run on a background thread

	fun create(engine: Engine): Level
	{
		engine.removeAllEntities()

		val level = Level()
		level.grid = Array2D<Tile>(width, height) { x, y -> Tile() }

		for (x in 0.. width-1)
		{
			for (y in 0..height-1)
			{
				val symbol = contents[x, y]
				for (slot in SpaceSlot.Values)
				{
					val el = symbol.contents[slot] ?: continue

					val entity = EntityLoader.load(el)

					val pos = Mappers.position.get(entity)
					pos.position = level.getTile(x, y)!!
					pos.slot = slot

					for (ex in 0..pos.size-1)
					{
						for (ey in 0..pos.size-1)
						{
							level.getTile(pos.position, ex, ey)?.contents?.put(pos.slot, entity)
						}
					}

					engine.addEntity(entity)

					if (entity.name() == "player")
					{
						level.player = entity
					}
				}
			}
		}

		val roomMap = ObjectMap<SymbolicRoom, Room>()
		for (room in rooms)
		{
			val r = Room(room.x, room.y, room.width, room.height)
			r.level = level

			roomMap.put(room, r)
		}

		for (room in rooms)
		{
			val r = roomMap[room]
			for (neighbour in room.neighbours)
			{
				val n = roomMap[neighbour]
				r.neighbours.add(n)
			}

			level.rooms.add(r)
		}

		return level
	}

	fun selectRooms()
	{
		for (room in levelData.rooms)
		{
			val count = EquationHelper.evaluate(room.spawnEquation, ran = ran).toInt()
			for (i in 0..count-1)
			{
				room.ran = ran
				chosenRooms.add(room.copy())
			}
		}
	}

	// ----------------------------------------------------------------------
	fun printGrid( )
	{
		for ( y in height-1 downTo 0)
		{
			for ( x in 0..width-1 )
			{
				System.out.print( contents[x, y].char );
			}
			System.out.print( "\n" );
		}
		System.out.println( "\n" );
	}

	companion object
	{
		fun load(xml: XmlReader.Element): AbstractLevelGenerator
		{
			val uname = xml.name.toUpperCase()
			val c = getClass(uname)
			val instance = ClassReflection.getConstructor(c).newInstance() as AbstractLevelGenerator

			instance.parse(xml)

			instance.levelData = SymbolicLevelData.load(xml)
			instance.ran = Random(0)

			return instance
		}

		fun getClass(name: String): Class<out AbstractLevelGenerator>
		{
			val type = when(name) {
				"RECURSIVEDOCK" -> RecursiveDockGenerator::class.java

			// ARGH everything broke
				else -> throw RuntimeException("Invalid level generator type: $name")
			}

			return type
		}
	}
}