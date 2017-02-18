package com.lyeeedar.AI.BehaviourTree.Actions

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.ExecutionState
import com.lyeeedar.AI.Tasks.TaskWait
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.combo
import com.lyeeedar.Components.pos
import com.lyeeedar.Components.tile
import com.lyeeedar.Direction
import com.lyeeedar.Global
import com.lyeeedar.MainGame
import com.lyeeedar.Screens.AbstractScreen
import com.lyeeedar.Util.Controls
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 23-Mar-16.
 */

class ActionProcessInput(): AbstractAction()
{
	override fun evaluate(entity: Entity): ExecutionState
	{
		if ((Global.game.screen as AbstractScreen).debugConsole.isVisible) return ExecutionState.FAILED

		var targetPos = getData( "clickpos", null ) as? Point

		if ( targetPos != null )
		{
			setData( "clickpos", null )
		}
		else
		{
			val up = Global.controls.isKeyDown( Controls.Keys.UP )
			val down = Global.controls.isKeyDown( Controls.Keys.DOWN )
			val left = Global.controls.isKeyDown( Controls.Keys.LEFT )
			val right = Global.controls.isKeyDown( Controls.Keys.RIGHT )
			val space = Global.controls.isKeyDown( Controls.Keys.WAIT )

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
				val tile = Mappers.position.get(entity).position
				targetPos = Point.obtain().set( tile.x + x, tile.y + y )
			}
		}

		val combo = entity.combo()
		if (combo != null)
		{
			val tile = entity.tile()!!

			// clear map states
			for (t in tile.level.grid)
			{
				t.isSelectedPoint = false
				t.isValidHitPoint = false
				t.isValidTarget = false
			}

//			if (combo.selectedCombo != null)
//			{
//				val allowedDirs: Array<Direction>
//				if (combo.currentCombo == null)
//				{
//					allowedDirs = Direction.CardinalValues
//				}
//				else if (combo.selectedCombo!!.current.canTurn)
//				{
//					allowedDirs = arrayOf(entity.pos().facing, entity.pos().facing.cardinalClockwise, entity.pos().facing.cardinalAnticlockwise)
//				}
//				else
//				{
//					allowedDirs = arrayOf(entity.pos().facing)
//				}
//
//				for (t in tile.level.grid)
//				{
//					for (dir in allowedDirs)
//					{
//						if (combo.selectedCombo!!.current.isValidTarget(entity, dir, t))
//						{
//							t.isValidTarget = true
//						}
//						if (combo.selectedCombo!!.current.isValidHitPoint(entity, t.level.getTile(t, dir)!!, t))
//						{
//							t.isValidHitPoint = true
//						}
//					}
//				}
//			}
		}

		parent.setData( "pos", null )
		if (targetPos != null)
		{
			val tile = entity.tile() ?: return state
			if (targetPos == tile)
			{
				Mappers.task.get(entity).tasks.add(TaskWait())
			}
			else
			{
				parent.setData( "pos", targetPos )
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

	}
}