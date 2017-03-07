package com.lyeeedar.Renderables.Particle

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.BlendMode
import com.lyeeedar.Direction
import com.lyeeedar.Util.*
import ktx.math.div

/**
 * Created by Philip on 14-Aug-16.
 */

class Particle(val emitter: Emitter)
{
	enum class CollisionAction
	{
		NONE,
		SLIDE,
		BOUNCE,
		DIE
	}

	private val moveVec = Vector2()
	private val oldPos = Vector2()
	private val normal = Vector2()
	private val reflection = Vector2()
	private val temp = Vector2()
	private val temp2 = Vector2()
	private val collisionList = Array<Direction>(false, 16)

	val particles = Array<ParticleData>(false, 16)

	var allowResize: Boolean = true
	lateinit var lifetime: Range
	lateinit var blend: BlendMode
	var drag = 0f
	var brownian = 0f
	var velocityAligned = false
	lateinit var collision: CollisionAction
	var blendKeyframes = false
	val texture = StepTimeline<TextureRegion>()
	val colour = ColourTimeline()
	val alpha = LerpTimeline()
	val rotationSpeed = RangeLerpTimeline()
	val size = RangeLerpTimeline()

	fun particleCount() = particles.size
	fun complete() = particles.size == 0

	fun simulate(delta: Float, collisionGrid: Array2D<Boolean>?, gravity: Float)
	{
		val itr = particles.iterator()
		while (itr.hasNext())
		{
			val particle = itr.next()
			particle.life += delta
			if (particle.life > lifetime.v2)
			{
				itr.remove()
				particle.free()
			}
			else
			{
				if (velocityAligned)
				{
					particle.rotation = vectorToAngle(particle.velocity.x, particle.velocity.y)
				}
				else
				{
					var rotation = rotationSpeed.valAt(particle.rotStream, particle.life).lerp(particle.ranVal)

					if (emitter.particleEffect.flipX && emitter.particleEffect.flipY)
					{

					}
					else if (emitter.particleEffect.flipX || emitter.particleEffect.flipY)
					{
						rotation *= -1f
					}

					particle.rotation += rotation * delta
				}

				temp.set(particle.velocity).scl(drag * delta)
				particle.velocity.sub(temp)

				particle.velocity.y += gravity * delta

				if (brownian > 0f)
				{
					val direction = temp2.set(particle.velocity)
					val length = particle.velocity.len()

					if (length != 0f) direction.div(length)

					val impulseVector = temp.set(Random.random()-0.5f, Random.random()-0.5f)
					impulseVector.nor()

					direction.lerp(impulseVector, brownian * delta)
					direction.nor()

					particle.velocity.set(direction).scl(length)
				}

				moveVec.set(particle.velocity).scl(delta)

				oldPos.set(particle.position)

				particle.position.add(moveVec)

				if (collisionGrid != null && collision != CollisionAction.NONE)
				{
					val aabb = getBoundingBox(particle)

					if (checkColliding(aabb, collisionGrid))
					{
						if (collision == CollisionAction.DIE)
						{
							itr.remove()
							particle.free()
						}
						else if (collision == CollisionAction.BOUNCE || collision == CollisionAction.SLIDE)
						{
							// calculate average collision normal
							normal.x = collisionList.sumBy { it.x }.toFloat()
							normal.y = collisionList.sumBy { it.y }.toFloat()
							normal.nor()

							// reflect vector around normal
							val reflected = reflection.set(moveVec).sub(temp.set(normal).scl(2 * moveVec.dot(normal)))

							// handle based on collision action
							if (collision == CollisionAction.BOUNCE)
							{
								particle.position.set(oldPos)
								particle.velocity.set(reflected)
							}
							else
							{
								val yaabb = getBoundingBox(particle, temp.set(particle.position.x, oldPos.y))
								val xaabb = getBoundingBox(particle, temp.set(oldPos.x, particle.position.y))

								// negate y
								if (!checkColliding(yaabb, collisionGrid))
								{
									particle.position.y = oldPos.y
								}
								// negate x
								else if (!checkColliding(xaabb, collisionGrid))
								{
									particle.position.x = oldPos.x
								}
								// negate both
								else
								{
									particle.position.set(oldPos)
								}
							}
						}
						else
						{
							throw NotImplementedError("Forgot to add code to deal with collision action")
						}
					}

					Pools.free(aabb)
				}
			}
		}
	}

