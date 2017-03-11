package com.lyeeedar.Systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.lyeeedar.AI.Tasks.TaskInterrupt
import com.lyeeedar.Components.*
import com.lyeeedar.Global
import com.lyeeedar.Renderables.Animation.BlinkAnimation
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.Colour

class StatisticsSystem : AbstractSystem(Family.one(StatisticsComponent::class.java).get())
{
	val blockEffect = AssetManager.loadParticleEffect("Block")
	val blockBrokenEffect = AssetManager.loadParticleEffect("BlockBroken")
	val hitEffect = AssetManager.loadParticleEffect("Hit")

	override fun doUpdate(deltaTime: Float)
	{
		for (entity in entities)
		{
			processEntity(entity)
		}
	}

	override fun onTurn()
	{
		for (e in entities)
		{
			if (e.stats().regeneratingHP > 0f)
			{
				e.stats().regenerationTimer++

				if (e.stats().regenerationTimer > 4)
				{
					val toregen = Math.min(1f, e.stats().regeneratingHP)
					e.stats().hp += toregen
				}
			}
		}

		super.onTurn()
	}

	fun processEntity(entity: Entity)
	{
		val stats = entity.stats()!!

		if (stats.hp <= 0)
		{
			if (entity.getComponent(MarkedForDeletionComponent::class.java) == null) entity.add(MarkedForDeletionComponent())
		}

		if (stats.tookDamage)
		{
			if (stats.showHp)
			{
				val sprite = entity.renderable()?.renderable as? Sprite

				if (sprite != null)
				{
					sprite.colourAnimation = BlinkAnimation.obtain().set(Colour(1f, 0.5f, 0.5f, 1f), sprite.colour, 0.15f, true)
				}
			}

			val tile = entity.tile()
			if (tile != null)
			{
				val p = hitEffect.copy()
				p.size[0] = entity.pos().size
				p.size[1] = entity.pos().size

				val pe = Entity()
				pe.add(RenderableComponent(p))
				val ppos = PositionComponent()

				ppos.size = entity.pos().size
				ppos.position = entity.pos().position

				pe.add(ppos)
				Global.engine.addEntity(pe)
			}

			stats.tookDamage = false
		}

		if (stats.blockBroken)
		{
			stats.blockBroken = false

			val p = blockBrokenEffect.copy()
			p.size[0] = entity.pos().size
			p.size[1] = entity.pos().size

			val pe = Entity()
			pe.add(RenderableComponent(p))
			val ppos = PositionComponent()

			ppos.size = entity.pos().size
			ppos.position = entity.pos().position

			pe.add(ppos)
			Global.engine.addEntity(pe)

			entity.task().tasks.add(TaskInterrupt())
		}
		else if (stats.blockedDamage)
		{
			stats.blockedDamage = false

			val p = blockEffect.copy()
			p.size[0] = entity.pos().size
			p.size[1] = entity.pos().size

			val pe = Entity()
			pe.add(RenderableComponent(p))
			val ppos = PositionComponent()

			ppos.size = entity.pos().size
			ppos.position = entity.pos().position

			pe.add(ppos)
			Global.engine.addEntity(pe)
		}
	}
}
