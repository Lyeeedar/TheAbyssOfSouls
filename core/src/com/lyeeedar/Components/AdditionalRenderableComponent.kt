package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Renderables.Renderable
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.children
import ktx.collections.set

class AdditionalRenderableComponent : AbstractComponent()
{
	val below = ObjectMap<String, Renderable>()
	val above = ObjectMap<String, Renderable>()

	override fun parse(xml: XmlReader.Element, entity: Entity)
	{
		val pos = entity.pos()

		val belowEls = xml.getChildByName("Below")
		if (belowEls != null)
		{
			for (el in belowEls.children())
			{
				val key = el.get("Key")
				val renderable = AssetManager.loadRenderable(el.getChildByName("Renderable"))

				if (pos != null)
				{
					renderable.size[0] = pos.size
					renderable.size[1] = pos.size
				}

				below[key] = renderable
			}
		}

		val aboveEls = xml.getChildByName("Above")
		if (aboveEls != null)
		{
			for (el in aboveEls.children())
			{
				val key = el.get("Key")
				val renderable = AssetManager.loadRenderable(el.getChildByName("Renderable"))

				if (pos != null)
				{
					renderable.size[0] = pos.size
					renderable.size[1] = pos.size
				}

				above[key] = renderable
			}
		}
	}
}
