package com.lyeeedar.Systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.BinaryHeap
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools
import com.lyeeedar.Components.*
import com.lyeeedar.Enums
import com.lyeeedar.GlobalData
import com.lyeeedar.Level.Tile
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Sprite.SpriteAnimation.MoveAnimation
import com.lyeeedar.Util.EnumBitflag
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 20-Mar-16.
 */

val MAX_LIGHTS: Int = 20

class RenderSystem(val batch: SpriteBatch): EntitySystem(6)
{
	val shader: ShaderProgram = createLightShader()
	val colArray: FloatArray = FloatArray(4 * MAX_LIGHTS)
	val dataArray: FloatArray = FloatArray(3 * MAX_LIGHTS)
	val smoothArray: FloatArray = FloatArray(4 * MAX_LIGHTS)
	lateinit var entities: ImmutableArray<Entity>
	val heap: BinaryHeap<RenderSprite> = BinaryHeap<RenderSprite>()
	val directionBitflag: EnumBitflag<Enums.Direction> = EnumBitflag<Enums.Direction>()

	var screenShakeRadius: Float = 0f
	var screenShakeAccumulator: Float = 0f
	var screenShakeSpeed: Float = 0f
	var screenShakeAngle: Float = 0f

	override fun addedToEngine(engine: Engine?)
	{
		entities = engine?.getEntitiesFor(Family.all(PositionComponent::class.java).one(SpriteComponent::class.java, TilingSpriteComponent::class.java, EffectComponent::class.java).get()) ?: throw RuntimeException("Engine is null!")
	}

	override fun update(deltaTime: Float)
	{
		if (GlobalData.Global.currentLevel == null)
		{
			return;
		}

		RenderSprite.setBlockSize(GlobalData.Global.currentLevel?.width?.toFloat() ?: 0f, GlobalData.Global.currentLevel?.height?.toFloat() ?: 0f)

		val player = GlobalData.Global.currentLevel?.player
		val playerPos = Mappers.position.get(player);
		val playerSprite = Mappers.sprite.get(player);
		val playerTile = player?.tile() ?: return

		var offsetx = GlobalData.Global.resolution[ 0 ] / 2 - playerPos.position.x * GlobalData.Global.tileSize - GlobalData.Global.tileSize / 2;
		var offsety = GlobalData.Global.resolution[ 1 ] / 2 - playerPos.position.y * GlobalData.Global.tileSize - GlobalData.Global.tileSize / 2;

		if ( playerSprite.sprite.spriteAnimation is MoveAnimation )
		{
			val offset = playerSprite.sprite.spriteAnimation.renderOffset

			offsetx -= offset[0]
			offsety -= offset[1]
		}

		// do screen shake
		if ( screenShakeRadius > 2 )
		{
			screenShakeAccumulator += deltaTime;
			while ( screenShakeAccumulator >= screenShakeSpeed )
			{
				screenShakeAccumulator -= screenShakeSpeed;
				screenShakeAngle += ( 150 + MathUtils.random() * 60 );
				screenShakeRadius *= 0.9f;
			}

			offsetx += Math.sin( screenShakeAngle.toDouble() ).toFloat() * screenShakeRadius;
			offsety += Math.cos( screenShakeAngle.toDouble() ).toFloat() * screenShakeRadius;
		}

		for (entity in entities)
		{
			val pos = Mappers.position.get(entity)
			val tile = entity.tile() ?: continue
			val sprite = Mappers.sprite.get(entity)
			val tilingSprite = Mappers.tilingSprite.get(entity)
			val effect = Mappers.effect.get(entity)

			val drawX = pos.position.x * GlobalData.Global.tileSize + offsetx;
			val drawY = pos.position.y * GlobalData.Global.tileSize + offsety;

			if (sprite != null)
			{
				sprite.sprite.size[0] = pos.size
				sprite.sprite.size[1] = pos.size

				queueSprite(sprite.sprite, drawX, drawY, offsetx, offsety, pos.slot, tile)
			}

			if (tilingSprite != null)
			{
				GlobalData.Global.currentLevel?.buildTilingBitflag(directionBitflag, pos.position.x, pos.position.y, tilingSprite.sprite.checkID);
				val spriteData = tilingSprite.sprite.getSprite( directionBitflag );

				spriteData.size[0] = pos.size
				spriteData.size[1] = pos.size

				queueSprite(spriteData, drawX, drawY, offsetx, offsety, pos.slot, tile)
			}

			if (effect != null)
			{
				effect.sprite.size[0] = pos.size
				effect.sprite.size[1] = pos.size

				queueSprite(effect.sprite, drawX, drawY, offsetx, offsety, pos.slot, tile)
			}
		}

		batch.shader = shader
		batch.begin()

		shader.setUniformf("u_ambient", 0f, 0f, 0f, 1f)

		while (heap.size > 0)
		{
			val rs = heap.pop();

			var i = 0
			while (i < MAX_LIGHTS && rs.lights[i] != null)
			{
				val light = rs.lights[i] ?: break

				colArray[i*4 + 0] = light.light.col.r
				colArray[i*4 + 1] = light.light.col.g
				colArray[i*4 + 2] = light.light.col.b
				colArray[i*4 + 3] = light.light.col.a

				dataArray[i*3 + 0] = light.light.x + offsetx
				dataArray[i*3 + 1] = light.light.y + offsety
				dataArray[i*3 + 2] = light.light.dist * GlobalData.Global.tileSize

				smoothArray[i*4 + 0] = light.corners[0]
				smoothArray[i*4 + 1] = light.corners[1]
				smoothArray[i*4 + 2] = light.corners[2]
				smoothArray[i*4 + 3] = light.corners[3]

				i++;
			}

			shader.setUniformi("u_lightNum", i)
			//shader.setUniform4fv("u_lightCol", colArray, 0, MAX_LIGHTS)
			shader.setUniform1fv("u_lightSmoothing", smoothArray, 0, MAX_LIGHTS*4)
			//shader.setUniform3fv("u_lightData", dataArray, 0, MAX_LIGHTS)

			rs.sprite.render( batch, rs.x, rs.y, GlobalData.Global.tileSize, GlobalData.Global.tileSize );
			batch.flush()
			rs.free()
		}

		batch.end()
		batch.shader = null
	}

