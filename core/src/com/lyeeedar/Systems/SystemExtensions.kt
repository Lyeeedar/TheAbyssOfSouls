package com.lyeeedar.Systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.utils.reflect.ClassReflection
import com.lyeeedar.Level.Level
import kotlin.reflect.KClass

val systemList: Array<KClass<out EntitySystem>> = arrayOf(
		TaskProcessorSystem::class,
		TileSystem::class,
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
		this.sceneTimeline().level = value
		this.tile().level = value
	}

val EntitySystem.systemProcessingTime: Float
	get()
	{
		if (this is RenderSystem) return this.processDuration
		if (this is TaskProcessorSystem) return this.processDuration
		if (this is LightingSystem) return this.processDuration
		if (this is ShadowCastSystem) return this.processDuration
		if (this is TileSystem) return this.processDuration
		else return 0f
	}

fun Engine.render() = this.getSystem(RenderSystem::class.java)
fun Engine.task() = this.getSystem(TaskProcessorSystem::class.java)
fun Engine.lighting() = this.getSystem(LightingSystem::class.java)
fun Engine.shadowCast() = this.getSystem(ShadowCastSystem::class.java)
fun Engine.sceneTimeline() = this.getSystem(SceneTimelineSystem::class.java)
fun Engine.tile() = this.getSystem(TileSystem::class.java)