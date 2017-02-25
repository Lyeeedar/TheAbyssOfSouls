package com.lyeeedar.Combo

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.PositionComponent
import com.lyeeedar.Components.RenderableComponent
import com.lyeeedar.Components.pos
import com.lyeeedar.Components.tile
import com.lyeeedar.Direction
import com.lyeeedar.Global
import com.lyeeedar.Level.Tile
import com.lyeeedar.Renderables.Particle.ParticleEffect
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.Point

class WaitComboStep: ComboStep()
{
	var particle: ParticleEffect? = null

	override fun activate(entity: Entity, direction: Direction, target: Point)
	{
		if (particle != null)
		{
			val p = particle!!.copy()
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

	override fun getAllValid(entity: Entity, direction: Direction): Array<Point>
	{
		val out = Array<Point>()
		out.add(entity.pos().position)
		return out
	}

	override fun isValid(entity: Entity, direction: Direction, target: Point, tree: ComboTree): Boolean
	{
		for (child in tree.random)
		{
			if (child.comboStep.isValid(entity, direction, target, child)) return true
		}

		return false
	}

	override fun parse(xml: XmlReader.Element)
	{
		val particleEl = xml.getChildByName("Particle")
		if (particleEl != null)
		{
			particle = AssetManager.loadParticleEffect(particleEl)
		}
	}
}