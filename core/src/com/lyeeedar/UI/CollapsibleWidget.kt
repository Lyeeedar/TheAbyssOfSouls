package com.lyeeedar.UI

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.badlogic.gdx.utils.GdxRuntimeException

/**
 * Widget containing table that can be collapsed.
 * @author Kotcrab
 * *
 * @since 0.3.1
 */
class CollapsibleWidget : WidgetGroup
{
	private var table: Table? = null

	private val collapseAction = CollapseAction()

	private var collapsed: Boolean = false
	private var actionRunning: Boolean = false

	private var currentWidth: Float = 0.toFloat()

	constructor()
	{
	}

	@JvmOverloads constructor(table: Table?, collapsed: Boolean = false)
	{
		this.collapsed = collapsed
		this.table = table

		updateTouchable()

		if (table != null) addActor(table)
	}

	fun setCollapsed(collapse: Boolean, withAnimation: Boolean)
	{
		this.collapsed = collapse
		updateTouchable()

		if (table == null) return

		actionRunning = true

		if (withAnimation)
		{
			addAction(collapseAction)
			if (!collapse) collapseAction.pad = 0.3f
		}
		else
		{
			if (collapse)
			{
				currentWidth = 0f
				collapsed = true
			} else
			{
				currentWidth = table!!.prefWidth
				collapsed = false
			}

			actionRunning = false
			invalidateHierarchy()
		}
	}

	var isCollapsed: Boolean
		get() = collapsed
		set(collapse) = setCollapsed(collapse, true)

	val isActionRunning: Boolean
		get() = actionRunning

	private fun updateTouchable()
	{
		if (collapsed)
			touchable = Touchable.disabled
		else
			touchable = Touchable.enabled
	}

	override fun draw(batch: Batch?, parentAlpha: Float)
	{
		if (currentWidth > 1)
		{
			batch!!.flush()
			val clipEnabled = clipBegin(x, y, currentWidth, height)

			super.draw(batch, parentAlpha)

			batch.flush()
			if (clipEnabled) clipEnd()
		}
	}

	override fun layout()
	{
		if (table == null) return

		table!!.setBounds(0f, 0f, table!!.prefWidth, table!!.prefHeight)

		if (actionRunning == false)
		{
			if (collapsed)
				currentWidth = 0f
			else
				currentWidth = table!!.prefWidth
		}
	}

	override fun getPrefHeight(): Float
	{
		return if (table == null) 0f else table!!.prefHeight
	}

	override fun getPrefWidth(): Float
	{
		if (table == null) return 0f

		if (actionRunning == false)
		{
			if (collapsed)
				return 0f
			else
				return table!!.prefWidth
		}

		return currentWidth
	}

	fun setTable(table: Table)
	{
		this.table = table
		clearChildren()
		addActor(table)
	}

	override fun childrenChanged()
	{
		super.childrenChanged()
		if (children.size > 1) throw GdxRuntimeException("Only one actor can be added to CollapsibleWidget")
	}

	private inner class CollapseAction : Action()
	{
		var pad: Float = 0f

		override fun act(delta: Float): Boolean
		{
			pad -= delta
			if (pad < 0f)
			{
				if (collapsed)
				{
					currentWidth -= delta * 1000
					if (currentWidth <= 0)
					{
						currentWidth = 0f
						collapsed = true
						actionRunning = false
					}
				} else
				{
					currentWidth += delta * 1000
					if (currentWidth > table!!.prefWidth)
					{
						currentWidth = table!!.prefWidth
						collapsed = false
						actionRunning = false
					}
				}
			}

			invalidateHierarchy()
			return !actionRunning
		}
	}
}