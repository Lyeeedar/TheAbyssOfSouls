package com.lyeeedar.Screens

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.lyeeedar.Components.*
import com.lyeeedar.GenerationGrammar.GenerationGrammar
import com.lyeeedar.Global
import com.lyeeedar.Level.Level
import com.lyeeedar.Level.Tile
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Systems.level
import com.lyeeedar.UI.DebugConsole
import com.lyeeedar.Util.Array2D
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.Colour

class TestCombatScreen : AbstractScreen()
{
	lateinit var font: BitmapFont
	lateinit var batch: SpriteBatch

	var timeMultiplier = 1f

	override fun create()
	{
		val grammar = GenerationGrammar.load("Test")

		val level = grammar.generate(10, Global.engine)
		Global.engine.level = level
		level.ambient.set(Colour.WHITE)


		font = Global.skin.getFont("default")
		batch = SpriteBatch()
	}

	override fun doRender(delta: Float)
	{
		Global.engine.update(delta * timeMultiplier)

		batch.begin()

		font.draw(batch, "$mousex,$mousey", 20f, Global.resolution.y - 20f)

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

		super.show()
	}

	override fun hide()
	{
		DebugConsole.unregister("TimeMultiplier")

		super.hide()
	}

	// ----------------------------------------------------------------------
	override fun mouseMoved( screenX: Int, screenY: Int ): Boolean
	{
		val level = Global.engine.level!!
		val player = level.player
		val playerPos = player.pos()!!
		val playerSprite = player.renderable()!!

		var offsetx = Global.resolution.x / 2 - playerPos.position.x * 32f - 32f / 2
		var offsety = Global.resolution.y / 2 - playerPos.position.y * 32f - 32f / 2

		val offset = playerSprite.renderable.animation?.renderOffset()
		if (offset != null)
		{
			offsetx -= offset[0] * 32f
			offsety -= offset[1] * 32f
		}

		mousex = ((screenX - offsetx) / 32f).toInt()
		mousey = (((Global.resolution[1] - screenY) - offsety) / 32f).toInt()

		return true
	}

	var mousex: Int = 0
	var mousey: Int = 0
}