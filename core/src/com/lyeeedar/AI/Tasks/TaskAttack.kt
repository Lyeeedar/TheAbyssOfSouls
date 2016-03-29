package com.lyeeedar.AI.Tasks

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.scenes.scene2d.actions.EventAction
import com.lyeeedar.Components.EffectComponent
import com.lyeeedar.Components.EventComponent
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.tile
import com.lyeeedar.Enums
import com.lyeeedar.Events.EventActionDamage
import com.lyeeedar.Events.EventActionGroup
import com.lyeeedar.Events.EventArgs
import com.lyeeedar.GlobalData
import com.lyeeedar.Items.Item
import com.lyeeedar.Sprite.Sprite

/**
 * Created by Philip on 29-Mar-16.
 */

class TaskAttack(var direction: Enums.Direction): AbstractTask(EventComponent.EventType.ATTACK)
{
	override fun execute(e: Entity)
	{
		val tile = e.tile() ?: return

		val next = tile.neighbours.get(direction)

		val weapon: Item

		val sprite = weapon.hitSprite.copy()

		val effect = Entity()
		effect.add(EffectComponent(sprite, Sprite.AnimationStage.END, EventArgs(EventComponent.EventType.ALL, null, e, 0f)))

		val event = EventComponent()
		val group = EventActionGroup()
		group.actions.add(EventActionDamage(group, weapon.stats))
		event.registerHandler(EventComponent.EventType.ALL, group)
		effect.add(event)

		GlobalData.Global.engine.addEntity(effect)
	}
}