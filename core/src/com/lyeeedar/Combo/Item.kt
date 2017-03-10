package com.lyeeedar.Combo

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.*
import com.lyeeedar.Global
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Screens.AbstractScreen
import com.lyeeedar.Sin
import com.lyeeedar.SpaceSlot
import com.lyeeedar.UI.ButtonKeyboardHelper
import com.lyeeedar.UI.SpriteWidget
import com.lyeeedar.UI.addClickListener
import com.lyeeedar.UI.showFullscreenText
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.Future
import com.lyeeedar.Util.getXml
import ktx.collections.set
import ktx.scene2d.label
import ktx.scene2d.table
import ktx.scene2d.textButton

abstract class Item
{
	lateinit var loadData: XmlReader.Element

	lateinit var icon: Sprite
	lateinit var name: String
	lateinit var description: String

	var count: Int = 1

	abstract fun steppedOn(entity: Entity, floorEntity: Entity)
	abstract fun parse(xml: XmlReader.Element)

	companion object
	{
		fun load(xml: XmlReader.Element): Item
		{
			val item: Item

			if (xml.name == "Weapon")
			{
				item = Weapon()
				item.parse(xml)
			}
			else
			{
				val type = xml.getAttribute("meta:RefKey").toUpperCase()
				if (type == "EQUIPMENT")
				{
					item = Weapon()

					val weaponxml = getXml("Items/" + xml.get("Weapon"))
					item.parse(weaponxml)
				}
				else if (type == "ATONEMENTSPIRIT")
				{
					item = AtonementSpirit()
					item.parse(xml)
				}
				else if (type == "ITEM")
				{
					item = BasicItem()
					item.parse(xml)
				}
				else if (type == "MONEY")
				{
					item = BasicItem()
					item.name = "Money"
					item.description = "Its money"
					item.icon = AssetManager.loadSprite("Oryx/uf_split/uf_items/coin_silver")
					item.count = xml.getInt("Value", 1)
				}
				else throw Exception("Unknown item type '$type'")
			}

			item.loadData = xml
			return item
		}
	}
}

class BasicItem : Item()
{
	override fun steppedOn(entity: Entity, floorEntity: Entity)
	{
		val inventory = entity.inventory()

		if (inventory != null)
		{
			val key = name.replace(" ", "").toLowerCase()

			val existing = inventory.items[key]
			if (existing != null)
			{
				existing.count += count
			}
			else
			{
				inventory.items[key] = this
			}

			val tile = floorEntity.tile()!!
			tile.contents[SpaceSlot.BELOWENTITY] = null

			Global.engine.removeEntity(floorEntity)
		}
	}

	override fun parse(xml: XmlReader.Element)
	{
		icon = AssetManager.loadSprite(xml.getChildByName("Icon"))
		name = xml.get("Name")
		description = xml.get("Description")

		count = xml.getInt("Count", 1)
	}

}

class AtonementSpirit() : Item()
{
	lateinit var sin: Sin

	constructor(sin: Sin) : this()
	{
		this.sin = sin

		icon = AssetManager.loadSprite("Oryx/Custom/items/orb")
		icon.drawActualSize = true
		icon.colour = sin.colour
	}

	override fun steppedOn(entity: Entity, floorEntity: Entity)
	{
		val sin = entity.sin() ?: return

		val sinned = sin.sins[this.sin] > 0
		sin.sins[this.sin] = Math.max(0, (sin.sins[this.sin] ?: 0)-1)
		sin.restored[this.sin] = true

		if (sinned && sin.sins[this.sin] == 0)
		{
			Future.call(
			{
				Global.pause = true

				showFullscreenText("Your sin of [" + this.sin.toString() + "]" + this.sin.toString().toLowerCase().capitalize() + "[] is cleansed.", 0.25f, {Global.pause = false})
			}, 0.5f)
		}

		val tile = floorEntity.tile()!!
		tile.contents[SpaceSlot.BELOWENTITY] = null

		Global.engine.removeEntity(floorEntity)
	}

	override fun parse(xml: XmlReader.Element)
	{
		name = xml.get("Sin")
		sin = Sin.valueOf(name.toUpperCase())

		icon = AssetManager.loadSprite("Oryx/Custom/items/orb")
		icon.drawActualSize = true
		icon.colour = sin.colour
	}
}

class Weapon : Item()
{
	lateinit var combosHint: String
	lateinit var combos: ComboTree

	override fun steppedOn(entity: Entity, floorEntity: Entity)
	{
		val keyboardHelper = ButtonKeyboardHelper()

		val background = Table()

		val table = table {
			defaults().pad(20f).center()

			this.background = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/background.png"), 24, 24, 24, 24)).tint(Color(1f, 1f, 1f, 0.7f))

			table {
				cell -> cell.growX()

				val icon = SpriteWidget(icon, 32f, 32f, false)
				add(icon).left().padRight(10f)

				label(this@Weapon.name, "title", Global.skin)
			}
			row()

			label(description, "default", Global.skin) {
				cell -> cell.growX()
				setWrap(true)
			}
			row()

			label(combosHint, "default", Global.skin) {
				cell -> cell.growX()
			}
			row()

			table {
				cell -> cell.growX()

				textButton("Equip", "default", Global.skin) {
					addClickListener {
						val combo = entity.combo()

						val oldItem = combo.comboSource

						combo.comboSource = this@Weapon
						combo.combos = combos

						if (oldItem == null)
						{
							val tile = floorEntity.tile()!!
							tile.contents[SpaceSlot.BELOWENTITY] = null

							Global.engine.removeEntity(floorEntity)
						}
						else
						{

							floorEntity.renderable().renderable = oldItem.icon
							floorEntity.pickup().item = oldItem
						}

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
		}

		background.add(table).grow().pad(20f)

		background.setFillParent(true)
		Global.stage.addActor(background)

		(Global.game.screen as AbstractScreen).keyboardHelper = keyboardHelper

		Global.pause = true
	}

	override fun parse(xml: XmlReader.Element)
	{
		icon = AssetManager.loadSprite(xml.getChildByName("Icon"))
		name = xml.get("Name")
		description = xml.get("Description")
		combosHint = xml.get("ComboHint")
		combos = ComboTree.Companion.load(xml)
	}
}
