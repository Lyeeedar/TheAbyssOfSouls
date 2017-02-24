package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.ObjectMap
import com.lyeeedar.Renderables.Renderable
import com.lyeeedar.Renderables.Sprite.Sprite
import ktx.collections.set

class RenderableComponent() : Component
{
	lateinit var renderable: Renderable

	constructor(renderable: Renderable) : this()
	{
		this.renderable = renderable
	}
}