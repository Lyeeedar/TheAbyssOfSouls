package com.lyeeedar.AI.BehaviourTree.Actions

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Matrix3
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.ExecutionState
import com.lyeeedar.AI.Tasks.TaskDoAttack
import com.lyeeedar.AI.Tasks.TaskPrepareAttack
import com.lyeeedar.Components.*
import com.lyeeedar.Enums
import com.lyeeedar.Events.EventArgs
import com.lyeeedar.GlobalData
import com.lyeeedar.Level.Level
import com.lyeeedar.Level.Tile
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Util.Point
import com.lyeeedar.Util.isAllies
import java.util.*
import com.lyeeedar.Util.ran

/**
 * Created by Philip on 31-Mar-16.
 */

class ActionTelegraphedAttack(): AbstractAction()
{
	data class ValidData(val combo: Combo, val direction: Enums.Direction)

	override fun evaluate(entity: Entity): ExecutionState
	{
		state = ExecutionState.FAILED

		val atkData = Mappers.telegraphed.get(entity) ?: return state
		entity.tile() ?: return state
		entity.stats() ?: return state

		if (atkData.currentCombo != null)
		{
			advanceCombo(entity)
		}
		else
		{
			beginCombo(entity)
		}

		return state
	}

	fun isValidAttack(attack: Attack, direction: Enums.Direction, srcTiles: com.badlogic.gdx.utils.Array<Tile>, entity: Entity): Boolean
	{
		val entityTile = entity.tile() ?: return false
		val entityStats = entity.stats() ?: return false

		val mat = Matrix3()
		mat.setToRotation( direction.angle )
		val vec = Vector3()

		for (tile in srcTiles)
		{
			for (point in attack.hitPoints)
			{
				vec.set( point.x.toFloat(), point.y.toFloat(), 0f );
				vec.mul( mat );

				val dx = Math.round( vec.x ).toInt();
				val dy = Math.round( vec.y ).toInt();

				val t = entityTile.level.getTile(tile, dx, dy) ?: continue

				for (e in t.contents)
				{
					val stats = e.stats() ?: continue
					if (!entityStats.factions.isAllies(stats.factions))
					{
						return true
					}
				}
			}
		}

		return false
	}

	fun readyAttack(entity: Entity)
	{
		val atkData = Mappers.telegraphed.get(entity)
		val entityTile = entity.tile() ?: return

		val rdy = Entity()
		rdy.add(SpriteComponent(atkData.currentAttack.readySprite.copy()))

		val task = Mappers.task.get(entity)
		task.tasks.add(TaskPrepareAttack(entityTile, rdy))

		atkData.readyEntity = rdy
	}

	fun beginCombo(entity: Entity)
	{
		val atkData = Mappers.telegraphed.get(entity)

		// find all valid combos + direction
		val valid = com.badlogic.gdx.utils.Array<ValidData>()

		for (dir in Enums.Direction.CardinalValues)
		{
			val srcTiles = entity.getEdgeTiles(dir)

			for (combo in atkData.combos)
			{
				if (isValidAttack(atkData.attacks.get(combo.steps[0].attack), dir, srcTiles, entity))
				{
					valid.add(ValidData(combo, dir))
				}
			}
		}

		// weight them
		val weightedValid = com.badlogic.gdx.utils.Array<ValidData>()
		for (v in valid)
		{
			for (i in 0..v.combo.weight)
			{
				weightedValid.add(v)
			}
		}

		if (weightedValid.size > 0)
		{
			// pick one randomly
			val chosen = weightedValid.random()

			// begin combo
			atkData.currentCombo = chosen.combo
			atkData.currentIndex = 0
			atkData.currentDir = chosen.direction

			readyAttack(entity)

			state = ExecutionState.RUNNING
		}
	}

	fun advanceCombo(entity: Entity)
	{
		val atkData = Mappers.telegraphed.get(entity)

		if (atkData.readyEntity != null)
		{
			//GlobalData.Global.engine?.removeEntity(atkData.readyEntity)
			atkData.readyEntity = null
		}

		// do actual attack
		val combo = atkData.currentComboStep

		// move forward if able
		if (combo.canMove)
		{
			// try to move forward
		}

		val task = Mappers.task.get(entity)
		task.tasks.add(TaskDoAttack(atkData.currentAttack, atkData.currentDir))

		//Queue next attack if possible else end
		val comboData = atkData.currentCombo ?: throw RuntimeException("Somehow combo got set to null whilst processing")
		if (atkData.currentIndex < comboData.steps.size-1)
		{
			atkData.currentIndex++
			val cstep = atkData.currentComboStep
			val catk = atkData.currentAttack

			val valid = com.badlogic.gdx.utils.Array<Enums.Direction>()
			if (isValidAttack(catk, atkData.currentDir, entity.getEdgeTiles(atkData.currentDir), entity))
			{
				for (i in 0..3)
				{
					valid.add(atkData.currentDir)
				}
			}
			if (cstep.canTurn)
			{
				val cw = atkData.currentDir.clockwise.clockwise
				if (isValidAttack(catk, cw, entity.getEdgeTiles(cw), entity))
				{
					valid.add(cw)
				}

				val ccw = atkData.currentDir.anticlockwise.anticlockwise
				if (isValidAttack(catk, ccw, entity.getEdgeTiles(ccw), entity))
				{
					valid.add(ccw)
				}
			}

			if (valid.size == 0 && cstep.canEnd)
			{
				atkData.currentCombo = null
				atkData.currentDir = Enums.Direction.CENTER
				atkData.currentIndex = 0
				atkData.currentTarget = null
				state = ExecutionState.COMPLETED
			}
			else
			{
				if (valid.size > 0)
				{
					atkData.currentDir = valid.random()
				}

				readyAttack(entity)

				state = ExecutionState.RUNNING
			}
		}
		else
		{
			atkData.currentCombo = null
			atkData.currentDir = Enums.Direction.CENTER
			atkData.currentIndex = 0
			atkData.currentTarget = null
			state = ExecutionState.COMPLETED
		}
	}

	override fun parse(xml: XmlReader.Element)
	{

	}

	override fun cancel()
	{

	}
}