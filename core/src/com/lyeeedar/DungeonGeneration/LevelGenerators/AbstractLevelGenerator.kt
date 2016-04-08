package com.lyeeedar.DungeonGeneration.LevelGenerators

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.utils.XmlReader
import com.badlogic.gdx.utils.reflect.ClassReflection
import com.lyeeedar.Components.EntityLoader
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.tile
import com.lyeeedar.DungeonGeneration.Data.Symbol
import com.lyeeedar.DungeonGeneration.Data.SymbolicLevelData
import com.lyeeedar.DungeonGeneration.Data.SymbolicRoom
import com.lyeeedar.DungeonGeneration.Data.SymbolicRoomData
import com.lyeeedar.Enums
import com.lyeeedar.Level.Level
import com.lyeeedar.Level.Tile
import com.lyeeedar.Util.Array2D
import java.util.*

/**
 * Created by Philip on 08-Apr-16.
 */

abstract class AbstractLevelGenerator()
{
	lateinit var levelData: SymbolicLevelData
	lateinit var toBePlacedRooms: com.badlogic.gdx.utils.Array<SymbolicRoomData>
	lateinit var placedRooms: com.badlogic.gdx.utils.Array<SymbolicRoomData>
	lateinit var rooms:  com.badlogic.gdx.utils.Array<SymbolicRoom>
	lateinit var ran: Random

	lateinit var contents: Array2D<Symbol?>
	val width: Int
		get() = contents.xSize
	val height: Int
		get() = contents.ySize

	abstract fun parse(xml: XmlReader.Element)
	abstract fun generate() // should be able to run on a background thread

	fun create(engine: Engine): Level
	{
		val level = Level()
		level.grid = Array2D<Tile>(width, height) { x, y -> Tile() }

		for (x in 0.. width-1)
		{
			for (y in 0..height-1)
			{
				val symbol = contents[x, y] ?: continue
				for (slot in Enums.SpaceSlot.Values)
				{
					val el = symbol.contents[slot] ?: continue

					val entity = EntityLoader.load(el)

					val pos = Mappers.position.get(entity)
					pos.position = level.getTile(x, y)!!

					if (slot != pos.slot)
					{
						throw RuntimeException("Entity in incorrect slot!")
					}

					for (ex in 0..pos.size-1)
					{
						for (ey in 0..pos.size-1)
						{
							level.getTile(pos.position, ex, ey)?.contents?.put(pos.slot, entity)
						}
					}

					engine.addEntity(entity)
				}
			}
		}

		return level
	}

	companion object
	{
		fun load(xml: XmlReader.Element): AbstractLevelGenerator
		{
			val uname = xml.name.toUpperCase()
			val c = getClass(uname)
			val instance = ClassReflection.getConstructor(c, AbstractLevelGenerator::class.java).newInstance() as AbstractLevelGenerator

			return instance
		}

		fun getClass(name: String): Class<out AbstractLevelGenerator>
		{
			val type = when(name) {
			//"ENABLE", "DISABLE" -> EventActionSetEnabled::class.java

			// ARGH everything broke
				else -> throw RuntimeException("Invalid level generator type: $name")
			}

			return type
		}
	}
}