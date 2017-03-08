package com.lyeeedar.Components

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.lyeeedar.Sin
import com.lyeeedar.Util.FastEnumMap

class SinComponent : AbstractComponent()
{
	var sins = FastEnumMap<Sin, Int>(Sin::class.java)

	override fun parse(xml: XmlReader.Element, entity: Entity)
	{

	}

	override fun saveData(kryo: Kryo, output: Output)
	{
		kryo.writeObject(output, sins)
	}

	override fun loadData(kryo: Kryo, input: Input)
	{
		sins = kryo.readObject(input, FastEnumMap::class.java) as FastEnumMap<Sin, Int>
	}
}
