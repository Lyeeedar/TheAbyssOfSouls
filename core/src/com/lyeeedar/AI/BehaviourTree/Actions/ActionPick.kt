package com.lyeeedar.AI.BehaviourTree.Actions

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.ExecutionState
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.stats
import com.lyeeedar.Components.tile
import com.lyeeedar.Global
import com.lyeeedar.Systems.level
import com.lyeeedar.Util.Point
import java.util.*

/**
 * Created by Philip on 21-Mar-16.
 */

class ActionPick(): AbstractAction()
{
	lateinit var input: String
	lateinit var output: String
	lateinit var criteria: String
	var lowest: Boolean = true

	val ran: Random = Random()

	override fun evaluate(entity: Entity): ExecutionState
	{
		state = ExecutionState.FAILED
		val tile = entity.tile() ?: return ExecutionState.FAILED

		val obj = getData(input, null)

		if (obj == null || obj !is Iterable<*>)
		{
			state = ExecutionState.FAILED
		}
		else
		{
			if (obj.count() == 0)
			{
				state = ExecutionState.FAILED
			}
			else
			{
				if (criteria == "random" || criteria == "ran" || criteria == "rnd")
				{
					val index = ran.nextInt(obj.count())
					parent.setData(output, obj.elementAt(index))
					state = ExecutionState.COMPLETED
				}
				else if (criteria == "distance" || criteria == "dist" || criteria == "dst")
				{
					obj.sortedBy { (it as? Point)?.taxiDist(tile) ?: (it as? Entity)?.tile()?.taxiDist(tile) }

					val item = if (lowest) obj.first() else obj.last()
					parent.setData(output, item)
					state = ExecutionState.COMPLETED
				}
				else if (criteria == "player")
				{
					for (e in obj)
					{
						if (e === Global.engine.level!!.player)
						{
							parent.setData(output, e)
							state = ExecutionState.COMPLETED
							break
						}
					}
				}
				else
				{
					obj.sortedBy { (it as? Entity)?.stats()?.variableMap?.get(criteria, 0f) }

					val item = if (lowest) obj.first() else obj.last()
					parent.setData(output, item);
					state = ExecutionState.COMPLETED;
				}
			}
		}

		return state
	}

	override fun parse(xml: XmlReader.Element)
	{
		input = xml.getAttribute("Input").toLowerCase()
		output = xml.getAttribute("Output").toLowerCase()
		criteria = xml.getAttribute("Criteria", "Distance").toLowerCase()
		lowest = xml.getBooleanAttribute("Lowest", true)
	}

	override fun cancel(entity: Entity) {

	}

}