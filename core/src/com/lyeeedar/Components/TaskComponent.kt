package com.lyeeedar.Components

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.BehaviourTree
import com.lyeeedar.AI.Tasks.AbstractTask
import com.lyeeedar.UI.DebugConsole
import com.lyeeedar.UI.IDebugCommandProvider

class TaskComponent: AbstractComponent(), IDebugCommandProvider
{
	lateinit var ai: BehaviourTree
	val tasks: com.badlogic.gdx.utils.Array<AbstractTask> = com.badlogic.gdx.utils.Array()

	override fun parse(xml: XmlReader.Element, entity: Entity)
	{
		ai = BehaviourTree.load(xml.get("AI"))
	}

	override fun detachCommands()
	{
		DebugConsole.unregister("Tasks")
		DebugConsole.unregister("AIData")
	}

	override fun attachCommands()
	{
		DebugConsole.register("Tasks", "", fun (args, console): Boolean {

			console.write("Task count: " + tasks.size)
			for (task in tasks)
			{
				console.write(task.toString())
			}

			return true
		})

		DebugConsole.register("AIData", "", fun (args, console): Boolean {

			console.write("Data count: " + ai.root.data!!.size)
			for (pair in ai.root.data!!.entries())
			{
				console.write(pair.key + ": " + pair.value)
			}

			return true
		})
	}
}