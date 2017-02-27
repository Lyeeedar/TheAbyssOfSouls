package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.lyeeedar.AI.BehaviourTree.BehaviourTree
import com.lyeeedar.AI.IAI
import com.lyeeedar.AI.Tasks.AbstractTask
import com.lyeeedar.UI.DebugConsole
import com.lyeeedar.UI.IDebugCommandProvider

class TaskComponent: Component, IDebugCommandProvider
{
	constructor(ai: IAI)
	{
		this.ai = ai
	}

	constructor(path: String)
	{
		ai = BehaviourTree.load(path)
	}

	val ai: IAI
	val tasks: com.badlogic.gdx.utils.Array<AbstractTask> = com.badlogic.gdx.utils.Array()

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

			console.write("Data count: " + (ai as BehaviourTree).root.data!!.size)
			for (pair in ai.root.data!!.entries())
			{
				console.write(pair.key + ": " + pair.value)
			}

			return true
		})
	}
}