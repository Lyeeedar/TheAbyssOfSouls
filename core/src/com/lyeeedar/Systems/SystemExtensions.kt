package com.lyeeedar.Systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.utils.reflect.ClassReflection
import com.lyeeedar.AI.BehaviourTree.AbstractTreeNode
import kotlin.reflect.KClass

/**
 * Created by Philip on 30-Mar-16.
 */

val systemList: Array<KClass<out EntitySystem>> = arrayOf(
		TaskProcessorSystem::class,
		ShadowCastSystem::class,
		SpriteUpdaterSystem::class,
		EffectSystem::class,
		EventSystem::class,
		SoundSystem::class,
		CleanupSystem::class,
		LightingSystem::class,
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

fun Engine.render() = this.getSystem(RenderSystem::class.java)