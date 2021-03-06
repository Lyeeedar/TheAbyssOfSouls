package com.lyeeedar.UI

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.*
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.lyeeedar.Components.combo
import com.lyeeedar.Components.sin
import com.lyeeedar.Components.stats
import com.lyeeedar.Global
import com.lyeeedar.Renderables.Particle.ParticleEffect
import com.lyeeedar.Renderables.SortedRenderer
import com.lyeeedar.Sin
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.floor
import com.lyeeedar.Util.romanNumerals

class StatsWidget() : Widget()
{
	val hp_full_green = AssetManager.loadTextureRegion("Sprites/GUI/health_full_green.png")!!
	val hp_full_red = AssetManager.loadTextureRegion("Sprites/GUI/health_full.png")!!
	val hp_full_blue = AssetManager.loadTextureRegion("Sprites/GUI/health_full_blue.png")!!
	val hp_empty = AssetManager.loadTextureRegion("Sprites/GUI/health_empty.png")!!

	val barback = NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/HpBarBack.png"), 4, 4, 4, 4)

	val sinEffect = AssetManager.loadParticleEffect("Charge")
	val activeEffects = Array<ParticleEffect>()
	val renderer: SortedRenderer by lazy{ SortedRenderer(20f, stage.width, stage.height, 1) }

	val font: BitmapFont = Global.skin.getFont("default")
	val glyphLayout = GlyphLayout()

	val pipWidth = 16f

	var widgetwidth = 48f
	var widgetheight = 58f

	var entity: Entity? = null

	override fun invalidate()
	{
		super.invalidate()

		if (entity == null) return
		val entity = entity!!

		val stats = entity.stats()!!

		val hpwidth = stats.maxHP.toInt() * pipWidth
		val staminawidth = stats.maxStamina.toInt() * pipWidth

		widgetwidth = 48f + 5f + Math.max(hpwidth, staminawidth)
		widgetheight = 48f + 10f
	}

	override fun getPrefWidth(): Float = widgetwidth
	override fun getMinWidth(): Float = widgetwidth

	override fun getPrefHeight(): Float = widgetheight
	override fun getHeight(): Float = widgetheight

	override fun draw(batch: Batch?, parentAlpha: Float)
	{
		super.draw(batch, parentAlpha)

		if (entity == null || Global.interaction != null || Global.pause || batch == null) return
		val entity = entity!!

		invalidate()

		val stats = entity.stats()!!
		val combo = entity.combo()!!
		val icon = combo.comboSource!!.icon

		batch.color = Color.WHITE
		barback.draw(batch, x, y+10f, 48f, 48f)
		icon.render(batch, x, y+10f, 48f, 48f)

		// draw hp
		val hp_full = hp_full_green

		val hp = stats.hp.toInt()
		val maxhp = stats.maxHP.toInt()

		var totalWidth = maxhp * pipWidth

		var solidSpaceRatio = 0.1f
		var space = totalWidth
		var spacePerPip = space / maxhp
		var spacing = spacePerPip * solidSpaceRatio
		var solid = spacePerPip - spacing

		for (i in 0..maxhp-1)
		{
			val pip: TextureRegion

			if (i >= hp && i < hp+stats.regeneratingHP.floor()) pip = hp_full_red
			else if(i < hp) pip = hp_full
			else pip = hp_empty

			batch.draw(pip, x+48f+5f+i*spacePerPip, y+height-pipWidth, solid, pipWidth)
		}

		// draw stamina
		val stamina = stats.stamina.toInt()
		val maxstamina = stats.maxStamina.toInt()

		totalWidth = maxstamina * pipWidth

		solidSpaceRatio = 0.1f
		space = totalWidth
		spacePerPip = space / maxstamina
		spacing = spacePerPip * solidSpaceRatio
		solid = spacePerPip - spacing

		for (i in 0..maxstamina-1)
		{
			var pip = if(i < stamina) hp_full_blue else hp_empty
			if (entity.stats().insufficientStamina > 0f)
			{
				pip = if(i < entity.stats().insufficientStaminaAmount) hp_full_red else hp_empty
			}

			batch.draw(pip, x+48f+5f+i*spacePerPip, y+height-pipWidth*2-1, solid, pipWidth)
		}

		// draw sins

		val sins = entity.sin()!!

		var current = x + 48 + 5
		for (sin in Sin.Values)
		{
			batch.color = sin.colour.color()

			glyphLayout.setText(font, sins.sins[sin].romanNumerals(), sin.colour.color(), 20f, Align.center, false)
			font.draw(batch, glyphLayout, current, y+glyphLayout.height+10f)

			if (sins.restored[sin] == true)
			{
				val effect = sinEffect.copy()
				effect.lockPosition = true
				effect.setPosition((current + glyphLayout.width/2) / renderer.tileSize, (y+glyphLayout.height+10f) / renderer.tileSize)
				activeEffects.add(effect)

				sins.restored[sin] = false
			}

			current += 25
		}

		renderer.begin(Gdx.graphics.deltaTime, 0f, 0f)

		val itr = activeEffects.iterator()
		while (itr.hasNext())
		{
			val effect = itr.next()
			renderer.queueParticle(effect, effect.position.x, effect.position.y - 0.5f, 0, 0)

			if (effect.completed && effect.complete())
			{
				itr.remove()
			}
		}

		renderer.flush(batch)
	}
}
