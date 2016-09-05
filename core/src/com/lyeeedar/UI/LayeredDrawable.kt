package com.lyeeedar.UI

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Array

/**
 * Created by Philip on 02-Jan-16.
 */
class LayeredDrawable(vararg drawables: Drawable) : Drawable
{
	var layers = Array<Drawable>()

	init
	{
		layers.addAll(*drawables)
	}

	override fun draw(batch: Batch, x: Float, y: Float, width: Float, height: Float)
	{
		for (drawable in layers)
		{
			drawable.draw(batch, x, y, width, height)
		}
	}

	override fun getLeftWidth(): Float
	{
		return layers.get(0).leftWidth
	}

	override fun setLeftWidth(leftWidth: Float)
	{
		for (drawable in layers)
		{
			drawable.leftWidth = leftWidth
		}
	}

	override fun getRightWidth(): Float
	{
		return layers.get(0).rightWidth
	}

	override fun setRightWidth(rightWidth: Float)
	{
		for (drawable in layers)
		{
			drawable.rightWidth = rightWidth
		}
	}

	override fun getTopHeight(): Float
	{
		return layers.get(0).topHeight
	}

	override fun setTopHeight(topHeight: Float)
	{
		for (drawable in layers)
		{
			drawable.topHeight = topHeight
		}
	}

	override fun getBottomHeight(): Float
	{
		return layers.get(0).bottomHeight
	}

	override fun setBottomHeight(bottomHeight: Float)
	{
		for (drawable in layers)
		{
			drawable.bottomHeight = bottomHeight
		}
	}

	override fun getMinWidth(): Float
	{
		return layers.get(0).minWidth
	}

	override fun setMinWidth(minWidth: Float)
	{
		for (drawable in layers)
		{
			drawable.minWidth = minWidth
		}
	}

	override fun getMinHeight(): Float
	{
		return layers.get(0).minHeight
	}

	override fun setMinHeight(minHeight: Float)
	{
		for (drawable in layers)
		{
			drawable.minHeight = minHeight
		}
	}
}
