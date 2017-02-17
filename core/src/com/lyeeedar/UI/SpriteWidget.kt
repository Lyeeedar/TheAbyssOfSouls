package com.lyeeedar.UI

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.HDRColourSpriteBatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.lyeeedar.Renderables.Sprite.Sprite

class SpriteWidget(private val drawable: Sprite, width: Float, height: Float, val fixHeight: Boolean = false) : Widget()
{
	private val scaling = Scaling.stretch
	private val align = Align.center
	private var imageX: Float = 0f
	private var imageY: Float = 0f
	private var imageWidth: Float = 0f
	private var imageHeight: Float = 0f

	init
	{
		this.width = width
		this.height = height
	}

	override fun layout()
	{
		val regionWidth = width
		val regionHeight = height

		val size = scaling.apply(regionWidth, regionHeight, width, height)
		imageWidth = size.x
		imageHeight = size.y

		if (align and Align.left != 0)
			imageX = 0f
		else if (align and Align.right != 0)
			imageX = (width - imageWidth).toInt().toFloat()
		else
			imageX = (width / 2 - imageWidth / 2).toInt().toFloat()

		if (align and Align.top != 0)
			imageY = (height - imageHeight).toInt().toFloat()
		else if (align and Align.bottom != 0)
			imageY = 0f
		else
			imageY = (height / 2 - imageHeight / 2).toInt().toFloat()
	}

	override fun draw(batch: Batch?, parentAlpha: Float)
	{
		validate()

		val color = color
		batch!!.setColor(color.r, color.g, color.b, color.a * parentAlpha)

		val x = x
		val y = if (fixHeight) y - 4f else y
		val scaleX = scaleX
		val scaleY = scaleY

		drawable.render(batch, x + imageX, y + imageY, imageWidth * scaleX, imageHeight * scaleY)
	}

	override fun getPrefWidth(): Float
	{
		return width
	}

	override fun getPrefHeight(): Float
	{
		return height
	}

	override fun setSize(width: Float, height: Float)
	{
		this.width = width
		this.height = height
	}

	override fun act(delta: Float)
	{
		super.act(delta)
		drawable.update(delta)
	}
}
