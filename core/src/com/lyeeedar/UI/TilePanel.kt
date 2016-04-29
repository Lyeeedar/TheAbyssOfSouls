package com.lyeeedar.UI

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.HDRColourSpriteBatch
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.input.GestureDetector.GestureListener
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pools
import com.lyeeedar.AssetManager
import com.lyeeedar.Sprite.Sprite

abstract class TilePanel(protected val skin: Skin, stage: Stage, protected var tileBackground: Sprite?, protected var tileBorder: Sprite?, protected var targetWidth: Int, protected var targetHeight: Int, protected var tileSize: Int, protected var expandVertically: Boolean) : Widget()
{
	private val thisRef = this

	protected var viewWidth: Int = 0
	protected var viewHeight: Int = 0

	protected var dataWidth: Int = 0
	protected var dataHeight: Int = 0

	protected var scrollX: Int = 0
	protected var scrollY: Int = 0

	var padding = 10

	var canBeExamined = true

	protected var tilePanelBackgroundH: NinePatch
	protected var tilePanelBackgroundV: NinePatch
	protected var drawHorizontalBackground = true

	protected var tileData = Array<Any>()
	protected var mouseOver: Any? = null

	init
	{
		this.stage = stage

		this.viewWidth = targetWidth
		this.viewHeight = targetHeight

		this.tilePanelBackgroundH = NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/PanelHorizontal.png"), 21, 21, 21, 21)
		this.tilePanelBackgroundV = NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/PanelVertical.png"), 21, 21, 21, 21)

		val listener = TilePanelListener()

		this.addListener(listener)
		this.width = prefWidth
	}

	abstract fun populateTileData()

	abstract fun getSpriteForData(data: Any): Sprite?

	abstract fun handleDataClicked(data: Any, event: InputEvent, x: Float, y: Float)

	abstract fun getToolTipForData(data: Any): Table?

	abstract fun getColourForData(data: Any): Color

	abstract fun onDrawItemBackground(data: Any, batch: Batch, x: Int, y: Int, width: Int, height: Int)

	abstract fun onDrawItem(data: Any, batch: Batch, x: Int, y: Int, width: Int, height: Int)

	abstract fun onDrawItemForeground(data: Any, batch: Batch, x: Int, y: Int, width: Int, height: Int)

	fun isPointInThis(x: Int, y: Int): Boolean
	{
		var vec2 = Pools.obtain(Vector2::class.java).set(x.toFloat(), y.toFloat())
		vec2 = this.screenToLocalCoordinates(vec2)

		if (vec2.x >= 0 && vec2.y >= 0 && vec2.x <= width && vec2.y <= height)
		{
			Pools.free(vec2)
			return true
		}
		Pools.free(vec2)
		return false
	}

	override fun invalidate()
	{
		super.invalidate()

		if (expandVertically)
		{
			viewHeight = ((height - padding) / (tileSize + padding)).toInt()
		}
	}

	override fun getMinWidth(): Float
	{
		return ((tileSize + padding) * targetWidth + padding).toFloat()
	}

	override fun getMinHeight(): Float
	{
		return ((tileSize + padding) * targetHeight + padding).toFloat()
	}

	private fun validateScroll()
	{
		val scrollableX = Math.max(0, dataWidth - viewWidth)
		val scrollableY = Math.max(0, dataHeight - viewHeight)

		scrollX = MathUtils.clamp(scrollX, 0, scrollableX)
		scrollY = MathUtils.clamp(scrollY, 0, scrollableY)
	}

	override fun draw(batch: Batch?, parentAlpha: Float)
	{
		populateTileData()
		validateScroll()

		val height = viewHeight * (tileSize + padding) + padding
		var width = viewWidth * (tileSize + padding) + padding

		if (dataHeight > viewHeight)
		{
			width += 25
		}

		batch!!.color = Color.WHITE

		if (drawHorizontalBackground)
		{
			tilePanelBackgroundH.draw(batch, x, y + getHeight() - height, width.toFloat(), height.toFloat())
		} else
		{
			tilePanelBackgroundV.draw(batch, x, y + getHeight() - height, width.toFloat(), height.toFloat())
		}

		val xOffset = x.toInt() + padding
		val top = (y - padding + getHeight()).toInt() - tileSize

		if (dataHeight > viewHeight)
		{
			tileBackground!!.render(batch as HDRColourSpriteBatch, xOffset + tileSize + 5.toFloat(), (top - height).toFloat(), 10f, height.toFloat())
		}

		var x = 0
		var y = 0

		batch.color = Color.DARK_GRAY
		y = 0
		while (y < viewHeight)
		{
			x = 0
			while (x < viewWidth)
			{
				if (tileBackground != null)
				{
					tileBackground!!.render(batch as HDRColourSpriteBatch, (x * (tileSize + padding) + xOffset).toFloat(), (top - y * (tileSize + padding)).toFloat(), tileSize.toFloat(), tileSize.toFloat())
				}
				x++
			}
			y++
		}

		x = 0
		y = 0

		val scrollOffset = scrollY * viewWidth + scrollX

		for (i in scrollOffset..tileData.size - 1)
		{
			val item = tileData.get(i)

			val baseColour = if (item != null && item === mouseOver) Color.WHITE else Color.LIGHT_GRAY
			var itemColour: Color? = getColourForData(item)
			if (itemColour != null)
			{
				itemColour = Color(baseColour).mul(itemColour)
			} else
			{
				itemColour = baseColour
			}

			batch.color = itemColour
			if (tileBackground != null)
			{
				tileBackground!!.render(batch as HDRColourSpriteBatch, (x * (tileSize + padding) + xOffset).toFloat(), (top - y * (tileSize + padding)).toFloat(), tileSize.toFloat(), tileSize.toFloat())
			}
			onDrawItemBackground(item, batch, x * (tileSize + padding) + xOffset, top - y * (tileSize + padding), tileSize, tileSize)

			batch.color = Color.WHITE
			val sprite = getSpriteForData(item)
			sprite?.render(batch as HDRColourSpriteBatch, (x * (tileSize + padding) + xOffset).toFloat(), (top - y * (tileSize + padding)).toFloat(), tileSize.toFloat(), tileSize.toFloat())
			onDrawItem(item, batch, x * (tileSize + padding) + xOffset, top - y * (tileSize + padding), tileSize, tileSize)

			batch.color = itemColour
			if (tileBorder != null && item != null)
			{
				tileBorder!!.render(batch as HDRColourSpriteBatch, (x * (tileSize + padding) + xOffset).toFloat(), (top - y * (tileSize + padding)).toFloat(), tileSize.toFloat(), tileSize.toFloat())
			}
			onDrawItemForeground(item, batch, x * (tileSize + padding) + xOffset, top - y * (tileSize + padding), tileSize, tileSize)

			x++
			if (x == viewWidth)
			{
				x = 0
				y++
				if (y == viewHeight)
				{
					break
				}
			}
		}
	}

