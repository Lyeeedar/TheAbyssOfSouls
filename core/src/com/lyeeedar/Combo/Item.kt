package com.lyeeedar.Combo

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.combo
import com.lyeeedar.Components.pickup
import com.lyeeedar.Components.renderable
import com.lyeeedar.Components.tile
import com.lyeeedar.Global
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Screens.AbstractScreen
import com.lyeeedar.SpaceSlot
import com.lyeeedar.UI.ButtonKeyboardHelper
import com.lyeeedar.UI.SpriteWidget
import com.lyeeedar.UI.addClickListener
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.getXml
import ktx.scene2d.label
import ktx.scene2d.table
import ktx.scene2d.textButton

class Item
{
	lateinit var icon: Sprite
	lateinit var name: String
	lateinit var description: String
	lateinit var combosHint: String
	lateinit var combos: ComboTree

	var floorEntity: Entity? = null

	fun showEquipWindow(entity: Entity)
	{
		val keyboardHelper = ButtonKeyboardHelper()

		val background = Table()

		val table = table {
			this.background = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/background.png"), 24, 24, 24, 24)).tint(Color(1f, 1f, 1f, 0.7f))

			val icon = SpriteWidget(icon, 32f, 32f, false)
			add(icon).left().padRight(10f)

			label(this@Item.name, "title", Global.skin) {
				cell -> cell.left()
			}
			row()

			label(description, "default", Global.skin) {
				cell -> cell.colspan(2)
				setWrap(true)
			}
			row()

			label(combosHint, "default", Global.skin) {
				cell -> cell.colspan(2)
			}
			row()

			textButton("Equip", "default", Global.skin) {
				addClickListener {
					val combo = entity.combo()

					val oldItem = combo.comboSource

					combo.comboSource = this@Item
					combo.combos = combos

					if (oldItem == null)
					{
						val tile = floorEntity!!.tile()!!
						tile.contents[SpaceSlot.BELOWENTITY] = null

						Global.engine.removeEntity(floorEntity!!)
					}
					else
					{

						floorEntity!!.renderable().renderable = oldItem.icon
						oldItem.floorEntity = floorEntity
						floorEntity!!.pickup().item = oldItem
					}

					floorEntity = null

					(Global.game.screen as AbstractScreen).keyboardHelper = null
					background.remove()

					Global.pause = false
				}

				keyboardHelper.add(this, 0, 0)
			}

			textButton("Drop", "default", Global.skin) {
				addClickListener {

					(Global.game.screen as AbstractScreen).keyboardHelper = null
					background.remove()
					Global.pause = false
				}

				keyboardHelper.add(this, 1, 0)
			}
		}

		background.add(table).grow().pad(20f)

		background.setFillParent(true)
		Global.stage.addActor(background)

		(Global.game.screen as AbstractScreen).keyboardHelper = keyboardHelper

		Global.pause = true
	}

	fun parse(xml: XmlReader.Element)
	{
		icon = AssetManager.loadSprite(xml.getChildByName("Icon"))
		name = xml.get("Name")
		description = xml.get("Description")
		combosHint = xml.get("ComboHint")
		combos = ComboTree.Companion.load(xml)
	}

	companion object
	{
		fun load(path: String): Item
		{
			val item = Item()
			val xml = getXml("Items/$path")
			item.parse(xml)
			return item
		}
	}
}
