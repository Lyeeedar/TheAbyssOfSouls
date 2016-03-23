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
	val debug: Box2DDebugRenderer
	var camera: Camera? = null

	lateinit var posLightEntities: ImmutableArray<Entity>
	lateinit var posOccludeEntities: ImmutableArray<Entity>

	init
	{
		//RayHandler.setGammaCorrection(true);
		RayHandler.useDiffuseLight(true);

		world = World(Vector2(0f, 0f), true)

		rayHandler = RayHandler(world)
		rayHandler.setAmbientLight(0f, 0f, 0f, 0.0f);
		rayHandler.setBlurNum(1);
		rayHandler.setCulling(true)

		fovLight = FovLight(rayHandler, 256)
		fovLight.color = Color.WHITE
		fovLight.isStaticLight = true
		fovLight.isSoft = false
		fovLight.setSoftnessLength(0f)

		debug = Box2DDebugRenderer()
	}

	fun setCamera(camera: OrthographicCamera)
	{
		this.camera = camera
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
		val player = GlobalData.Global.currentLevel?.player
		val playerPos = Mappers.position.get(player);
		val playerStats = Mappers.stats.get(player)
		val playerOffset = player?.renderOffset()

		var px = playerPos.position.x.toFloat();
		var py = playerPos.position.y.toFloat();

		if (playerOffset != null)
		{
			px += playerOffset[0] / GlobalData.Global.tileSize
			py += playerOffset[1] / GlobalData.Global.tileSize
		}

		fovLight.setPosition(px, py)
		fovLight.distance = playerStats.stats.get(Enums.Statistic.SIGHT)

		for (entity in posLightEntities)
		{
			val pos = Mappers.position.get(entity)
			val light = Mappers.light.get(entity)
			val offset = entity.renderOffset()

			var x = pos.position.x.toFloat()
			var y = pos.position.y.toFloat()

			if (offset != null)
			{
				x += offset[0] / GlobalData.Global.tileSize
				y += offset[1] / GlobalData.Global.tileSize
			}

			light.lightObj?.color = light.col
			light.lightObj?.distance = light.dist
			light.lightObj?.setPosition(x, y)
		}

		for (entity in posOccludeEntities)
		{
			val pos = Mappers.position.get(entity)
			val occlude = Mappers.occluder.get(entity)

			var x = pos.position.x.toFloat()
			var y = pos.position.y.toFloat()

			occlude.body?.setTransform(x, y, 0f)
		}

		world.step(deltaTime, 0, 0)
		rayHandler.updateAndRender()

		//debug.render(world, camera?.combined)
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

			val light = PointLight(rayHandler, 256)
			light.isStaticLight = true
			light.isActive = true
			light.isSoft = false
			light.setSoftnessLength(0f)

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
			boxShape.setAsBox(0.5f, 0.5f)
			def.shape = boxShape

			val boxBodyDef = BodyDef();
			val boxBody = world.createBody(boxBodyDef);
			boxBody.createFixture(def);

			boxShape.dispose()

			occludeData.body = boxBody
		}

	}
}