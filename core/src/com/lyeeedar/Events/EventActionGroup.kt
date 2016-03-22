package com.lyeeedar.Events

import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.tile

/**
 * Created by Philip on 22-Mar-16.
 */

class EventActionGroup()
{
	lateinit var name: String
	lateinit var description: String
	var aoe: Int = 0

	var enabled: Boolean = true

	val actions: com.badlogic.gdx.utils.Array<AbstractEventAction> = com.badlogic.gdx.utils.Array()

	fun handle(args: EventArgs)
	{
		if (!enabled) return

		val ctile = args.receiver.tile() ?: return
		for (x in -aoe..aoe)
		{
			for (y in -aoe..aoe)
			{
				val tile = ctile.level.getTile(ctile.x + x, ctile.y + y) ?: continue

				for (action in actions)
				{
					action.handle(args, tile)
				}
			}
		}
	}

	fun parse(xml: XmlReader.Element)
	{
		name = xml.getAttribute("Name")
		description = xml.getAttribute("Description", "")
		aoe = xml.getIntAttribute("AOE", aoe)
		enabled = xml.getBooleanAttribute("Enabled", true)

		for (i in 0..xml.childCount-1)
		{
			val cxml = xml.getChild(i)
			val action = AbstractEventAction.get(cxml.name.toUpperCase(), this)
			action.parse(cxml)

			actions.add(action)
		}
	}
}