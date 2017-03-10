package com.lyeeedar.Renderables.Particle

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pools
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Direction
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

	var loop = true
	var completed = false
	var killOnAnimComplete = true
	private var warmupTime = 0f
	private var doneWarmup = false
	val emitters = Array<Emitter>()

	// local stuff
	val position = Vector2()
	var lockPosition = false
	var facing: Direction = Direction.NORTH
	var useFacing = true

	var collisionGrid: Array2D<Boolean>? = null
	var collisionFun: ((x: Int, y: Int) -> Unit)? = null

	val lifetime: Float
		get() = (animation?.duration() ?: emitters.maxBy { it.lifetime() }!!.lifetime())

	fun start()
	{
		for (emitter in emitters)
		{
			emitter.time = 0f
			emitter.emitted = false
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

		complete = animation?.update(delta) ?: false
		if (complete)
		{
			if (killOnAnimComplete) stop()
			animation?.free()
			animation = null
		}

		val posOffset = animation?.renderOffset()
		val x = position.x + (posOffset?.get(0) ?: 0f)
		val y = position.y + (posOffset?.get(1) ?: 0f)

		val scale = animation?.renderScale()
		val sx = size[0] * (scale?.get(0) ?: 1f)
		val sy = size[1] * (scale?.get(1) ?: 1f)

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

		for (emitter in emitters) emitter.update(delta, collisionGrid)

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

				if (!loop) stop()
				else for (emitter in emitters) emitter.emitted = false
			}
			else
			{
				complete = false
			}
		}

		return complete
	}

	fun complete() = emitters.all{ it.complete() }

	fun setPosition(x: Float, y: Float)
	{
		position.set(x, y)
	}

	override fun doRender(batch: Batch, x: Float, y: Float, tileSize: Float)
	{

	}

	fun debug(shape: ShapeRenderer, offsetx: Float, offsety: Float, tileSize: Float, drawEmitter: Boolean, drawParticles: Boolean)
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
		val temp2 = Pools.obtain(Vector2::class.java)
		val temp3 = Pools.obtain(Vector2::class.java)

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

			if (!drawEmitter)
			{

			}
			else if (emitter.shape == Emitter.EmissionShape.BOX)
			{
				shape.rect(ex-w2, ey-h2, w2, h2, w, h, 1f, 1f, emitter.emitterRotation + rotation)
			}
			else if (emitter.shape == Emitter.EmissionShape.CIRCLE)
			{
				shape.ellipse(ex-w2, ey-h2, w, h, emitter.emitterRotation + rotation)
			}
			else if (emitter.shape == Emitter.EmissionShape.CONE)
			{
				val angleMin = -emitter.width*0.5f
				val angleMax = emitter.width*.5f

				val core = temp
				val min = temp2
				val max = temp3

				core.set(ex, ey)

				min.set(0f, h)
				min.rotate(angleMin)
				min.rotate(emitter.emitterRotation)
				min.rotate(emitter.rotation)
				min.add(core)

				max.set(0f, h)
				max.rotate(angleMax)
				max.rotate(emitter.emitterRotation)
				max.rotate(emitter.rotation)
				max.add(core)

				shape.line(core, min)
				shape.line(core, max)
				shape.line(min, max)
			}
			else
			{
				throw Exception("Unhandled emitter type '${emitter.shape}'!")
			}

			if (drawParticles)
			{
				for (particle in emitter.particles)
				{
					var px = 0f
					var py = 0f

					if (emitter.simulationSpace == Emitter.SimulationSpace.LOCAL)
					{
						temp.set(emitter.offset.valAt(0, emitter.time))
						temp.scl(emitter.size)
						temp.rotate(emitter.rotation)

						px += (emitter.position.x + temp.x)
						py += (emitter.position.y + temp.y)
					}

					for (pdata in particle.particles)
					{
						val size = particle.size.valAt(pdata.sizeStream, pdata.life).lerp(pdata.ranVal)
						var sizex = size
						var sizey = size

						if (particle.allowResize)
						{
							sizex *= emitter.size.x
							sizey *= emitter.size.y
						}

						sizex *= tileSize
						sizey *= tileSize

						val rotation = if (emitter.simulationSpace == Emitter.SimulationSpace.LOCAL) pdata.rotation + emitter.rotation + emitter.emitterRotation else pdata.rotation

						temp.set(pdata.position)

						if (emitter.simulationSpace == Emitter.SimulationSpace.LOCAL) temp.scl(emitter.size).rotate(emitter.rotation + emitter.emitterRotation)

						val drawx = (temp.x + px) * tileSize + offsetx
						val drawy = (temp.y + py) * tileSize + offsety

						shape.rect(drawx - sizex / 2f, drawy - sizey / 2f, sizex / 2f, sizey / 2f, sizex, sizey, 1f, 1f, rotation)
					}
				}
			}
		}

		Pools.free(temp)
		Pools.free(temp2)
		Pools.free(temp3)
	}

	override fun copy(): ParticleEffect
	{
		val effect = ParticleEffect.load(loadPath)
		effect.killOnAnimComplete = killOnAnimComplete
		effect.setPosition(position.x, position.y)
		effect.rotation = rotation
		effect.colour.set(colour)
		effect.warmupTime = warmupTime
		effect.loop = loop
		effect.flipX = flipX
		effect.flipY = flipY
		effect.useFacing = useFacing
		effect.size[0] = size[0]
		effect.size[1] = size[1]
		return effect
	}

	companion object
	{
		fun load(xml: XmlReader.Element): ParticleEffect
		{
			val effect = ParticleEffect()

			effect.warmupTime = xml.getFloat("Warmup", 0f)
			effect.loop = xml.getBoolean("Loop", true)

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