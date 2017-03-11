package com.lyeeedar.Interaction

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut
import com.badlogic.gdx.scenes.scene2d.actions.Actions.removeActor
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Global
import com.lyeeedar.UI.lamda
import com.lyeeedar.UI.showFullscreenText
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.Future
import ktx.actors.alpha
import ktx.actors.plus
import ktx.actors.then

class InteractionActionCompleteGame : AbstractInteractionAction()
{
	override fun interact(activating: Entity, parent: Entity, interaction: Interaction): Boolean
	{
		Global.pause = true

		val fadeTable = Table()
		fadeTable.background = TextureRegionDrawable(AssetManager.loadTextureRegion("Sprites/white.png"))
		fadeTable.alpha = 0f

		val sequence = Actions.alpha(0f) then Actions.fadeIn(2f) then lamda {

			Gdx.files.local("save.dat")?.delete()
			Global.pause = false

			Future.call({

							showFullscreenText("You ascended to heaven.\n\nFor the rest of eternity you lived in peace.\n\nWell done, your journey is over.", 1f, { Gdx.app.exit() })
						}, 1.5f)

		} then fadeOut(1f) then removeActor()

		fadeTable + sequence

		Global.stage.addActor(fadeTable)
		fadeTable.setFillParent(true)

		return true
	}

	override fun parse(xml: XmlReader.Element)
	{

	}

	override fun resolve(nodes: ObjectMap<String, InteractionNode>)
	{

	}

}
