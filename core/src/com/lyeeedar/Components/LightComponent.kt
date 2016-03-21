package com.lyeeedar.Components

import box2dLight.PointLight
import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Color

/**
 * Created by Philip on 21-Mar-16.
 */

class LightComponent: Component
{
	constructor(col: Color, dist: Float)
	{
		this.col = col
		this.dist = dist
	}

	val col: Color
	val dist: Float
	var lightObj: PointLight? = null
}