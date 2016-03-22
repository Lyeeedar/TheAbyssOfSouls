package com.lyeeedar.Events

import com.badlogic.gdx.utils.XmlReader
import com.badlogic.gdx.utils.reflect.ClassReflection

/**
 * Created by Philip on 22-Mar-16.
 */

abstract class AbstractEventAction()
{
	companion object
	{
		fun get(name: String): AbstractEventAction
		{
			val uname = name.toUpperCase()
			val c = getClass(uname)
			val instance = ClassReflection.newInstance(c)

			return instance
		}

		fun getClass(name: String): Class<out AbstractEventAction>
		{
			val type = when(name) {
				"SETENABLED" -> EventActionSetEnabled::class.java

			// ARGH everything broke
				else -> throw RuntimeException("Invalid event type: $name")
			}

			return type
		}
	}

	abstract fun handle(args: EventArgs)
	abstract fun parse(xml: XmlReader.Element)
}