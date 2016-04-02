package com.lyeeedar.AI.Tasks

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Matrix3
import com.badlogic.gdx.math.Vector3
import com.lyeeedar.Components.*
import com.lyeeedar.Enums
import com.lyeeedar.Events.EventArgs
import com.lyeeedar.GlobalData
import com.lyeeedar.Level.Tile
import com.lyeeedar.Sprite.Sprite

/**
 * Created by Philip on 02-Apr-16.
 */

class TaskDoAttack(val atk: Attack, val dir: Enums.Direction): AbstractTask(EventComponent.EventType.NONE)
{
	override fun execute(e: Entity)
	{
		val entityTile = e.tile() ?: return

		// actually do the attack
		if (atk.hitType.equals("all"))
		{
			val srcTiles = e.getEdgeTiles(dir)

			// find min and max tile
			var min: Tile? = null
			var max: Tile? = null
			val mat = Matrix3()
			mat.setToRotation( dir.angle )
			val vec = Vector3()

			for (tile in srcTiles)
			{
				for (point in atk.hitPoints)
				{
					vec.set( point.x.toFloat(), point.y.toFloat(), 0f );
					vec.mul( mat );

					val dx = Math.round( vec.x ).toInt();
					val dy = Math.round( vec.y ).toInt();

					val t = entityTile.level.getTile(tile, dx, dy) ?: continue
					if (t.x <= min?.x ?: Int.MAX_VALUE && t.y <= min?.y ?: Int.MAX_VALUE)
					{
						min = t
					}
					if (t.x >= max?.x ?: -Int.MAX_VALUE && t.y >= max?.y ?: -Int.MAX_VALUE)
					{
						max = t
					}
				}
			}

			val effectEntity = Entity()
			val effect = EffectComponent(atk.hitSprite.copy(), dir)
			effect.eventMap.put(Sprite.AnimationStage.MIDDLE, EventArgs(EventComponent.EventType.ALL, e, effectEntity, 0f))
			effectEntity.add(effect)

			val position = PositionComponent()
			position.min = min!!
			position.position = min
			position.max = max!!
			position.slot = Enums.SpaceSlot.AIR
			effectEntity.add(position)

			val event = EventComponent()
			event.parse(atk.effectData)
			effectEntity.add(event)

			GlobalData.Global.engine?.addEntity(effectEntity)
		}
	}

}