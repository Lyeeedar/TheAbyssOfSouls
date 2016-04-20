package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.lyeeedar.Sound.SoundInstance

/**
 * Created by Philip on 20-Apr-16.
 */

class SoundComponent: Component
{
	constructor()

	val ambientSounds: com.badlogic.gdx.utils.Array<SoundInstance> = com.badlogic.gdx.utils.Array()
	var continuous: com.badlogic.gdx.utils.Array<SoundInstance> = com.badlogic.gdx.utils.Array()
	val shoutSounds: com.badlogic.gdx.utils.Array<SoundInstance> = com.badlogic.gdx.utils.Array()
}
