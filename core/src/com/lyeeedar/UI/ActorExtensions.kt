package com.lyeeedar.UI

import com.badlogic.gdx.scenes.scene2d.Actor

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