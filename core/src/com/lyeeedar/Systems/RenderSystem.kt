package com.lyeeedar.Systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
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

class RenderSystem(val batch: SpriteBatch): EntitySystem(6)
{
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
			val sprite = Mappers.sprite.get(entity)
			val tilingSprite = Mappers.tilingSprite.get(entity)
			val effect = Mappers.effect.get(entity)

			val drawX = pos.position.x * GlobalData.Global.tileSize + offsetx;
			val drawY = pos.position.y * GlobalData.Global.tileSize + offsety;

			if (sprite != null)
			{
				sprite.sprite.size[0] = pos.size
				sprite.sprite.size[1] = pos.size

				queueSprite(sprite.sprite, drawX, drawY, offsetx, offsety, pos.slot)
			}

			if (tilingSprite != null)
			{
				GlobalData.Global.currentLevel?.buildTilingBitflag(directionBitflag, pos.position.x, pos.position.y, tilingSprite.sprite.checkID);
				val spriteData = tilingSprite.sprite.getSprite( directionBitflag );

				spriteData.size[0] = pos.size
				spriteData.size[1] = pos.size

				queueSprite(spriteData, drawX, drawY, offsetx, offsety, pos.slot)
			}

			if (effect != null)
			{
				effect.sprite.size[0] = pos.size
				effect.sprite.size[1] = pos.size

				queueSprite(effect.sprite, drawX, drawY, offsetx, offsety, pos.slot)
			}
		}

		batch.begin()

		while (heap.size > 0)
		{
			val rs = heap.pop();

			rs.sprite.render( batch, rs.x, rs.y, GlobalData.Global.tileSize, GlobalData.Global.tileSize );
			rs.free()
		}

		batch.end()
	}

	// ----------------------------------------------------------------------
	fun queueSprite(sprite: Sprite, ix: Float, iy: Float, offsetx: Float, offsety: Float, slot: Enums.SpaceSlot)
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

		val rs = RenderSprite.obtain().set( sprite, x, y, offsetx, offsety, slot );

		heap.add( rs, rs.comparisonVal );
	}
}

class RenderSprite : BinaryHeap.Node(0f) {
	lateinit var sprite: Sprite
	var x: Float = 0f
	var y: Float = 0f

	var comparisonVal: Float = 0f

	operator fun set(sprite: Sprite, x: Float, y: Float, offsetx: Float, offsety: Float, slot: Enums.SpaceSlot): RenderSprite {
		this.sprite = sprite
		this.x = x
		this.y = y

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