package com.lyeeedar.Util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.*
import com.badlogic.gdx.utils.Array
import java.util.*

/**
 * Created by Philip on 04-Jul-16.
 */

class Smoothstep() : Interpolation()
{
	override fun apply(a: Float): Float = a * a * ( 3f - 2f * a )
}
val smoothStep = Smoothstep()

class Leap() : Interpolation()
{
	override fun apply(a: Float): Float
	{
		var t = a

		if (t <= 0.5f) return 2.0f * t * (1.0f - t)

		t -= 0.5f

		return 2.0f * t * t + 0.5f
	}
}
val leap = Leap()

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

fun getXml(path: String): XmlReader.Element
{
	var handle = Gdx.files.internal("$path.xml")
	if (!handle.exists()) handle = Gdx.files.absolute(path)
	return XmlReader().parse(handle)
}

inline fun <reified T : Any> getPool(): Pool<T> = Pools.get(T::class.java, Int.MAX_VALUE)

fun Actor.addClickListener(func: () -> Unit)
{
	this.addListener(object : ClickListener() {
		override fun clicked(event: InputEvent?, x: Float, y: Float)
		{
			super.clicked(event, x, y)
			func()
		}
	})
}

fun <T> com.badlogic.gdx.utils.Array<T>.tryGet(i: Int): T = this[MathUtils.clamp(i, 0, this.size-1)]
fun <T> com.badlogic.gdx.utils.Array<T>.random(ran: Random): T = this[ran.nextInt(this.size)]
fun <T> com.badlogic.gdx.utils.Array<T>.removeRandom(ran: Random): T
{
	val index = ran.nextInt(this.size)
	val item = this[index]
	this.removeIndex(index)

	return item
}
fun <T> com.badlogic.gdx.utils.Array<T>.addAll(collection: Sequence<T>)
{
	for (item in collection) this.add(item)
}

fun vectorToAngle(x: Float, y: Float) : Float
{
	// basis vector 0,1
	val dot = (0 * x + 1 * y).toDouble() // dot product
	val det = (0 * y - 1 * x).toDouble() // determinant
	val angle = Math.atan2(det, dot).toFloat() * MathUtils.radiansToDegrees

	return angle
}

fun getRotation(p1: Point, p2: Point) : Float
{
	val vec = Pools.obtain(Vector2::class.java)
	vec.x = (p2.x - p1.x).toFloat()
	vec.y = (p2.y - p1.y).toFloat()
	vec.nor()

	val angle = vectorToAngle(vec.x, vec.y)

	Pools.free(vec)

	return angle
}

fun getRotation(p1: Vector2, p2: Vector2) : Float
{
	val vec = Pools.obtain(Vector2::class.java)
	vec.x = (p2.x - p1.x).toFloat()
	vec.y = (p2.y - p1.y).toFloat()
	vec.nor()

	val angle = vectorToAngle(vec.x, vec.y)

	Pools.free(vec)

	return angle
}

fun error(message: String) { System.err.println(message) }

fun Float.abs() = Math.abs(this)
fun Float.ciel() = MathUtils.ceil(this)
fun Float.floor() = MathUtils.floor(this)

fun String.neaten() = this.substring(0, 1).toUpperCase() + this.substring(1).toLowerCase()

operator fun <V> IntMap<V>.set(key: Int, value: V) = this.put(key, value)

operator fun IntIntMap.get(key: Int) = this.get(key, 0)
operator fun IntIntMap.set(key: Int, value: Int) = this.put(key, value)
operator fun <K, V> ObjectMap<K, V>.set(key: K, value: V) = this.put(key, value)

fun <T> Sequence<T>.random() = if (this.count() > 0) this.elementAt(MathUtils.random(this.count()-1)) else null
fun <T> Sequence<T>.random(ran: Random) = if (this.count() > 0) this.elementAt(ran.nextInt(this.count())) else null
inline fun <reified T> Sequence<T>.random(num: Int): Sequence<T>
{
	val array = Array<T>()
	for (item in this) array.add(item)

	val outArray = Array<T>()
	for (i in 0..num-1)
	{
		if (array.size == 0) break
		outArray.add(array.removeRandom(MathUtils.random))
	}

	return outArray.asSequence()
}

fun Color.toHSV(out: FloatArray? = null): FloatArray
{
	val max = Math.max(this.r, Math.max(this.g, this.b))
	val min = Math.min(this.r, Math.min(this.g, this.b))
	val delta = max - min

	val saturation = if (delta == 0f) 0f else delta / max
	val hue = if (this.r == max) ((this.g - this.b) / delta) % 6
				else if (this.g == max) 2 + (this.b - this.r) / delta
					else 4 + (this.r - this.g) / delta
	val value = max

	val output = if (out != null && out.size >= 3) out else kotlin.FloatArray(3)
	output[0] = (hue * 60f) / 360f
	output[1] = saturation
	output[2] = value

	return output
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