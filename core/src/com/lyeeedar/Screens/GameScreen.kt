package com.lyeeedar.Screens

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.lyeeedar.AI.TestAI
import com.lyeeedar.AssetManager
import com.lyeeedar.Components.*
import com.lyeeedar.Enums
import com.lyeeedar.GlobalData
import com.lyeeedar.Level.Level
import com.lyeeedar.Level.Tile
import com.lyeeedar.Sprite.SpriteAnimation.MoveAnimation
import com.lyeeedar.Systems.*
import com.lyeeedar.UI.ButtonKeyboardHelper

/**
 * Created by Philip on 20-Mar-16.
 */

class GameScreen(): AbstractScreen()
{
	val engine = Engine()
	lateinit var lightingSystem: LightingSystem

	override fun create()
	{
		engine.addSystem(ShadowCastSystem())
		engine.addSystem(TaskProcessorSystem())
		engine.addSystem(SpriteUpdaterSystem())
		engine.addSystem(RenderSystem(batch))

		lightingSystem = LightingSystem()
		engine.addSystem(lightingSystem)
		engine.addSystem(CleanupSystem())

		val grid = Array(20, {i -> Array(20, { i -> Tile() } ) } )

		for (x in 0..19)
		{
			for (y in 0..19)
			{
				if (x == 0 || y == 0 || x == 19 || y == 19 || MathUtils.random(5) == 0)
				{
					val e = Entity()

					val occlude = OccluderComponent()
					e.add(occlude)

					val pos = PositionComponent()
					pos.position = grid[x][y]
					pos.slot = Enums.SpaceSlot.WALL
					grid[x][y].contents.put(pos.slot, e)
					e.add(pos)

					val sprite = SpriteComponent(AssetManager.loadSprite("wall"))
					e.add(sprite)

					engine.addEntity(e)
				}

				val e = Entity()

				val pos = PositionComponent()
				pos.position = grid[x][y]
				pos.slot = Enums.SpaceSlot.FLOOR
				grid[x][y].contents.put(pos.slot, e)
				e.add(pos)

				val sprite = SpriteComponent(AssetManager.loadSprite("grass"))
				e.add(sprite)

				if (MathUtils.random(50) == 0)
				{
					val light = LightComponent(Color.RED, 7f)
					e.add(light)
				}

				engine.addEntity(e)

			}
		}

		val p = EntityLoader.load("player")
		val pos = Mappers.position.get(p)
		pos.position = grid[10][10]
		p.tile()?.contents?.put(pos.slot, p)

		engine.addEntity(p)

		val level = Level()
		level.grid = grid

		level.player = p

		GlobalData.Global.currentLevel = level

		keyboardHelper = ButtonKeyboardHelper()
	}

	override fun doRender(delta: Float)
	{
		lightingSystem.setCamera(camera)

		engine.update(delta)
	}

	// ----------------------------------------------------------------------
	override fun scrolled(amount: Int): Boolean
	{
		GlobalData.Global.tileSize -= amount * 5;
		if ( GlobalData.Global.tileSize < 2 )
		{
			GlobalData.Global.tileSize = 2f;
		}

		return true
	}

}