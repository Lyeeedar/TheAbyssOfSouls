/*******************************************************************************
 * Copyright 2011 See AUTHORS file.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.badlogic.gdx.graphics.g2d

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.VertexAttributes.Usage
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Affine2
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.NumberUtils
import com.lyeeedar.Util.Colour

/** Draws batched quads using indices.
 * @see Batch

 * @author mzechner
 * *
 * @author Nathan Sweet
 */
class HDRColourSpriteBatch
/** Constructs a new HDRColourSpriteBatch. Sets the projection matrix to an orthographic projection with y-axis point upwards, x-axis
 * point to the right and the origin being in the bottom left corner of the screen. The projection will be pixel perfect with
 * respect to the current screen resolution.
 *
 *
 * The defaultShader specifies the shader to use. Note that the names for uniforms for this default shader are different than
 * the ones expect for shaders set with [.setShader]. See [.createDefaultShader].
 * @param size The max number of sprites in a single batchHDRColour. Max of 5460.
 * *
 * @param defaultShader The default shader to use. This is not owned by the HDRColourSpriteBatch and must be disposed separately.
 */
@JvmOverloads constructor(size: Int = 4000, defaultShader: ShaderProgram? = null) : Batch
{
	private val mesh: Mesh

	internal val vertices: FloatArray
	internal var idx = 0
	internal var lastTexture: Texture? = null
	internal var invTexWidth = 0f
	internal var invTexHeight = 0f

	internal var drawing = false

	private val transformMatrix = Matrix4()
	private val projectionMatrix = Matrix4()
	private val combinedMatrix = Matrix4()

	private var blendingDisabled = false
	private var blendSrcFunc = GL20.GL_SRC_ALPHA
	private var blendDstFunc = GL20.GL_ONE_MINUS_SRC_ALPHA

	private lateinit var shader: ShaderProgram
	private var customShader: ShaderProgram? = null
	private var ownsShader: Boolean = false

	val colour = Colour(1f)
	private val tempcolor = Color()

	private val tempVertexBuffer = FloatArray(11)

	/** Number of render calls since the last [.begin].  */
	var renderCalls = 0

	/** Number of rendering calls, ever. Will not be reset unless set manually.  */
	var totalRenderCalls = 0

	/** The maximum number of sprites rendered in one batchHDRColour so far.  */
	var maxSpritesInBatch = 0

	var forceExtractVertices = false

	init
	{
		// 32767 is max index, so 32767 / 6 - (32767 / 6 % 3) = 5460.
		if (size > 5460) throw IllegalArgumentException("Can't have more than 5460 sprites per batchHDRColour: " + size)

		var vertexDataType: Mesh.VertexDataType = Mesh.VertexDataType.VertexArray
		if (Gdx.gl30 != null)
		{
			vertexDataType = Mesh.VertexDataType.VertexBufferObjectWithVAO
		}
		mesh = Mesh(vertexDataType, false, size * 4, size * 6,
					VertexAttribute(Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
					VertexAttribute(Usage.ColorUnpacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
					VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"),
					VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "1"),
					VertexAttribute(Usage.Generic, 1, "a_blendAlpha")
				   )

		projectionMatrix.setToOrtho2D(0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())

		vertices = FloatArray(size * (2 + 4 + 2 + 2 + 1))

		val len = size * 6
		val indices = ShortArray(len)
		var j = 0
		var i = 0
		while (i < len)
		{
			indices[i] = j.toShort()
			indices[i + 1] = (j + 1).toShort()
			indices[i + 2] = (j + 2).toShort()
			indices[i + 3] = (j + 2).toShort()
			indices[i + 4] = (j + 3).toShort()
			indices[i + 5] = j.toShort()
			i += 6
			j += 4
		}
		mesh.setIndices(indices)

		if (defaultShader == null)
		{
			shader = createDefaultShader()
			ownsShader = true
		}
		else
			shader = defaultShader
	}

	override fun begin()
	{
		if (drawing) throw IllegalStateException("HDRColourSpriteBatch.end must be called before begin.")
		renderCalls = 0

		Gdx.gl.glDepthMask(false)
		if (customShader != null)
			customShader!!.begin()
		else
			shader.begin()
		setupMatrices()

		drawing = true
	}

	override fun end()
	{
		if (!drawing) throw IllegalStateException("HDRColourSpriteBatch.begin must be called before end.")
		if (idx > 0) flush()
		lastTexture = null
		drawing = false

		val gl = Gdx.gl
		gl.glDepthMask(true)
		if (isBlendingEnabled) gl.glDisable(GL20.GL_BLEND)

		if (customShader != null)
			customShader!!.end()
		else
			shader.end()
	}

	fun tintColour(tint: Colour)
	{
		colour.timesAssign(tint)
	}

	fun tintColour(tint: Color)
	{
		colour.timesAssign(tint)
	}

	override fun setColor(tint: Color)
	{
		colour.r = tint.r
		colour.g = tint.g
		colour.b = tint.b
		colour.a = tint.a
	}

	fun setColor(tint: Colour)
	{
		colour.set(tint)
	}

	override fun setColor(r: Float, g: Float, b: Float, a: Float)
	{
		colour.set(r, g, b, a)
	}

	override fun setColor(color: Float)
	{
		val intBits = NumberUtils.floatToIntColor(color)
		colour.r = (intBits and 0xff) / 255f
		colour.g = (intBits.ushr(8) and 0xff) / 255f
		colour.b = (intBits.ushr(16) and 0xff) / 255f
		colour.a = (intBits.ushr(24) and 0xff) / 255f
	}

	override fun getColor(): Color
	{
		return tempcolor.set(colour.r, colour.g, colour.b, colour.a)
	}

	override fun getPackedColor(): Float
	{
		return colour.toFloatBits()
	}

	override fun draw(texture: Texture, x: Float, y: Float, originX: Float, originY: Float, width: Float, height: Float, scaleX: Float, scaleY: Float, rotation: Float, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int, flipX: Boolean, flipY: Boolean)
	{
		throw RuntimeException("Unimplemented draw method in HDRSpriteBatch!")
	}

	override fun draw(texture: Texture, x: Float, y: Float, width: Float, height: Float, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int, flipX: Boolean, flipY: Boolean)
	{
		throw RuntimeException("Unimplemented draw method in HDRSpriteBatch!")
	}

	override fun draw(texture: Texture, x: Float, y: Float, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int)
	{
		throw RuntimeException("Unimplemented draw method in HDRSpriteBatch!")
	}

	override fun draw(texture: Texture, x: Float, y: Float, width: Float, height: Float, u: Float, v: Float, u2: Float, v2: Float)
	{
		throw RuntimeException("Unimplemented draw method in HDRSpriteBatch!")
	}

	override fun draw(texture: Texture, x: Float, y: Float)
	{
		throw RuntimeException("Unimplemented draw method in HDRSpriteBatch!")
	}

	override fun draw(texture: Texture, x: Float, y: Float, width: Float, height: Float)
	{
		throw RuntimeException("Unimplemented draw method in HDRSpriteBatch!")
	}

	override fun draw(texture: Texture, spriteVertices: FloatArray, offset: Int, count: Int)
	{
		var offset = offset
		var count = count
		if (!drawing) throw IllegalStateException("SpriteBatch.begin must be called before draw.")

		if (count % 11 == 0 && !forceExtractVertices)
		{
			val verticesLength = vertices.size
			var remainingVertices = verticesLength
			if (texture !== lastTexture)
				switchTexture(texture)
			else
			{
				remainingVertices -= idx
				if (remainingVertices == 0)
				{
					flush()
					remainingVertices = verticesLength
				}
			}
			var copyCount = Math.min(remainingVertices, count)

			System.arraycopy(spriteVertices, offset, vertices, idx, copyCount)
			idx += copyCount
			count -= copyCount
			while (count > 0)
			{
				offset += copyCount
				flush()
				copyCount = Math.min(verticesLength, count)
				System.arraycopy(spriteVertices, offset, vertices, 0, copyCount)
				idx += copyCount
				count -= copyCount
			}
		}
		else
		{
			// decompose and extract vertices
			var i = offset
			while (i < offset + count)
			{
				val x = spriteVertices[i + 0]
				val y = spriteVertices[i + 1]
				val u = spriteVertices[i + 3]
				val v = spriteVertices[i + 4]

				tempVertexBuffer[0] = x
				tempVertexBuffer[1] = y

				tempVertexBuffer[2] = colour.r
				tempVertexBuffer[3] = colour.g
				tempVertexBuffer[4] = colour.b
				tempVertexBuffer[5] = colour.a

				tempVertexBuffer[6] = u
				tempVertexBuffer[7] = v

				tempVertexBuffer[8] = u
				tempVertexBuffer[9] = v

				tempVertexBuffer[10] = 0f

				val oldForceExtract = forceExtractVertices
				forceExtractVertices = false
				draw(texture, tempVertexBuffer, 0, 11)
				forceExtractVertices = oldForceExtract
				i += 5
			}
		}
	}

	fun drawVertices(texture: Texture, spriteVertices: FloatArray, offset: Int, count: Int)
	{
		var offset = offset
		var count = count
		if (!drawing) throw IllegalStateException("SpriteBatch.begin must be called before draw.")

		val verticesLength = vertices.size
		var remainingVertices = verticesLength
		if (texture !== lastTexture)
			switchTexture(texture)
		else
		{
			remainingVertices -= idx
			if (remainingVertices == 0)
			{
				flush()
				remainingVertices = verticesLength
			}
		}
		var copyCount = Math.min(remainingVertices, count)

		System.arraycopy(spriteVertices, offset, vertices, idx, copyCount)
		idx += copyCount
		count -= copyCount
		while (count > 0)
		{
			offset += copyCount
			flush()
			copyCount = Math.min(verticesLength, count)
			System.arraycopy(spriteVertices, offset, vertices, 0, copyCount)
			idx += copyCount
			count -= copyCount
		}
	}

	override fun draw(region: TextureRegion, x: Float, y: Float)
	{
		throw RuntimeException("Unimplemented draw method in HDRSpriteBatch!")
	}

	override fun draw(region: TextureRegion, x: Float, y: Float, width: Float, height: Float)
	{
		throw RuntimeException("Unimplemented draw method in HDRSpriteBatch!")
	}

	override fun draw(region: TextureRegion, x: Float, y: Float, originX: Float, originY: Float, width: Float, height: Float,
					  scaleX: Float, scaleY: Float, rotation: Float)
	{
		throw RuntimeException("Unimplemented draw method in HDRSpriteBatch!")
	}

	override fun draw(region: TextureRegion, x: Float, y: Float, originX: Float, originY: Float, width: Float, height: Float, scaleX: Float, scaleY: Float, rotation: Float, clockwise: Boolean)
	{
		throw RuntimeException("Unimplemented draw method in HDRSpriteBatch!")
	}

	override fun draw(region: TextureRegion, width: Float, height: Float, transform: Affine2)
	{
		throw RuntimeException("Unimplemented draw method in HDRSpriteBatch!")
	}

	override fun flush()
	{
		if (idx == 0) return

		renderCalls++
		totalRenderCalls++
		val spritesInBatch = idx / (4 * 11)
		if (spritesInBatch > maxSpritesInBatch) maxSpritesInBatch = spritesInBatch
		val count = spritesInBatch * 6

		lastTexture!!.bind()
		val mesh = this.mesh
		mesh.setVertices(vertices, 0, idx)
		mesh.indicesBuffer.position(0)
		mesh.indicesBuffer.limit(count)

		if (blendingDisabled)
		{
			Gdx.gl.glDisable(GL20.GL_BLEND)
		}
		else
		{
			Gdx.gl.glEnable(GL20.GL_BLEND)
			if (blendSrcFunc != -1) Gdx.gl.glBlendFunc(blendSrcFunc, blendDstFunc)
		}

		mesh.render(if (customShader != null) customShader else shader, GL20.GL_TRIANGLES, 0, count)

		idx = 0
	}

	override fun disableBlending()
	{
		if (blendingDisabled) return
		flush()
		blendingDisabled = true
	}

	override fun enableBlending()
	{
		if (!blendingDisabled) return
		flush()
		blendingDisabled = false
	}

	override fun setBlendFunction(srcFunc: Int, dstFunc: Int)
	{
		if (blendSrcFunc == srcFunc && blendDstFunc == dstFunc) return
		flush()
		blendSrcFunc = srcFunc
		blendDstFunc = dstFunc
	}

	override fun getBlendSrcFunc(): Int
	{
		return blendSrcFunc
	}

	override fun getBlendDstFunc(): Int
	{
		return blendDstFunc
	}

	override fun dispose()
	{
		mesh.dispose()
		if (ownsShader) shader.dispose()
	}

	override fun getProjectionMatrix(): Matrix4
	{
		return projectionMatrix
	}

	override fun getTransformMatrix(): Matrix4
	{
		return transformMatrix
	}

	override fun setProjectionMatrix(projection: Matrix4)
	{
		if (drawing) flush()
		projectionMatrix.set(projection)
		if (drawing) setupMatrices()
	}

	override fun setTransformMatrix(transform: Matrix4)
	{
		if (drawing) flush()
		transformMatrix.set(transform)
		if (drawing) setupMatrices()
	}

	private fun setupMatrices()
	{
		combinedMatrix.set(projectionMatrix).mul(transformMatrix)
		if (customShader != null)
		{
			customShader!!.setUniformMatrix("u_projTrans", combinedMatrix)
			customShader!!.setUniformi("u_texture", 0)
		}
		else
		{
			shader.setUniformMatrix("u_projTrans", combinedMatrix)
			shader.setUniformi("u_texture", 0)
		}
	}

	protected fun switchTexture(texture: Texture)
	{
		flush()
		lastTexture = texture
		invTexWidth = 1.0f / texture.width
		invTexHeight = 1.0f / texture.height
	}

	override fun setShader(shader: ShaderProgram)
	{
		if (drawing)
		{
			flush()
			if (customShader != null)
				customShader!!.end()
			else
				this.shader.end()
		}
		customShader = shader
		if (drawing)
		{
			if (customShader != null)
				customShader!!.begin()
			else
				this.shader.begin()
			setupMatrices()
		}
	}

	override fun getShader(): ShaderProgram
	{
		return customShader ?: shader
	}

	override fun isBlendingEnabled(): Boolean
	{
		return !blendingDisabled
	}

	override fun isDrawing(): Boolean
	{
		return drawing
	}

	companion object
	{

		/** Returns a new instance of the default shader used by HDRColourSpriteBatch for GL2 when no shader is specified.  */
		fun createDefaultShader(): ShaderProgram
		{
			val vertexShader = """
attribute vec4 ${ShaderProgram.POSITION_ATTRIBUTE};
attribute vec4 ${ShaderProgram.COLOR_ATTRIBUTE};
attribute vec2 ${ShaderProgram.TEXCOORD_ATTRIBUTE}0;
attribute vec2 ${ShaderProgram.TEXCOORD_ATTRIBUTE}1;
attribute float a_blendAlpha;

uniform mat4 u_projTrans;

varying vec4 v_color;
varying vec2 v_texCoords1;
varying vec2 v_texCoords2;
varying float v_blendAlpha;

void main()
{
	v_color = ${ShaderProgram.COLOR_ATTRIBUTE};
	v_texCoords1 = ${ShaderProgram.TEXCOORD_ATTRIBUTE}0;
	v_texCoords2 = ${ShaderProgram.TEXCOORD_ATTRIBUTE}1;
	v_blendAlpha = a_blendAlpha;
	gl_Position =  u_projTrans * ${ShaderProgram.POSITION_ATTRIBUTE};
}
"""
			val fragmentShader = """
#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

varying vec4 v_color;
varying vec2 v_texCoords1;
varying vec2 v_texCoords2;
varying float v_blendAlpha;

uniform sampler2D u_texture;

void main()
{
	vec4 col1 = texture2D(u_texture, v_texCoords1);
	vec4 col2 = texture2D(u_texture, v_texCoords2);

	vec4 outCol = lerp(col1, col2, v_blendAlpha);

	v_color.a = min(1, v_color.a);
	gl_FragColor = v_color * outCol;
}
"""

			val shader = ShaderProgram(vertexShader, fragmentShader)
			if (!shader.isCompiled) throw IllegalArgumentException("Error compiling shader: " + shader.log)
			return shader
		}
	}
}