	inner class TilePanelListener : InputListener()
	{
		internal var longPressed = false

		internal var dragged = false
		internal var dragX: Float = 0.toFloat()
		internal var dragY: Float = 0.toFloat()

		internal var lastX: Float = 0.toFloat()
		internal var lastY: Float = 0.toFloat()

		private fun pointToItem(x: Float, y: Float): Any?
		{
			var y = y
			if (x < padding || y < padding || x > width - padding || y > height - padding)
			{
				return null
			}

			y = height - y

			var xIndex = ((x - padding) / (tileSize + padding)).toInt()
			var yIndex = ((y - padding) / (tileSize + padding)).toInt()

			if (xIndex >= viewWidth || yIndex >= viewHeight)
			{
				return null
			}

			val xpos = (x - (tileSize + padding) * xIndex).toInt()
			val ypos = (y - (tileSize + padding) * yIndex).toInt()

			if (xpos > tileSize + padding || ypos > tileSize + padding)
			{
				return null
			}

			xIndex += scrollX
			yIndex += scrollY

			val index = yIndex * viewWidth + xIndex
			if (index >= tileData.size || index < 0)
			{
				return null
			}
			return tileData.get(index)
		}

		override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int)
		{
			if (!dragged && (Math.abs(x - dragX) > 10 || Math.abs(y - dragY) > 10))
			{
				dragged = true

				lastX = x
				lastY = y
			}

			if (dragged)
			{
				val xdiff = ((x - lastX) / tileSize).toInt()
				val ydiff = ((y - lastY) / tileSize).toInt()

				if (xdiff != 0)
				{
					scrollX -= xdiff
					lastX = x
				}

				if (ydiff != 0)
				{
					scrollY += ydiff
					lastY = y
				}
			}
		}

		override fun scrolled(event: InputEvent?, x: Float, y: Float, amount: Int): Boolean
		{
			if (dataWidth > viewWidth)
			{
				scrollX += amount
			} else
			{
				scrollY += amount
			}

			return true
		}

		override fun mouseMoved(event: InputEvent?, x: Float, y: Float): Boolean
		{
			if (Tooltip.openTooltip != null)
			{
				Tooltip.openTooltip.isVisible = false
				Tooltip.openTooltip.remove()
				Tooltip.openTooltip = null
			}

			val item = pointToItem(x, y)

			if (item != null)
			{
				val table = getToolTipForData(item)

				if (table != null)
				{
					val tooltip = Tooltip(table, skin, stage)
					tooltip.show(event, x, y, false)
				}
			}

			mouseOver = item

			stage.scrollFocus = thisRef

			return true
		}

		override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean
		{
			if (Tooltip.openTooltip != null)
			{
				Tooltip.openTooltip.isVisible = false
				Tooltip.openTooltip.remove()
				Tooltip.openTooltip = null
				Tooltip.openTooltip = null
			}

			dragged = false
			dragX = x
			dragY = y

			return true
		}

		override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int)
		{
			if (Tooltip.openTooltip != null)
			{
				Tooltip.openTooltip.isVisible = false
				Tooltip.openTooltip.remove()
				Tooltip.openTooltip = null
			}

			val item = pointToItem(x, y)

			if (!longPressed && !dragged && item != null)
			{
				if (canBeExamined)
				{
					val table = getToolTipForData(item)

					if (table != null)
					{
						val tooltip = Tooltip(table, skin, stage)
						tooltip.show(event, x, y, false)
					}
				} else
				{
					handleDataClicked(item, event!!, x, y)
				}
			}

			dragged = false
		}

		override fun enter(event: InputEvent?, x: Float, y: Float, pointer: Int, toActor: Actor?)
		{
			stage.scrollFocus = thisRef
		}

		override fun exit(event: InputEvent?, x: Float, y: Float, pointer: Int, toActor: Actor?)
		{
			mouseOver = null

			if (Tooltip.openTooltip != null)
			{
				Tooltip.openTooltip.isVisible = false
				Tooltip.openTooltip.remove()
				Tooltip.openTooltip = null
				Tooltip.openTooltip = null
			}
		}
	}
}
