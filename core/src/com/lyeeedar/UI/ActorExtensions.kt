package com.lyeeedar.UI

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.lyeeedar.Global

fun Actor.ensureOnScreen(pad: Float = 0f)
{
	if (width > stage.width - pad*2f)
	{
		width = stage.width - pad*2f
	}

	if (height > stage.height - pad*2f)
	{
		height = stage.height - pad*2f
	}

	// Fit within stage

	if (x < pad)
	{
		x = pad
	}
	else if (x + width > stage.width - pad)
	{
		x = stage.width - width - pad
	}

	if (y < pad)
	{
		y = pad
	}
	else if (y + height > stage.height - pad)
	{
		y = stage.height - height - pad
	}

	setPosition(x, y)
}

fun Actor.addClickListener(func: () -> Unit)
{
	this.addListener(object : ClickListener() {
		override fun clicked(event: InputEvent?, x: Float, y: Float)
		{
			super.clicked(event, x, y)
			func()
		}
	})
}

fun Actor.addToolTip(title: String, body: String, stage: Stage)
{
	val titleLabel = Label(title, Global.skin, "title")
	val bodyLabel = Label(body, Global.skin)

	val table = Table()
	table.add(titleLabel).expandX().center()
	table.row()
	table.add(bodyLabel).expand().fill()

	val tooltip = Tooltip(table, Global.skin, stage)

	this.addListener(TooltipListener(tooltip))
}