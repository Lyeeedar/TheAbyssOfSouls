package com.lyeeedar.Renderables.Sprite

import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.ObjectSet
import com.lyeeedar.Util.random
import com.lyeeedar.Util.set

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

	private val upSprites = ObjectMap<String, Sprite>()
	private val downSprites = ObjectMap<String, Sprite>()
	private val availableAnimations = ObjectSet<String>()

	fun hasAnim(anim: String) = availableAnimations.contains(anim)

	fun addAnim(name: String, up: Sprite, down: Sprite)
	{
		if (availableAnimations.contains(name)) throw RuntimeException("Tried to add a duplicate animation for '$name'!")

		upSprites[name] = up
		downSprites[name] = down
		availableAnimations.add(name)
	}

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