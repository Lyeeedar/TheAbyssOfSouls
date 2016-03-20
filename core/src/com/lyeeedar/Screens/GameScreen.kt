package com.lyeeedar.Screens

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.lyeeedar.AI.TestAI
import com.lyeeedar.AssetManager
import com.lyeeedar.Components.PositionComponent
import com.lyeeedar.Components.SpriteComponent
import com.lyeeedar.Components.TaskComponent
import com.lyeeedar.Enums
import com.lyeeedar.GlobalData
import com.lyeeedar.Level.Level
import com.lyeeedar.Level.Tile
import com.lyeeedar.Systems.RenderSystem
import com.lyeeedar.Systems.SpriteUpdaterSystem
import com.lyeeedar.Systems.TaskProcessorSystem
import com.lyeeedar.UI.ButtonKeyboardHelper

/**
 * Created by Philip on 20-Mar-16.
 */

class GameScreen(): AbstractScreen()
{
	val engine = Engine()

	override fun create()
	{
		val grid = Array(20, {i -> Array(20, { i -> Tile() } ) } )

		for (x in 0..19)
		{
			for (y in 0..19)
			{
				val e = Entity()

				val pos = PositionComponent()
				pos.position = grid[x][y]
				pos.slot = Enums.SpaceSlot.FLOOR
				grid[x][y].contents.put(pos.slot, e)
				e.add(pos)

				val sprite = SpriteComponent(AssetManager.loadSprite("grass"))
				e.add(sprite)

				engine.addEntity(e)
			}
		}

		val p = Entity()

		val pos = PositionComponent()
		pos.position = grid[10][10]
		pos.slot = Enums.SpaceSlot.ENTITY
		grid[10][10].contents.put(pos.slot, p)
		p.add(pos)

		val sprite = SpriteComponent(AssetManager.loadSprite("player"))
		p.add(sprite)

		val task = TaskComponent(TestAI())
		p.add(task)

		engine.addEntity(p)

		val level = Level()
		level.grid = grid

		level.player = p

		GlobalData.Global.currentLevel = level

		engine.addSystem(TaskProcessorSystem())
		engine.addSystem(SpriteUpdaterSystem())
		engine.addSystem(RenderSystem(batch))

		keyboardHelper = ButtonKeyboardHelper()
	}

	override fun doRender(delta: Float) {
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