package com.lyeeedar.Events

/**
 * Created by Philip on 22-Mar-16.
 */

class EventActionGroup()
{
	lateinit var name: String
	lateinit var description: String

	var enabled: Boolean = true

	val actions: com.badlogic.gdx.utils.Array<AbstractEventAction> = com.badlogic.gdx.utils.Array()

	fun handle(args: EventArgs)
	{
		if (!enabled) return

		for (action in actions)
		{
			action.handle(args)
		}
	}
}