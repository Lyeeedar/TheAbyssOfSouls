package com.lyeeedar.Interaction

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Global
import com.lyeeedar.UI.lamda
import com.lyeeedar.Util.AssetManager
import ktx.actors.alpha
import ktx.actors.plus
import ktx.actors.then
import ktx.scene2d.table

class InteractionActionImage : AbstractInteractionAction()
{
	lateinit var path: String

	var createdTable: Table? = null

	override fun interact(activating: Entity, parent: Entity, interaction: Interaction): Boolean
	{
		if (createdTable != null)
		{
			createdTable = null
			return true
		}

		val fadeTable = table {
			add(Image(Texture(path))).grow().pad(30f)
		}

		fadeTable.background = TextureRegionDrawable(AssetManager.loadTextureRegion("Sprites/white.png")).tint(Color(0f, 0f, 0f, 0.7f))
		fadeTable.alpha = 0f

		val sequence = Actions.alpha(0f) then Actions.fadeIn(0.2f) then lamda {

			val outsequence = Actions.fadeOut(0.2f) then lamda {
				interaction.interact(activating, parent)
			} then Actions.removeActor()

			Global.controls.onInput += fun(code): Boolean {

				fadeTable + outsequence

				return true
			}

		}

		fadeTable + sequence

		Global.stage.addActor(fadeTable)
		fadeTable.setFillParent(true)

		createdTable = fadeTable

		return false
	}

	override fun parse(xml: XmlReader.Element)
	{
		path = xml.get("Path")
	}

	override fun resolve(nodes: ObjectMap<String, InteractionNode>)
	{

	}
}