package com.lyeeedar.Systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.utils.reflect.ClassReflection
import com.lyeeedar.Level.Level
import kotlin.reflect.KClass

val systemList: Array<KClass<out EntitySystem>> = arrayOf(
		TaskProcessorSystem::class,
		DirectionalSpriteSystem::class,
		ShadowCastSystem::class,
		LightingSystem::class,
		SceneTimelineSystem::class,
		RenderSystem::class
)

fun createEngine(): Engine
{
	val engine = Engine()

	for (system in systemList)
	{
		val instance: EntitySystem = ClassReflection.newInstance(system.java)
		engine.addSystem(instance)
	}

	return engine
}

var Engine.level: Level?
	get() = this.render().level
	set(value)
	{
		this.render().level = value
		this.task().level = value
		this.lighting().level = value
		this.shadowCast().level = value
	}

fun Engine.render() = this.getSystem(RenderSystem::class.java)
fun Engine.task() = this.getSystem(TaskProcessorSystem::class.java)
fun Engine.lighting() = this.getSystem(LightingSystem::class.java)
fun Engine.shadowCast() = this.getSystem(ShadowCastSystem::class.java)