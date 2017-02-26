package com.lyeeedar.Util

import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Direction

/**
 * Created by Philip on 08-Apr-16.
 */

class Array2D<T> (val xSize: Int, val ySize: Int, val array: Array<Array<T>>): Sequence<T> {

	val width: Int
		get() = xSize

	val height: Int
		get() = ySize

	companion object {

		inline operator fun <reified T> invoke() = Array2D(0, 0, Array(0, { emptyArray<T>() }))

		inline operator fun <reified T> invoke(xWidth: Int, yWidth: Int) =
				Array2D(xWidth, yWidth, Array(xWidth, { arrayOfNulls<T>(yWidth) }))

		inline operator fun <reified T> invoke(xWidth: Int, yWidth: Int, operator: (Int, Int) -> (T)): Array2D<T> {
			val array = Array(xWidth, {
				val x = it
				Array(yWidth, {operator(x, it)})})
			return Array2D(xWidth, yWidth, array)
		}
	}

	inline fun inBounds(x: Int, y: Int) = x >= 0 && x < xSize && y >= 0 && y < ySize

	inline fun tryGet(x:Int, y:Int, fallback:T?): T?
	{
		if (!inBounds(x, y)) return fallback
		else return this[x, y]
	}

	operator fun get(x: Int, y: Int, fallback:T?): T? = tryGet(x, y, fallback)

	operator fun get(x: Int, y: Int): T {
		return array[x][y]
	}

	operator fun set(x: Int, y: Int, t: T) {
		array[x][y] = t
	}

	operator fun get(p: Point): T {
		return array[p.x][p.y]
	}

	operator fun get(p: Point, dir: Direction): T {
		return array[p.x + dir.x][p.y + dir.y]
	}

	operator fun set(p: Point, t: T) {
		array[p.x][p.y] = t
	}

	inline fun forEach(operation: (T) -> Unit) {
		array.forEach { it.forEach { operation.invoke(it) } }
	}

	inline fun forEachIndexed(operation: (x: Int, y: Int, T) -> Unit) {
		array.forEachIndexed { x, p -> p.forEachIndexed { y, t -> operation.invoke(x, y, t) } }
	}

	override operator fun iterator(): Iterator<T> =  Array2DIterator(this)

	class Array2DIterator<T>(val array: Array2D<T>): Iterator<T>
	{
		var x = 0
		var y = 0

		override fun hasNext(): Boolean = x < array.xSize

		override fun next(): T
		{
			val el = array[x, y]

			y++
			if (y == array.ySize)
			{
				y = 0
				x++
			}

			return el
		}

	}

	override fun toString(): String
	{
		var string = ""

		for (y in 0..ySize-1)
		{
			for (x in 0..xSize-1)
			{
				string += this[x,y].toString() + " "
			}
			string += "\n\n"
		}

		return string
	}
}