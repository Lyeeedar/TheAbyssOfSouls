package com.lyeeedar.Util

import com.badlogic.gdx.utils.OrderedSet
import java.util.*

/**
 * Created by Philip on 30-Mar-16.
 */

fun OrderedSet<String>.isAllies(other: OrderedSet<String>): Boolean
{
	for (faction in this)
	{
		if (other.contains(faction)) return true
	}

	return false
}

fun <T> com.badlogic.gdx.utils.Array<T>.ran(ran: Random): T = this[ran.nextInt(this.size)]

fun Float.abs() = Math.abs(this)

fun String.neaten() = this.substring(0, 1).toUpperCase() + this.substring(1).toLowerCase()

fun array2dOfChar(sizeOuter: Int, sizeInner: Int): Array<CharArray> = Array(sizeOuter) { CharArray(sizeInner) }