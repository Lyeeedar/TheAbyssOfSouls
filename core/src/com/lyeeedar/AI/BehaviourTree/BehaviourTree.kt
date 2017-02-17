package com.lyeeedar.AI.BehaviourTree

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.IAI

/**
 * Created by Philip on 21-Mar-16.
 */

class BehaviourTree(): IAI
{
	lateinit var root: AbstractNodeContainer

	override fun setData(key: String, value: Any?) { root.setData(key, value) }

	override fun update(e: Entity)
	{
		root.evaluate(e)
	}

	override fun cancel(e: Entity)
	{
		root.cancel(e)
	}

	companion object
	{
		fun load(path: String): BehaviourTree
		{
			val root = AbstractTreeNode.load(path, true)
			val tree = BehaviourTree()
			tree.root = root as AbstractNodeContainer

			return tree
		}
	}
}

enum class ExecutionState
{
	NONE,
	RUNNING,
	COMPLETED,
	FAILED
}