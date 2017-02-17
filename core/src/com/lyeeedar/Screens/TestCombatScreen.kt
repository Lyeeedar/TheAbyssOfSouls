package com.lyeeedar.Screens

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.lyeeedar.Components.*
import com.lyeeedar.Global
import com.lyeeedar.Level.Level
import com.lyeeedar.Level.Tile
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Systems.level
import com.lyeeedar.Util.Array2D
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.Colour

class TestCombatScreen : AbstractScreen()
{
	lateinit var font: BitmapFont
	lateinit var batch: SpriteBatch

	override fun create()
	{
		val level = Level()
		Global.engine.level = level
		level.ambient.set(Colour.WHITE)

		level.grid = Array2D(10, 10) { x, y -> Tile() }
		for (tile in level.grid)
		{
			val e = Entity()
			val sprite = RenderableComponent(AssetManager.loadSprite("Oryx/uf_split/uf_terrain/ground_grass_1"))
			e.add(sprite)

			val pos = PositionComponent()
			e.add(pos)
			pos.position = tile
			pos.slot = SpaceSlot.FLOOR

			tile.contents[SpaceSlot.FLOOR] = e

			Global.engine.addEntity(e)
		}

		val player = EntityLoader.load("player")

		level.player = player
		level.grid[2, 2].contents[player.pos()!!.slot] = player
		player.pos()!!.position = level.grid[2, 2]

		Global.engine.addEntity(player)

		val monster = EntityLoader.load("testmonster")

		level.grid[7, 7].contents[monster.pos()!!.slot] = monster
		monster.pos()!!.position = level.grid[7, 7]

		Global.engine.addEntity(monster)

		font = Global.skin.getFont("default")
		batch = SpriteBatch()
	}

	override fun doRender(delta: Float)
	{
		Global.engine.update(delta)

		batch.begin()

		font.draw(batch, "$mousex,$mousey", 20f, Global.resolution.y - 20f)

		batch.end()
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