package com.lyeeedar.AI.BehaviourTree.Actions

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.ExecutionState
import com.lyeeedar.AI.Tasks.TaskWait
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.tile
import com.lyeeedar.Global
import com.lyeeedar.Util.Controls
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 23-Mar-16.
 */

class ActionProcessInput(): AbstractAction()
{
	override fun evaluate(entity: Entity): ExecutionState
	{
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

	override fun cancel()
	{

	}
}