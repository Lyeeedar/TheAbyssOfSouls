package com.lyeeedar.AI

import com.badlogic.ashley.core.Entity
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.TaskMove
import com.lyeeedar.Enums
import com.lyeeedar.GlobalData
import com.lyeeedar.Util.Controls

/**
 * Created by Philip on 20-Mar-16.
 */

class TestAI(): IAI
{
	override fun update(e: Entity)
	{
		val task = Mappers.task.get(e)

		if (GlobalData.Global.controls.isKeyDown(Controls.Keys.LEFT))
		{
			task.tasks.add(TaskMove(Enums.Direction.WEST))
		}
		else if (GlobalData.Global.controls.isKeyDown(Controls.Keys.UP))
		{
			task.tasks.add(TaskMove(Enums.Direction.NORTH))
		}
		else if (GlobalData.Global.controls.isKeyDown(Controls.Keys.RIGHT))
		{
			task.tasks.add(TaskMove(Enums.Direction.EAST))
		}
		else if (GlobalData.Global.controls.isKeyDown(Controls.Keys.DOWN))
		{
			task.tasks.add(TaskMove(Enums.Direction.SOUTH))
		}
	}

}