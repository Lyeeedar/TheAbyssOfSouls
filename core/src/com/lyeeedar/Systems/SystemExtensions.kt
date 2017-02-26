package com.lyeeedar.Systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.utils.reflect.ClassReflection
import com.lyeeedar.Level.Level
import kotlin.reflect.KClass

val systemList: Array<KClass<out AbstractSystem>> = arrayOf(
		TaskProcessorSystem::class,
		StatisticsSystem::class,
		TileSystem::class,
		SceneTimelineSystem::class,
		DeletionSystem::class,
		ShadowCastSystem::class,
		LightingSystem::class,
		DirectionalSpriteSystem::class,
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
		for (system in systemList)
		{
			this.getSystem(system.java).level = value
		}
	}

fun Engine.render() = this.getSystem(RenderSystem::class.java)
fun Engine.task() = this.getSystem(TaskProcessorSystem::class.java)
fun Engine.lighting() = this.getSystem(LightingSystem::class.java)
fun Engine.shadowCast() = this.getSystem(ShadowCastSystem::class.java)
fun Engine.sceneTimeline() = this.getSystem(SceneTimelineSystem::class.java)
fun Engine.tile() = this.getSystem(TileSystem::class.java)