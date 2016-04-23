package com.lyeeedar.Ability.Targetting

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.isAllies
import com.lyeeedar.Components.tile
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 22-Apr-16.
 */

class TargettingEntities(): AbstractTargetting()
{
	enum class Mode
	{
		ALL,
		ALLIES,
		ENEMIES,
		SELF
	}
	lateinit var mode: Mode

	override fun parse(xml: XmlReader.Element)
	{
		mode = when (xml.name.toUpperCase())
		{
			"ALLIES", "ALLY", "FRIEND" -> Mode.ALLIES
			"ENEMIES", "ENEMY", "FOE" -> Mode.ENEMIES
			"SELF", "ME" -> Mode.SELF
			else -> Mode.ALL
		}
	}

	override fun restrict(entity: Entity, tiles: Array<Point>)
	{
		val casterTile = entity.tile() ?: return

		val itr = tiles.iterator()
		while (itr.hasNext())
		{
			val point = itr.next()
			val tile = casterTile.level.getTile(point)
			if (tile == null)
			{
				itr.remove()
				continue
			}

			var valid = false
			for (e in tile.contents)
			{
				if (mode == Mode.ALL)
				{
					valid = true
					break
				}

				if (mode == Mode.SELF)
				{
					if (e == entity)
					{
						valid = true
						break
					}
				}

				val allies = e.isAllies(entity)
				if (allies && mode == Mode.ALLIES)
				{
					valid = true
					break
				}
				if (!allies && mode == Mode.ENEMIES)
				{
					valid = true
					break
				}
			}

			if (!valid)
			{
				itr.remove()
			}
		}
	}
}