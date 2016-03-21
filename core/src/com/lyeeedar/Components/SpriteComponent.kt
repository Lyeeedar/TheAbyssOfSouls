package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.lyeeedar.Sprite.Sprite

/**
 * Created by Philip on 20-Mar-16.
 */

class SpriteComponent: Component
{
	constructor(sprite: Sprite)
	{
		this.sprite = sprite
	}

	val sprite: Sprite
}