package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.lyeeedar.Events.EventArgs
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Util.FastEnumMap

/**
 * Created by Philip on 21-Mar-16.
 */

class EffectComponent: Component
{
	constructor(sprite: Sprite)
	{
		this.sprite = sprite
	}

	constructor(sprite: Sprite, stage: Sprite.AnimationStage, args: EventArgs)
	{
		this.sprite = sprite
		this.eventMap.put(stage, args)
	}

	val sprite: Sprite
	val eventMap: FastEnumMap<Sprite.AnimationStage, EventArgs>	= FastEnumMap(Sprite.AnimationStage::class.java)
	var completed: Boolean = false
}