package com.lyeeedar.Util

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.HDRColourSpriteBatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils

inline fun draw(batch: Batch, region: TextureRegion,
		 x: Float, y: Float, originX: Float, originY: Float,
		 width: Float, height: Float, scaleX: Float, scaleY: Float,
		 rotation: Float, flipX: Boolean, flipY: Boolean, removeAmount: Float)
{
	if (batch is SpriteBatch)
	{
		doDraw(batch, region, batch.packedColor, x, y, originX, originY, width, height, scaleX, scaleY, rotation, flipX, flipY, removeAmount)
	}
	else if (batch is HDRColourSpriteBatch)
	{
		doDraw(batch, region, batch.colour, x, y, originX, originY, width, height, scaleX, scaleY, rotation, flipX, flipY, removeAmount)
	}
	else
	{
		throw Exception("Unknown Batch type '" + batch.javaClass.canonicalName + "'!")
	}
}

val tempCol1 = Colour()
val tempCol2 = Colour()
inline fun drawBlend(batch: Batch, region1: TextureRegion, region2: TextureRegion, blendAlpha: Float,
				x: Float, y: Float, originX: Float, originY: Float,
				width: Float, height: Float, scaleX: Float, scaleY: Float,
				rotation: Float, flipX: Boolean, flipY: Boolean, removeAmount: Float)
{
	if (batch is SpriteBatch)
	{
		tempCol1.set(batch.color, batch.packedColor)

		tempCol2.set(tempCol1)
		tempCol2.a *= 1f - blendAlpha

		doDraw(batch, region1, tempCol2.toFloatBits(), x, y, originX, originY, width, height, scaleX, scaleY, rotation, flipX, flipY, removeAmount)

		tempCol2.set(tempCol1)
		tempCol2.a *= blendAlpha

		doDraw(batch, region2, tempCol2.toFloatBits(), x, y, originX, originY, width, height, scaleX, scaleY, rotation, flipX, flipY, removeAmount)
	}
	else if (batch is HDRColourSpriteBatch)
	{
		tempCol1.set(batch.colour)

		tempCol2.set(tempCol1)
		tempCol2.a *= 1f - blendAlpha

		doDraw(batch, region1, tempCol2, x, y, originX, originY, width, height, scaleX, scaleY, rotation, flipX, flipY, removeAmount)

		tempCol2.set(tempCol1)
		tempCol2.a *= blendAlpha

		doDraw(batch, region2, tempCol2, x, y, originX, originY, width, height, scaleX, scaleY, rotation, flipX, flipY, removeAmount)
	}
	else
	{
		throw Exception("Unknown Batch type '" + batch.javaClass.canonicalName + "'!")
	}
}

// 4 vertices of order x, y, colour, u, v
val verticesSpriteBatch: FloatArray by lazy { FloatArray(4 * 5) }
val verticesHdrBatchBatch: FloatArray by lazy { FloatArray(4 * 8) }
inline fun doDraw(batch: SpriteBatch, region: TextureRegion, packedColor: Float,
		   x: Float, y: Float, originX: Float, originY: Float,
		   width: Float, height: Float, scaleX: Float, scaleY: Float,
		   rotation: Float, flipX: Boolean, flipY: Boolean, removeAmount: Float)
{
	val (x1, x2, x3, x4, y1, y2, y3, y4, u, u2, v, v2) = calculateVertexData(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation, flipX, flipY, removeAmount)

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

inline fun doDraw(batch: HDRColourSpriteBatch, region: TextureRegion, colour: Colour,
		   x: Float, y: Float, originX: Float, originY: Float,
		   width: Float, height: Float, scaleX: Float, scaleY: Float,
		   rotation: Float, flipX: Boolean, flipY: Boolean, removeAmount: Float)
{
	val (x1, x2, x3, x4, y1, y2, y3, y4, u, u2, v, v2) = calculateVertexData(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation, flipX, flipY, removeAmount)

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
							   rotation: Float, flipX: Boolean, flipY: Boolean, removeAmount: Float): VertexData
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