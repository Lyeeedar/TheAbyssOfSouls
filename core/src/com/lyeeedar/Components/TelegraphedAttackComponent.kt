package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AssetManager
import com.lyeeedar.Enums
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 31-Mar-16.
 */

class TelegraphedAttackComponent: Component
{
	constructor()

	val attacks: ObjectMap<String, Attack> = ObjectMap()
	val combos: com.badlogic.gdx.utils.Array<Combo> = com.badlogic.gdx.utils.Array()

	var currentCombo: Combo? = null
	var currentIndex: Int = 0
	var currentDir: Enums.Direction = Enums.Direction.CENTER
	var currentTarget: Point? = null
	var currentSource: Point? = null

	val currentComboStep: ComboStep
		get()
		{
			return currentCombo!!.steps[currentIndex]
		}

	val currentAttack: Attack
		get()
		{
			val name = currentComboStep.attack
			return attacks[name]
		}

	fun parse(xml: XmlReader.Element)
	{
		val attacksEl = xml.getChildByName("Attacks")
		val combosEl = xml.getChildByName("Combos")

		for (i in 0..attacksEl.childCount-1)
		{
			val attackEl = attacksEl.getChild(i)
			val atk = Attack()
			atk.parse(attackEl)

			attacks.put(atk.name, atk)
		}

		for (i in 0..combosEl.childCount-1)
		{
			val comboEl = combosEl.getChild(i)
			val combo = Combo()
			combo.parse(comboEl)

			combos.add(combo)
		}
	}
}

class Attack()
{
	lateinit var name: String
	val hitPoints: com.badlogic.gdx.utils.Array<Point> = com.badlogic.gdx.utils.Array()
	lateinit var hitType: String
	lateinit var readyType: String
	lateinit var readySprite: Sprite
	lateinit var hitSprite: Sprite
	lateinit var effectData: XmlReader.Element

	fun parse(xml: XmlReader.Element)
	{
		name = xml.get("Name").toLowerCase()
		hitType = xml.get("HitType", "all").toLowerCase()
		readyType = xml.get("ReadyType", "closest").toLowerCase()
		readySprite = AssetManager.loadSprite(xml.getChildByName("ReadySprite"))
		hitSprite = AssetManager.loadSprite(xml.getChildByName("HitSprite"))
		effectData = xml.getChildByName("Effects")

		val hitPatternElement = xml.getChildByName("HitPattern")
		val hitGrid = Array(hitPatternElement.childCount, { CharArray(0) })
		val centralPoint = Point();

		for (y in 0..hitPatternElement.childCount-1)
		{
			val lineElement = hitPatternElement.getChild( y );
			val text = lineElement.text;

			hitGrid[ y ] = text.toCharArray();

			for (x in 0..hitGrid[ y ].size-1)
			{
				if (hitGrid[ y ][ x ] == '@')
				{
					centralPoint.x = x;
					centralPoint.y = y;
				}
			}
		}

		for (y in 0..hitGrid.size-1)
		{
			for (x in 0..hitGrid[0].size-1)
			{
				if (hitGrid[y][x] == '#')
				{
					val dx = x - centralPoint.x;
					val dy = centralPoint.y - y;

					hitPoints.add( Point(dx, dy) );
				}
			}
		}
	}
}

class Combo()
{
	val steps: com.badlogic.gdx.utils.Array<ComboStep> = com.badlogic.gdx.utils.Array()
	var weight: Int = 1

	fun parse(xml: XmlReader.Element)
	{
		weight = xml.getIntAttribute("Weight", 1)
		for (i in 0..xml.childCount-1)
		{
			val el = xml.getChild(i)

			val step = ComboStep()
			step.parse(el)

			steps.add(step)
		}
	}
}

class ComboStep()
{
	lateinit var attack: String
	var canEnd: Boolean = false
	var canTurn: Boolean = false
	var canMove: Boolean = false

	fun parse(xml: XmlReader.Element)
	{
		attack = xml.name.toLowerCase()
		canEnd = xml.getBooleanAttribute("CanEnd", false)
		canTurn = xml.getBooleanAttribute("CanTurn", false)
		canMove = xml.getBooleanAttribute("CanMove", false)
	}
}