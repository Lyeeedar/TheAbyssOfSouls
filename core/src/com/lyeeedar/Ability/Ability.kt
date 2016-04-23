package com.lyeeedar.Ability

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Ability.Cost.AbstractCost
import com.lyeeedar.Ability.Targetting.AbstractTargetting
import com.lyeeedar.Ability.Targetting.TargettingEntities
import com.lyeeedar.AssetManager
import com.lyeeedar.Enums
import com.lyeeedar.Level.Tile
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 22-Apr-16.
 */

class Ability()
{
	lateinit var name: String
	lateinit var description: String
	lateinit var icon: Sprite

	val costs: com.badlogic.gdx.utils.Array<AbstractCost> = com.badlogic.gdx.utils.Array()
	val targetting: com.badlogic.gdx.utils.Array<AbstractTargetting> = com.badlogic.gdx.utils.Array()
	val hitPoints: com.badlogic.gdx.utils.Array<Point> = com.badlogic.gdx.utils.Array()
	lateinit var hitType: String
	lateinit var hitSprite: Sprite
	lateinit var effectData: XmlReader.Element

	var caster: Entity? = null
	var dir: Enums.Direction = Enums.Direction.CENTRE
	var targetTile: Tile? = null

	fun process()
	{
		// find hit tiles
	}

	companion object
	{
		fun load(xml: XmlReader.Element) : Ability
		{
			val ability = Ability()

			ability.name = xml.get("Name", "")
			ability.description = xml.get("Description", "")

			val icon = xml.getChildByName("Icon")
			if (icon != null) ability.icon = AssetManager.loadSprite(icon)

			val costs = xml.getChildByName("Cost")
			if (costs != null)
			{
				for (i in 0..costs.childCount-1)
				{
					val el = costs.getChild(i)
					val cost = AbstractCost.load(el)

					ability.costs.add(cost)
				}
			}

			val target = xml.getChildByName("Target")
			if (target != null)
			{
				for (i in 0..target.childCount-1)
				{
					val el = target.getChild(i)
					val tar = AbstractTargetting.load(el)

					ability.targetting.add(tar)
				}
			}
			else
			{
				val target = TargettingEntities()
				target.mode = TargettingEntities.Mode.ENEMIES
				ability.targetting.add(target)
			}

			val hitPatternElement = xml.getChildByName("HitPattern")
			if (hitPatternElement != null)
			{
				val hitGrid = Array(hitPatternElement.childCount, { CharArray(0) })
				val centralPoint = Point();

				for (y in 0..hitPatternElement.childCount - 1)
				{
					val lineElement = hitPatternElement.getChild(y);
					val text = lineElement.text;

					hitGrid[y] = text.toCharArray();

					for (x in 0..hitGrid[y].size - 1)
					{
						if (hitGrid[y][x] == '@')
						{
							centralPoint.x = x;
							centralPoint.y = y;
						}
					}
				}

				for (y in 0..hitGrid.size - 1)
				{
					for (x in 0..hitGrid[0].size - 1)
					{
						if (hitGrid[y][x] == '#')
						{
							val dx = x - centralPoint.x;
							val dy = centralPoint.y - y;

							ability.hitPoints.add(Point(dx, dy));
						}
					}
				}
			}
			else
			{
				ability.hitPoints.add(Point.ZERO)
			}

			ability.hitType = xml.get("HitType", "All").toLowerCase()

			val hitSprite = xml.getChildByName("HitSprite")
			if (hitSprite != null) ability.hitSprite = AssetManager.loadSprite(xml)

			ability.effectData = xml.getChildByName("Effects")

			return ability
		}
	}
}