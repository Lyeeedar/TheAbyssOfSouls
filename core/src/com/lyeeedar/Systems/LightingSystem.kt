package com.lyeeedar.Systems

import box2dLight.FovLight
import box2dLight.PointLight
import box2dLight.RayHandler
import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.lyeeedar.Components.*
import com.lyeeedar.Enums
import com.lyeeedar.GlobalData
import com.lyeeedar.Sprite.SpriteAnimation.MoveAnimation

/**
 * Created by Philip on 21-Mar-16.
 */

class LightingSystem(): EntitySystem(10)
{
	val world: World
	val rayHandler: RayHandler
	val fovLight: FovLight

	lateinit var posLightEntities: ImmutableArray<Entity>
	lateinit var posOccludeEntities: ImmutableArray<Entity>

	var lastTileSize: Float = 0f

	init
	{
		RayHandler.setGammaCorrection(true);
		RayHandler.useDiffuseLight(true);

		world = World(Vector2(0f, 0f), true)

		rayHandler = RayHandler(world)
		rayHandler.setAmbientLight(0f, 0f, 0f, 0.5f);
		rayHandler.setBlurNum(3);
		rayHandler.setCulling(true)

		fovLight = FovLight(rayHandler, 256)
		fovLight.color = Color.WHITE
		fovLight.isStaticLight = true
		fovLight.isSoft = false
	}

	fun setCamera(camera: OrthographicCamera)
	{
		rayHandler.setCombinedMatrix(camera)
	}

	override fun addedToEngine(engine: Engine?)
	{
		val posLight = Family.all(PositionComponent::class.java, LightComponent::class.java).get()
		val posOcclude = Family.all(PositionComponent::class.java, OccluderComponent::class.java).get()

		posLightEntities = engine?.getEntitiesFor(posLight) ?: throw RuntimeException("Engine is null!")
		posOccludeEntities = engine?.getEntitiesFor(posOcclude) ?: throw RuntimeException("Engine is null!")

		engine?.addEntityListener(posLight, LightListener())
		engine?.addEntityListener(posOcclude, OccludeListener())
	}

	override fun update(deltaTime: Float)
	{
		val tileSize = GlobalData.Global.tileSize
		val tileSize2 = tileSize / 2f

		val player = GlobalData.Global.currentLevel?.player
		val playerPos = Mappers.position.get(player);
		val playerSprite = Mappers.sprite.get(player);
		val playerStats = Mappers.stats.get(player)

		var offsetx = GlobalData.Global.resolution[ 0 ] / 2 - playerPos.position.x * tileSize - tileSize2;
		var offsety = GlobalData.Global.resolution[ 1 ] / 2 - playerPos.position.y * tileSize - tileSize2;

		var px = playerPos.position.x.toFloat() * tileSize + offsetx + tileSize2;
		var py = playerPos.position.y.toFloat() * tileSize + offsety + tileSize2;
		fovLight.setPosition(px, py)
		fovLight.distance = playerStats.stats.get(Enums.Statistic.SIGHT) * tileSize

		if ( playerSprite.sprite.spriteAnimation is MoveAnimation )
		{
			val offset = playerSprite.sprite.spriteAnimation.renderOffset

			offsetx -= offset[0]
			offsety -= offset[1]
		}

		for (entity in posLightEntities)
		{
			val pos = Mappers.position.get(entity)
			val light = Mappers.light.get(entity)
			val sprite = Mappers.sprite.get(entity)

			var x = pos.position.x.toFloat() * tileSize + offsetx + tileSize2
			var y = pos.position.y.toFloat() * tileSize + offsety + tileSize2

			if (sprite != null && sprite.sprite.spriteAnimation != null && sprite.sprite.spriteAnimation is MoveAnimation)
			{
				val offset = sprite.sprite.spriteAnimation.renderOffset

				x += offset[0]
				y += offset[1]
			}

			light.lightObj?.color = light.col
			light.lightObj?.distance = light.dist * tileSize
			light.lightObj?.setPosition(x, y)
		}

		for (entity in posOccludeEntities)
		{
			val pos = Mappers.position.get(entity)
			val occlude = Mappers.occluder.get(entity)
			val sprite = Mappers.sprite.get(entity)

			var x = pos.position.x.toFloat() * tileSize + offsetx + tileSize2
			var y = pos.position.y.toFloat() * tileSize + offsety + tileSize2

			if (sprite != null && sprite.sprite.spriteAnimation != null && sprite.sprite.spriteAnimation is MoveAnimation)
			{
				val offset = sprite.sprite.spriteAnimation.renderOffset

				x += offset[0]
				y += offset[1]
			}

			occlude.body?.setTransform(x, y, 0f)

			if (tileSize == -1f)
			{
				val body = occlude.body ?: continue

				for (fixture in body.fixtureList.asSequence())
				{
					body.destroyFixture(fixture)
				}

				val def = FixtureDef()
				val boxShape = PolygonShape()
				boxShape.setAsBox(tileSize2, tileSize2)
				def.shape = boxShape

				body.createFixture(def);

				boxShape.dispose()
			}
		}

		lastTileSize = tileSize

		world.step(deltaTime, 0, 0)
		rayHandler.updateAndRender()
	}

	inner class LightListener(): EntityListener
	{
		override fun entityRemoved(entity: Entity?)
		{
			val lightData = Mappers.light.get(entity)

			lightData.lightObj?.remove()

			lightData.lightObj?.dispose()
			lightData.lightObj = null
		}

		override fun entityAdded(entity: Entity?)
		{
			val lightData = Mappers.light.get(entity)

			val light = PointLight(rayHandler, 128)
			light.isStaticLight = true
			light.isActive = true
			light.isSoft = false

			light.add(rayHandler)

			lightData.lightObj = light
		}
	}

	inner class OccludeListener(): EntityListener
	{
		override fun entityRemoved(entity: Entity?)
		{
			val occludeData = Mappers.occluder.get(entity)

			world.destroyBody(occludeData.body)

			occludeData.body = null
		}

		override fun entityAdded(entity: Entity?)
		{
			val occludeData = Mappers.occluder.get(entity)

			val def = FixtureDef()
			val boxShape = PolygonShape()
			boxShape.setAsBox(16f, 16f)
			def.shape = boxShape

			val boxBodyDef = BodyDef();
			val boxBody = world.createBody(boxBodyDef);
			boxBody.createFixture(def);

			boxShape.dispose()

			occludeData.body = boxBody
		}

	}
}