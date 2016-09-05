package com.lyeeedar.UI

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.lyeeedar.Global
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.Event0Arg

/**
 * Created by Philip on 02-Aug-16.
 */

abstract class FullscreenTable() : Table()
{
	val onClosed = Event0Arg()

	init
	{
		background = TextureRegionDrawable(AssetManager.loadTextureRegion("white")).tint(Color(0f, 0f, 0f, 0.4f))
		touchable = Touchable.enabled
		setFillParent(true)

		Global.stage.addActor(this)
	}

	override fun remove(): Boolean
	{
		onClosed()
		return super.remove()
	}
}