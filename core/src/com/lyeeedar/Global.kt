package com.lyeeedar

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.ObjectMap
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.lyeeedar.Screens.AbstractScreen
import com.lyeeedar.Systems.createEngine
import com.lyeeedar.UI.LayeredDrawable
import com.lyeeedar.UI.Seperator
import com.lyeeedar.UI.TabPanel
import com.lyeeedar.UI.Tooltip
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.Controls
import com.lyeeedar.Util.Point
import ktx.collections.set
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * Created by Philip on 04-Jul-16.
 */

class Global
{
	companion object
	{
		val PARTICLE_EDITOR = false

		lateinit var skin: Skin
		var fps = 60
		var android = false
		var release = false
		lateinit var game: MainGame
		lateinit var applicationChanger: AbstractApplicationChanger
		val settings: Settings by lazy { Settings() }
		lateinit var engine: Engine

		var resolution = Point(800, 600)

		lateinit var controls: Controls

		val stage: Stage
			get() = (game.screen as AbstractScreen).stage

		fun setup()
		{
			skin = loadSkin()
			engine = createEngine()
			controls = Controls()
		}

		private fun loadSkin(): Skin
		{
			val skin = Skin()

			val font = AssetManager.loadFont("Sprites/Unpacked/font.ttf", 12, Color(0.97f, 0.87f, 0.7f, 1f), 1, Color.BLACK, false)
			skin.add("default", font)

			val titlefont = AssetManager.loadFont("Sprites/Unpacked/font.ttf", 20, Color(1f, 0.9f, 0.8f, 1f), 1, Color.BLACK, true)
			skin.add("title", titlefont)

			val popupfont = AssetManager.loadFont("Sprites/Unpacked/font.ttf", 20, Color(1f, 1f, 1f, 1f), 1, Color.DARK_GRAY, true)
			skin.add("popup", popupfont)

			val consolefont = AssetManager.loadFont("Sprites/Unpacked/font.ttf", 8, Color(0.9f, 0.9f, 0.9f, 1f), 0, Color.BLACK, false)
			skin.add("console", consolefont)

			val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
			pixmap.setColor(Color.WHITE)
			pixmap.fill()
			skin.add("white", Texture(pixmap))

			val buttonBackground = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/Button.png"), 6, 6, 6, 6))

			val textField = TextField.TextFieldStyle()
			textField.fontColor = Color.WHITE
			textField.font = skin.getFont("default")
			textField.background = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/TextField.png"), 6, 6, 6, 6))
			textField.focusedBackground = (textField.background as NinePatchDrawable).tint(Color(0.9f, 0.9f, 0.9f, 1.0f))
			textField.cursor = skin.newDrawable("white", Color.WHITE)
			textField.selection = skin.newDrawable("white", Color.LIGHT_GRAY)
			skin.add("default", textField)

			val consoleText = TextField.TextFieldStyle()
			consoleText.fontColor = Color.WHITE
			consoleText.font = skin.getFont("console")
			consoleText.background = TextureRegionDrawable(AssetManager.loadTextureRegion("Sprites/white.png")).tint(Color(0.1f, 0.1f, 0.1f, 0.6f))
			consoleText.focusedBackground = TextureRegionDrawable(AssetManager.loadTextureRegion("Sprites/white.png")).tint(Color(0.3f, 0.3f, 0.3f, 0.6f))
			consoleText.cursor = skin.newDrawable("white", Color.WHITE)
			consoleText.selection = skin.newDrawable("white", Color.LIGHT_GRAY)
			skin.add("console", consoleText)

			val consolelabel = Label.LabelStyle()
			consolelabel.font = skin.getFont("console")
			skin.add("console", consolelabel)

			val label = Label.LabelStyle()
			label.font = skin.getFont("default")
			skin.add("default", label)

			val titleLabel = Label.LabelStyle()
			titleLabel.font = skin.getFont("title")
			skin.add("title", titleLabel)

			val popupLabel = Label.LabelStyle()
			popupLabel.font = skin.getFont("popup")
			skin.add("popup", popupLabel)

			val checkButton = CheckBox.CheckBoxStyle()
			checkButton.checkboxOff = TextureRegionDrawable(AssetManager.loadTextureRegion("Sprites/GUI/Unchecked.png"))
			checkButton.checkboxOn = TextureRegionDrawable(AssetManager.loadTextureRegion("Sprites/GUI/Checked.png"))
			checkButton.font = skin.getFont("default")
			checkButton.fontColor = Color.LIGHT_GRAY
			checkButton.overFontColor = Color.WHITE
			skin.add("default", checkButton)

			val textButton = TextButton.TextButtonStyle()
			textButton.up = buttonBackground
			textButton.font = skin.getFont("default")
			textButton.fontColor = Color.LIGHT_GRAY
			textButton.overFontColor = Color.WHITE
			//textButton.checked = new NinePatchDrawable( new NinePatch( AssetManager.loadTextureRegion( "Sprites/GUI/ButtonDown.png" ), 12, 12, 12, 12 ) );
			textButton.over = (textButton.up as NinePatchDrawable).tint(Color(0.9f, 0.9f, 0.9f, 1.0f))
			skin.add("default", textButton)

			val bigTextButton = TextButton.TextButtonStyle()
			bigTextButton.up = buttonBackground
			bigTextButton.font = skin.getFont("title")
			bigTextButton.fontColor = Color.LIGHT_GRAY
			bigTextButton.overFontColor = Color.WHITE
			//bigTextButton.checked = new NinePatchDrawable( new NinePatch( AssetManager.loadTextureRegion( "Sprites/GUI/ButtonDown.png" ), 12, 12, 12, 12 ) );
			bigTextButton.over = (bigTextButton.up as NinePatchDrawable).tint(Color(0.9f, 0.9f, 0.9f, 1.0f))
			skin.add("big", bigTextButton)

			val keyBindingButton = TextButton.TextButtonStyle()
			keyBindingButton.up = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/TextField.png"), 6, 6, 6, 6))
			keyBindingButton.font = skin.getFont("default")
			keyBindingButton.fontColor = Color.LIGHT_GRAY
			keyBindingButton.overFontColor = Color.WHITE
			//textButton.checked = new NinePatchDrawable( new NinePatch( AssetManager.loadTextureRegion( "Sprites/GUI/ButtonDown.png" ), 12, 12, 12, 12 ) );
			keyBindingButton.over = (keyBindingButton.up as NinePatchDrawable).tint(Color(0.9f, 0.9f, 0.9f, 1.0f))
			skin.add("keybinding", keyBindingButton)

			val toolTip = Tooltip.TooltipStyle()
			toolTip.background = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/Tooltip.png"), 21, 21, 21, 21))
			skin.add("default", toolTip)

			val progressBar = ProgressBar.ProgressBarStyle()
			progressBar.background = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/TextField.png"), 6, 6, 6, 6))
			progressBar.knobBefore = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/ProgressIndicator.png"), 8, 8, 8, 8))
			skin.add("default-horizontal", progressBar)

			val buttonStyle = Button.ButtonStyle()
			buttonStyle.up = buttonBackground
			buttonStyle.over = (buttonStyle.up as NinePatchDrawable).tint(Color(0.9f, 0.9f, 0.9f, 1.0f))
			skin.add("default", buttonStyle)

			val closeButton = Button.ButtonStyle()
			closeButton.up = LayeredDrawable(
					buttonBackground,
					TextureRegionDrawable(AssetManager.loadTextureRegion("Sprites/Oryx/uf_split/uf_interface/uf_interface_681.png")).tint(Color(0.97f, 0.87f, 0.7f, 1f)))
			closeButton.over = LayeredDrawable(
					buttonBackground.tint(Color.LIGHT_GRAY),
					TextureRegionDrawable(AssetManager.loadTextureRegion("Sprites/Oryx/uf_split/uf_interface/uf_interface_681.png")).tint(Color(0.87f, 0.77f, 0.6f, 1f)))
			skin.add("close", closeButton)

			val horiSeperatorStyle = Seperator.SeperatorStyle()
			horiSeperatorStyle.vertical = false
			horiSeperatorStyle.thickness = 6
			horiSeperatorStyle.background = TextureRegionDrawable(AssetManager.loadTextureRegion("Sprites/GUI/SeperatorHorizontal.png"))
			skin.add("horizontal", horiSeperatorStyle)

			val vertSeperatorStyle = Seperator.SeperatorStyle()
			vertSeperatorStyle.vertical = true
			vertSeperatorStyle.thickness = 6
			vertSeperatorStyle.background = TextureRegionDrawable(AssetManager.loadTextureRegion("Sprites/GUI/SeperatorVertical.png"))
			skin.add("vertical", vertSeperatorStyle)

			val scrollPaneStyle = ScrollPane.ScrollPaneStyle()
			scrollPaneStyle.vScroll = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/TextField.png"), 6, 6, 6, 6))
			scrollPaneStyle.vScrollKnob = buttonBackground
			skin.add("default", scrollPaneStyle)

			val listStyle = com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle()
			listStyle.background = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/Tooltip.png"), 21, 21, 21, 21))
			listStyle.font = skin.getFont("default")
			listStyle.selection = skin.newDrawable("white", Color.LIGHT_GRAY)
			skin.add("default", listStyle)

			val selectBoxStyle = SelectBox.SelectBoxStyle()
			selectBoxStyle.fontColor = Color.WHITE
			selectBoxStyle.font = skin.getFont("default")
			selectBoxStyle.background = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/TextField.png"), 6, 6, 6, 6))
			selectBoxStyle.scrollStyle = scrollPaneStyle
			selectBoxStyle.listStyle = listStyle
			selectBoxStyle.backgroundOver = (selectBoxStyle.background as NinePatchDrawable).tint(Color(0.9f, 0.9f, 0.9f, 1.0f))
			skin.add("default", selectBoxStyle)

			val sliderStyle = Slider.SliderStyle()
			sliderStyle.background = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/TextField.png"), 6, 6, 6, 6))
			sliderStyle.knob = buttonBackground
			sliderStyle.knobOver = (sliderStyle.knob as NinePatchDrawable).tint(Color(0.9f, 0.9f, 0.9f, 1.0f))
			sliderStyle.knobDown = (sliderStyle.knob as NinePatchDrawable).tint(Color.LIGHT_GRAY)
			skin.add("default-horizontal", sliderStyle)

			val tabPanelStyle = TabPanel.TabPanelStyle()
			tabPanelStyle.font = skin.getFont("default")
			tabPanelStyle.fontColor = Color.LIGHT_GRAY
			tabPanelStyle.overFontColor = Color.WHITE
			tabPanelStyle.bodyBackground = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/TextField.png"), 6, 6, 6, 6)).tint(Color(1f, 1f, 1f, 0.2f))
			tabPanelStyle.titleButtonUnselected = buttonBackground
			tabPanelStyle.titleButtonSelected = (tabPanelStyle.titleButtonUnselected as NinePatchDrawable).tint(Color(0.8f, 0.8f, 0.8f, 1.0f))
			skin.add("default", tabPanelStyle)

			return skin
		}
	}
}

class Settings
{
	val kryo = Kryo()

	val data = ObjectMap<String, Any>()

	fun hasKey(key: String) = data.containsKey(key)
	fun <T> get(key: String, default: T) = if (data.containsKey(key)) data[key] as T else default
	fun set(key: String, value: Any) { data[key] = value; save() }

	init
	{
		load()
	}

	fun save()
	{
		val outputFile = Gdx.files.local("settings.dat")

		var output: Output? = null
		try
		{
			output = Output(GZIPOutputStream(outputFile.write(false)))
		}
		catch (e: Exception)
		{
			e.printStackTrace()
			return
		}

		kryo.writeObject(output, data)

		output.close()
	}

	fun load()
	{
		var input: Input? = null

		try
		{
			input = Input(GZIPInputStream(Gdx.files.local("settings.dat").read()))
			val newData = kryo.readObject(input, ObjectMap::class.java)

			for (pair in newData)
			{
				data[pair.key as String] = pair.value
			}
		}
		catch (e: Exception)
		{
			e.printStackTrace()
		}
		finally
		{
			input?.close()
		}
	}
}