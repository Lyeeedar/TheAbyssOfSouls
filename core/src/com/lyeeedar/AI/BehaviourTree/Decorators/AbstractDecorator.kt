package com.lyeeedar.AI.BehaviourTree.Decorators

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.AbstractNodeContainer
import com.lyeeedar.AI.BehaviourTree.AbstractTreeNode

/**
 * Created by Philip on 21-Mar-16.
 */

abstract class AbstractDecorator(): AbstractNodeContainer()
{
	var node: AbstractTreeNode? = null
		set(value)
		{
			if ( value?.data == null )
			{
				value?.data = data;
			}
			value?.parent = this;
			field = value;
		}

	// ----------------------------------------------------------------------
	override fun parse( xml: XmlReader.Element)
	{
		val child = xml.getChild( 0 )

		val node = AbstractTreeNode.get(child.getAttribute("meta:RefKey").toUpperCase())
		this.node = node
		node.parent = this

		node.parse(child)
	}

	// ----------------------------------------------------------------------
	override fun cancel(entity: Entity)
	{
		node?.cancel(entity);
	}
}