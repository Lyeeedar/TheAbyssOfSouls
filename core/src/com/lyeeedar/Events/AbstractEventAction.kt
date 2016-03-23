package com.lyeeedar.Events

import com.badlogic.gdx.utils.XmlReader
import com.badlogic.gdx.utils.reflect.ClassReflection
import com.lyeeedar.Level.Tile

/**
 * Created by Philip on 22-Mar-16.
 */

abstract class AbstractEventAction(val group: EventActionGroup)
{
	companion object
	{
		fun get(name: String, group: EventActionGroup): AbstractEventAction
		{
			val uname = name.toUpperCase()
			val c = getClass(uname)
			val instance = ClassReflection.getConstructor(c, EventActionGroup::class.java).newInstance(group) as AbstractEventAction

			return instance
		}

		fun getClass(name: String): Class<out AbstractEventAction>
		{
			val type = when(name) {
				"ENABLE", "DISABLE" -> EventActionSetEnabled::class.java
				"SETSPRITE", "CLEARSPRITE" -> EventActionSetSprite::class.java
				"SETTILINGSPRITE", "CLEARTILINGSPRITE" -> EventActionSetTilingSprite::class.java
				"SETLIGHT", "CLEARLIGHT" -> EventActionSetLight::class.java
				"REMOVE" -> EventActionRemove::class.java
				//"EFFECT" -> EventActionEffect::class.java - not done yet

			// ARGH everything broke
				else -> throw RuntimeException("Invalid event type: $name")
			}

			return type
		}
	}

	abstract fun handle(args: EventArgs, tile: Tile)
	abstract fun parse(xml: XmlReader.Element)
}