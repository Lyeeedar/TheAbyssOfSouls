package com.lyeeedar.Renderables.Particle

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Direction
import com.lyeeedar.Util.Array2D
import com.lyeeedar.Util.Random
import com.lyeeedar.Util.ciel

class Emitter(val particleEffect: ParticleEffect)
{
	val MAX_DELTA = 1f / 15f // dont update faster than 15fps

	enum class EmissionType
	{
		ABSOLUTE,
		ACCUMULATED
	}

	enum class SimulationSpace
	{
		LOCAL,
		WORLD
	}

	enum class EmissionShape
	{
		CIRCLE,
		BOX,
		CONE
	}

	enum class EmissionArea
	{
		INTERIOR,
		BORDER
	}

	enum class EmissionDirection
	{
		RADIAL,
		RANDOM,
		UP,
		DOWN,
		LEFT,
		RIGHT
	}

	private val temp = Vector2()
	private val spawnPos = Vector2()
	private val spawnDir = Vector2()
	private val spawnOffset = Vector2()

	val particles: Array<Particle> = Array(false, 16)

	val position = Vector2()
	var rotation: Float = 0f
	val size: Vector2 = Vector2(1f, 1f)

	val offset = VectorTimeline()
	lateinit var type: EmissionType
	lateinit var simulationSpace: SimulationSpace
	val emissionRate = LerpTimeline()
	var singleBurst = false
	lateinit var particleSpeed: Range
	lateinit var particleRotation: Range
	lateinit var shape: EmissionShape
	var width: Float = 0f
	var height: Float = 0f
	var emitterRotation: Float = 0f
	lateinit var area: EmissionArea
	lateinit var dir: EmissionDirection
	var gravity: Float = 0f
	var isCollisionEmitter: Boolean = false

	var time: Float = 0f
		set(value)
		{
			field = value
		}

	var emissionAccumulator: Float = 0f

	var emitted = false
	var stopped = false

	fun lifetime() = emissionRate.length().toFloat() + particles.maxBy { it.lifetime.v2 }!!.lifetime.v2
	fun complete(): Boolean
	{
		if (singleBurst)
		{
			return emitted && particles.all { it.complete() }
		}
		else
		{
			return (time >= emissionRate.length() || stopped) && particles.all { it.complete() }
		}
	}

	fun stop() { stopped = true }
	fun start() { stopped = false }

	fun update(delta: Float, collisionGrid: Array2D<Boolean>?)
	{
		time += delta

		val scaledDelta = Math.min(delta, MAX_DELTA)

		if (!stopped || (singleBurst && !emitted))
		{
			val duration = emissionRate.length()
			val rate = emissionRate.valAt(0, time)

			if (duration == 0f || (singleBurst && !emitted) || time <= duration)
			{
				if (type == EmissionType.ABSOLUTE || singleBurst)
				{
					if (singleBurst)
					{
						if (!emitted && time >= emissionRate.streams[0][0].first)
						{
							emitted = true

							val toSpawn = Math.max(0f, rate - particles.sumBy { it.particleCount() }).ciel()
							for (i in 1..toSpawn)
							{
								spawn()
							}
						}
					}
					else
					{
						emitted = true

						val toSpawn = Math.max(0f, rate - particles.sumBy { it.particleCount() }).ciel()
						for (i in 1..toSpawn)
						{
							spawn()
						}
					}
				}
				else
				{
					emissionAccumulator += scaledDelta * rate
					emitted = true

					while (emissionAccumulator > 1f)
					{
						emissionAccumulator -= 1f
						spawn()
					}
				}
			}
		}

		for (particle in particles)
		{
			particle.simulate(scaledDelta, collisionGrid, gravity)
		}

		if (!stopped)
		{
			val duration = emissionRate.length()
			val rate = emissionRate.valAt(0, time)

			if (duration == 0f || time <= duration)
			{
				if (type == EmissionType.ABSOLUTE && !singleBurst)
				{
					val toSpawn = Math.max(0f, rate - particles.sumBy { it.particleCount() }).ciel()
					for (i in 1..toSpawn)
					{
						spawn()
					}
				}
			}
		}
	}

