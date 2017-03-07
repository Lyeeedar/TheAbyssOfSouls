package com.lyeeedar.Screens

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.lyeeedar.Components.EntityLoader
import com.lyeeedar.Components.name
import com.lyeeedar.Components.pos
import com.lyeeedar.Components.renderable
import com.lyeeedar.GenerationGrammar.GenerationGrammar
import com.lyeeedar.Global
import com.lyeeedar.Level.LevelData
import com.lyeeedar.Level.Tile
import com.lyeeedar.Level.World
import com.lyeeedar.Save.SaveGame
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Systems.level
import com.lyeeedar.Systems.systemList
import com.lyeeedar.UI.DebugConsole
import com.lyeeedar.UI.IDebugCommandProvider
import com.lyeeedar.Util.Future
import com.lyeeedar.Util.Point

class GameScreen : AbstractScreen()
{
	lateinit var font: BitmapFont
	lateinit var batch: SpriteBatch

	var timeMultiplier = 1f
	var showFPS = true

	data class SystemData(val name: String, var time: Float, var entities: String)
	val systemTimes: Array<SystemData> by lazy { Array<SystemData>(systemList.size) { i -> SystemData(systemList[i].java.simpleName.replace("System", ""), 0f, "--") } }
	var totalSystemTime: Float = 0f
	var drawSystemTime = false

	var clickedTile: Tile? = null
	var selectedEntity: Entity? = null
	var selectedComponent: IDebugCommandProvider? = null

	init
	{
		instance = this
	}

	override fun create()
	{
		loadLevel(World.world.root)

		font = Global.skin.getFont("default")
		batch = SpriteBatch()
	}

	fun loadLevel(level: LevelData)
	{
		Global.engine.removeAllEntities()

		val grammar = GenerationGrammar.load(level.grammar)

		val start = System.nanoTime()
		val level = grammar.generate(10, Global.engine, true)
		val end = System.nanoTime()

		level.updateMetaRegions()

		val player = EntityLoader.load("player")
		Global.engine.addEntity(player)
		val spawnTile = level.getClosestMetaRegion("playerspawn", Point.ZERO)!!
		player.pos().position = spawnTile
		spawnTile.contents[SpaceSlot.ENTITY] = player

		level.player = player

		println("Time: " + ((end - start)/1000000f))

		Global.engine.level = level
	}

	override fun doRender(delta: Float)
	{
		Global.engine.update(delta * timeMultiplier)

		systemUpdateAccumulator += delta
		if (systemUpdateAccumulator > 0.5f)
		{
			systemUpdateAccumulator = 0f
			var totalTime = 0f

			for (i in 0..systemTimes.size-1)
			{
				val system = Global.engine.getSystem(systemList[i].java)
				val time = system.processDuration
				val perc = (time / frameDuration) * 100f
				systemTimes[i].time = perc
				systemTimes[i].entities = if (system.family != null) system.entities.size().toString() else "-"

				totalTime += perc
			}

			totalSystemTime = totalTime
		}

		batch.begin()

		font.draw(batch, "$mousex,$mousey", 20f, Global.resolution.y - 20f)

		if (showFPS)
		{
			font.draw(batch, "FPS: $fps", Global.resolution.x - 100f, Global.resolution.y - 20f)
		}

		if (drawSystemTime)
		{
			val x = 20f
			var y = Global.resolution.y - 40f

			font.draw(batch, "System Debug: Total time usage - $totalSystemTime%", x, y)
			y -= 20f

			for (pair in systemTimes.sortedByDescending { it.time })
			{
				font.draw(batch, pair.name + " (" + pair.entities + ")", x, y)
				font.draw(batch, " - ${pair.time}%", x + 15 * 10, y)
				y -= 20f
			}
		}

		batch.end()
	}

