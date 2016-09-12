package com.lyeeedar.Renderables

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.HDRColourSpriteBatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.*
import com.badlogic.gdx.utils.Array
import com.lyeeedar.BlendMode
import com.lyeeedar.Direction
import com.lyeeedar.Global
import com.lyeeedar.Renderables.Particle.ParticleEffect
import com.lyeeedar.Renderables.Particle.Emitter
import com.lyeeedar.Renderables.Particle.Particle
import com.lyeeedar.Renderables.Particle.ParticleData
import com.lyeeedar.Renderables.Sprite.TilingSprite
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Util.*
import java.util.*

/**
 * Created by Philip on 04-Jul-16.
 */

class SortedRenderer(var tileSize: Float, val width: Float, val height: Float, val layers: Int)
{
	var batchID: Int = 0

	val tempVec = Vector2()
	val tempPoint = Point()
	val tempCol = Colour()
	val bitflag = EnumBitflag<Direction>()
	val heap: BinaryHeap<RenderSprite> = BinaryHeap()
	var tilingMap: ObjectMap<Point, ObjectSet<Long>> = ObjectMap()

	val setPool: Pool<ObjectSet<Long>> = getPool()

	var screenShakeRadius: Float = 0f
	var screenShakeAccumulator: Float = 0f
	var screenShakeSpeed: Float = 0f
	var screenShakeAngle: Float = 0f

	val BLENDMODES = BlendMode.values().size
	val MAX_INDEX = 3 * BLENDMODES
	val X_BLOCK_SIZE = layers * MAX_INDEX
	val Y_BLOCK_SIZE = X_BLOCK_SIZE * width
	val MAX_Y_BLOCK_SIZE = Y_BLOCK_SIZE * height
	val MAX_X_BLOCK_SIZE = X_BLOCK_SIZE * width

	var delta: Float = 0f

	// ----------------------------------------------------------------------
	fun setScreenShake(amount: Float)
	{
		screenShakeRadius = amount
	}

	// ----------------------------------------------------------------------
	fun flush(deltaTime: Float, offsetx: Float, offsety: Float, batch: HDRColourSpriteBatch)
	{
		// do screen shake
		var offsetx = offsetx
		var offsety = offsety
		delta = deltaTime

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

		while (heap.size > 0)
		{
			val rs = heap.pop()

			batch.setBlendFunction(rs.blend.src, rs.blend.dst)

			batch.setColor(rs.colour)
			rs.sprite?.render(batch, rs.x + offsetx, rs.y + offsety, tileSize * rs.width, tileSize * rs.height )

			if (rs.tilingSprite != null)
			{
				bitflag.clear()
				for (dir in Direction.Values)
				{
					tempPoint.set(rs.point).plusAssign(dir)
					val keys = tilingMap[tempPoint]

					if (!(keys?.contains(rs.tilingSprite!!.checkID) ?: false))
					{
						bitflag.setBit(dir)
					}
				}

				val sprite = rs.tilingSprite!!.getSprite(bitflag)
				sprite.render(batch, rs.x + offsetx, rs.y + offsety, tileSize * rs.width, tileSize * rs.height )
			}

			if (rs.texture != null)
			{
				batch.draw(rs.texture, rs.x + offsetx, rs.y + offsety, 0.5f, 0.5f, 1f, 1f, tileSize * rs.width, tileSize * rs.height, rs.rotation, rs.flipX, rs.flipY)
			}

			rs.free()
		}

		batchID = random.nextInt()

		for (entry in tilingMap)
		{
			entry.key.free()
			setPool.free(entry.value)
		}
		tilingMap.clear()

		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
	}

