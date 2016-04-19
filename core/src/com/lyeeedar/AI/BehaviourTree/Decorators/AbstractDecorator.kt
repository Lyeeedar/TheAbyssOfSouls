package com.lyeeedar.AI.BehaviourTree.Decorators

import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.AbstractNodeContainer
import com.lyeeedar.AI.BehaviourTree.AbstractTreeNode
import com.lyeeedar.AI.BehaviourTree.Selectors.AbstractSelector
import com.lyeeedar.Util.Point

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
	override fun setData( key:String, value:Any? )
	{
		val oldVal = data?.get(key)
		if (oldVal != value && oldVal is Point && oldVal.javaClass == Point::class.java)
		{
			oldVal.free()
		}

		if ( value == null )
		{
			data?.remove( key );
		}
		else
		{
			data?.put( key, value );
		}

		if ( node is AbstractSelector || node is AbstractDecorator )
		{
			node?.setData( key, value );
		}
	}

	// ----------------------------------------------------------------------
	override fun parse( xml: XmlReader.Element)
	{
		val child = xml.getChild( 0 );

		val node = AbstractTreeNode.get(child.name.toUpperCase())
		this.node = node
		node.parent = this

		node.parse(child)
	}

	// ----------------------------------------------------------------------
	override fun cancel()
	{
		node?.cancel();
	}
}