package com.lyeeedar.AI.BehaviourTree.Actions

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.ExecutionState
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.stats
import com.lyeeedar.Components.tile
import com.lyeeedar.GlobalData
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
		state = ExecutionState.FAILED;
		val tile = entity.tile() ?: return ExecutionState.FAILED

		var obj = getData(input, null);

		if (obj == null || obj !is Iterable<*>)
		{
			state = ExecutionState.FAILED;
		}
		else
		{
			if (obj.count() == 0)
			{
				state = ExecutionState.FAILED;
			}
			else
			{
				if (criteria.equals("random") || criteria.equals("ran") || criteria.equals("rnd"))
				{
					var index = ran.nextInt(obj.count());
					parent.setData(output, obj.elementAt(index));
					state = ExecutionState.COMPLETED;
				}
				else if (criteria.equals("distance") || criteria.equals("dist") || criteria.equals("dst"))
				{
					obj.sortedBy { if (it is Point) it.taxiDist(tile) else (it as? Entity)?.tile()?.taxiDist(tile) }

					val item = if (lowest) obj.first() else obj.last()
					parent.setData(output, item);
					state = ExecutionState.COMPLETED;
				}
				else if (criteria.equals("player"))
				{
					for (e in obj)
					{
						if (e === GlobalData.Global.currentLevel?.player)
						{
							parent.setData(output, e);
							state = ExecutionState.COMPLETED;
							break;
						}
					}
				}
				else
				{
					obj.sortedBy { (it as? Entity)?.stats()?.variableMap?.get(criteria) }

					val item = if (lowest) obj.first() else obj.last()
					parent.setData(output, item);
					state = ExecutionState.COMPLETED;
				}
			}
		}

		return state;
	}

	override fun parse(xml: XmlReader.Element)
	{
		input = xml.getAttribute("Input")
		output = xml.getAttribute("Output")
		criteria = xml.getAttribute("Criteria", "Distance").toLowerCase()
		lowest = xml.getBooleanAttribute("Lowest", true)
	}

	override fun cancel() {

	}

}