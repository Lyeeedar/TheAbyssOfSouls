package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.lyeeedar.Renderables.Sprite.TilingSprite

/**
 * Created by Philip on 20-Mar-16.
 */

class TilingSpriteComponent: Component
{
	constructor(sprite: TilingSprite)
	{
		this.sprite = sprite
	}

	val sprite: TilingSprite
}