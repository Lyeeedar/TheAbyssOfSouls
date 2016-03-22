package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.lyeeedar.Pathfinding.ShadowCastCache

/**
 * Created by Philip on 21-Mar-16.
 */

class ShadowCastComponent: Component
{
	constructor()

	val cache: ShadowCastCache = ShadowCastCache()
}