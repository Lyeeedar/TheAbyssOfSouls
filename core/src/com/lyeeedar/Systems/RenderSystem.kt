package com.lyeeedar.Systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.HDRColourSpriteBatch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.BinaryHeap
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools

import com.lyeeedar.Components.*
import com.lyeeedar.Direction
import com.lyeeedar.Global
import com.lyeeedar.Level.Tile
import com.lyeeedar.Renderables.Animation.MoveAnimation
import com.lyeeedar.Renderables.SortedRenderer
import com.lyeeedar.Renderables.Sprite.DirectionalSprite
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.Colour
import com.lyeeedar.Util.EnumBitflag
import com.lyeeedar.Util.Point
import com.lyeeedar.Util.abs

/**
 * Created by Philip on 20-Mar-16.
 */

class RenderSystem(): EntitySystem(systemList.indexOf(RenderSystem::class))
{
	val batchHDRColour: HDRColourSpriteBatch = HDRColourSpriteBatch()
	lateinit var entities: ImmutableArray<Entity>

	var screenShakeRadius: Float = 0f
	var screenShakeAccumulator: Float = 0f
	var screenShakeSpeed: Float = 0f
	var screenShakeAngle: Float = 0f

	val tileSize = 32f

	val renderer = SortedRenderer(tileSize, 200f, 200f, SpaceSlot.Values.size)

	override fun addedToEngine(engine: Engine?)
	{
		entities = engine?.getEntitiesFor(Family.all(PositionComponent::class.java).one(SpriteComponent::class.java, TilingSpriteComponent::class.java, EffectComponent::class.java, ParticleComponent::class.java).get()) ?: throw RuntimeException("Engine is null!")
	}

	override fun update(deltaTime: Float)
	{
		val player = Global.currentLevel.player
		val playerPos = Mappers.position.get(player)
		val playerSprite = Mappers.sprite.get(player)

		for (entity in entities)
		{
			val pos = Mappers.position.get(entity) ?: continue
			val tile = entity.tile() ?: continue
			//if (!tile.seen) continue

			if (!tile.visible)
			{
				// dont draw dynamic entities on non visible tiles
				//val task = Mappers.task.get(entity)
				//if (task != null) continue
			}

			val px = pos.position.x.toFloat()
			val py = pos.position.y.toFloat()

			val sprite = Mappers.sprite.get(entity)
			val tilingSprite = Mappers.tilingSprite.get(entity)
			val effect = Mappers.effect.get(entity)
			val particle = Mappers.particle.get(entity)

			if (sprite != null)
			{
				sprite.sprite.size[0] = pos.size
				sprite.sprite.size[1] = pos.size

				renderer.queueSprite(sprite.sprite, px, py, pos.slot.ordinal, 0, tile.light)
			}

			if (tilingSprite != null)
			{
				renderer.queueSprite(tilingSprite.sprite, px, py, pos.slot.ordinal, 0, tile.light)
			}

			if (effect != null)
			{
				effect.sprite.size[0] = pos.max.x - pos.min.x + 1
				effect.sprite.size[1] = pos.max.y - pos.min.y + 1

				if (effect.direction == Direction.EAST || effect.direction == Direction.WEST)
				{
					val temp = effect.sprite.size[0]
					effect.sprite.size[0] = effect.sprite.size[1]
					effect.sprite.size[1] = temp

					effect.sprite.fixPosition = true
				}

				effect.sprite.rotation = effect.direction.angle

				renderer.queueSprite(effect.sprite, px, py, SpaceSlot.AIR.ordinal, 0, tile.light)
			}

			if (particle != null)
			{
				renderer.queueParticle(particle.particleEffect, px, py, SpaceSlot.AIR.ordinal, 0, tile.light)
			}
		}

		var offsetx = Global.resolution[ 0 ] / 2 - playerPos.position.x * tileSize - tileSize / 2
		var offsety = Global.resolution[ 1 ] / 2 - playerPos.position.y * tileSize - tileSize / 2

		val offset = playerSprite.sprite.animation?.renderOffset()
		if (offset != null)
		{
			offsetx -= offset[0] * tileSize
			offsety -= offset[1] * tileSize
		}

		// do screen shake
		if ( screenShakeRadius > 2 )
		{
			screenShakeAccumulator += deltaTime
			while ( screenShakeAccumulator >= screenShakeSpeed )
			{
				screenShakeAccumulator -= screenShakeSpeed
				screenShakeAngle += ( 150 + MathUtils.random() * 60 )
				screenShakeRadius *= 0.9f
			}

			offsetx += Math.sin( screenShakeAngle.toDouble() ).toFloat() * screenShakeRadius
			offsety += Math.cos( screenShakeAngle.toDouble() ).toFloat() * screenShakeRadius
		}

		batchHDRColour.begin()
		renderer.flush(deltaTime, offsetx, offsety, batchHDRColour)
		batchHDRColour.end()
	}
}