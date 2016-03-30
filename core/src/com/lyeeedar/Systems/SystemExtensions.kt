package com.lyeeedar.Systems

import com.badlogic.ashley.core.EntitySystem
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
		CleanupSystem::class,
		LightingSystem::class,
		RenderSystem::class
)