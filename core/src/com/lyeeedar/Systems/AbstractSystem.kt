package com.lyeeedar.Systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.lyeeedar.Components.*
import com.lyeeedar.Level.Level
import com.lyeeedar.Util.Colour

abstract class AbstractSystem() : EntitySystem()
{
	init
	{
		if (!systemList.contains(javaClass.kotlin)) throw Exception("System not registered in system list!")

		priority = systemList.indexOf(javaClass.kotlin)
	}

	constructor(family: Family) : this()
	{
		this.family = family
	}

	var family: Family? = null
	lateinit var entities: ImmutableArray<Entity>

	var level: Level? = null
		get() = field
		set(value)
		{
			field = value

			onLevelChanged()
		}

	var processDuration: Float = 0f

	override fun addedToEngine(engine: Engine?)
	{
		if (family != null)
		{
			entities = engine?.getEntitiesFor(family) ?: throw RuntimeException("Engine is null!")
		}
	}

	override fun update(deltaTime: Float)
	{
		val start = System.nanoTime()

		doUpdate(deltaTime)

		val end = System.nanoTime()
		val diff = (end - start) / 1000000000f

		processDuration = (processDuration + diff) / 2f
	}

	abstract fun doUpdate(deltaTime: Float)

	open fun onLevelChanged()
	{

	}
}