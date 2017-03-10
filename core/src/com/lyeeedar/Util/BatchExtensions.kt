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
		doDraw(batch, region, region, batch.colour, x, y, originX, originY, width, height, scaleX, scaleY, rotation, flipX, flipY, removeAmount, 0f)
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
	val blendAlpha = blendAlpha.clamp(0f, 1f)

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
		doDraw(batch, region1, region2, batch.colour, x, y, originX, originY, width, height, scaleX, scaleY, rotation, flipX, flipY, removeAmount, blendAlpha)
	}
	else
	{
		throw Exception("Unknown Batch type '" + batch.javaClass.canonicalName + "'!")
	}
}

// 4 vertices of order x, y, colour, u, v
val verticesSpriteBatch: FloatArray by lazy { FloatArray(4 * 5) }
val verticesHdrBatchBatch: FloatArray by lazy { FloatArray(4 * 11) }
inline fun doDraw(batch: SpriteBatch, region: TextureRegion, packedColor: Float,
		   x: Float, y: Float, originX: Float, originY: Float,
		   width: Float, height: Float, scaleX: Float, scaleY: Float,
		   rotation: Float, flipX: Boolean, flipY: Boolean, removeAmount: Float)
{
	val (x1, x2, x3, x4, y1, y2, y3, y4, r1u, r1u2, r1v, r1v2, r2u, r2u2, r2v, r2v2) = calculateVertexData(region, region, x, y, originX, originY, width, height, scaleX, scaleY, rotation, flipX, flipY, removeAmount)

	val vertices = verticesSpriteBatch
	vertices[0] = x1
	vertices[1] = y1
	vertices[2] = packedColor
	vertices[3] = r1u
	vertices[4] = r1v

	vertices[5] = x2
	vertices[6] = y2
	vertices[7] = packedColor
	vertices[8] = r1u
	vertices[9] = r1v2

	vertices[10] = x3
	vertices[11] = y3
	vertices[12] = packedColor
	vertices[13] = r1u2
	vertices[14] = r1v2

	vertices[15] = x4
	vertices[16] = y4
	vertices[17] = packedColor
	vertices[18] = r1u2
	vertices[19] = r1v

	batch.draw(region.texture, vertices, 0, 20)
}

inline fun doDraw(batch: HDRColourSpriteBatch, region1: TextureRegion, region2: TextureRegion, colour: Colour,
		   x: Float, y: Float, originX: Float, originY: Float,
		   width: Float, height: Float, scaleX: Float, scaleY: Float,
		   rotation: Float, flipX: Boolean, flipY: Boolean, removeAmount: Float, blendAlpha: Float)
{
	val (x1, x2, x3, x4, y1, y2, y3, y4, r1u, r1u2, r1v, r1v2, r2u, r2u2, r2v, r2v2) = calculateVertexData(region1, region2, x, y, originX, originY, width, height, scaleX, scaleY, rotation, flipX, flipY, removeAmount)

	val r = colour.r//.clamp(0f, 1f)
	val g = colour.g//.clamp(0f, 1f)
	val b = colour.b//.clamp(0f, 1f)
	val a = colour.a.clamp(0f, 1f)

	val vertices = verticesHdrBatchBatch
	vertices[0] = x1
	vertices[1] = y1
	vertices[2] = r
	vertices[3] = g
	vertices[4] = b
	vertices[5] = a
	vertices[6] = r1u
	vertices[7] = r1v
	vertices[8] = r2u
	vertices[9] = r2v
	vertices[10] = blendAlpha

	vertices[11] = x2
	vertices[12] = y2
	vertices[13] = r
	vertices[14] = g
	vertices[15] = b
	vertices[16] = a
	vertices[17] = r1u
	vertices[18] = r1v2
	vertices[19] = r2u
	vertices[20] = r2v2
	vertices[21] = blendAlpha

	vertices[22] = x3
	vertices[23] = y3
	vertices[24] = r
	vertices[25] = g
	vertices[26] = b
	vertices[27] = a
	vertices[28] = r1u2
	vertices[29] = r1v2
	vertices[30] = r2u2
	vertices[31] = r2v2
	vertices[32] = blendAlpha

	vertices[33] = x4
	vertices[34] = y4
	vertices[35] = r
	vertices[36] = g
	vertices[37] = b
	vertices[38] = a
	vertices[39] = r1u2
	vertices[40] = r1v
	vertices[41] = r2u2
	vertices[42] = r2v
	vertices[43] = blendAlpha

	batch.drawVertices(region1.texture, vertices, 0, 44)
}

inline fun calculateVertexData(region1: TextureRegion, region2: TextureRegion,
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

	val r1u = if (flipX) region1.u2 else region1.u
	var r1v = if (flipY) region1.v else region1.v2
	val r1u2 = if (flipX) region1.u else region1.u2
	val r1v2 = if (flipY) region1.v2 else region1.v

	val r2u = if (flipX) region2.u2 else region2.u
	var r2v = if (flipY) region2.v else region2.v2
	val r2u2 = if (flipX) region2.u else region2.u2
	val r2v2 = if (flipY) region2.v2 else region2.v

	if (removeAmount > 0f)
	{
		val yMove = (y1-y2) * removeAmount
		y1 -= yMove / 2f
		y4 -= yMove / 2f

		y2 += yMove / 2f
		y3 += yMove / 2f

		val vMove1 = (r1v-r1v2) * removeAmount
		r1v -= vMove1

		val vMove2 = (r2v-r2v2) * removeAmount
		r2v -= vMove2
	}

	return VertexData(x1, x2, x3, x4, y1, y2, y3, y4, r1u, r1u2, r1v, r1v2, r2u, r2u2, r2v, r2v2)
}
data class VertexData(
		val x1: Float, val x2: Float, val x3: Float, val x4: Float,
		val y1: Float, val y2: Float, val y3: Float, val y4: Float,
		val r1u: Float, val r1u2: Float, val r1v: Float, val r1v2: Float,
		val r2u: Float, val r2u2: Float, val r2v: Float, val r2v2: Float)