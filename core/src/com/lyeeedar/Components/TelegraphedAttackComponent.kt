package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
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
	var readyEntity: Entity? = null
}

class Attack()
{
	lateinit var name: String
	val hitPoints: com.badlogic.gdx.utils.Array<Point> = com.badlogic.gdx.utils.Array()
	lateinit var readySprite: Sprite
	lateinit var hitSprite: Sprite
	lateinit var effectData: XmlReader.Element
}

class Combo()
{
	val steps: com.badlogic.gdx.utils.Array<ComboStep> = com.badlogic.gdx.utils.Array()
	var weight: Int = 1
}

class ComboStep()
{
	lateinit var attack: String
	var canEnd: Boolean = false
	var canTurn: Boolean = false
	var canMove: Boolean = false
}