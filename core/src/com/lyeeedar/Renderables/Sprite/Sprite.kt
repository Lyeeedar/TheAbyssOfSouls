package com.lyeeedar.Renderables.Sprite

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.HDRColourSpriteBatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Matrix3
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array
import com.lyeeedar.Renderables.Animation.AbstractAnimation
import com.lyeeedar.Renderables.Animation.AbstractColourAnimation
import com.lyeeedar.Renderables.Renderable
import com.lyeeedar.Util.Colour
import com.lyeeedar.Util.Point

class Sprite(val fileName: String, var animationDelay: Float, var textures: Array<TextureRegion>, colour: Colour, mode: Sprite.AnimationMode, var drawActualSize: Boolean) : Renderable()
{
	enum class AnimationStage
	{
		INVALID,
		START,
		MIDDLE,
		END;

		companion object
		{
			val Values = values()
		}
	}

	enum class AnimationMode
	{
		NONE, TEXTURE, SHRINK, SINE
	}

	var referenceSize: Float? = null

	var tempCol = Colour()
	var colour = Colour(1f,1f,1f,1f)
	var colourAnimation: AbstractColourAnimation? = null

	var repeatDelay = 0f
	var repeatAccumulator: Float = 0.toFloat()
	var animationAccumulator: Float = 0.toFloat()

	var faceInMoveDirection: Boolean = false
	val lastPos: Vector2 = Vector2()
	var fixPosition: Boolean = false


	var completed = false

	var animationStage = AnimationStage.INVALID
	var animationState: AnimationState

	var baseScale = floatArrayOf(1f, 1f)

	var completionCallback: (() -> Unit)? = null

	var removeAmount: Float = 0.0f

	init
	{
		animationState = AnimationState()
		animationState.mode = mode

		this.colour = colour
	}

	val lifetime: Float
		get() = if (animation != null) animation!!.duration() else animationDelay * textures.size

	val remainingLifetime: Float
		get() = if (animation != null) animation!!.duration() - animation!!.time() else animationDelay * (textures.size - animationState.texIndex)

