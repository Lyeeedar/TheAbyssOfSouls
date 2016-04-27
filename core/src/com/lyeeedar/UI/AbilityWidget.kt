package com.lyeeedar.UI

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.HDRColourSpriteBatch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.lyeeedar.Ability.Targetting.AbilityWrapper
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Util.Colour
import java.awt.event.MouseEvent
import java.awt.event.MouseListener

/**
 * Created by Philip on 27-Apr-16.
 */

class AbilityWidget(val ability: AbilityWrapper, val entity: Entity, var w: Float, var h: Float) : Widget()
{
	init
	{
		addListener( object : ClickListener() {
			override fun clicked(event: InputEvent?, x: Float, y: Float)
			{
				val abilityData = Mappers.ability.get(entity)

				if (abilityData.current == null)
				{
					abilityData.current = ability
				}
			}

			override fun enter(event: InputEvent?, x: Float, y: Float, pointer: Int, fromActor: Actor?)
			{
				mouseOver = true
			}

			override fun exit(event: InputEvent?, x: Float, y: Float, pointer: Int, toActor: Actor?)
			{
				mouseOver = false
			}
		})
	}

	var mouseOver: Boolean = false

	override fun getPrefWidth() = w
	override fun getPrefHeight() = h

	override fun act(delta: Float)
	{
		super.act(delta)
		ability.current.ability.icon.update(delta)
	}

	override fun draw(batch: Batch?, parentAlpha: Float)
	{
		val hdrBatch = batch!! as HDRColourSpriteBatch

		validate()

		val abilityData = Mappers.ability.get(entity)

		if (abilityData.current == ability)
		{
			hdrBatch.setColor(ability.current.ability.icon.colour)
			hdrBatch.tintColour(Color.YELLOW)
			ability.current.ability.icon.render(hdrBatch, x, y, w, h)
		}
		else if (abilityData.current != null)
		{
			hdrBatch.setColor(ability.current.ability.icon.colour)
			hdrBatch.tintColour(Color.DARK_GRAY)
			ability.current.ability.icon.render(hdrBatch, x, y, w, h)
		}
		else
		{
			hdrBatch.setColor(ability.current.ability.icon.colour)

			if (mouseOver) hdrBatch.tintColour(Color.FIREBRICK)

			ability.current.ability.icon.render(hdrBatch, x, y, w, h)
		}
	}
}