	override fun show()
	{
		DebugConsole.register("TimeMultiplier", "'TimeMultiplier speed' to enable, 'TimeMultiplier false' to disable", fun (args, console): Boolean {
			if (args[0] == "false")
			{
				timeMultiplier = 1f
				return true
			}
			else
			{
				try
				{
					val speed = args[0].toFloat()
					timeMultiplier = speed

					return true
				}
				catch (ex: Exception)
				{
					console.error(ex.message!!)
					return false
				}
			}
		})

		DebugConsole.register("ShowFPS", "'ShowFPS true' to enable, 'ShowFPS false' to disable", fun (args, console): Boolean {
			if (args[0] == "false")
			{
				showFPS = false
				return true
			}
			else if (args[0] == "true")
			{
				showFPS = true
				return true
			}

			return false
		})

		DebugConsole.register("SystemDebug", "'SystemDebug true' to enable, 'SystemDebug false' to disable", fun (args, console): Boolean {
			if (args[0] == "false")
			{
				drawSystemTime = false
				return true
			}
			else if (args[0] == "true")
			{
				drawSystemTime = true
				return true
			}

			return false
		})

		DebugConsole.register("Select", "", fun (args, console): Boolean {
			if (clickedTile == null)
			{
				console.error("No tile selected!")
			}
			else if (args.size == 1)
			{
				if (selectedEntity != null)
				{
					val componentName = args[0] + "component"
					for (component in selectedEntity!!.components)
					{
						if (component.javaClass.simpleName.toLowerCase() == componentName)
						{
							if (component is IDebugCommandProvider)
							{
								selectedComponent = component
								component.attachCommands()

								console.write("")
								console.write("Selected component: " + args[0])

								return true
							}
							else
							{

								console.error("Component has no debugging commands!")
								return false
							}
						}
					}
				}

				val slot = SpaceSlot.valueOf(args[0].toUpperCase())

				if (clickedTile!!.contents.containsKey(slot))
				{
					selectedEntity = clickedTile!!.contents[slot]
					selectedComponent?.detachCommands()
					selectedComponent = null

					console.write("")
					console.write("Selected entity: " + (selectedEntity!!.name()?.name ?: selectedEntity.toString()))
					for (component in selectedEntity!!.components)
					{
						val isProvider = component is IDebugCommandProvider
						console.write(component.javaClass.simpleName.replace("Component", "") + if (isProvider) " - Debug" else "")
					}

					return true
				}
				else
				{
					console.error("No entity in that slot on the selected tile!")
				}
			}
			else
			{
				console.error("Too many arguments!")
			}

			return false
		})

		DebugConsole.register("Save", "", fun (args, console): Boolean {
			SaveGame.save(Global.engine.level!!)

			return true
		})

		DebugConsole.register("Load", "", fun (args, console): Boolean {

			Future.call({
							Global.engine.removeAllEntities()
							val level = SaveGame.load()
							Global.engine.level = level
						}, 0.01f)

			return true
		})

		super.show()
	}

	override fun hide()
	{
		DebugConsole.unregister("TimeMultiplier")
		DebugConsole.unregister("ShowFPS")
		DebugConsole.unregister("SystemDebug")

		super.hide()
	}

	// ----------------------------------------------------------------------
	override fun mouseMoved( screenX: Int, screenY: Int ): Boolean
	{
		updateMousePos(screenX, screenY)

		return true
	}

	override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean
	{
		updateMousePos(screenX, screenY)

		if (debugConsole.isVisible)
		{
			val tile = Global.engine.level!!.getTile(mousex, mousey)
			if (tile != null)
			{
				tile.isSelectedPoint = true
				Future.call({ tile.isSelectedPoint = false }, 1f, tile)

				debugConsole.write("")
				debugConsole.write("Select point: $mousex,$mousey")

				for (slot in SpaceSlot.Values)
				{
					if (tile.contents.containsKey(slot))
					{
						debugConsole.write(slot.toString() + ": " + (tile.contents[slot].name()?.name ?: tile.contents[slot].toString()))
					}
				}

				selectedEntity = null
				selectedComponent?.detachCommands()
				selectedComponent = null

				clickedTile = tile
			}
		}

		return super.touchDown(screenX, screenY, pointer, button)
	}

	fun updateMousePos(screenX: Int, screenY: Int)
	{
		val level = Global.engine.level!!
		val player = level.player
		val playerPos = player.pos()!!
		val playerSprite = player.renderable() ?: return

		var offsetx = Global.resolution.x / 2 - playerPos.position.x * Global.tilesize - Global.tilesize / 2
		var offsety = Global.resolution.y / 2 - playerPos.position.y * Global.tilesize - Global.tilesize / 2

		val offset = playerSprite.renderable.animation?.renderOffset()
		if (offset != null)
		{
			offsetx -= offset[0] * Global.tilesize
			offsety -= offset[1] * Global.tilesize
		}

		mousex = ((screenX - offsetx) / Global.tilesize).toInt()
		mousey = (((Global.resolution[1] - screenY) - offsety) / Global.tilesize).toInt()
	}

	var mousex: Int = 0
	var mousey: Int = 0

	var systemUpdateAccumulator = 0f

	companion object
	{
		lateinit var instance: GameScreen
	}
}