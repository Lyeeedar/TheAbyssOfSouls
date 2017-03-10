package com.lyeeedar.UI

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Value
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.lyeeedar.Global
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.Future
import ktx.actors.alpha
import ktx.actors.plus
import ktx.actors.then
import ktx.scene2d.label
import ktx.scene2d.table

fun showFullscreenText(text: String, minDuration: Float, exitAction: ()->Unit)
{
	val fadeTable = table {
		label(text, "default", Global.skin) {
			cell -> cell.top().padTop(Value.percentHeight(0.3f, this@table))
		}
	}

	fadeTable.background = TextureRegionDrawable(AssetManager.loadTextureRegion("Sprites/white.png")).tint(Color.BLACK)
	fadeTable.alpha = 0f

	val sequence = Actions.alpha(0f) then Actions.fadeIn(0.2f) then lamda {
		val outsequence = Actions.fadeOut(0.2f) then Actions.removeActor()

		Future.call({
			Global.controls.onInput += fun (key: Int): Boolean {
				fadeTable + outsequence
				exitAction.invoke()
				return true
			}
		}, minDuration)
	}

	fadeTable + sequence

	Global.stage.addActor(fadeTable)
	fadeTable.setFillParent(true)
}