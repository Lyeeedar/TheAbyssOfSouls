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

fun <T> Array<T>.ran(ran: Random): T = this[ran.nextInt(this.size)]
