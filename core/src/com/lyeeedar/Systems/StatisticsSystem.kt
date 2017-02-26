package com.lyeeedar.Systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.lyeeedar.AI.Tasks.TaskInterrupt
import com.lyeeedar.Components.*
import com.lyeeedar.Global
import com.lyeeedar.Util.AssetManager

class StatisticsSystem : AbstractSystem(Family.one(StatisticsComponent::class.java).get())
{
	val blockEffect = AssetManager.loadParticleEffect("Block")
	val blockBrokenEffect = AssetManager.loadParticleEffect("BlockBroken")

	override fun doUpdate(deltaTime: Float)
	{
		for (entity in entities)
		{
			processEntity(entity)
		}
	}

	fun processEntity(entity: Entity)
	{
		val stats = entity.stats()!!

		if (stats.hp <= 0)
		{
			if (entity.getComponent(MarkedForDeletionComponent::class.java) == null) entity.add(MarkedForDeletionComponent())
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
		else if (stats.blockBroken)
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
	}
}
