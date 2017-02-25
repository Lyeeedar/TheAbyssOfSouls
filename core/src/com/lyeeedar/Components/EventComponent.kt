package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.lyeeedar.Util.Event0Arg

class EventComponent : Component
{
	val onTurn = Event0Arg()
}
