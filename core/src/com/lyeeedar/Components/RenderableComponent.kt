package com.lyeeedar.Components

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.evaluate
import com.lyeeedar.Renderables.Renderable
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.children
import com.lyeeedar.Util.round
import ktx.collections.set

class RenderableComponent() : AbstractComponent()
{
	lateinit var renderable: Renderable

	constructor(renderable: Renderable) : this()
	{
		this.renderable = renderable
	}

	override fun parse(xml: XmlReader.Element, entity: Entity)
	{
		var renderableEl = xml.getChildByName("Renderable")

		val variantsEl = xml.getChildByName("Variants")
		if (variantsEl != null)
		{
			for (el in variantsEl.children())
			{
				val chance = el.get("Chance")
				if (chance.evaluate().round() == 1)
				{
					renderableEl = el.getChildByName("Renderable")
					break
				}
			}
		}

		fun loadRenderable(): Renderable
		{
			val renderable = when (renderableEl.getAttribute("meta:RefKey"))
			{
				"Sprite" -> AssetManager.loadSprite(renderableEl)
				"TilingSprite" -> AssetManager.loadTilingSprite(renderableEl)
				"ParticleEffect" -> AssetManager.loadParticleEffect(renderableEl)
				else -> throw Exception("Unknown renderable type '" + renderableEl.getAttribute("meta:RefKey") + "'!")
			}

			val pos = entity.pos()
			if (pos != null)
			{
				renderable.size[0] = pos.size
				renderable.size[1] = pos.size
			}

			return renderable
		}

		if (xml.getBoolean("IsShared", false))
		{
			synchronized(EntityLoader.sharedRenderableMap)
			{
				val key = renderableEl.toString().hashCode()

				if (!EntityLoader.sharedRenderableMap.containsKey(key))
				{
					EntityLoader.sharedRenderableMap[key] = loadRenderable()
				}

				renderable = EntityLoader.sharedRenderableMap[key]
			}
		}
		else
		{
			renderable = loadRenderable()
		}
	}
}