package com.lyeeedar.Components

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Pathfinding.ShadowCastCache
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.Colour
import squidpony.squidgrid.FOV

class LightComponent() : AbstractComponent()
{
	lateinit var col: Colour
	var dist: Float = 0f

	var x: Float = 0f
	var y: Float = 0f

	val cache: ShadowCastCache = ShadowCastCache(FOV.SHADOW)

	override fun parse(xml: XmlReader.Element, entity: Entity)
	{
		col = AssetManager.loadColour(xml.getChildByName("Colour"))
		dist = xml.getFloat("Distance")
	}
}