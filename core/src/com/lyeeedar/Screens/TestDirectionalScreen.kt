package com.lyeeedar.Screens

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.lyeeedar.Combo.ComboStep
import com.lyeeedar.Combo.SlashComboStep
import com.lyeeedar.Combo.WaitComboStep
import com.lyeeedar.Components.*
import com.lyeeedar.Global
import com.lyeeedar.Level.Level
import com.lyeeedar.Level.Tile
import com.lyeeedar.Renderables.Sprite.DirectionalSprite
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Systems.createEngine
import com.lyeeedar.Util.*

class TestDirectionalScreen : AbstractScreen()
{
	lateinit var font: BitmapFont
	lateinit var batch: SpriteBatch

	override fun create()
	{
		Global.currentLevel = Level()
		Global.currentLevel.ambient.set(Colour.WHITE)

		Global.currentLevel.grid = Array2D(10, 10) { x, y -> Tile() }
		for (tile in Global.currentLevel.grid)
		{
			val e = Entity()
			val sprite = SpriteComponent(AssetManager.loadSprite("Oryx/uf_split/uf_terrain/ground_grass_1"))
			e.add(sprite)

			val pos = PositionComponent()
			e.add(pos)
			pos.position = tile
			pos.slot = SpaceSlot.FLOOR

			tile.contents[SpaceSlot.FLOOR] = e

			Global.engine.addEntity(e)
		}

		val player = EntityLoader.load("player")

		Global.currentLevel.player = player
		Global.currentLevel.grid[2, 2].contents[player.pos()!!.slot] = player
		player.pos()!!.position = Global.currentLevel.grid[2, 2]

		Global.engine.addEntity(player)

		val monster = EntityLoader.load("monster")

		Global.currentLevel.grid[7, 7].contents[monster.pos()!!.slot] = monster
		monster.pos()!!.position = Global.currentLevel.grid[7, 7]

		Global.engine.addEntity(monster)

		font = Global.skin.getFont("default")
		batch = SpriteBatch()
	}

	override fun doRender(delta: Float)
	{
		Global.engine.update(delta)

		batch.begin()

		font.draw(batch, "$mousex,$mousey", 20f, Global.resolution[ 1 ] - 20f)

		batch.end()
	}

	// ----------------------------------------------------------------------
	override fun mouseMoved( screenX: Int, screenY: Int ): Boolean
	{
		val player = Global.currentLevel.player
		val playerPos = Mappers.position.get(player)
		val playerSprite = Mappers.sprite.get(player)

		var offsetx = Global.resolution[ 0 ] / 2 - playerPos.position.x * 32f - 32f / 2
		var offsety = Global.resolution[ 1 ] / 2 - playerPos.position.y * 32f - 32f / 2

		val offset = playerSprite.sprite.animation?.renderOffset()
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