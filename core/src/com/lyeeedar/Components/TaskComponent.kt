package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.lyeeedar.AI.IAI
import com.lyeeedar.AI.Tasks.AbstractTask
import com.lyeeedar.Enums
import com.lyeeedar.GlobalData
import com.lyeeedar.Level.Tile
import com.lyeeedar.Sprite.SpriteAnimation.MoveAnimation

/**
 * Created by Philip on 20-Mar-16.
 */

class TaskComponent: Component
{
	constructor(ai: IAI)
	{
		this.ai = ai
	}

	val ai: IAI
	val tasks: com.badlogic.gdx.utils.Array<AbstractTask> = com.badlogic.gdx.utils.Array()
	var actionDelay: Float = 0f
}