	fun callCollisionFunc(func: (x: Int, y: Int) -> Unit)
	{
		for (particle in particles)
		{
			val aabb = getBoundingBox(particle)

			for (x in aabb.x.toInt()..(aabb.x+aabb.width).toInt())
			{
				for (y in aabb.y.toInt()..(aabb.y + aabb.height).toInt())
				{
					func(x, y)
				}
			}

			Pools.free(aabb)
		}
	}

	fun checkColliding(aabb: Rectangle, collisionGrid: Array2D<Boolean>): Boolean
	{
		collisionList.clear()

		for (x in aabb.x.toInt()..(aabb.x+aabb.width).toInt())
		{
			for (y in aabb.y.toInt()..(aabb.y+aabb.height).toInt())
			{
				if (collisionGrid.tryGet(x, y, false)!!)
				{
					// calculate collision normal

					val wy = (aabb.width + 1f) * ((aabb.y+aabb.height*0.5f) - (y+0.5f))
					val hx = (aabb.height + 1f) * ((aabb.x+aabb.width*0.5f) - (x+0.5f))

					var dir: Direction

					if (wy > hx)
					{
						if (wy > -hx)
						{
							/* top */
							dir = Direction.SOUTH
						}
						else
						{
							/* left */
							dir = Direction.WEST
						}
					}
					else
					{
						if (wy > -hx)
						{
							/* right */
							dir = Direction.EAST
						}
						else
						{
							/* bottom */
							dir = Direction.NORTH
						}
					}

					collisionList.add(dir)
				}
			}
		}

		return collisionList.size > 0
	}

	fun getBoundingBox(particle: ParticleData, overridePos: Vector2? = null): Rectangle
	{
		val scale = size.valAt(particle.sizeStream, particle.life).lerp(particle.ranVal)
		val sx = scale * emitter.size.x
		val sy = scale * emitter.size.y

		val x = overridePos?.x ?: particle.position.x
		val y = overridePos?.y ?: particle.position.y

		var actualx = x
		var actualy = y

		if (emitter.simulationSpace == Emitter.SimulationSpace.LOCAL)
		{
			temp.set(emitter.offset.valAt(0, emitter.time))
			temp.scl(emitter.size)

			if (emitter.particleEffect.flipX)
			{
				temp.x *= -1
			}
			if (emitter.particleEffect.flipY)
			{
				temp.y *= -1
			}

			temp.rotate(emitter.rotation)

			val ex = temp.x + emitter.position.x
			val ey = temp.y + emitter.position.y

			temp.set(x, y)
			temp.rotate(emitter.rotation + emitter.emitterRotation)

			actualx = ex + temp.x
			actualy = ey + temp.y
		}

		return Pools.obtain(Rectangle::class.java).set(actualx-sx*0.5f, actualy-sy*0.5f, sx, sy)
	}

	fun spawn(position: Vector2, velocity: Vector2, rotation: Float)
	{
		val particle = ParticleData.obtain().set(
				position, velocity,
				rotation, (lifetime.v2 - lifetime.v1) * Random.random(),
				Random.random(texture.streams.size-1),
				Random.random(colour.streams.size-1),
				Random.random(alpha.streams.size-1),
				Random.random(rotationSpeed.streams.size-1),
				Random.random(size.streams.size-1),
				Random.random())

		particles.add(particle)
	}

