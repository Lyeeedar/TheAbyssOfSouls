package com.lyeeedar.Sprite

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.HDRColourSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Matrix3
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array
import com.lyeeedar.GlobalData
import com.lyeeedar.Sound.SoundInstance
import com.lyeeedar.Sprite.SpriteAnimation.*
import com.lyeeedar.Util.Colour
import com.lyeeedar.Util.Point

class Sprite(var fileName: String, var animationDelay: Float, var textures: Array<TextureRegion>, colour: Colour, mode: Sprite.AnimationMode, var sound: SoundInstance?, var drawActualSize: Boolean)
{
	enum class AnimationStage
	{
		INVALID,
		START,
		MIDDLE,
		END;

		companion object
		{
			val Values = AnimationStage.values()
		}
	}

	enum class AnimationMode
	{
		NONE, TEXTURE, SHRINK, SINE
	}

	var colour = Colour(1f)

	var renderDelay = -1f
	var showBeforeRender = false

	var repeatDelay = 0f
	var repeatAccumulator: Float = 0.toFloat()
	var animationAccumulator: Float = 0.toFloat()

	var rotation: Float = 0.toFloat()
	var fixPosition: Boolean = false

	var flipX: Boolean = false
	var flipY: Boolean = false

	val size = intArrayOf(1, 1)

	var spriteAnimation: AbstractSpriteAnimation? = null
		set(value)
		{
			if (value != null && field != null)
			{
				var hybrid: HybridAnimation
				if (field is HybridAnimation)
				{
					hybrid = field as HybridAnimation
				}
				else
				{
					hybrid = HybridAnimation()
				}

				if (value is MoveAnimation || value is BumpAnimation)
				{
					hybrid.offset = value
				}
				else if (value is StretchAnimation)
				{
					hybrid.scale = value
				}
				else throw RuntimeException("No entry for sprite anim type")

				field = hybrid
			}
			else
			{
				field = value
			}
		}

	var animationStage = AnimationStage.INVALID
	var animationState: AnimationState

	var baseScale = floatArrayOf(1f, 1f)

	init
	{

		animationState = AnimationState()
		animationState.mode = mode

		this.colour = colour
	}

	val lifetime: Float
		get() = if (spriteAnimation != null) spriteAnimation!!.duration() else animationDelay * textures.size

	val remainingLifetime: Float
		get() = if (spriteAnimation != null) spriteAnimation!!.duration() - spriteAnimation!!.time() else animationDelay * (textures.size - animationState.texIndex)

	fun update(delta: Float): Boolean
	{
		if (renderDelay > 0)
		{
			renderDelay -= delta

			if (renderDelay > 0)
			{
				return false
			}
		}

		if (repeatAccumulator > 0)
		{
			repeatAccumulator -= delta
		}

		var looped = false
		if (repeatAccumulator <= 0)
		{
			if (animationStage == AnimationStage.INVALID) animationStage = AnimationStage.START
			animationAccumulator += delta

			while (animationAccumulator >= animationDelay)
			{
				animationAccumulator -= animationDelay

				if (animationState.mode == AnimationMode.TEXTURE)
				{
					if (spriteAnimation == null && animationState.texIndex == textures.size / 2)
					{
						animationStage = AnimationStage.MIDDLE
					}

					animationState.texIndex++
					if (animationState.texIndex >= textures.size)
					{
						animationState.texIndex = 0
						looped = true
						repeatAccumulator = repeatDelay
					}
				} else if (animationState.mode == AnimationMode.SHRINK)
				{
					animationState.isShrunk = !animationState.isShrunk
					looped = animationState.isShrunk
				} else if (animationState.mode == AnimationMode.SINE)
				{
					looped = true
				}
			}
		}

		if (animationState.mode == AnimationMode.SINE)
		{
			animationState.sinOffset = Math.sin(animationAccumulator / (animationDelay / (2 * Math.PI))).toFloat()
		}

		if (spriteAnimation != null)
		{
			if (animationStage == AnimationStage.INVALID) animationStage = AnimationStage.START
			looped = spriteAnimation!!.update(delta)

			if (spriteAnimation!!.time() >= spriteAnimation!!.duration() / 2f)
			{
				if (spriteAnimation == null && animationState.texIndex == textures.size / 2)
				{
					animationStage = AnimationStage.MIDDLE
				}
			}

			if (looped)
			{
				spriteAnimation!!.free()
				spriteAnimation = null
			}
		}

		if (looped)
		{
			animationStage = AnimationStage.END
		}

		return looped
	}

	fun render(batch: HDRColourSpriteBatch, x: Float, y: Float, width: Float, height: Float)
	{
		var scaleX = baseScale[0]
		var scaleY = baseScale[1]

		if (spriteAnimation != null)
		{
			val scale = spriteAnimation!!.renderScale()
			if (scale != null)
			{
				scaleX *= scale[0]
				scaleY *= scale[1]
			}
		}

		render(batch, x, y, width, height, scaleX, scaleY, animationState)
	}

	fun render(batch: HDRColourSpriteBatch, x: Float, y: Float, width: Float, height: Float, scaleX: Float, scaleY: Float, animationState: AnimationState)
	{
		var oldCol: Colour? = null
		if (colour.a == 0f)
		{
			return
		}

		oldCol = batch.colour

		val col = tempColour.set(oldCol)
		col.timesAssign(colour)
		batch.setColor(col)

		drawTexture(batch, textures.items[animationState.texIndex], x, y, width, height, scaleX, scaleY, animationState)

		if (oldCol != null)
		{
			batch.setColor(oldCol)
		}
	}

