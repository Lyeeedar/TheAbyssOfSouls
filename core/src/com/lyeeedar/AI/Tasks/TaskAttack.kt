package com.lyeeedar.AI.Tasks

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.scenes.scene2d.actions.EventAction
import com.lyeeedar.Components.*
import com.lyeeedar.Enums
import com.lyeeedar.Events.EventActionDamage
import com.lyeeedar.Events.EventActionGroup
import com.lyeeedar.Events.EventArgs
import com.lyeeedar.GlobalData
import com.lyeeedar.Items.Item
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Sprite.SpriteAnimation.BumpAnimation

/**
 * Created by Philip on 29-Mar-16.
 */

class TaskAttack(var direction: Enums.Direction): AbstractTask(EventComponent.EventType.ATTACK)
{
	override fun execute(e: Entity)
	{
		val tile = e.tile() ?: return
		e.sprite().sprite.spriteAnimation = BumpAnimation.obtain().set(0.25f, direction)

		val next = tile.neighbours.get(direction)

		val weapon: Item = e.getEquip(Enums.EquipmentSlot.WEAPON)

		val sprite = weapon.hitSprite?.copy() ?: return

		val effect = Entity()
		effect.add(EffectComponent(sprite, direction, Sprite.AnimationStage.MIDDLE, EventArgs(EventComponent.EventType.HIT, null, effect, 0f)))

		val event = EventComponent()
		val group = EventActionGroup()
		group.actions.add(EventActionDamage(group, weapon.stats))
		event.registerHandler(EventComponent.EventType.HIT, group)
		effect.add(event)

		val pos = PositionComponent(next)
		pos.max = next
		effect.add(pos)
		next.effects.add(effect)

		GlobalData.Global.engine.addEntity(effect)
	}
}