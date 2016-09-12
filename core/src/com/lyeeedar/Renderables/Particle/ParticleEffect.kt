package com.lyeeedar.Renderables.Particle

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.HDRColourSpriteBatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Renderables.Renderable
import com.lyeeedar.Util.Array2D
import com.lyeeedar.Util.Colour
import com.lyeeedar.Util.getXml

/**
 * Created by Philip on 14-Aug-16.
 */

class ParticleEffect : Renderable()
{
	private lateinit var loadPath: String

	var colour: Colour = Colour(Color.WHITE)
	var speedMultiplier: Float = 1f

	var completed = false
	var killOnAnimComplete = true
	private var warmupTime = 0f
	private var doneWarmup = false
	var moveSpeed: Float = 1f
	val emitters = Array<Emitter>()

	// local stuff
	val position = Vector2()
	var rotation: Float = 0f
	var sizex: Float = 1f
	var sizey: Float = 1f
	var flipX: Boolean = false
	var flipY: Boolean = false

	var collisionGrid: Array2D<Boolean>? = null
	var collisionFun: ((x: Int, y: Int) -> Unit)? = null

	val lifetime: Float
		get() = (animation?.duration() ?: emitters.maxBy { it.lifetime() }!!.lifetime()) * (1f / speedMultiplier)

	fun start()
	{
		for (emitter in emitters)
		{
			emitter.time = 0f
			emitter.start()
		}
	}

	fun stop()
	{
		for (emitter in emitters) emitter.stop()
	}

	override fun doUpdate(delta: Float): Boolean
	{
		var complete = false

		if (moveSpeed == 0f)
		{
			animation?.free()
			animation = null
		}
		else
		{
			complete = animation?.update(delta * speedMultiplier) ?: false
			if (complete)
			{
				if (killOnAnimComplete) stop()
				animation?.free()
				animation = null
			}
		}

		val posOffset = animation?.renderOffset()
		val x = position.x + (posOffset?.get(0) ?: 0f)
		val y = position.y + (posOffset?.get(1) ?: 0f)

		val scale = animation?.renderScale()
		val sx = sizex * (scale?.get(0) ?: 1f)
		val sy = sizey * (scale?.get(1) ?: 1f)

		for (emitter in emitters)
		{
			emitter.rotation = rotation
			emitter.position.set(x, y)
			emitter.size.x = sx
			emitter.size.y = sy
		}

		if (warmupTime > 0f && !doneWarmup)
		{
			doneWarmup = true
			val deltaStep = 1f / 15f // simulate at 15 fps
			val steps = (warmupTime / deltaStep).toInt()
			for (i in 0..steps-1)
			{
				for (emitter in emitters) emitter.update(deltaStep, collisionGrid)
			}
		}

		for (emitter in emitters) emitter.update(delta * speedMultiplier, collisionGrid)

		if (collisionFun != null)
		{
			for (emitter in emitters) emitter.callCollisionFunc(collisionFun!!)
		}

		if (animation == null)
		{
			if (complete())
			{
				for (emitter in emitters) emitter.time = 0f
				complete = true
				completed = true
			}
			else
			{
				complete = false
			}
		}

		return complete
	}

	fun complete() = emitters.firstOrNull{ !it.complete() } == null

	fun setPosition(x: Float, y: Float)
	{
		position.set(x, y)
	}

	override fun doRender(batch: HDRColourSpriteBatch, x: Float, y: Float, tileSize: Float)
	{

	}

	fun debug(shape: ShapeRenderer, offsetx: Float, offsety: Float, tileSize: Float)
	{
		val posOffset = animation?.renderOffset()
		val x = position.x + (posOffset?.get(0) ?: 0f)
		val y = position.y + (posOffset?.get(1) ?: 0f)

		val worldx = x * tileSize + offsetx
		val worldy = y * tileSize + offsety

		// draw effect center
		shape.color = Color.CYAN
		shape.rect(worldx - 5f, worldy - 5f,10f, 10f)

		val temp = Pools.obtain(Vector2::class.java)

		// draw emitter volumes
		shape.color = Color.GOLDENROD
		for (emitter in emitters)
		{
			val emitterx = emitter.position.x * tileSize + offsetx
			val emittery = emitter.position.y * tileSize + offsety

			temp.set(emitter.offset.valAt(0, emitter.time))
			temp.scl(emitter.size)
			temp.rotate(emitter.rotation)

			val ex = emitterx + temp.x * tileSize
			val ey = emittery + temp.y * tileSize

			val w = emitter.width * tileSize * emitter.size.x
			val h = emitter.height * tileSize * emitter.size.y

			val w2 = w * 0.5f
			val h2 = h * 0.5f

			if (emitter.shape == Emitter.EmissionShape.BOX)
			{
				shape.rect(ex-w2, ey-h2, w2, h2, w, h, 1f, 1f, emitter.emitterRotation + rotation)
			}
			else if (emitter.shape == Emitter.EmissionShape.CIRCLE)
			{
				shape.ellipse(ex-w2, ey-h2, w, h, emitter.emitterRotation + rotation)
			}
			else if (emitter.shape == Emitter.EmissionShape.CONE)
			{
				val start = w + emitter.emitterRotation + emitter.rotation
				shape.arc(ex, ey, emitter.height * tileSize, start, emitter.width)
			}
		}

		Pools.free(temp)
	}

	override fun copy(): ParticleEffect
	{
		val effect = ParticleEffect.load(loadPath)
		effect.setPosition(position.x, position.y)
		effect.rotation = rotation
		effect.colour.set(colour)
		effect.speedMultiplier = speedMultiplier
		effect.flipX = flipX
		effect.flipY = flipY
		effect.sizex = sizex
		effect.sizey = sizey
		return effect
	}

	companion object
	{
		fun load(xml: XmlReader.Element): ParticleEffect
		{
			val effect = ParticleEffect()

			effect.warmupTime = xml.getFloat("Warmup", 0f)
			effect.moveSpeed = xml.getFloat("MoveSpeed", 1f)

			val emittersEl = xml.getChildByName("Emitters")
			for (i in 0..emittersEl.childCount-1)
			{
				val el = emittersEl.getChild(i)
				val emitter = Emitter.load(el, effect) ?: continue
				effect.emitters.add(emitter)
			}

			return effect
		}

		fun load(path: String): ParticleEffect
		{
			val xml = getXml("Particles/$path")
			val effect = load(xml)
			effect.loadPath = path
			return effect
		}
	}
}