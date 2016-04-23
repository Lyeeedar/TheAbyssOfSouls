package com.lyeeedar.Ability.Targetting

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.badlogic.gdx.utils.reflect.ClassReflection
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 22-Apr-16.
 */

abstract class AbstractTargetting()
{
	abstract fun parse(xml: XmlReader.Element)
	abstract fun restrict(entity: Entity, tiles: com.badlogic.gdx.utils.Array<Point>)

	companion object
	{
		fun load(xml: XmlReader.Element): AbstractTargetting
		{
			val uname = xml.name.toUpperCase()
			val c = getClass(uname)
			val instance = ClassReflection.getConstructor(c).newInstance() as AbstractTargetting

			instance.parse(xml)

			return instance
		}

		fun getClass(name: String): Class<out AbstractTargetting>
		{
			val type = when(name) {
				"ALL", "ENTITIES", "ALLIES", "ENEMIES", "ALLY", "ENEMY", "FRIEND", "FOE", "SELF", "ME" -> TargettingEntities::class.java

			// ARGH everything broke
				else -> throw RuntimeException("Invalid ability targetting type: $name")
			}

			return type
		}
	}
}