	private fun drawTexture(batch: HDRColourSpriteBatch, texture: TextureRegion, x: Float, y: Float, width: Float, height: Float, scaleX: Float, scaleY: Float, animationState: AnimationState)
	{
		var x = x
		var y = y
		var width = width
		var height = height
		if (renderDelay > 0 && !showBeforeRender)
		{
			return
		}

		if (drawActualSize)
		{
			val widthRatio = width / 32.0f
			val heightRatio = height / 32.0f

			val trueWidth = texture.regionWidth * widthRatio
			val trueHeight = texture.regionHeight * heightRatio

			val widthOffset = (trueWidth - width) / 2

			x -= widthOffset
			width = trueWidth
			height = trueHeight
		}

		width = width * size[0]
		height = height * size[1]

		if (animationState.mode == AnimationMode.SHRINK && animationState.isShrunk)
		{
			height *= 0.85f
		} else if (animationState.mode == AnimationMode.SINE)
		{
			y += height / 15f * animationState.sinOffset
		}

		if (rotation != 0f && fixPosition)
		{
			val offset = getPositionCorrectionOffsets(x, y, width / 2.0f, height / 2.0f, width, height, scaleX, scaleY, rotation)
			x -= offset.x
			y -= offset.y
		}

		// Check if not onscreen
		if (x + width < 0 || y + height < 0 || x > GlobalData.Global.resolution[0] || y > GlobalData.Global.resolution[1])
		{
			return  // skip drawing
		}

		batch.draw(texture, x, y, width / 2.0f, height / 2.0f, width, height, scaleX, scaleY, rotation, flipX, flipY)
	}

	val currentTexture: TextureRegion
		get() = textures.get(animationState.texIndex)

	fun copy(): Sprite
	{
		val sprite = Sprite(fileName, animationDelay, textures, colour, animationState.mode, sound, drawActualSize)
		if (spriteAnimation != null)
		{
			sprite.spriteAnimation = spriteAnimation!!.copy()
		}

		sprite.flipX = flipX
		sprite.flipY = flipY

		return sprite
	}

	class AnimationState
	{
		lateinit var mode: AnimationMode

		var texIndex: Int = 0
		var isShrunk: Boolean = false
		var sinOffset: Float = 0.toFloat()

		fun copy(): AnimationState
		{
			val `as` = AnimationState()

			`as`.mode = mode
			`as`.texIndex = texIndex
			`as`.isShrunk = isShrunk
			`as`.sinOffset = sinOffset

			return `as`
		}

		fun set(other: AnimationState)
		{
			mode = other.mode
			texIndex = other.texIndex
			isShrunk = other.isShrunk
			sinOffset = other.sinOffset
		}
	}

	companion object
	{

		private val tempColour = Colour()

		private val tempVec = Vector3()
		private val tempMat = Matrix3()

		private fun getPositionCorrectionOffsets(x: Float, y: Float, originX: Float, originY: Float, width: Float, height: Float,
												 scaleX: Float, scaleY: Float, rotation: Float): Vector3
		{
			// bottom left and top right corner points relative to origin
			val worldOriginX = x + originX
			val worldOriginY = y + originY
			var fx = -originX
			var fy = -originY
			var fx2 = width - originX
			var fy2 = height - originY

			// scale
			if (scaleX != 1f || scaleY != 1f)
			{
				fx *= scaleX
				fy *= scaleY
				fx2 *= scaleX
				fy2 *= scaleY
			}

			// construct corner points, start from top left and go counter clockwise
			val p1x = fx
			val p1y = fy
			val p2x = fx
			val p2y = fy2
			val p3x = fx2
			val p3y = fy2
			val p4x = fx2
			val p4y = fy

			val x1: Float
			val y1: Float
			val x2: Float
			val y2: Float
			val x3: Float
			val y3: Float
			val x4: Float
			val y4: Float

			// rotate
			if (rotation != 0f)
			{
				val cos = MathUtils.cosDeg(rotation)
				val sin = MathUtils.sinDeg(rotation)

				x1 = cos * p1x - sin * p1y
				y1 = sin * p1x + cos * p1y

				x2 = cos * p2x - sin * p2y
				y2 = sin * p2x + cos * p2y

				x3 = cos * p3x - sin * p3y
				y3 = sin * p3x + cos * p3y

				x4 = x1 + (x3 - x2)
				y4 = y3 - (y2 - y1)
			} else
			{
				x1 = p1x
				y1 = p1y

				x2 = p2x
				y2 = p2y

				x3 = p3x
				y3 = p3y

				x4 = p4x
				y4 = p4y
			}

			tempVec.set(x1, y1, 0f)

			if (x2 < tempVec.x) tempVec.x = x2
			if (x3 < tempVec.x) tempVec.x = x3
			if (x4 < tempVec.x) tempVec.x = x4

			if (y2 < tempVec.y) tempVec.y = y2
			if (y3 < tempVec.y) tempVec.y = y3
			if (y4 < tempVec.y) tempVec.y = y4

			tempVec.x += worldOriginX
			tempVec.y += worldOriginY

			tempVec.x -= x
			tempVec.y -= y

			return tempVec
		}
	}
}
