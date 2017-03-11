package com.lyeeedar.Util

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.kryo.FastEnumMapSerializer
import com.lyeeedar.Renderables.Sprite.Sprite
import ktx.collections.set

fun Kryo.registerLyeeedarSerialisers()
{
	val kryo = this

	kryo.register(FastEnumMap::class.java, FastEnumMapSerializer())

	kryo.register(Sprite::class.java, object : Serializer<Sprite>()
	{
		override fun read(kryo: Kryo, input: Input, type: Class<Sprite>): Sprite
		{
			val fileName = input.readString()
			val animDelay = input.readFloat()
			val repeatDelay = input.readFloat()
			val colour = kryo.readObject(input, Colour::class.java)
			val modeVal = input.readInt()
			val mode = Sprite.AnimationMode.values()[modeVal]
			val scale = input.readFloats(2)
			val drawActualSize = input.readBoolean()

			val sprite = AssetManager.loadSprite(fileName, animDelay, colour, mode, drawActualSize)
			sprite.baseScale = scale
			sprite.repeatDelay = repeatDelay
			return sprite
		}

		override fun write(kryo: Kryo, output: Output, sprite: Sprite)
		{
			output.writeString(sprite.fileName)
			output.writeFloat(sprite.animationDelay)
			output.writeFloat(sprite.repeatDelay)
			kryo.writeObject(output, sprite.colour)
			output.writeInt(sprite.animationState.mode.ordinal)
			output.writeFloats(sprite.baseScale)
			output.writeBoolean(sprite.drawActualSize)
		}
	})

	kryo.register(Point::class.java, object : Serializer<Point>()
	{
		override fun read(kryo: Kryo, input: Input, type: Class<Point>): Point
		{
			val x = input.readInt()
			val y = input.readInt()

			return Point.obtain().set(x, y)
		}

		override fun write(kryo: Kryo, output: Output, point: Point)
		{
			output.writeInt(point.x)
			output.writeInt(point.y)
		}
	})

	kryo.register(Colour::class.java, object : Serializer<Colour>()
	{
		override fun read(kryo: Kryo, input: Input, type: Class<Colour>): Colour
		{
			val r = input.readFloat()
			val g = input.readFloat()
			val b = input.readFloat()
			val a = input.readFloat()

			return Colour(r, g, b, a)
		}

		override fun write(kryo: Kryo, output: Output, colour: Colour)
		{
			output.writeFloat(colour.r)
			output.writeFloat(colour.g)
			output.writeFloat(colour.b)
			output.writeFloat(colour.a)
		}
	})

	kryo.register(Array2D::class.java, object : Serializer<Array2D<*>>()
	{
		override fun write(kryo: Kryo, output: Output, `object`: Array2D<*>)
		{
			output.writeInt(`object`.width)
			output.writeInt(`object`.height)
			for (x in 0..`object`.width-1)
			{
				for (y in 0..`object`.height-1)
				{
					kryo.writeClassAndObject(output, `object`[x, y])
				}
			}
		}

		override fun read(kryo: Kryo, input: Input, type: Class<Array2D<*>>): Array2D<*>
		{
			val width = input.readInt()
			val height = input.readInt()

			val grid = Array2D<Any>(width, height)
			kryo.reference(grid)

			for (x in 0..width-1)
			{
				for (y in 0..height-1)
				{
					val obj = kryo.readClassAndObject(input)
					grid[x, y] = obj
				}
			}

			return grid
		}

	})
}

fun Kryo.registerGdxSerialisers()
{
	val kryo = this

	kryo.register(Array::class.java, object : Serializer<Array<*>>()
	{
		override fun read(kryo: Kryo, input: Input, type: Class<Array<*>>): Array<*>
		{
			val array = Array<Any>()
			kryo.reference(array)

			val length = input.readInt(true)
			array.ensureCapacity(length)

			for (i in 0..length-1)
			{
				val obj = kryo.readClassAndObject(input)
				array.add(obj)
			}

			return array
		}

		override fun write(kryo: Kryo, output: Output, array: Array<*>)
		{
			val length = array.size
			output.writeInt(length, true)

			for (i in 0..length-1)
			{
				kryo.writeClassAndObject(output, array[i])
			}
		}
	})

	kryo.register(ObjectMap::class.java, object : Serializer<ObjectMap<*, *>>()
	{
		override fun read(kryo: Kryo, input: Input, type: Class<ObjectMap<*, *>>): ObjectMap<*, *>
		{
			val map = ObjectMap<Any, Any>()
			kryo.reference(map)

			val length = input.readInt(true)
			map.ensureCapacity(length)

			for (i in 0..length-1)
			{
				val key = kryo.readClassAndObject(input)
				val value = kryo.readClassAndObject(input)

				map[key] = value
			}

			return map
		}

		override fun write(kryo: Kryo, output: Output, map: ObjectMap<*, *>)
		{
			val length = map.size
			output.writeInt(length, true)

			for (entry in map)
			{
				kryo.writeClassAndObject(output, entry.key)
				kryo.writeClassAndObject(output, entry.value)
			}
		}
	})

	kryo.register(ObjectFloatMap::class.java, object : Serializer<ObjectFloatMap<*>>()
	{
		override fun read(kryo: Kryo, input: Input, type: Class<ObjectFloatMap<*>>): ObjectFloatMap<*>
		{
			val map = ObjectFloatMap<Any>()
			kryo.reference(map)

			val length = input.readInt(true)
			map.ensureCapacity(length)

			for (i in 0..length-1)
			{
				val key = kryo.readClassAndObject(input)
				val value = input.readFloat()

				map.put(key, value)
			}

			return map
		}

		override fun write(kryo: Kryo, output: Output, map: ObjectFloatMap<*>)
		{
			val length = map.size
			output.writeInt(length, true)

			for (entry in map)
			{
				kryo.writeClassAndObject(output, entry.key)
				output.writeFloat(entry.value)
			}
		}
	})

	kryo.register(XmlReader.Element::class.java, object : Serializer<XmlReader.Element>()
	{
		override fun read(kryo: Kryo, input: Input, type: Class<XmlReader.Element>): XmlReader.Element
		{
			val xml = input.readString()

			try
			{
				val reader = XmlReader()
				val element = reader.parse(xml)
				return element
			}
			catch (ex: Exception)
			{
				return XmlReader.Element("", null)
			}
		}

		override fun write(kryo: Kryo, output: Output, element: XmlReader.Element)
		{
			output.writeString(element.toString())
		}
	})
}
