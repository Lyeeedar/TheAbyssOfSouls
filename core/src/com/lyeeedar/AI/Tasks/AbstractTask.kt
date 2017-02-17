package com.lyeeedar.AI.Tasks

import com.badlogic.ashley.core.Entity

abstract class AbstractTask()
{
	abstract fun execute(e: Entity)
}