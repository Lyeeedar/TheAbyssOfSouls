package com.lyeeedar.Util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import kotlin.coroutines.experimental.buildSequence

fun getXml(path: String, extension: String = "xml"): XmlReader.Element
{
	try
	{
		var filepath = path
		if (!filepath.endsWith("." + extension))
		{
			filepath += "." + extension
		}

		var handle = Gdx.files.internal(filepath)
		if (!handle.exists()) handle = Gdx.files.absolute(filepath)
		return XmlReader().parse(handle)
	}
	catch (ex: Exception)
	{
		System.err.println(ex.message)
		throw ex
	}
}

fun XmlReader.Element.ranChild() = this.getChild(Random.random(this.childCount-1))!!

fun XmlReader.Element.children(): Sequence<XmlReader.Element>
{
	val el = this
	return buildSequence {
		for (i in 0..el.childCount - 1)
		{
			yield(el.getChild(i))
		}
	}
}

operator fun XmlReader.Element.iterator(): Iterator<XmlReader.Element> = this.children().iterator()

fun XmlReader.Element.getChildrenByAttributeRecursively(attribute: String, value: String, result: Array<XmlReader.Element> = Array()): Array<XmlReader.Element>
{
	if (this.children().count() == 0) return result
	for (child in this.children())
	{
		if (child.getAttribute(attribute, null) == value) result.add(child)

		child.getChildrenByAttributeRecursively(attribute, value, result)
	}

	return result
}

fun XmlReader.Element.getChildrenRecursively(out: Array<XmlReader.Element> = Array()) : Array<XmlReader.Element>
{
	for (i in 0..this.childCount-1)
	{
		val el = getChild(i)
		out.add(el)
		el.getChildrenRecursively(out)
	}

	return out
}

fun XmlReader.Element.toCharGrid(): Array2D<Char>
{
	val grid = Array2D<Char>(this.getChild(0).text.length, this.childCount) {x, y -> ' '}

	for (y in 0..this.childCount-1)
	{
		val lineContents = this.getChild(y)
		for (x in 0..lineContents.text.length-1)
		{
			grid[x, y] = lineContents.text[x]
		}
	}

	return grid
}

fun XmlReader.Element.toHitPointArray(): com.badlogic.gdx.utils.Array<Point>
{
	val grid = this.toCharGrid()
	val center = Point(-1, -1)

	outer@ for (x in 0..grid.width-1)
	{
		for (y in 0..grid.height-1)
		{
			if (grid[x, y] == '@')
			{
				center.set(x, y)
				break@outer
			}
		}
	}

	if (center.x == -1 || center.y == -1)
	{
		center.x = grid.width/2
		center.y = grid.height/2
	}

	val hitPoints = Array<Point>()

	for (x in 0..grid.width-1)
	{
		for (y in 0..grid.height-1)
		{
			if (grid[x, y] == '#')
			{
				val dx = x - center.x
				val dy = center.y - y

				hitPoints.add(Point(dx, dy))
			}
		}
	}

	return hitPoints
}