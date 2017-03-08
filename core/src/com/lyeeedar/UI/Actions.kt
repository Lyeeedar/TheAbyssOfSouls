package com.lyeeedar.UI

import com.badlogic.gdx.scenes.scene2d.Action
import com.lyeeedar.Util.Random

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
			shakeAngle += (150 + Random.random() * 60)
		}

		target.moveBy(-offsetx, -offsety)

		offsetx = Math.sin( shakeAngle.toDouble() ).toFloat() * shakeRadius
		offsety = Math.cos( shakeAngle.toDouble() ).toFloat() * shakeRadius

		target.moveBy(offsetx, offsety)

		return time >= duration
	}
}

fun shake(amount: Float, speed: Float, duration: Float): ShakeAction = ShakeAction(amount, speed, duration)

class LambdaAction(val lambda: ()->Unit) : Action()
{
	override fun act(delta: Float): Boolean
	{
		lambda.invoke()

		return true
	}
}

fun lamda(lambda: ()->Unit): LambdaAction = LambdaAction(lambda)