	companion object
	{
		fun load(xml: XmlReader.Element, emitter: Emitter): Particle
		{
			val particle = Particle(emitter)

			particle.lifetime = Range(xml.get("Lifetime"))
			particle.blend = BlendMode.valueOf(xml.get("BlendMode", "Additive").toUpperCase())
			particle.collision = CollisionAction.valueOf(xml.get("Collision", "None").toUpperCase())
			particle.drag = xml.getFloat("Drag", 0f)
			particle.velocityAligned = xml.getBoolean("VelocityAligned", false)
			particle.allowResize = xml.getBoolean("AllowResize", true)
			particle.brownian = xml.getFloat("Brownian", 0f)

			particle.blendKeyframes = xml.getBoolean("BlendKeyframes", false)

			val textureEls = xml.getChildByName("TextureKeyframes")
			if (textureEls != null)
			{
				particle.texture.parse(textureEls, { AssetManager.loadTextureRegion(it) ?: throw RuntimeException("Failed to find texture $it!") }, particle.lifetime.v2)
			}
			else
			{
				particle.texture[0, 0f] = AssetManager.loadTextureRegion("white")!!
			}

			val colourEls = xml.getChildByName("ColourKeyframes")
			if (colourEls != null)
			{
				particle.colour.parse(colourEls, { AssetManager.loadColour(it) }, particle.lifetime.v2)
			}
			else
			{
				particle.colour[0, 0f] = Colour(1f, 1f, 1f, 1f)
			}

			val alphaEls = xml.getChildByName("AlphaKeyframes")
			if (alphaEls != null)
			{
				particle.alpha.parse(alphaEls, { it.toFloat() }, particle.lifetime.v2)
			}
			else
			{
				particle.alpha[0, 0f] = 1f
			}

			val rotationSpeedEls = xml.getChildByName("RotationSpeedKeyframes")
			if (rotationSpeedEls != null)
			{
				particle.rotationSpeed.parse(rotationSpeedEls, { Range(it) }, particle.lifetime.v2)
			}
			else
			{
				particle.rotationSpeed[0, 0f] = Range(0f, 0f)
			}

			val sizeEls = xml.getChildByName("SizeKeyframes")
			if (sizeEls != null)
			{
				particle.size.parse(sizeEls, { Range(it) }, particle.lifetime.v2)
			}
			else
			{
				particle.size[0, 0f] = Range(1f, 1f)
			}

			return particle
		}
	}
}

data class ParticleData(val position: Vector2, val velocity: Vector2,
								 var rotation: Float, var life: Float,
								 var texStream: Int, var colStream: Int, var alphaStream: Int, var rotStream: Int, var sizeStream: Int,
								 var ranVal: Float)
{
	constructor(): this(Vector2(), Vector2(0f, 1f), 0f, 0f, 0, 0, 0, 0, 0, 0f)

	fun set(position: Vector2, velocity: Vector2, rotation: Float, life: Float, texStream: Int, colStream: Int, alphaStream: Int, rotStream: Int, sizeStream: Int, ranVal: Float): ParticleData
	{
		this.position.set(position)
		this.velocity.set(velocity)
		this.life = life
		this.rotation = rotation
		this.texStream = texStream
		this.colStream = colStream
		this.alphaStream = alphaStream
		this.rotStream = rotStream
		this.sizeStream = sizeStream
		this.ranVal = ranVal
		return this
	}

	var obtained: Boolean = false
	companion object
	{
		private val pool: Pool<ParticleData> = object : Pool<ParticleData>() {
			override fun newObject(): ParticleData
			{
				return ParticleData()
			}

		}

		@JvmStatic fun obtain(): ParticleData
		{
			val particle = pool.obtain()

			if (particle.obtained) throw RuntimeException()

			particle.obtained = true
			particle.life = 0f
			return particle
		}
	}
	fun free() { if (obtained) { pool.free(this); obtained = false } }
}
