package com.lyeeedar.Events

import com.badlogic.ashley.core.Entity
import com.lyeeedar.Components.EventComponent

/**
 * Created by Philip on 22-Mar-16.
 */

data class EventArgs(val type: EventComponent.EventType, val sender: Entity, val receiver: Entity, val value: Float)