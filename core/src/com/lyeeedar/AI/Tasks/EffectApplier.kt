package com.lyeeedar.AI.Tasks

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Matrix3
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.*
import com.lyeeedar.Enums
import com.lyeeedar.Events.EventActionDamage
import com.lyeeedar.Events.EventArgs
import com.lyeeedar.GlobalData
import com.lyeeedar.Level.Tile
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Sprite.SpriteAnimation.BumpAnimation
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 23-Apr-16.
 */

class EffectApplier()
{
	companion object
	{
		fun apply(
				srcEntity: Entity, srcTileOffset: Point, // src data
				dir: Enums.Direction, targetTile: Point, // target data
				hitPoints: com.badlogic.gdx.utils.Array<Point>, hitType: String, // hit data
				hitSprite: Sprite, effectData: XmlReader.Element) // effect data
		{
			val entityTile = srcEntity.tile() ?: return
			val stats = srcEntity.stats() ?: return

			srcEntity.sprite().sprite.spriteAnimation = BumpAnimation.obtain().set(0.25f, dir);

			// actually do the attack
			if (hitType.equals("all"))
			{
				// find min and max tile
				var min: Tile? = null
				var max: Tile? = null
				val mat = Matrix3()
				mat.setToRotation( dir.angle )
				val vec = Vector3()

				for (point in hitPoints)
				{
					vec.set( point.x.toFloat(), point.y.toFloat(), 0f );
					vec.mul( mat );

					val dx = Math.round( vec.x ).toInt() + srcTileOffset.x;
					val dy = Math.round( vec.y ).toInt() + srcTileOffset.y;

					val t = entityTile.level.getTile(entityTile, dx, dy) ?: continue
					if (t.x <= min?.x ?: Int.MAX_VALUE && t.y <= min?.y ?: Int.MAX_VALUE)
					{
						min = t
					}
					if (t.x >= max?.x ?: -Int.MAX_VALUE && t.y >= max?.y ?: -Int.MAX_VALUE)
					{
						max = t
					}
				}

				val effectEntity = Entity()
				val effect = EffectComponent(hitSprite.copy(), dir)
				effect.eventMap.put(Sprite.AnimationStage.MIDDLE, EventArgs(EventComponent.EventType.HIT, srcEntity, effectEntity, 0f))
				effectEntity.add(effect)

				val position = PositionComponent()
				position.min = min!!
				position.max = max!!
				position.slot = Enums.SpaceSlot.AIR
				effectEntity.add(position)

				val event = EventComponent()
				event.parseHitData(effectData)
				for (group in event.handlers.get(EventComponent.EventType.HIT))
				{
					for (action in group.actions)
					{
						if (action is EventActionDamage)
						{
							for (elem in Enums.ElementType.Values)
							{
								val baseAtk = stats.attack.get(elem)
								val sclAtk = action.damMap.get(elem)

								val sclVal = sclAtk / 100f
								val newAtk = baseAtk * sclVal

								action.damMap.put(elem, newAtk)
							}
						}
					}
				}
				effectEntity.add(event)

				GlobalData.Global.engine.addEntity(effectEntity)

				for (x in min.x..max.x)
				{
					for (y in min.y..max.y)
					{
						val tile = entityTile.level.getTile(x, y) ?: continue
						tile.effects.add(effectEntity)
					}
				}

				srcTileOffset.free()
			}
		}
	}
}