	fun spawn()
	{
		spawnPos.set(when (shape)
		{
			EmissionShape.CIRCLE -> spawnCircle()
			EmissionShape.BOX -> spawnBox()
			EmissionShape.CONE -> spawnCone()
			else -> throw RuntimeException("Invalid emitter shape! $shape")
		})

		val velocity = when (dir)
		{
			EmissionDirection.RADIAL -> spawnDir.set(spawnPos).nor()
			EmissionDirection.RANDOM -> spawnDir.setToRandomDirection()
			EmissionDirection.UP -> spawnDir.set(Direction.NORTH.x.toFloat(), Direction.NORTH.y.toFloat())
			EmissionDirection.DOWN -> spawnDir.set(Direction.SOUTH.x.toFloat(), Direction.SOUTH.y.toFloat())
			EmissionDirection.LEFT -> spawnDir.set(Direction.WEST.x.toFloat(), Direction.WEST.y.toFloat())
			EmissionDirection.RIGHT -> spawnDir.set(Direction.EAST.x.toFloat(), Direction.EAST.y.toFloat())
			else -> throw RuntimeException("Invalid emitter direction type! $dir")
		}

		val speed = particleSpeed.lerp(Random.random())
		var localRot = particleRotation.lerp(Random.random()) + rotation
		val offset = this.offset.valAt(0, time)

		if (particleEffect.flipX)
		{
			offset.x *= -1f
			spawnPos.x *= -1f
			velocity.x *= -1f
		}

		if (particleEffect.flipY)
		{
			offset.y *= -1f
			spawnPos.y *= -1f
			velocity.y *= -1f
		}

		if (particleEffect.flipX && particleEffect.flipY)
		{

		}
		else if (particleEffect.flipX || particleEffect.flipY)
		{
			localRot *= -1f
		}

		if (simulationSpace == SimulationSpace.WORLD)
		{
			spawnPos.rotate(rotation)
			spawnPos.add(position)

			// rotate offset
			temp.set(offset).scl(size).rotate(rotation)
			spawnPos.add(temp)

			velocity.scl(size)

			velocity.rotate(rotation)
		}
		else
		{
			// just add offset
			spawnPos.add(offset)
		}

		velocity.scl(speed)

		// pick random particle
		val particle = particles.random()
		particle.spawn(spawnPos, velocity, localRot)
	}

	fun spawnCone(): Vector2
	{
		if (area == EmissionArea.INTERIOR)
		{
			val angle = -width*0.5f + Random.random() * width
			val h = Random.random() * height

			temp.set(0f, h)
			temp.rotate(angle)
		}
		else if (area == EmissionArea.BORDER)
		{
			val angle = -width*0.5f + Random.random() * width
			temp.set(0f, height)
			temp.rotate(angle)
		}
		else throw RuntimeException("Invalid emitter area type $area")

		temp.rotate(emitterRotation)

		return temp
	}

	fun spawnCircle(): Vector2
	{
		if (area == EmissionArea.INTERIOR)
		{
			val ranVal = Random.random()
			val sqrtRanVal = Math.sqrt(ranVal.toDouble()).toFloat()
			val phi = Random.random() * (2f * Math.PI)
			val x = sqrtRanVal * Math.cos(phi).toFloat() * (width / 2f)
			val y = sqrtRanVal * Math.sin(phi).toFloat() * (height / 2f)

			temp.set(x, y)
		}
		else if (area == EmissionArea.BORDER)
		{
			val phi = Random.random() * (2f * Math.PI)
			val x = Math.cos(phi).toFloat() * (width / 2f)
			val y = Math.sin(phi).toFloat() * (height / 2f)

			temp.set(x, y)
		}
		else throw RuntimeException("Invalid emitter area type $area")

		return temp
	}

