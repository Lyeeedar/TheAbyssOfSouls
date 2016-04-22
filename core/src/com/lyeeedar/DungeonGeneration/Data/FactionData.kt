package com.lyeeedar.DungeonGeneration.Data

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Enums
import com.lyeeedar.Util.Point
import com.lyeeedar.Util.ran
import com.lyeeedar.Util.removeRan
import java.util.*

/**
 * Created by Philip on 21-Apr-16.
 */

class FactionData()
{
	var theme: RoomTheme? = null
	var minPack: Int = 1
	var maxPack: Int = 4
	val leaders: com.badlogic.gdx.utils.Array<XmlReader.Element> = com.badlogic.gdx.utils.Array()
	val minions: com.badlogic.gdx.utils.Array<XmlReader.Element> = com.badlogic.gdx.utils.Array()

	fun apply(room: SymbolicRoom, ran: Random)
	{
		theme?.apply(room, ran)

		// build lists of valid tiles
		val validList = com.badlogic.gdx.utils.Array<Point>();
		for ( x in 0..room.width-1 )
		{
			for ( y in 0..room.height-1 )
			{
				if ( room.contents[x, y].getPassable( Enums.SpaceSlot.WALL, null ) )
				{
					val point = Point.obtain().set( x, y );
					if ( x > 0 && x < room.width - 1 && y > 0 && y < room.height - 1 )
					{
						validList.add( point );
					}
				}
			}
		}

		var packSize = if (minPack >= maxPack) minPack - 1 else (minPack + ran.nextInt(maxPack-minPack)) - 1

		val leader = leaders.ran(ran)
		placeEntity(leader, room, ran, validList)

		while (packSize > 0 && minions.size > 0)
		{
			val minion = minions.ran(ran)
			placeEntity(minion, room, ran, validList)

			packSize--
		}
	}

	fun placeEntity(xml: XmlReader.Element, room: SymbolicRoom, ran: Random, validList: com.badlogic.gdx.utils.Array<Point>)
	{
		val sizeEl = xml.getChildByNameRecursive("Size")
		val size = sizeEl?.text?.toInt() ?: 1

		val slotEl = xml.getChildByNameRecursive("Slot")
		val slotText = slotEl?.text ?: "Entity"
		val slot = Enums.SpaceSlot.valueOf(slotText.toUpperCase())

		val temp = com.badlogic.gdx.utils.Array<Point>(validList)
		var placed = false
		outer@ while (temp.size > 0)
		{
			val testPoint = temp.removeRan(ran)

			for (x in 0..size-1)
			{
				for (y in 0..size-1)
				{
					val px = testPoint.x + x
					val py = testPoint.y + y

					if (px >= room.width || py >= room.height)
					{
						continue@outer
					}

					val symbol = room.contents[px, py]
					if (symbol.contents.containsKey(slot))
					{
						continue@outer
					}
				}
			}

			// its valid! yay!
			room.contents[testPoint].contents[slot] = xml
			placed = true
			break
		}

		if (!placed)
		{
			val point = validList.ran(ran)
			room.contents[point].contents[slot] = xml
		}
	}

	companion object
	{
		fun load(xml: XmlReader.Element): FactionData
		{
			var faction: FactionData

			if (xml.childCount > 0)
			{
				// faction is here
				faction = loadFactionData(xml)
			}
			else
			{
				// Get faction by its name
				val factionName = xml.text
				val path = "Entities/$factionName/Faction.xml"
				val nxml = XmlReader().parse(Gdx.files.internal(path))
				faction = loadFactionData(nxml)
			}

			return faction
		}

		private fun loadFactionData(xml: XmlReader.Element): FactionData
		{
			var faction = FactionData()

			val themeEl = xml.getChildByName("Theme")
			if (themeEl != null) faction.theme = RoomTheme.load(themeEl)

			faction.minPack = xml.getInt("MinPack", faction.minPack)
			faction.maxPack = xml.getInt("MaxPack", faction.maxPack)

			val leadersEl = xml.getChildByName("Leaders")
			for (i in 0..leadersEl.childCount-1)
			{
				val el = leadersEl.getChild(i)
				faction.leaders.add(el)
			}

			val minionsEl = xml.getChildByName("Minions")
			if (minionsEl != null)
			{
				for (i in 0..minionsEl.childCount-1)
				{
					val el = minionsEl.getChild(i)
					faction.minions.add(el)
				}
			}

			return faction
		}
	}
}