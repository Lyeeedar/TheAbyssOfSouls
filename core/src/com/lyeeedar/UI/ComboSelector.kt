package com.lyeeedar.UI

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.badlogic.gdx.utils.Array
import com.lyeeedar.Combo.ComboTree
import com.lyeeedar.Components.combo
import com.lyeeedar.Util.Future
import com.lyeeedar.Util.sequenceEquals

class ComboSelector(val entity: Entity) : Table()
{
	lateinit var content: CollapsibleWidget
	val contentTable = Table()
	val selectedTable = Table()

	val currentCombos = Array<ComboTree>()
	val targetCombos = Array<ComboTree>()

	init
	{
		content = CollapsibleWidget(contentTable, false)
		this.add(content).expand().fill().right()
		this.add(selectedTable).size(64f).right()
	}

	override fun act(delta: Float)
	{
		super.act(delta)

		val combo = entity.combo() ?: return

		targetCombos.clear()

		if (combo.currentCombo == null)
		{
			targetCombos.addAll(combo.combos)
		}
		else
		{
			targetCombos.addAll(combo.currentCombo!!.next)
		}

		if (!currentCombos.asSequence().sequenceEquals(targetCombos.asSequence()))
		{
			if (content.isCollapsed && !content.isActionRunning)
			{
				currentCombos.clear()
				currentCombos.addAll(targetCombos)

				contentTable.clear()
				for (item in currentCombos)
				{
					contentTable.add(SpriteWidget(item.current.icon, 48f, 48f, false))
				}
				content.invalidate()
				content.setCollapsed(false, true)
			}
			else if (!content.isCollapsed)
			{
				content.setCollapsed(true, true)
			}
		}
	}
}