	fun spawnBox(): Vector2
	{
		if (area == EmissionArea.BORDER)
		{
			val w2 = width/2f
			val h2 = height/2f
			val p1 = Vector2(-w2, h2) // top left
			val p2 = Vector2(w2, h2) // top right
			val p3 = Vector2(w2, -h2) // bottom right
			val p4 = Vector2(-w2, -h2) // bottom left
			val points = arrayOf(p1, p2, p3, p4)
			val dists = floatArrayOf(width, height, width, height)
			for (i in 1..dists.size-1) dists[i] += dists[i-1]

			val totalDist = dists.last()
			val chosenDst = Random.random() * totalDist

			var i = 0
			while (i < dists.size)
			{
				if (dists[i] > chosenDst)
				{
					break
				}

				i++
			}
			if (i == points.size) i = points.size-1

			val delta = dists[i] - chosenDst
			val start = points[i]
			val end = if (i+1 == points.size) points[0] else points[i+1]
			val diff = start.dst(end)

			temp.set(start).lerp(end, delta / diff)
		}
		else if (area == EmissionArea.INTERIOR)
		{
			val x = Random.random() * width - (width / 2f)
			val y = Random.random() * height - (height / 2f)

			temp.set(x, y)
		}
		else throw RuntimeException("Invalid emitter area type $area")

		temp.rotate(emitterRotation)

		return temp
	}

	fun callCollisionFunc(func: (x: Int, y: Int) -> Unit)
	{
		if (!isCollisionEmitter) return

		for (particle in particles) particle.callCollisionFunc(func)
	}

	companion object
	{
		fun load(xml: XmlReader.Element, particleEffect: ParticleEffect): Emitter?
		{
			if (!xml.getBoolean("Enabled", true)) return null

			val emitter = Emitter(particleEffect)

			emitter.type = EmissionType.valueOf(xml.get("Type", "Absolute").toUpperCase())
			emitter.simulationSpace = SimulationSpace.valueOf(xml.get("Space", "World").toUpperCase())
			emitter.shape = EmissionShape.valueOf(xml.get("Shape", "Box").toUpperCase())
			emitter.width = xml.getFloat("Width", 1f)
			if (emitter.width == 0f) emitter.width = 0.001f
			emitter.height = xml.getFloat("Height", 1f)
			if (emitter.height == 0f) emitter.height = 0.001f
			emitter.emitterRotation = xml.getFloat("Rotation", 0f)
			emitter.area = EmissionArea.valueOf(xml.get("Area", "Interior").toUpperCase())
			emitter.dir = EmissionDirection.valueOf(xml.get("Direction", "Radial").toUpperCase())
			emitter.particleSpeed = Range(xml.get("ParticleSpeed"))
			emitter.particleRotation = Range(xml.get("ParticleRotation"))
			emitter.gravity = xml.getFloat("Gravity", 0f)
			emitter.isCollisionEmitter = xml.getBoolean("IsCollisionEmitter", false)

			val offset = xml.getChildByName("Offset")
			if (offset != null)
			{
				emitter.offset.parse(offset, fun(off: String): Vector2 {
					val split = off.split(",")
					val x = split[0].toFloat()
					val y = split[1].toFloat()
					return Vector2(x, y)
				})

				if (emitter.offset.streams.size == 0)
				{
					emitter.offset.streams.add(Array())
					emitter.offset.streams[0].add(Pair(0f, Vector2()))
				}
			}
			else
			{
				emitter.offset.streams.add(Array())
				emitter.offset.streams[0].add(Pair(0f, Vector2()))
			}

			val rateEls = xml.getChildByName("RateKeyframes")
			emitter.emissionRate.parse(rateEls, { it.toFloat() })
			emitter.singleBurst = xml.getBoolean("SingleBurst", false)

			val particlesEl = xml.getChildByName("Particles")
			for (i in 0..particlesEl.childCount-1)
			{
				val el = particlesEl.getChild(i)
				val particle = Particle.load(el, emitter)
				emitter.particles.add(particle)
			}

			return emitter
		}
	}
}