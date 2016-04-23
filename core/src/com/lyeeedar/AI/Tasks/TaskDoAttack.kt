package com.lyeeedar.AI.Tasks

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Matrix3
import com.badlogic.gdx.math.Vector3
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
 * Created by Philip on 02-Apr-16.
 */

class TaskDoAttack(val atk: Attack, val dir: Enums.Direction, val srcTileOffset: Point): AbstractTask(EventComponent.EventType.NONE)
{
	var comboAttack: TaskPrepareAttack? = null

	override fun execute(e: Entity)
	{
		val attack = Mappers.telegraphed.get(e) ?: return
		EffectApplier.apply(e, srcTileOffset, dir, attack.currentTarget ?: Point.ZERO, atk.hitPoints, atk.hitType, atk.hitSprite, atk.effectData )

		comboAttack?.execute(e)
	}

}