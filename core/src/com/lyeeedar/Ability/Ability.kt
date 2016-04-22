package com.lyeeedar.Ability

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Ability.Cost.AbstractCost
import com.lyeeedar.Ability.Targetting.AbstractTargetting
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
}