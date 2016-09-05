package com.lyeeedar.UI

import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.lyeeedar.Global
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.addClickListener

/**
 * Created by Philip on 03-Aug-16.
 */

class MessageBox(title: String, message: String, vararg buttons: Pair<String, () -> Unit>) : FullscreenTable()
{
	val table = Table()

	init
	{
		table.background = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/background.png"), 24, 24, 24, 24))
		this.add(table)

		table.add(Label(title, Global.skin, "title")).expandX().center().pad(30f)
		table.row()

		val descLabel = Label(message, Global.skin)
		descLabel.setWrap(true)
		table.add(descLabel).expandX().fillX()
		table.row()

		val buttonTable = Table()
		for (button in buttons)
		{
			val b = TextButton(button.first, Global.skin)
			b.addClickListener{
				this@MessageBox.remove()
				button.second()
			}
			buttonTable.add(b).expand()
		}

		table.add(buttonTable).expandX().fillX().pad(15f)
	}
}