package com.lyeeedar.AI.BehaviourTree.Actions

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.ExecutionState
import com.lyeeedar.AI.Tasks.TaskMove
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Direction
import com.lyeeedar.GlobalData
import com.lyeeedar.Level.Tile
import com.lyeeedar.Pathfinding.Pathfinder
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 21-Mar-16.
 */

class ActionMoveTo(): AbstractAction()
{
	var dst: Int = 0
	var towards: Boolean = true
	lateinit var key: String

	var lastPos: Point = Point.ZERO

	override fun evaluate(entity: Entity): ExecutionState
	{
		val target = getData( key, null ) as? Point;
		val posData = Mappers.position.get(entity)
		val taskData = Mappers.task.get(entity)
		val tile = posData.position as? Tile

		// doesnt have all the needed data, fail
		if ( target == null || posData == null || tile == null || taskData == null )
		{
			state = ExecutionState.FAILED;
			return state;
		}

		// if we arrived at our target, succeed
		if ( (towards && tile.taxiDist(target) <= dst) || (!towards && tile.taxiDist(target) >= dst) )
		{
			lastPos = Point.ZERO
			state = ExecutionState.COMPLETED;
			return state;
		}

		val pathFinder = Pathfinder(tile.level.grid.array, tile.x, tile.y, target.x, target.y, GlobalData.Global.canMoveDiagonal, posData.size, entity);
		val path = pathFinder.getPath( posData.slot );

		if (path == null)
		{
			lastPos = Point.ZERO
			state = ExecutionState.FAILED;
			return state;
		}

		// if couldnt find a valid path, fail
		if ( path.size < 2 )
		{
			lastPos = Point.ZERO
			Point.freeAll(path)
			state = ExecutionState.FAILED;
			return state;
		}

		var nextTile = tile.level.getTile( path.get( 1 ) );

		// possible loop, quit just in case
		if (nextTile == lastPos)
		{
			lastPos = Point.ZERO
			Point.freeAll(path)
			state = ExecutionState.FAILED;
			return state;
		}

		// if next step is impassable then fail
		if ( ! ( nextTile?.getPassable( posData.slot, entity ) ?: false ) )
		{
			lastPos = Point.ZERO
			Point.freeAll(path)
			state = ExecutionState.FAILED;
			return state;
		}

		var offset = path.get( 1 ) - path.get( 0 );

		// if moving towards path to the object
		if ( towards )
		{
			if ( path.size - 1 <= dst || offset == Point.ZERO )
			{
				lastPos = Point.ZERO
				Point.freeAll(path)
				offset.free()
				state = ExecutionState.COMPLETED;
				return state;
			}

			lastPos = tile
			taskData.tasks.add(TaskMove(Direction.getDirection(offset)));
		}
		// if moving away then just run directly away
		else
		{
			if ( path.size - 1 >= dst || offset == Point.ZERO )
			{
				lastPos = Point.ZERO
				Point.freeAll(path)
				offset.free()
				state = ExecutionState.COMPLETED;
				return state;
			}

			lastPos = tile
			val opposite = offset * -1
			taskData.tasks.add(TaskMove(Direction.getDirection(opposite)));
			opposite.free()
		}

		Point.freeAll(path)
		offset.free()
		state = ExecutionState.RUNNING;
		return state;
	}

	override fun cancel()
	{
		lastPos = Point.ZERO
	}

	override fun parse( xml: XmlReader.Element)
	{
		if (xml.name.equals("MoveTo"))
		{
			dst = Integer.parseInt( xml.getAttribute( "Distance", "0" ) );
			towards = true;

		}
		else
		{
			dst = Integer.parseInt( xml.getAttribute( "Distance", "500" ) );
			towards = false;
		}
		key = xml.getAttribute( "Key" ).toLowerCase()
	}
}