	override fun doUpdate(delta: Float): Boolean
	{
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
					if (animation == null && animationState.texIndex == textures.size / 2)
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
				}
				else if (animationState.mode == AnimationMode.SHRINK)
				{
					animationState.isShrunk = !animationState.isShrunk
					looped = animationState.isShrunk
				}
				else if (animationState.mode == AnimationMode.SINE)
				{
					looped = true
				}
			}
		}

		if (animationState.mode == AnimationMode.SINE)
		{
			animationState.sinOffset = Math.sin(animationAccumulator / (animationDelay / (2 * Math.PI))).toFloat()
		}

		if (animation != null)
		{
			if (animationStage == AnimationStage.INVALID) animationStage = AnimationStage.START
			looped = animation!!.update(delta)

			if (animation!!.time() >= animation!!.duration() / 2f)
			{
				if (animation == null && animationState.texIndex == textures.size / 2)
				{
					animationStage = AnimationStage.MIDDLE
				}
			}

			if (looped)
			{
				animation!!.free()
				animation = null
			}
		}

		if (colourAnimation != null)
		{
			val looped = colourAnimation!!.update(delta)
			if (looped && colourAnimation!!.oneTime)
			{
				colourAnimation!!.free()
				colourAnimation = null
			}
		}

		if (looped)
		{
			animationStage = AnimationStage.END

			completionCallback?.invoke()
			completionCallback = null
		}

		if (!completed) completed = looped
		return looped
	}

	override fun doRender(batch: Batch, x: Float, y: Float, size: Float)
	{
		var scaleX = baseScale[0]
		var scaleY = baseScale[1]

		if (animation != null)
		{
			val scale = animation!!.renderScale()
			if (scale != null)
			{
				scaleX *= scale[0]
				scaleY *= scale[1]
			}
		}

		render(batch, x, y, size, size, scaleX, scaleY, animationState)
	}

	fun render(batch: Batch, x: Float, y: Float, width: Float, height: Float, scaleX: Float = 1f, scaleY: Float = 1f)
	{
		var scaleX = baseScale[0] * scaleX
		var scaleY = baseScale[1] * scaleY

		if (animation != null)
		{
			val scale = animation!!.renderScale()
			if (scale != null)
			{
				scaleX *= scale[0]
				scaleY *= scale[1]
			}
		}

		render(batch, x, y, width, height, scaleX, scaleY, animationState)
	}

	private fun render(batch: Batch, x: Float, y: Float, width: Float, height: Float, scaleX: Float, scaleY: Float, animationState: AnimationState)
	{
		val colour = if (colourAnimation != null) colourAnimation!!.renderColour()!! else if (animation?.renderColour() != null) animation!!.renderColour()!! else this.colour

		if (colour.a == 0f)
		{
			return
		}

		if (colour == Colour.WHITE)
		{
			drawTexture(batch, textures.items[animationState.texIndex], x, y, width, height, scaleX, scaleY, animationState)
		}
		else
		{
			val oldCol = (batch as? HDRColourSpriteBatch)?.colour ?: tempCol.set(batch.color, batch.packedColor)

			val col = tempColour.set(oldCol)
			col *= colour
			(batch as? HDRColourSpriteBatch)?.setColor(col) ?: batch.setColor(col.toFloatBits())

			drawTexture(batch, textures.items[animationState.texIndex], x, y, width, height, scaleX, scaleY, animationState)

			(batch as? HDRColourSpriteBatch)?.setColor(oldCol) ?: batch.setColor(oldCol.toFloatBits())
		}
	}

	private fun drawTexture(batch: Batch, texture: TextureRegion, x: Float, y: Float, width: Float, height: Float, scaleX: Float, scaleY: Float, animationState: AnimationState)
	{
		var x = x
		var y = y
		var width = width
		var height = height

		width *= size[0]
		height *= size[1]

		if (drawActualSize)
		{
			val widthRatio = width / 32f
			val heightRatio = height / 32f

			val regionWidth = referenceSize ?: texture.regionWidth.toFloat()
			val regionHeight = referenceSize ?: texture.regionHeight.toFloat()

			val trueWidth = regionWidth * widthRatio
			val trueHeight = regionHeight * heightRatio

			val widthOffset = (trueWidth - width) / 2

			x -= widthOffset
			width = trueWidth
			height = trueHeight
		}

		if (rotation != 0f && fixPosition)
		{
			val offset = getPositionCorrectionOffsets(x, y, width / 2.0f, height / 2.0f, width, height, scaleX, scaleY, rotation)
			x -= offset.x
			y -= offset.y
		}

		if (batch is SpriteBatch)
		{
			doDraw(batch, texture, batch.packedColor, x, y, width / 2.0f, height / 2.0f, width, height, scaleX, scaleY, rotation, flipX, flipY)
		}
		else if (batch is HDRColourSpriteBatch)
		{
			doDraw(batch, texture, batch.colour, x, y, width / 2.0f, height / 2.0f, width, height, scaleX, scaleY, rotation, flipX, flipY)
		}
	}

	// 4 vertices of order x, y, colour, u, v
	val verticesSpriteBatch: FloatArray by lazy { FloatArray(4 * 5) }
	val verticesHdrBatchBatch: FloatArray by lazy { FloatArray(4 * 8) }
	fun doDraw(batch: SpriteBatch, region: TextureRegion, packedColor: Float,
			   x: Float, y: Float, originX: Float, originY: Float,
			   width: Float, height: Float, scaleX: Float, scaleY: Float,
			   rotation: Float, flipX: Boolean, flipY: Boolean)
	{
		val (x1, x2, x3, x4, y1, y2, y3, y4, u, u2, v, v2) = calculateVertexData(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation, flipX, flipY)

		val vertices = verticesSpriteBatch
		vertices[0] = x1
		vertices[1] = y1
		vertices[2] = packedColor
		vertices[3] = u
		vertices[4] = v

		vertices[5] = x2
		vertices[6] = y2
		vertices[7] = packedColor
		vertices[8] = u
		vertices[9] = v2

		vertices[10] = x3
		vertices[11] = y3
		vertices[12] = packedColor
		vertices[13] = u2
		vertices[14] = v2

		vertices[15] = x4
		vertices[16] = y4
		vertices[17] = packedColor
		vertices[18] = u2
		vertices[19] = v

		batch.draw(region.texture, vertices, 0, 20)
	}

	fun doDraw(batch: HDRColourSpriteBatch, region: TextureRegion, colour: Colour,
			   x: Float, y: Float, originX: Float, originY: Float,
			   width: Float, height: Float, scaleX: Float, scaleY: Float,
			   rotation: Float, flipX: Boolean, flipY: Boolean)
	{
		val (x1, x2, x3, x4, y1, y2, y3, y4, u, u2, v, v2) = calculateVertexData(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation, flipX, flipY)

		val vertices = verticesHdrBatchBatch
		vertices[0] = x1
		vertices[1] = y1
		vertices[2] = colour.r
		vertices[3] = colour.g
		vertices[4] = colour.b
		vertices[5] = colour.a
		vertices[6] = u
		vertices[7] = v

		vertices[8] = x2
		vertices[9] = y2
		vertices[10] = colour.r
		vertices[11] = colour.g
		vertices[12] = colour.b
		vertices[13] = colour.a
		vertices[14] = u
		vertices[15] = v2

		vertices[16] = x3
		vertices[17] = y3
		vertices[18] = colour.r
		vertices[19] = colour.g
		vertices[20] = colour.b
		vertices[21] = colour.a
		vertices[22] = u2
		vertices[23] = v2

		vertices[24] = x4
		vertices[25] = y4
		vertices[26] = colour.r
		vertices[27] = colour.g
		vertices[28] = colour.b
		vertices[29] = colour.a
		vertices[30] = u2
		vertices[31] = v

		batch.draw(region.texture, vertices, 0, 32)
	}

	inline fun calculateVertexData(region: TextureRegion,
							x: Float, y: Float, originX: Float, originY: Float,
							width: Float, height: Float, scaleX: Float, scaleY: Float,
							rotation: Float, flipX: Boolean, flipY: Boolean): VertexData
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

		var x1: Float
		var y1: Float
		var x2: Float
		var y2: Float
		var x3: Float
		var y3: Float
		var x4: Float
		var y4: Float

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
		}
		else
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

		x1 += worldOriginX
		y1 += worldOriginY
		x2 += worldOriginX
		y2 += worldOriginY
		x3 += worldOriginX
		y3 += worldOriginY
		x4 += worldOriginX
		y4 += worldOriginY

		val u = if (flipX) region.u2 else region.u
		var v = if (flipY) region.v else region.v2
		val u2 = if (flipX) region.u else region.u2
		val v2 = if (flipY) region.v2 else region.v

		if (removeAmount > 0f)
		{
			val yMove = (y1-y2) * removeAmount
			y1 -= yMove / 2f
			y4 -= yMove / 2f

			y2 += yMove / 2f
			y3 += yMove / 2f

			val vMove = (v-v2) * removeAmount
			v -= vMove
		}

		return VertexData(x1, x2, x3, x4, y1, y2, y3, y4, u, u2, v, v2)
	}
	data class VertexData(
			val x1: Float, val x2: Float, val x3: Float, val x4: Float,
			val y1: Float, val y2: Float, val y3: Float, val y4: Float,
			val u: Float, val u2: Float, val v: Float, val v2: Float)

	val currentTexture: TextureRegion
		get() = textures.get(animationState.texIndex)

	override fun copy(): Sprite
	{
		val sprite = Sprite(fileName, animationDelay, textures, colour, animationState.mode, drawActualSize)
		sprite.referenceSize = referenceSize
		sprite.animation = animation?.copy()
		sprite.colourAnimation = colourAnimation?.copy() as? AbstractColourAnimation

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

		fun getPositionCorrectionOffsets(x: Float, y: Float, originX: Float, originY: Float, width: Float, height: Float,
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
			}
			else
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
