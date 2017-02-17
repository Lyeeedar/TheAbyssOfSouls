package com.lyeeedar.UI

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Action

class ShakeAction(val amount: Float, val speed: Float, val duration: Float) : Action()
{
	var time = 0f

	var shakeRadius = amount
	var shakeAccumulator = 0f
	var shakeAngle = 0f

	var offsetx = 0f
	var offsety = 0f

	override fun act(delta: Float): Boolean
	{
		time += delta
		shakeAccumulator += delta
		while ( shakeAccumulator >= speed )
		{
			shakeAccumulator -= speed
			shakeAngle += ( 150 + MathUtils.random() * 60 )
		}

		target.moveBy(-offsetx, -offsety)

		offsetx = Math.sin( shakeAngle.toDouble() ).toFloat() * shakeRadius
		offsety = Math.cos( shakeAngle.toDouble() ).toFloat() * shakeRadius

		target.moveBy(offsetx, offsety)

		return time >= duration
	}
}

fun shake(amount: Float, speed: Float, duration: Float): ShakeAction = ShakeAction(amount, speed, duration)