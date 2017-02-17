package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.lyeeedar.Renderables.Renderable

class RenderableComponent() : Component
{
	lateinit var renderable: Renderable

	constructor(renderable: Renderable) : this()
	{
		this.renderable = renderable
	}
}