	// ----------------------------------------------------------------------
	fun getComparisonVal(x: Float, y: Float, layer: Int, index: Int, blend: BlendMode) : Float
	{
		if (index > MAX_INDEX-1) throw RuntimeException("Index too high! $index >= $MAX_INDEX!")
		if (layer > layers-1) throw RuntimeException("Layer too high! $index >= $layers!")

		val sx = (x / tileSize).toInt()
		val sy = (y / tileSize).toInt()

		return MAX_Y_BLOCK_SIZE - sy * Y_BLOCK_SIZE + (MAX_X_BLOCK_SIZE - sx * X_BLOCK_SIZE) + layer * MAX_INDEX + index * BLENDMODES + blend.ordinal
	}

	// ----------------------------------------------------------------------
	fun queueParticle(effect: ParticleEffect, ix: Float, iy: Float, layer: Int, index: Int, colour: Colour = Colour.WHITE, width: Float = 1f, height: Float = 1f)
	{
		if (effect.batchID != batchID) effect.update(delta)
		effect.batchID = batchID

		if (!effect.visible) return
		if (effect.renderDelay > 0 && !effect.showBeforeRender)
		{
			return
		}

		val x = ix * tileSize
		val y = iy * tileSize

		//val scale = effect.animation?.renderScale()?.get(0) ?: 1f
		val animCol = effect.animation?.renderColour() ?: Colour.WHITE

		for (emitter in effect.emitters)
		{
			for (particle in emitter.particles)
			{
				var offsetx = x
				var offsety = y

				if (emitter.simulationSpace == Emitter.SimulationSpace.LOCAL)
				{
					tempVec.set(emitter.offset.valAt(0, emitter.time))
					tempVec.scl(emitter.size)
					tempVec.rotate(emitter.rotation)

					offsetx += (emitter.position.x + tempVec.x) * tileSize
					offsety += (emitter.position.y + tempVec.y) * tileSize
				}

				for (pdata in particle.particles)
				{
					val tex = particle.texture.valAt(pdata.texStream, pdata.life)
					val col = tempCol.set(particle.colour.valAt(pdata.colStream, pdata.life))
					col.a = particle.alpha.valAt(pdata.alphaStream, pdata.life)
					val size = particle.size.valAt(pdata.sizeStream, pdata.life).lerp(pdata.ranVal)
					var sizex = size * width
					var sizey = size * height

					if (particle.allowResize)
					{
						sizex *= emitter.size.x
						sizey *= emitter.size.y
					}

					val rotation = if (emitter.simulationSpace == Emitter.SimulationSpace.LOCAL) pdata.rotation + emitter.rotation + emitter.emitterRotation else pdata.rotation

					col.mul(colour).mul(animCol).mul(effect.colour)

					tempVec.set(pdata.position)

					if (emitter.simulationSpace == Emitter.SimulationSpace.LOCAL) tempVec.scl(emitter.size).rotate(emitter.rotation + emitter.emitterRotation)

					val drawx = tempVec.x * tileSize + offsetx
					val drawy = tempVec.y * tileSize + offsety

					val comparisonVal = getComparisonVal(drawx-sizex*0.5f*tileSize, drawy-sizey*0.5f*tileSize, layer, index, particle.blend)

					val rs = RenderSprite.obtain().set( null, null, tex, drawx, drawy, tempVec.x, tempVec.y, col, sizex, sizey, rotation, effect.flipX, effect.flipY, particle.blend, comparisonVal )

					heap.add( rs, rs.comparisonVal )
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	fun queueSprite(tilingSprite: TilingSprite, ix: Float, iy: Float, layer: Int, index: Int, colour: Colour = Colour.WHITE, width: Float = 1f, height: Float = 1f)
	{
		if (tilingSprite.batchID != batchID) tilingSprite.update(delta)
		tilingSprite.batchID = batchID

		if (!tilingSprite.visible) return
		if (tilingSprite.renderDelay > 0 && !tilingSprite.showBeforeRender)
		{
			return
		}

		var x = ix * tileSize
		var y = iy * tileSize

		if ( tilingSprite.animation != null )
		{
			val offset = tilingSprite.animation?.renderOffset()

			if (offset != null)
			{
				x += offset[0] * tileSize
				y += offset[1] * tileSize
			}
		}

		val comparisonVal = getComparisonVal(x, y, layer, index, BlendMode.MULTIPLICATIVE)

		val rs = RenderSprite.obtain().set( null, tilingSprite, null, x, y, ix, iy, colour, width, height, 0f, false, false, BlendMode.MULTIPLICATIVE, comparisonVal )

		val point = Point.obtain().set(ix.toInt(), iy.toInt())
		var keys = tilingMap[point]
		if (keys == null)
		{
			keys = setPool.obtain()
			keys.clear()
		}
		keys.add(rs.tilingSprite!!.checkID)
		tilingMap[point] = keys

		heap.add( rs, rs.comparisonVal )
	}

	// ----------------------------------------------------------------------
	fun queueSprite(sprite: Sprite, ix: Float, iy: Float, layer: Int, index: Int, colour: Colour = Colour.WHITE, update: Boolean = true, width: Float = 1f, height: Float = 1f)
	{
		if (update)
		{
			if (sprite.batchID != batchID) sprite.update(delta)
		}
		sprite.batchID = batchID

		if (!sprite.visible) return
		if (sprite.renderDelay > 0 && !sprite.showBeforeRender)
		{
			return
		}

		var x = ix * tileSize
		var y = iy * tileSize

		if ( sprite.animation != null )
		{
			val offset = sprite.animation?.renderOffset()

			if (offset != null)
			{
				x += offset[0] * tileSize
				y += offset[1] * tileSize
			}
		}

		val comparisonVal = getComparisonVal(x, y, layer, index, BlendMode.MULTIPLICATIVE)

		val rs = RenderSprite.obtain().set( sprite, null, null, x, y, ix, iy, colour, width, height, 0f, false, false, BlendMode.MULTIPLICATIVE, comparisonVal )

		heap.add( rs, rs.comparisonVal )
	}

	// ----------------------------------------------------------------------
	companion object
	{
		val random = Random()
	}
}

// ----------------------------------------------------------------------
class RenderSprite : BinaryHeap.Node(0f)
{
	val point = Point()
	val colour: Colour = Colour(1f, 1f, 1f, 1f)
	var sprite: Sprite? = null
	var tilingSprite: TilingSprite? = null
	var texture: TextureRegion? = null
	var x: Float = 0f
	var y: Float = 0f
	var width: Float = 1f
	var height: Float = 1f
	var rotation: Float = 0f
	var flipX: Boolean = false
	var flipY: Boolean = false
	var blend: BlendMode = BlendMode.MULTIPLICATIVE

	var comparisonVal: Float = 0f

	// ----------------------------------------------------------------------
	operator fun set(sprite: Sprite?, tilingSprite: TilingSprite?, texture: TextureRegion?,
					 x: Float, y: Float,
					 ix: Float, iy: Float,
					 colour: Colour,
					 width: Float, height: Float,
					 rotation: Float,
					 flipX: Boolean, flipY: Boolean,
					 blend: BlendMode,
					 comparisonVal: Float): RenderSprite
	{
		point.set(ix.toInt(), iy.toInt())
		this.colour.set(colour)
		this.sprite = sprite
		this.tilingSprite = tilingSprite
		this.texture = texture
		this.x = x
		this.y = y
		this.width = width
		this.height = height
		this.comparisonVal = comparisonVal
		this.blend = blend
		this.rotation = rotation
		this.flipX = flipX
		this.flipY = flipY

		return this
	}

	// ----------------------------------------------------------------------
	fun free() = pool.free(this)

	// ----------------------------------------------------------------------
	companion object
	{
		val pool: Pool<RenderSprite> = Pools.get( RenderSprite::class.java, Int.MAX_VALUE )
		fun obtain() = pool.obtain()
	}
}
