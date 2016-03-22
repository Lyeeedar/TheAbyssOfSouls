package com.lyeeedar.Components

import com.badlogic.ashley.core.Component

/**
 * Created by Philip on 22-Mar-16.
 */

class NameComponent: Component
{
	constructor(name: String)
	{
		this.name = name
	}

	val name: String
}