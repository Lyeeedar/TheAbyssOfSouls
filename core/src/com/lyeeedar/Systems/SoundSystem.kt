package com.lyeeedar.Systems

import com.badlogic.ashley.core.EntitySystem
import com.lyeeedar.Sound.SoundGroup

/**
 * Created by Philip on 20-Apr-16.
 */

class SoundSystem(): EntitySystem(systemList.indexOf(SoundSystem::class))
{
	override fun update(deltaTime: Float)
	{
		SoundGroup.update(deltaTime)
	}
}