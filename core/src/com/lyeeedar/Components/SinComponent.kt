package com.lyeeedar.Components

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.lyeeedar.Sin
import com.lyeeedar.Util.FastEnumMap
import com.lyeeedar.Util.Random

class SinComponent : AbstractComponent()
{
	var sins = FastEnumMap<Sin, Int>(Sin::class.java)

	init
	{
		for (sin in Sin.Values)
		{
			sins[sin] = 1+Random.random(2)
		}
	}

	override fun parse(xml: XmlReader.Element, entity: Entity)
	{

	}

	override fun saveData(kryo: Kryo, output: Output)
	{
		kryo.writeClassAndObject(output, sins)
	}

	override fun loadData(kryo: Kryo, input: Input)
	{
		sins = kryo.readClassAndObject(input) as FastEnumMap<Sin, Int>
	}
}
