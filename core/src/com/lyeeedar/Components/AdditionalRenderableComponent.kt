package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap
import com.lyeeedar.Renderables.Renderable

class AdditionalRenderableComponent : Component
{
	val below = ObjectMap<String, Renderable>()
	val above = ObjectMap<String, Renderable>()
}
