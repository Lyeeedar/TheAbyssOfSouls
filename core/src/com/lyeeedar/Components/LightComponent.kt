package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Color
import com.lyeeedar.Pathfinding.ShadowCastCache
import com.lyeeedar.Util.Colour
import com.lyeeedar.Util.Point
import squidpony.squidgrid.FOV

/**
 * Created by Philip on 21-Mar-16.
 */

class LightComponent: Component
{
	constructor(col: Colour, dist: Float)
	{
		this.col = col
		this.dist = dist
	}

	val col: Colour
	val dist: Float

	var x: Float = 0f
	var y: Float = 0f

	val cache: ShadowCastCache = ShadowCastCache(FOV.SHADOW)
}