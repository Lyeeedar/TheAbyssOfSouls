package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.lyeeedar.Direction
import com.lyeeedar.Util.Point

class WaterComponent : Component
{
	var flowDir = Direction.CENTRE
	var flowChance = 0.3f
	var depth: Float = 0.3f
}