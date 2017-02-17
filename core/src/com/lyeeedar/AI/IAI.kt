package com.lyeeedar.AI

import com.badlogic.ashley.core.Entity

/**
 * Created by Philip on 20-Mar-16.
 */

interface IAI
{
	fun update(e: Entity)
	fun cancel(e: Entity)
	fun setData(key: String, value: Any?)
}