package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.physics.box2d.Body

/**
 * Created by Philip on 21-Mar-16.
 */

class OccluderComponent: Component
{
	constructor()

	var body: Body? = null
}