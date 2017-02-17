package com.lyeeedar.UI

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.lyeeedar.Global
import com.lyeeedar.Util.AssetManager

import ktx.scene2d.*
import ktx.actors.*

class TutorialPopup(val text: String, val pos: Vector2, val key: String) : Table()
{
	init
	{
		background = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/background.png"), 24, 24, 24, 24)).tint(Color(1f,1f,1f,0.7f))
		touchable = Touchable.enabled

		val label = Label(text, Global.skin)
		label.setWrap(true)

		add(label).grow().width(Global.stage.width / 3f)

		val sequenceIn = alpha(0f) then fadeIn(1f)
		val sequenceOut = fadeOut(0.1f) then removeActor()

		addAction(sequenceIn)

		onClick { inputEvent, tutorialPopup -> addAction(sequenceOut) }

		pack()

		val placeLeft = pos.x > Global.stage.width-pos.x
		val placeTop = pos.y < Global.stage.height-pos.y

		if (placeLeft)
		{
			pos.x = pos.x - width - 20
		}
		else
		{
			pos.x = pos.x + 20
		}

		if (placeTop)
		{
			pos.y = pos.y + 20
		}
		else
		{
			pos.y = pos.y - height - 20
		}

		if (!Global.settings.get(key, false))
		{
			setPosition(pos.x, pos.y)
			Global.stage.addActor(this)

			Global.settings.set(key, true)
		}
	}
}