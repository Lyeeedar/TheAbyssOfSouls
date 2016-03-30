package com.lyeeedar.Systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.lyeeedar.Components.*
import com.lyeeedar.Enums
import com.lyeeedar.GlobalData
import com.lyeeedar.Sprite.SpriteAnimation.MoveAnimation

/**
 * Created by Philip on 21-Mar-16.
 */

class LightingSystem(): EntitySystem(systemList.indexOf(LightingSystem::class))
{
	lateinit var posLightEntities: ImmutableArray<Entity>

	override fun addedToEngine(engine: Engine?)
	{
		val posLight = Family.all(PositionComponent::class.java, LightComponent::class.java).get()

		posLightEntities = engine?.getEntitiesFor(posLight) ?: throw RuntimeException("Engine is null!")
	}

	override fun update(deltaTime: Float)
	{
		val level = posLightEntities[0].tile()?.level ?: return
		for (x in 0.. level.width-1)
		{
			for (y in 0..level.height-1)
			{
				level.getTile(x, y)?.lights?.clear()
			}
		}

		for (entity in posLightEntities)
		{
			val pos = Mappers.position.get(entity)
			val light = Mappers.light.get(entity)
			val offset = entity.renderOffset()

			var x = pos.position.x.toFloat() * GlobalData.Global.tileSize + GlobalData.Global.tileSize / 2f
			var y = pos.position.y.toFloat() * GlobalData.Global.tileSize + GlobalData.Global.tileSize / 2f

			if (offset != null)
			{
				x += offset[0]
				y += offset[1]
			}

			light.x = x
			light.y = y

			val shadowCast = light.cache.currentShadowCast
			for (point in shadowCast)
			{
				for (dir in Enums.Direction.values())
				{
					val tile = level.getTile(point, dir) ?: continue

					var lightData: LightDataWrapper? = null
					for (ld in tile.lights)
					{
						if (ld.light == light)
						{
							lightData = ld
						}
					}

					if (lightData == null)
					{
						lightData = LightDataWrapper(light, FloatArray(4))
						tile.lights.add(lightData)
					}

					val corners = cornerMap[dir] ?: continue
					for (corner in corners)
					{
						lightData.corners[corner] = 1f
					}
				}
			}
		}
	}
}

// 0 : bottom left
// 1 : top left
// 2 : top right
// 3 : bottom right

private val cornerMap: Map<Enums.Direction, IntArray> = mapOf(
		Enums.Direction.CENTER to intArrayOf(0, 1, 2, 3),
		Enums.Direction.NORTH to intArrayOf(0, 3),
		Enums.Direction.NORTHEAST to intArrayOf(0),
		Enums.Direction.EAST to intArrayOf(0, 1),
		Enums.Direction.SOUTHEAST to intArrayOf(1),
		Enums.Direction.SOUTH to intArrayOf(1, 2),
		Enums.Direction.SOUTHWEST to intArrayOf(2),
		Enums.Direction.WEST to intArrayOf( 2, 3),
		Enums.Direction.NORTHWEST to intArrayOf(3)
)

class LightDataWrapper
{
	val light: LightComponent
	val corners: FloatArray

	constructor(light: LightComponent, corners: FloatArray)
	{
		this.light = light
		this.corners = corners
	}
}