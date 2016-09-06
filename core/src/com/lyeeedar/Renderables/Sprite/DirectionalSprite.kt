package com.lyeeedar.Renderables.Sprite

import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.ObjectSet
import com.lyeeedar.Util.random

class DirectionalSprite
{
	enum class VDir
	{
		UP,
		DOWN
	}

	enum class HDir
	{
		LEFT,
		RIGHT
	}

	val upSprites = ObjectMap<String, Sprite>()
	val downSprites = ObjectMap<String, Sprite>()

	val availableAnimations = ObjectSet<String>()

	val idles = ObjectSet<String>()

	fun hasAnim(anim: String) = availableAnimations.contains(anim)

	fun getIdle(v: VDir, h: HDir) = getSprite(idles.asSequence().random() ?: "idle", v, h)

	fun getSprite(anim: String, v: VDir, h: HDir): Sprite
	{
		val map = when (v)
		{
			VDir.UP -> upSprites
			VDir.DOWN -> downSprites
			else -> throw NotImplementedError()
		}

		val sprite = map[anim] ?: throw RuntimeException("Failed to find direction sprite for $anim!")

		if (h == HDir.LEFT)
		{
			sprite.flipX = true
		}
		else
		{
			sprite.flipX = false
		}

		return sprite
	}
}