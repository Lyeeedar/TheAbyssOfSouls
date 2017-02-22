package com.lyeeedar.SceneTimeline

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.renderable
import com.lyeeedar.Components.stats
import com.lyeeedar.ElementType
import com.lyeeedar.Renderables.Animation.BlinkAnimation
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Util.Colour

class DamageAction() : AbstractTimelineAction()
{
	var amount: Int = 1
	lateinit var element: ElementType
	var elementalConversion: Float = 0.0f

	override fun enter()
	{
		for (tile in parent.destinationTiles)
		{
			for (entity in tile.contents)
			{
				val stats = entity.stats() ?: continue
				//stats.dealDamage(amount, element, elementalConversion)

				val sprite = entity.renderable()?.renderable as? Sprite ?: continue
				sprite.colourAnimation = BlinkAnimation.obtain().set(Colour(1f, 0.5f, 0.5f, 1f), sprite.colour, 0.15f, true)
			}
		}
	}

	override fun exit()
	{

	}

	override fun copy(parent: SceneTimeline): AbstractTimelineAction
	{
		val action = DamageAction()
		action.parent = parent

		action.startTime = startTime
		action.duration = duration

		action.amount = amount
		action.element = element
		action.elementalConversion = elementalConversion

		return action
	}

	override fun parse(xml: XmlReader.Element)
	{
		amount = xml.getInt("Amount", 1)
		element = ElementType.valueOf(xml.get("Element", "None").toUpperCase())
		elementalConversion = xml.getFloat("ElementalConversion", 0.0f)
	}
}