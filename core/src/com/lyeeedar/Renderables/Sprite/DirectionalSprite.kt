package com.lyeeedar.Renderables.Sprite

import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.ObjectSet
import com.lyeeedar.Renderables.Renderable
import com.lyeeedar.Util.random
import ktx.collections.get
import ktx.collections.set

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

	var size: Int
		get() = -1
		set(value)
		{
			for (up in upSprites)
			{
				up.value.size[0] = value
				up.value.size[1] = value
			}

			for (down in downSprites)
			{
				down.value.size[0] = value
				down.value.size[1] = value
			}
		}

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

		sprite.flipX = h == HDir.LEFT

		return sprite
	}
}