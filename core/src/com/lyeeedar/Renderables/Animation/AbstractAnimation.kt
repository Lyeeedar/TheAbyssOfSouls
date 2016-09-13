package com.lyeeedar.Renderables.Animation

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.XmlReader
import java.util.HashMap

import com.badlogic.gdx.utils.XmlReader.Element
import com.badlogic.gdx.utils.reflect.ClassReflection
import com.badlogic.gdx.utils.reflect.ReflectionException
import com.lyeeedar.Util.Colour

abstract class AbstractAnimation
{
	var startDelay = 0f

	abstract fun renderOffset(): FloatArray?
	abstract fun renderScale(): FloatArray?
	abstract fun renderColour(): Colour?

	abstract fun duration(): Float
	abstract fun time(): Float
	abstract fun update(delta: Float): Boolean
	abstract fun parse(xml: Element)

	abstract fun free()

	abstract fun copy(): AbstractAnimation

	companion object
	{
		fun load(xml: Element): AbstractAnimation
		{
			val uname = xml.name.toUpperCase()
			val c = getClass(uname)
			val instance = ClassReflection.getConstructor(c).newInstance() as AbstractAnimation

			instance.parse(xml)

			return instance
		}

		fun getClass(name: String): Class<out AbstractAnimation>
		{
			val type = when(name) {
				"MOVE" -> MoveAnimation::class.java
				"BOUNCE" -> BumpAnimation::class.java
				"BLINK" -> BlinkAnimation::class.java

			// ARGH everything broke
				else -> throw RuntimeException("Invalid sprite animation type: $name")
			}

			return type
		}
	}
}
