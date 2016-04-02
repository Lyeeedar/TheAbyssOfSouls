package com.lyeeedar.AI.BehaviourTree.Actions

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Matrix3
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.ExecutionState
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

		val atkData = Mappers.telegraphed.get(entity)
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
		rdy.add(PositionComponent(entityTile))
		GlobalData.Global.engine?.addEntity(rdy)

		atkData.readyEntity = rdy
	}

	fun beginCombo(entity: Entity)
	{
		val atkData = Mappers.telegraphed.get(entity)

		// find all valid combos + direction
		val valid = com.badlogic.gdx.utils.Array<ValidData>()

		for (dir in Enums.Direction.values())
		{
			val srcTiles = getSrcTiles(entity, dir)

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
		val entityTile = entity.tile() ?: return

		if (atkData.readyEntity != null)
		{
			GlobalData.Global.engine?.removeEntity(atkData.readyEntity)
		}

		// do actual attack
		val combo = atkData.currentComboStep
		val atk = atkData.currentAttack

		// move forward if able
		if (combo.canMove)
		{
			// try to move forward
		}

		// actually do the attack
		if (atk.hitType.equals("all"))
		{
			val srcTiles = getSrcTiles(entity, atkData.currentDir)

			// find min and max tile
			var min: Tile? = null
			var max: Tile? = null
			val mat = Matrix3()
			mat.setToRotation( atkData.currentDir.angle )
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
			val effect = EffectComponent(atk.hitSprite.copy(), atkData.currentDir)
			effect.eventMap.put(Sprite.AnimationStage.MIDDLE, EventArgs(EventComponent.EventType.ALL, entity, effectEntity, 0f))
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

		//Queue next attack if possible else end
		val comboData = atkData.currentCombo ?: throw RuntimeException("Somehow combo got set to null whilst processing")
		if (atkData.currentIndex < comboData.steps.size-1)
		{
			atkData.currentIndex++
			val cstep = atkData.currentComboStep
			val catk = atkData.currentAttack

			val valid = com.badlogic.gdx.utils.Array<Enums.Direction>()
			if (isValidAttack(catk, atkData.currentDir, getSrcTiles(entity, atkData.currentDir), entity))
			{
				for (i in 0..3)
				{
					valid.add(atkData.currentDir)
				}
			}
			if (cstep.canTurn)
			{
				val cw = atkData.currentDir.clockwise.clockwise
				if (isValidAttack(catk, cw, getSrcTiles(entity, cw), entity))
				{
					valid.add(cw)
				}

				val ccw = atkData.currentDir.anticlockwise.anticlockwise
				if (isValidAttack(catk, ccw, getSrcTiles(entity, ccw), entity))
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

	fun getSrcTiles(entity: Entity, dir: Enums.Direction): com.badlogic.gdx.utils.Array<Tile>
	{
		val pos = entity.position()
		val tile = entity.tile() ?: throw RuntimeException("argh tile is null")

		var xstep = 0;
		var ystep = 0;

		var sx = 0;
		var sy = 0;

		if ( dir == Enums.Direction.NORTH )
		{
			sx = 0;
			sy = pos.size - 1;

			xstep = 1;
			ystep = 0;
		}
		else if ( dir == Enums.Direction.SOUTH )
		{
			sx = 0;
			sy = 0;

			xstep = 1;
			ystep = 0;
		}
		else if ( dir == Enums.Direction.EAST )
		{
			sx = pos.size - 1;
			sy = 0;

			xstep = 0;
			ystep = 1;
		}
		else if ( dir == Enums.Direction.WEST )
		{
			sx = 0;
			sy = 0;

			xstep = 0;
			ystep = 1;
		}

		val tiles = com.badlogic.gdx.utils.Array<Tile>()
		for (i in 0..pos.size-1)
		{
			val t = tile.level.getTile(tile, sx + xstep * i, sy + ystep * i);
			tiles.add(t)
		}

		return tiles
	}

	override fun parse(xml: XmlReader.Element)
	{

	}

	override fun cancel()
	{

	}
}