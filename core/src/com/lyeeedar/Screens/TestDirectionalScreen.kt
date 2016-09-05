package com.lyeeedar.Screens

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
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
	override fun create()
	{
		Global.currentLevel = Level()
		Global.currentLevel.ambient.set(Colour.WHITE)

		Global.currentLevel.grid = Array2D(5, 5) { x, y -> Tile() }
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

		val dirSprite = DirectionalSprite()
		dirSprite.upSprites["idle"] = AssetManager.loadSprite("Monster/rat_up_idle", drawActualSize = true)
		dirSprite.downSprites["idle"] = AssetManager.loadSprite("Monster/rat_down_idle", drawActualSize = true)

		player.remove(SpriteComponent::class.java)
		player.add(DirectionalSpriteComponent(dirSprite))

		Global.engine.addEntity(player)
	}

	override fun doRender(delta: Float)
	{
		Global.engine.update(delta)
	}
}