	// ----------------------------------------------------------------------
	fun queueSprite(sprite: Sprite, ix: Float, iy: Float, offsetx: Float, offsety: Float, slot: Enums.SpaceSlot, tile: Tile)
	{
		var x = ix
		var y = iy

		if ( sprite.spriteAnimation != null )
		{
			val offset = sprite.spriteAnimation.renderOffset;
			x += offset[ 0 ];
			y += offset[ 1 ];
		}

		if ( x + GlobalData.Global.tileSize < 0 || y + GlobalData.Global.tileSize < 0 || x > GlobalData.Global.resolution[ 0 ] || y > GlobalData.Global.resolution[ 1 ] ) { return; }

		val rs = RenderSprite.obtain().set( sprite, x, y, offsetx, offsety, slot, tile );

		heap.add( rs, rs.comparisonVal );
	}
}

class RenderSprite : BinaryHeap.Node(0f) {
	lateinit var sprite: Sprite
	var lights: Array<LightDataWrapper?> = arrayOfNulls<LightDataWrapper?>(MAX_LIGHTS)
	var x: Float = 0f
	var y: Float = 0f

	var comparisonVal: Float = 0f

	operator fun set(sprite: Sprite, x: Float, y: Float, offsetx: Float, offsety: Float, slot: Enums.SpaceSlot, tile: Tile): RenderSprite {
		this.sprite = sprite
		this.x = x
		this.y = y

		var src = 0
		var i = 0
		while (i < MAX_LIGHTS)
		{
			val ld = if (src < tile.lights.size) tile.lights[src++] else null

			if (ld != null)
			{
				var count = 0
				for (corner in ld.corners)
				{
					if (corner > 0f)
					{
						count++
					}
				}

				if (count == 2)
				{
					//continue
				}
			}

			lights[i] = ld

			i++
		}

		val bx = (x - offsetx).toFloat() / GlobalData.Global.tileSize
		val by = (y - offsety).toFloat() / GlobalData.Global.tileSize

		val sx = bx.toInt()
		var sy = by.toInt()

		comparisonVal = MAX_Y_BLOCK_SIZE - sy * Y_BLOCK_SIZE + (MAX_X_BLOCK_SIZE - sx * X_BLOCK_SIZE) + slot.ordinal

		return this
	}

