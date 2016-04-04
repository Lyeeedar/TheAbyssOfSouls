package com.lyeeedar.Systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.HDRColourSpriteBatch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.BinaryHeap
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools
import com.lyeeedar.AssetManager
import com.lyeeedar.Components.*
import com.lyeeedar.Enums
import com.lyeeedar.GlobalData
import com.lyeeedar.Level.Tile
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Sprite.SpriteAnimation.MoveAnimation
import com.lyeeedar.Util.Colour
import com.lyeeedar.Util.EnumBitflag
import com.lyeeedar.Util.Point
import com.lyeeedar.Util.abs

/**
 * Created by Philip on 20-Mar-16.
 */

class RenderSystem(): EntitySystem(systemList.indexOf(RenderSystem::class))
{
	val batchHDRColour: HDRColourSpriteBatch = HDRColourSpriteBatch()
	val damFont: BitmapFont = AssetManager.loadFont("Sprites/Unpacked/font.ttf", 20)
	lateinit var entities: ImmutableArray<Entity>
	lateinit var hpEntities: ImmutableArray<Entity>
	val heap: BinaryHeap<RenderSprite> = BinaryHeap<RenderSprite>()
	val directionBitflag: EnumBitflag<Enums.Direction> = EnumBitflag<Enums.Direction>()

	var screenShakeRadius: Float = 0f
	var screenShakeAccumulator: Float = 0f
	var screenShakeSpeed: Float = 0f
	var screenShakeAngle: Float = 0f

	override fun addedToEngine(engine: Engine?)
	{
		entities = engine?.getEntitiesFor(Family.all(PositionComponent::class.java).one(SpriteComponent::class.java, TilingSpriteComponent::class.java, EffectComponent::class.java).get()) ?: throw RuntimeException("Engine is null!")
		hpEntities = engine?.getEntitiesFor(Family.all(PositionComponent::class.java, StatisticsComponent::class.java).get()) ?: throw RuntimeException("It broked")
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
				effect.sprite.baseScale[0] = pos.max.x - pos.min.x + 1f
				effect.sprite.baseScale[1] = pos.max.y - pos.min.y + 1f

				if (effect.direction == Enums.Direction.EAST || effect.direction == Enums.Direction.WEST)
				{
					val temp = effect.sprite.baseScale[0]
					effect.sprite.baseScale[0] = effect.sprite.baseScale[1]
					effect.sprite.baseScale[1] = temp
				}

				effect.sprite.rotation = effect.direction.angle

				queueSprite(effect.sprite, drawX, drawY, offsetx, offsety, Enums.SpaceSlot.AIR, tile)
			}
		}

		batchHDRColour.begin()

		while (heap.size > 0)
		{
			val rs = heap.pop();

			batchHDRColour.setColor(rs.light)
			rs.sprite.render(batchHDRColour, rs.x, rs.y, GlobalData.Global.tileSize, GlobalData.Global.tileSize );

			rs.free()
		}

		for (entity in hpEntities)
		{
			val pos = entity.position()
			val stats = entity.stats()

			val txt = stats.damTally.abs().toInt().toString()

			val drawX = pos.position.x * GlobalData.Global.tileSize + offsetx;
			val drawY = (pos.position.y+1) * GlobalData.Global.tileSize + offsety;

			damFont.draw(batchHDRColour, txt, drawX, drawY)
		}

		batchHDRColour.end()
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
	var light: Colour = Colour()
	var x: Float = 0f
	var y: Float = 0f

	var comparisonVal: Float = 0f

	operator fun set(sprite: Sprite, x: Float, y: Float, offsetx: Float, offsety: Float, slot: Enums.SpaceSlot, tile: Tile): RenderSprite {
		this.sprite = sprite
		this.x = x
		this.y = y

		this.light.set(tile.light)

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

		val X_BLOCK_SIZE = Enums.SpaceSlot.Values.size
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
