package com.lyeeedar.GenerationGrammar

import com.badlogic.gdx.math.Matrix3
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectFloatMap
import com.lyeeedar.Util.Array2D

class Area
{
	var x: Int = 0
		set(value)
		{
			field = value
			if (value < 0 || value >= grid.width) throw Exception("Invalid area x '$value'!")
		}

	var y: Int = 0
		set(value)
		{
			field = value
			if (value < 0 || value >= grid.height) throw Exception("Invalid area y '$value'!")
		}

	var width: Int = 0
		set(value)
		{
			field = value
			if (value < 0) throw Exception("Invalid area width '$value'!")
		}

	var height: Int = 0
		set(value)
		{
			field = value
			if (value < 0) throw Exception("Invalid area height '$value'!")
		}

	lateinit var grid: Array2D<GrammarSymbol>

	var isPoints = false
	val points = Array<Pos>()

	var flipX: Boolean = false
	var flipY: Boolean = false
	var orientation: Float = 0f
		get() = field
		set(value)
		{
			field = value

			mat.setToRotation(field)
		}
	val mat: Matrix3 = Matrix3()
	val vec: Vector3 = Vector3()

	var xMode: Boolean = true

	var pos: Int
		get() = if (xMode) x else y
		set(value)
		{
			if (xMode)
			{
				x = value
			}
			else
			{
				y = value
			}
		}

	var size: Int
		get() = if (xMode) width else height
		set(value)
		{
			if (xMode)
			{
				width = value
			}
			else
			{
				height = value
			}
		}

	val hasContents: Boolean
		get()
		{
			if (isPoints && points.size == 0) return false
			if (width == 0) return false
			if (height == 0) return false

			return true
		}

	fun writeVariables(variables: ObjectFloatMap<String>)
	{
		variables.put("x", x.toFloat())
		variables.put("y", y.toFloat())
		variables.put("width", width.toFloat())
		variables.put("height", height.toFloat())
		variables.put("size", size.toFloat())
		variables.put("pos", pos.toFloat())

		if (isPoints)
		{
			variables.put("count", points.size.toFloat())
		}
	}

	fun convertToPoints()
	{
		points.addAll(getAllPoints())
		isPoints = true
	}

	fun getAllPoints(): Array<Pos>
	{
		if (isPoints) return points

		val allPoints = Array<Pos>()

		for (ix in 0..width-1)
		{
			for (iy in 0..height-1)
			{
				allPoints.add(Pos(x + ix, y + iy))
			}
		}

		return allPoints
	}

	fun addPointsWithin(area: Area)
	{
		if (width == 0 || height == 0) return

		for (point in area.points)
		{
			if (point.x >= x && point.x < x+width && point.y >= y && point.y < y+height)
			{
				points.add(point)
			}
		}
	}

	fun copy(): Area
	{
		val area = Area()
		area.grid = grid
		area.x = x
		area.y = y
		area.width = width
		area.height = height
		area.flipX = flipX
		area.flipY = flipY
		area.orientation = orientation
		area.xMode = xMode
		area.isPoints = isPoints
		area.points.addAll(points)

		return area
	}

	operator fun get(x: Int, y: Int): GrammarSymbol?
	{
		val (nx, ny) = localToWorld(x, y)
		return grid.tryGet(nx, ny, null)
	}

	fun localToWorld(x: Int, y: Int): Pos
	{
		val cx = this.x + width/2
		val cy = this.y + height/2

		val lx = x - width/2
		val ly = y - height/2

		vec.set(lx.toFloat(),ly.toFloat(), 0f)
		vec.mul(mat)

		var dx = Math.round(vec.x)
		var dy = Math.round(vec.y)

		if (flipX) dx *= -1
		if (flipY) dy *= -1

		return Pos(dx + cx, dy + cy)
	}
}

data class Pos(val x: Int, val y: Int)