	fun free() = RenderSprite.pool.free(this)

	companion object
	{
		val pool: Pool<RenderSprite> = Pools.get( RenderSprite::class.java, Int.MAX_VALUE )
		fun obtain() = RenderSprite.pool.obtain()

		val X_BLOCK_SIZE = Enums.SpaceSlot.values().size
		var Y_BLOCK_SIZE = 0f
		var MAX_Y_BLOCK_SIZE = 0f
		var MAX_X_BLOCK_SIZE = 0f
		fun setBlockSize(width: Float, height: Float) {
			Y_BLOCK_SIZE = X_BLOCK_SIZE * width

			MAX_Y_BLOCK_SIZE = Y_BLOCK_SIZE * height
			MAX_X_BLOCK_SIZE = X_BLOCK_SIZE * width
		}
	}
}

private fun createLightShader () : ShaderProgram {

	val posAtt = ShaderProgram.POSITION_ATTRIBUTE
	val colAtt = ShaderProgram.COLOR_ATTRIBUTE
	val texAtt = ShaderProgram.TEXCOORD_ATTRIBUTE + "0"

	val smoothingCount = MAX_LIGHTS * 4

	val vertexShader =
			"""
attribute vec4 $posAtt;
attribute vec4 $colAtt;
attribute vec2 $texAtt;
attribute float a_corner;

uniform mat4 u_projTrans;
uniform float u_lightSmoothing[$smoothingCount];
uniform int u_lightNum;

varying float v_smoothing[$MAX_LIGHTS];
varying vec4 v_color;
varying vec2 v_texCoords;
varying vec2 v_pos;

void main()
{
	for (int i = 0; i < u_lightNum; i++)
	{
		v_smoothing[i] = u_lightSmoothing[i*4 + (int)a_corner];
	}

	v_color = $colAtt;
	v_color.a = v_color.a * (255.0/254.0);
	v_texCoords = $texAtt;
	v_pos = $posAtt;
	gl_Position =  u_projTrans * $posAtt;
}""";


	val fragmentShader =
			"""
#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

varying float v_smoothing[$MAX_LIGHTS];
varying LOWP vec4 v_color;
varying vec2 v_texCoords;
varying vec2 v_pos;

uniform sampler2D u_texture;
uniform vec4 u_ambient;
uniform vec4 u_lightCol[$MAX_LIGHTS];
uniform vec3 u_lightData[$MAX_LIGHTS];
uniform int u_lightNum;

void main()
{
	vec3 light = u_ambient.rgb * u_ambient.a;

	for (int i = 0; i < u_lightNum; i++)
	{
		float dst = length(v_pos - u_lightData[i].xy);
		float alpha = 1.0 - dst / u_lightData[i].z;

		light.rgb += v_smoothing[i]; //u_lightCol[i].rgb * alpha * u_lightCol[i].a * round(v_smoothing[i]);
	}

	gl_FragColor = v_color * texture2D(u_texture, v_texCoords) * vec4(light, 1.0);
}""";

	//ShaderProgram.pedantic = true
	val shader = ShaderProgram(vertexShader, fragmentShader);
	if (shader.isCompiled == false) throw IllegalArgumentException("Error compiling shader: " + shader.log);
	return shader;
}