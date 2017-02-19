package com.lyeeedar.Util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader

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

fun XmlReader.Element.ranChild() = this.getChild(MathUtils.random(this.childCount-1))!!

fun XmlReader.Element.children(): Array<XmlReader.Element>
{
	val els = Array<XmlReader.Element>()

	for (i in 0..this.childCount-1)
	{
		els.add(this.getChild(i))
	}

	return els
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