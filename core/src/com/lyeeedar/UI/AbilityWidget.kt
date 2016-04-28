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
import com.lyeeedar.Ability.AbilityChain
import com.lyeeedar.Ability.AbilityWrapper
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Util.Colour
import java.awt.event.MouseEvent
import java.awt.event.MouseListener

/**
 * Created by Philip on 27-Apr-16.
 */

class AbilityWidget(val entity: Entity) : Widget()
{
	var maxAbilities: Int = 0
	val tileSize: Float = 32f

	val currentAbilities: com.badlogic.gdx.utils.Array<AbilityChain> = com.badlogic.gdx.utils.Array()

	init
	{
		val abilityData = Mappers.ability.get(entity)

		maxAbilities = abilityData.abilities.size

		addListener( object : ClickListener() {
			override fun clicked(event: InputEvent?, x: Float, y: Float)
			{
				var clickedOn: AbilityChain? = null

				var i = 0
				for (ability in currentAbilities)
				{
					var yo = i++ * tileSize

					val ay = getY() + yo

					if (y >= ay && y <= ay + tileSize)
					{
						clickedOn = ability
						break
					}
				}

				if (clickedOn != null)
				{
					// if root then prepare
					var rootWrapper: AbilityWrapper? = abilityData.abilities.singleOrNull { x -> x.root == clickedOn }
					if (rootWrapper != null)
					{
						abilityData.current = rootWrapper
					}
					// else if chain then advance
					else
					{
						var wrapper: AbilityWrapper? = abilityData.abilities.singleOrNull { x -> x.current.next.contains(clickedOn) }
						wrapper?.current = clickedOn
					}
				}
			}

			override fun exit(event: InputEvent?, x: Float, y: Float, pointer: Int, toActor: Actor?)
			{
				mouseOverAbility = null
			}

			override fun mouseMoved(event: InputEvent?, x: Float, y: Float): Boolean
			{
				mouseOverAbility = null

				var i = 0
				for (ability in currentAbilities)
				{
					var yo = i++ * tileSize

					if ( ability.ability.icon.spriteAnimation != null )
					{
						val offset = ability.ability.icon.spriteAnimation!!.renderOffset();
						if (offset != null) yo += offset[1];
					}

					val ay = getY() + yo

					if (y >= ay && y <= ay + tileSize)
					{
						mouseOverAbility = ability
						return true
					}
				}

				return false
			}
		})
	}

	var mouseOverAbility: AbilityChain? = null

	override fun getPrefWidth() = tileSize
	override fun getPrefHeight() = tileSize * maxAbilities

	override fun act(delta: Float)
	{
		super.act(delta)

		for (ability in currentAbilities) ability.ability.icon.update(delta)
	}

	override fun draw(batch: Batch?, parentAlpha: Float)
	{
		val hdrBatch = batch!! as HDRColourSpriteBatch

		validate()

		val abilityData = Mappers.ability.get(entity)

		var i = 0
		for (ability in currentAbilities)
		{
			if (abilityData.current?.current == ability)
			{
				hdrBatch.color = Color.YELLOW
			}
			else if (abilityData.current != null)
			{
				hdrBatch.color = Color.DARK_GRAY
			}
			else if (mouseOverAbility == ability)
			{
				hdrBatch.color = Color.FIREBRICK
			}
			else
			{
				hdrBatch.color = Color.WHITE
			}

			var yo = i++ * tileSize

			if ( ability.ability.icon.spriteAnimation != null )
			{
				val offset = ability.ability.icon.spriteAnimation!!.renderOffset();
				if (offset != null) yo += offset[1];
			}

			ability.ability.icon.render(hdrBatch, x, y + yo, tileSize, tileSize)
		}
	}
}