package com.lyeeedar.AI.BehaviourTree.Selectors

import com.badlogic.gdx.utils.JsonValue
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.AbstractNodeContainer
import com.lyeeedar.AI.BehaviourTree.AbstractTreeNode
import com.lyeeedar.AI.BehaviourTree.Decorators.AbstractDecorator

/**
 * Created by Philip on 21-Mar-16.
 */

abstract class AbstractSelector(): AbstractNodeContainer()
{
	val nodes: com.badlogic.gdx.utils.Array<AbstractTreeNode> = com.badlogic.gdx.utils.Array<AbstractTreeNode>()

	// ----------------------------------------------------------------------
	fun addNode( node: AbstractTreeNode )
	{
		if ( node.data == null )
		{
			node.data = data;
		}
		node.parent = this;
		nodes.add( node );
	}

	// ----------------------------------------------------------------------
	override fun setData( key:String, value:Any? )
	{
		if ( value == null )
		{
			data?.remove( key );
		}
		else
		{
			data?.put( key, value );
		}

		for ( i in 0..nodes.size-1 )
		{
			if ( nodes.get( i ) is AbstractSelector || nodes.get( i ) is AbstractDecorator )
			{
				nodes.get( i ).setData( key, value );
			}
		}
	}

	// ----------------------------------------------------------------------
	override fun parse( xml: XmlReader.Element)
	{
		for ( i in 0.. xml.childCount -1 )
		{
			val value = xml.getChild(i)

			val node = AbstractTreeNode.get(value.name.toUpperCase())

			addNode( node );

			node.parse( value );
		}
	}
}
