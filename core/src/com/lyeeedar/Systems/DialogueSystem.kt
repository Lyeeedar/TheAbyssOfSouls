package com.lyeeedar.Systems

import com.badlogic.ashley.core.Family
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import com.lyeeedar.Components.*
import com.lyeeedar.Global
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Util.AssetManager

class DialogueSystem : AbstractSystem(Family.all(DialogueComponent::class.java).get())
{
	val layout = GlyphLayout()
	val font = Global.skin.getFont("default")
	val speechBubbleBack = NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/SpeechBubble.png")!!, 10, 10, 10, 10)
	val speechBubbleArrow = AssetManager.loadTextureRegion("Sprites/GUI/SpeechBubbleArrow.png")!!

	val tileSize: Float
		get() = engine.render().tileSize

	val batch: SpriteBatch = SpriteBatch()

	val tempCol = Color()

	override fun onTurn()
	{
		for (entity in entities)
		{
			val dialogue = entity.dialogue()
			if (dialogue.turnsToShow > 0)
			{
				dialogue.turnsToShow--
				if (dialogue.turnsToShow == 0)
				{
					dialogue.remove = true
				}
			}
		}
	}

	override fun doUpdate(deltaTime: Float)
	{
		if (level == null) return

		val player = level!!.player
		val playerPos = player.pos()
		val playerSprite = player.renderable().renderable ?: return

		var offsetx = Global.resolution.x / 2 - playerPos.position.x * tileSize - tileSize / 2
		var offsety = Global.resolution.y / 2 - playerPos.position.y * tileSize - tileSize / 2

		val offset = playerSprite.animation?.renderOffset()
		if (offset != null)
		{
			offsetx -= offset[0] * tileSize
			offsety -= offset[1] * tileSize
		}

		batch.begin()

		for (entity in entities)
		{
			val dialogue = entity.dialogue()

			if (dialogue.remove)
			{
				dialogue.textFade -= deltaTime
				if (dialogue.textFade <= 0f)
				{
					entity.remove(DialogueComponent::class.java)
					continue
				}
			}

			if (dialogue.displayedText != dialogue.text)
			{
				dialogue.textAccumulator += deltaTime
				while (dialogue.textAccumulator >= 0.02f)
				{
					dialogue.textAccumulator -= 0.02f

					val currentPos = dialogue.displayedText.length
					val nextChar = dialogue.text[currentPos]
					var nextString = "" + nextChar
					if (nextChar == '[')
					{
						// this is a colour tag, so read ahead to the closing tag, and the letter after
						var current = currentPos + 1
						while (true)
						{
							val char = dialogue.text[current]
							nextString += char

							current++
							if (char == ']') break
						}

						if (current < dialogue.text.length)
						{
							val char = dialogue.text[current]
							nextString += char
						}
					}

					dialogue.displayedText += nextString

					if (dialogue.displayedText == dialogue.text) break
				}
			}

			if (!entity.tile()!!.isVisible) continue

			tempCol.set(1f, 1f, 1f, dialogue.alpha)

			var x = offsetx + entity.pos().x * tileSize
			var y = offsety + entity.pos().y * tileSize

			val renderOffset = entity.renderOffset()
			if (renderOffset != null)
			{
				x += renderOffset[0] * tileSize
				y += renderOffset[1] * tileSize
			}

			x += tileSize * 0.5f

			if (entity.renderable()?.renderable is Sprite && (entity.renderable().renderable as Sprite).drawActualSize)
			{
				y += tileSize * 1.5f
			}
			else
			{
				y += tileSize
			}

			layout.setText(font, dialogue.text, tempCol, Global.stage.width * 0.5f, Align.left, true)

			var left = x - (layout.width * 0.5f) - 10f
			if (left < 0) left = 0f

			val right = left + layout.width + 20
			if (right >= Global.stage.width) left = right - Global.stage.width

			val width = layout.width
			val height = layout.height

			layout.setText(font, dialogue.displayedText, tempCol, Global.stage.width * 0.5f, Align.left, true)

			batch.color = tempCol
			speechBubbleBack.draw( batch, left, y, width + 20, height + 20 )
			batch.draw( speechBubbleArrow, x - 4f, y - 6f, 8f, 8f )

			font.draw( batch, layout, left + 10, y + layout.height + 10 )
		}

		batch.end()
	}

}
