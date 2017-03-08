package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output

abstract class AbstractComponent : Component
{
	var fromLoad = false

	abstract fun parse(xml: XmlReader.Element, entity: Entity)

	open fun saveData(kryo: Kryo, output: Output)
	{

	}

	open fun loadData(kryo: Kryo, input: Input)
	{

	}
}