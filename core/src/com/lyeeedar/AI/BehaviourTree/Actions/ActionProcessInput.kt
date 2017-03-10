package com.lyeeedar.AI.BehaviourTree.Actions

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.ExecutionState
import com.lyeeedar.AI.Tasks.TaskCombo
import com.lyeeedar.AI.Tasks.TaskWait
import com.lyeeedar.Combo.ComboTree
import com.lyeeedar.Components.*
import com.lyeeedar.Direction
import com.lyeeedar.Global
import com.lyeeedar.Screens.AbstractScreen
import com.lyeeedar.Util.*


/**
 * Created by Philip on 23-Mar-16.
 */

class ActionProcessInput(): AbstractAction()
{
	override fun evaluate(entity: Entity): ExecutionState
	{
		if ((Global.game.screen as AbstractScreen).debugConsole.isVisible) return ExecutionState.FAILED

		val tile = entity.tile() ?: return ExecutionState.FAILED

		var targetPos = getData<Point>( "clickpos", null )

		if (targetPos != null)
		{
			setData( "clickpos", null )
		}
		else if ( Gdx.input.isTouched( 0 ) && !Global.release )
		{
			val touchX = Gdx.input.x
			val touchY = Gdx.input.y

			val mousePos = Global.stage.camera.unproject(Vector3(touchX.toFloat(), touchY.toFloat(), 0f))

			val mousePosX = mousePos.x.toInt()
			val mousePosY = mousePos.y.toInt()

			var offsetx = Global.resolution.x / 2 - tile.x * Global.tilesize - Global.tilesize / 2
			var offsety = Global.resolution.y / 2 - tile.y * Global.tilesize - Global.tilesize / 2

			val offset = entity.renderOffset()
			if (offset != null)
			{
				offsetx -= offset[0] * Global.tilesize
				offsety -= offset[1] * Global.tilesize
			}

			val mousex = ((mousePosX - offsetx) / Global.tilesize).toInt()
			val mousey = ((mousePosY - offsety) / Global.tilesize).toInt()

			val dx = mousex - tile.x
			val dy = mousey - tile.y

			if (dx == 0 && dy == 0)
			{
				if (Gdx.input.justTouched())
				{
					targetPos = Point.obtain().set(tile.x, tile.y)
				}
			}
			else if (Math.abs(dx) > Math.abs(dy))
			{
				if (dx < 0)
				{
					targetPos = Point.obtain().set(tile.x - 1, tile.y)
				}
				else
				{
					targetPos = Point.obtain().set(tile.x + 1, tile.y)
				}
			}
			else
			{
				if (dy < 0)
				{
					targetPos = Point.obtain().set(tile.x, tile.y - 1)
				}
				else
				{
					targetPos = Point.obtain().set(tile.x, tile.y + 1)
				}
			}
		}
		else
		{
			val up = Controls.Keys.UP.isDownAndNotConsumed()
			val down = Controls.Keys.DOWN.isDownAndNotConsumed()
			val left = Controls.Keys.LEFT.isDownAndNotConsumed()
			val right = Controls.Keys.RIGHT.isDownAndNotConsumed()
			val space = Controls.Keys.WAIT.consumePress()

			var x = 0
			var y = 0

			if ( up )
			{
				y = 1
			}
			else if ( down )
			{
				y = -1
			}

			if ( left )
			{
				x = -1
			}
			else if ( right )
			{
				x = 1
			}

			if ( x != 0 || y != 0 || space )
			{
				targetPos = Point.obtain().set( tile.x + x, tile.y + y )
			}
		}

		val combo = entity.combo()
		if (combo != null)
		{
			if (combo.currentCombo != null)
			{
				val current = combo.currentCombo!!

				fun tryDoAttack(key: ComboTree.ComboKey)
				{
					val next = current.keybinding[key]
					combo.currentCombo = if (next.keybinding.size > 0) next else null

					entity.task().tasks.add(TaskCombo(next, entity.pos().facing, tile))

					if (combo.currentCombo == null)
					{
						Future.call({ entity.directionalSprite()?.currentAnim = "idle" }, 0.5f)
					}
				}

				if (Controls.Keys.ATTACKNORMAL.consumePress())
				{
					if (current.keybinding.containsKey(ComboTree.ComboKey.ATTACKNORMAL))
					{
						tryDoAttack(ComboTree.ComboKey.ATTACKNORMAL)
					}
				}
				else if (Controls.Keys.ATTACKSPECIAL.consumePress())
				{
					if (current.keybinding.containsKey(ComboTree.ComboKey.ATTACKSPECIAL))
					{
						tryDoAttack(ComboTree.ComboKey.ATTACKSPECIAL)
					}
				}
				else if (Controls.Keys.DEFENSE.consumePress())
				{
					if (current.keybinding.containsKey(ComboTree.ComboKey.DEFENSE))
					{
						tryDoAttack(ComboTree.ComboKey.DEFENSE)
					}
				}
				else if (Global.controls.isDirectionDownAndNotConsumed())
				{
					if (current.keybinding.containsKey(ComboTree.ComboKey.DIRECTION))
					{
						val up = Controls.Keys.UP.consumePress()
						val down = Controls.Keys.DOWN.consumePress()
						val left = Controls.Keys.LEFT.consumePress()
						val right = Controls.Keys.RIGHT.consumePress()

						if (up) entity.pos().facing = Direction.NORTH
						else if (down) entity.pos().facing = Direction.SOUTH
						else if (left) entity.pos().facing = Direction.WEST
						else if (right) entity.pos().facing = Direction.EAST

						tryDoAttack(ComboTree.ComboKey.DIRECTION)
					}
					else
					{
						combo.currentCombo = null
					}
				}

				if (combo.currentCombo != null || entity.task().tasks.size > 0)
				{
					targetPos = null
				}
			}
			else
			{
				val first = combo.combos

				entity.directionalSprite()?.currentAnim = "idle"

				fun tryDoAttack(key: ComboTree.ComboKey)
				{
					val next = first.keybinding[key]
					if (entity.stats().stamina < next.staminaCost)
					{
						entity.stats().insufficientStamina = 0.5f
						entity.stats().insufficientStaminaAmount = next.staminaCost
						return
					}

					if (Global.controls.isDirectionDownAndNotConsumed())
					{
						val up = Controls.Keys.UP.consumePress()
						val down = Controls.Keys.DOWN.consumePress()
						val left = Controls.Keys.LEFT.consumePress()
						val right = Controls.Keys.RIGHT.consumePress()

						Controls.Keys.ATTACKNORMAL.consumePress()
						Controls.Keys.ATTACKSPECIAL.consumePress()
						Controls.Keys.DEFENSE.consumePress()

						if (up) entity.pos().facing = Direction.NORTH
						else if (down) entity.pos().facing = Direction.SOUTH
						else if (left) entity.pos().facing = Direction.WEST
						else if (right) entity.pos().facing = Direction.EAST

						combo.currentCombo = if (next.keybinding.size > 0) next else null

						entity.task().tasks.add(TaskCombo(next, entity.pos().facing, tile))
					}
					else
					{
						entity.directionalSprite()?.currentAnim = next.comboStep.anim
					}

					targetPos = null
				}

				if (Controls.Keys.ATTACKNORMAL.isDownAndNotConsumed() && first.keybinding.containsKey(ComboTree.ComboKey.ATTACKNORMAL))
				{
					tryDoAttack(ComboTree.ComboKey.ATTACKNORMAL)
				}
				else if (Controls.Keys.ATTACKSPECIAL.isDownAndNotConsumed() && first.keybinding.containsKey(ComboTree.ComboKey.ATTACKSPECIAL))
				{
					tryDoAttack(ComboTree.ComboKey.ATTACKSPECIAL)
				}
				else if (Controls.Keys.DEFENSE.isDownAndNotConsumed() && first.keybinding.containsKey(ComboTree.ComboKey.DEFENSE))
				{
					tryDoAttack(ComboTree.ComboKey.DEFENSE)
				}
			}
		}

		setData( "pos", null )
		if (targetPos != null)
		{
			if (targetPos == tile)
			{
				Mappers.task.get(entity).tasks.add(TaskWait())
			}
			else
			{
				val targetTile = tile.level.getTile(targetPos!!)
				if (targetTile != null)
				{
					for (e in targetTile.contents)
					{
						val interaction = e.interaction()
						if (interaction != null)
						{
							Global.interaction = interaction.interaction
							interaction.interaction.interact(entity, e)

							entity.task().tasks.add(TaskWait())

							break
						}
					}
				}

				if (Global.interaction == null) setData( "pos", targetPos )
			}
		}

		state = if (targetPos != null) ExecutionState.COMPLETED else ExecutionState.FAILED
		return state
	}

	override fun parse(xml: XmlReader.Element)
	{

	}

	override fun cancel(entity: Entity)
	{
		setData("pos", null)
	}
}