package com.lyeeedar.AI.BehaviourTree

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.XmlReader

/**
 * Created by Philip on 21-Mar-16.
 */

class BehaviourTree()
{
	lateinit var root: AbstractNodeContainer

	fun setData(key: String, value: Any?) = root.setData(key, value)

	fun update(entity: Entity)
	{
		root.evaluate(entity)
	}

	companion object
	{
		fun load(path: String): BehaviourTree
		{
			val root = AbstractTreeNode.load(path)
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