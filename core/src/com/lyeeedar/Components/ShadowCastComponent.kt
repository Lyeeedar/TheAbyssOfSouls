package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.lyeeedar.Pathfinding.ShadowCastCache

class ShadowCastComponent: Component
{
	constructor()

	val cache: ShadowCastCache = ShadowCastCache()
}