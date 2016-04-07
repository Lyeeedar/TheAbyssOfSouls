package com.lyeeedar.AI.BehaviourTree.Actions

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Matrix3
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.ExecutionState
import com.lyeeedar.AI.Tasks.TaskDoAttack
import com.lyeeedar.AI.Tasks.TaskMove
import com.lyeeedar.AI.Tasks.TaskPrepareAttack
import com.lyeeedar.Components.*
import com.lyeeedar.Enums
import com.lyeeedar.GlobalData
import com.lyeeedar.Level.Tile
import com.lyeeedar.Util.Point
import com.lyeeedar.Util.isAllies

/**
 * Created by Philip on 31-Mar-16.
 */

class ActionTelegraphedAttack(): AbstractAction()
{
	data class ValidData(val combo: Combo, val direction: Enums.Direction, val srcTile: Tile)
	var readyEntity: Entity? = null
		set(value)
		{
			if (field != null)
			{
				GlobalData.Global.engine?.removeEntity(field)
			}

			field = value
		}

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

	fun isValidAttack(attack: Attack, direction: Enums.Direction, srcTile: Tile, entity: Entity): Boolean
	{
		val entityTile = entity.tile() ?: return false
		val entityStats = entity.stats() ?: return false

		val mat = Matrix3()
		mat.setToRotation( direction.angle )
		val vec = Vector3()

		for (point in attack.hitPoints)
		{
			vec.set( point.x.toFloat(), point.y.toFloat(), 0f );
			vec.mul( mat );

			val dx = Math.round( vec.x ).toInt();
			val dy = Math.round( vec.y ).toInt();

			val t = entityTile.level.getTile(srcTile, dx, dy) ?: continue

			for (e in t.contents)
			{
				val stats = e.stats() ?: continue
				if (!entityStats.factions.isAllies(stats.factions))
				{
					return true
				}
			}
		}

		return false
	}

	fun readyAttack(entity: Entity)
	{
		val atkData = Mappers.telegraphed.get(entity)

		val rdy = Entity()
		rdy.add(SpriteComponent(atkData.currentAttack.readySprite.copy()))

		var minTile: Point = Point.MAX
		var maxTile: Point = Point.MIN

		if (atkData.currentAttack.readyType == "closest")
		{
			minTile = (atkData.currentSource!! + atkData.currentDir)
			maxTile = minTile
		}
		else if (atkData.currentAttack.readyType == "target")
		{
			minTile = atkData.currentTarget!!
			maxTile = minTile
		}
		else if (atkData.currentAttack.readyType == "left")
		{
			minTile = atkData.currentSource!! + atkData.currentDir.anticlockwise
			maxTile = minTile
		}
		else if (atkData.currentAttack.readyType == "left")
		{
			minTile = atkData.currentSource!! + atkData.currentDir.clockwise
			maxTile = minTile
		}
		else if (atkData.currentAttack.readyType == "pattern")
		{
			val mat = Matrix3()
			mat.setToRotation( atkData.currentDir.angle )
			val vec = Vector3()

			for (point in atkData.currentAttack.readyPoints)
			{
				vec.set(point.x.toFloat(), point.y.toFloat(), 0f);
				vec.mul(mat);

				val dx = Math.round(vec.x).toInt();
				val dy = Math.round(vec.y).toInt();

				val pos = atkData.currentSource!! + Point(dx, dy)

				if (pos < minTile)
				{
					minTile = pos
				}
				if (pos > maxTile)
				{
					maxTile = pos
				}
			}
		}
		else
		{
			throw RuntimeException("Invalid Ready type '"+atkData.currentAttack.readyType+"'!")
		}

		val task = Mappers.task.get(entity)
		val prepareAtk = TaskPrepareAttack(minTile - entity.tile()!!, maxTile - entity.tile()!!, rdy, atkData.currentDir)

		if (task.tasks.size > 0 && task.tasks.last() is TaskDoAttack)
		{
			(task.tasks.last() as TaskDoAttack).comboAttack = prepareAtk
		}
		else
		{
			task.tasks.add(prepareAtk)
		}

		readyEntity = rdy
	}

	fun beginCombo(entity: Entity)
	{
		val atkData = Mappers.telegraphed.get(entity)

		readyEntity = null

		// find all valid combos + direction
		val valid = com.badlogic.gdx.utils.Array<ValidData>()

		for (dir in Enums.Direction.CardinalValues)
		{
			val srcTiles = entity.getEdgeTiles(dir)

			combo@ for (combo in atkData.combos)
			{
				for (tile in srcTiles)
				{
					if (isValidAttack(atkData.attacks.get(combo.steps[0].attack), dir, tile, entity))
					{
						valid.add(ValidData(combo, dir, tile))
						continue@combo
					}
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
			atkData.currentSource = chosen.srcTile

			readyAttack(entity)

			state = ExecutionState.RUNNING
		}
	}

	fun advanceCombo(entity: Entity)
	{
		val atkData = Mappers.telegraphed.get(entity)

		readyEntity = null
		val task = Mappers.task.get(entity)

		// do actual attack
		val combo = atkData.currentComboStep

		// move forward if able
		if (combo.canMove)
		{
			// try to move forward
			val move = TaskMove(atkData.currentDir)
			move.cost = 0f

			task.tasks.add(move)
		}

		task.tasks.add(TaskDoAttack(atkData.currentAttack, atkData.currentDir, atkData.currentSource!! - entity.tile()!!))

		//Queue next attack if possible else end
		val comboData = atkData.currentCombo ?: throw RuntimeException("Somehow combo got set to null whilst processing")
		if (atkData.currentIndex < comboData.steps.size-1)
		{
			atkData.currentIndex++
			val cstep = atkData.currentComboStep
			val catk = atkData.currentAttack

			val valid = com.badlogic.gdx.utils.Array<ValidData>()
			updateValid(comboData, atkData.currentDir, 3, entity, catk, valid)

			if (cstep.canTurn)
			{
				val cw = atkData.currentDir.clockwise.clockwise
				updateValid(comboData, cw, 1, entity, catk, valid)


				val ccw = atkData.currentDir.anticlockwise.anticlockwise
				updateValid(comboData, ccw, 1, entity, catk, valid)
			}

			if (valid.size == 0 && cstep.canEnd)
			{
				atkData.currentCombo = null
				atkData.currentDir = Enums.Direction.CENTER
				atkData.currentIndex = 0
				atkData.currentTarget = null
				atkData.currentSource = null
				state = ExecutionState.COMPLETED
			}
			else
			{
				if (valid.size > 0)
				{
					val chosen = valid.random()
					atkData.currentDir = chosen.direction
					atkData.currentSource = chosen.srcTile
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
			atkData.currentSource = null
			state = ExecutionState.COMPLETED
		}
	}

	fun updateValid(combo: Combo, direction: Enums.Direction, count: Int, entity: Entity, attack: Attack, valid: com.badlogic.gdx.utils.Array<ValidData>)
	{
		var srcTiles = entity.getEdgeTiles(direction)
		for (srcTile in srcTiles)
		{
			if (isValidAttack(attack, direction, srcTile, entity))
			{
				for (i in 0..count-1)
				{
					valid.add(ValidData(combo, direction, srcTile))
				}
			}
		}
	}

	override fun parse(xml: XmlReader.Element)
	{

	}

	override fun cancel()
	{
		readyEntity = null
	}
}