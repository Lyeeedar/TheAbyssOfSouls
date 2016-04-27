package com.lyeeedar

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.lyeeedar.Level.Level
import com.lyeeedar.Quests.QuestManager
import com.lyeeedar.Sound.SoundGroup
import com.lyeeedar.Systems.createEngine
import com.lyeeedar.UI.Seperator
import com.lyeeedar.UI.TabPanel
import com.lyeeedar.UI.Tooltip
import com.lyeeedar.Util.Controls
import java.util.concurrent.Executors

/**
 * Created by Philip on 20-Mar-16.
 */

class GlobalData
{
	companion object
	{
		@JvmField val Global: GlobalData = GlobalData()
	}

	@JvmField val targetResolution: FloatArray = floatArrayOf(800f, 600f)
	@JvmField val resolution: FloatArray = floatArrayOf(800f, 600f)
	@JvmField val screenSize: IntArray = intArrayOf(800, 600)

	@JvmField var fps: Int = 60

	lateinit var game: MainGame

	@JvmField var currentLevel: Level = Level()
	lateinit var engine: Engine
	lateinit var questManager: QuestManager

	val player: Entity
		get() = currentLevel.player

	@JvmField val controls: Controls = Controls()
	lateinit var skin: Skin
	@JvmField var canMoveDiagonal: Boolean = false
	@JvmField var movementTypePathfind: Boolean = false
	@JvmField var animationSpeed: Float = 1f
	@JvmField var tileSize: Float = 32f

	@JvmField var musicVolume: Float = 1f

	@JvmField var applicationChanger: AbstractApplicationChanger? = null
	@JvmField var android: Boolean = false
	@JvmField var release: Boolean = false

	@JvmField val threadpool = Executors.newFixedThreadPool(3)

	fun setup()
	{
		skin = loadSkin()
		engine = createEngine()
		questManager = QuestManager()
		SoundGroup.init()
	}

	fun loadSkin(): Skin
	{
		val skin = Skin()

		val font = AssetManager.loadFont("Sprites/Unpacked/font.ttf", 12, Color(0.97f, 0.87f, 0.7f, 1f), 1, Color.BLACK, false)
		skin.add("default", font)

		val titlefont = AssetManager.loadFont("Sprites/Unpacked/font.ttf", 20, Color(1f, 0.9f, 0.8f, 1f), 1, Color.BLACK, true)
		skin.add("title", titlefont)

		val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
		pixmap.setColor(Color.WHITE)
		pixmap.fill()
		skin.add("white", Texture(pixmap))

		val textField = TextField.TextFieldStyle()
		textField.fontColor = Color.WHITE
		textField.font = skin.getFont("default")
		textField.background = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/TextField.png"), 6, 6, 6, 6))
		textField.focusedBackground = (textField.background as NinePatchDrawable).tint(Color(0.9f, 0.9f, 0.9f, 1.0f))
		textField.cursor = skin.newDrawable("white", Color.WHITE)
		textField.selection = skin.newDrawable("white", Color.LIGHT_GRAY)
		skin.add("default", textField)

		val label = Label.LabelStyle()
		label.font = skin.getFont("default")
		skin.add("default", label)

		val titleLabel = Label.LabelStyle()
		titleLabel.font = skin.getFont("title")
		skin.add("title", titleLabel)

		val checkButton = CheckBox.CheckBoxStyle()
		checkButton.checkboxOff = TextureRegionDrawable(AssetManager.loadTextureRegion("Sprites/GUI/Unchecked.png"))
		checkButton.checkboxOn = TextureRegionDrawable(AssetManager.loadTextureRegion("Sprites/GUI/Checked.png"))
		checkButton.font = skin.getFont("default")
		checkButton.fontColor = Color.LIGHT_GRAY
		checkButton.overFontColor = Color.WHITE
		skin.add("default", checkButton)

		val textButton = TextButton.TextButtonStyle()
		textButton.up = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/Button.png"), 12, 12, 12, 12))
		textButton.font = skin.getFont("default")
		textButton.fontColor = Color.LIGHT_GRAY
		textButton.overFontColor = Color.WHITE
		//textButton.checked = new NinePatchDrawable( new NinePatch( AssetManager.loadTextureRegion( "Sprites/GUI/ButtonDown.png" ), 12, 12, 12, 12 ) );
		textButton.over = (textButton.up as NinePatchDrawable).tint(Color(0.9f, 0.9f, 0.9f, 1.0f))
		skin.add("default", textButton)

		val bigTextButton = TextButton.TextButtonStyle()
		bigTextButton.up = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/Button.png"), 12, 12, 12, 12))
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
		buttonStyle.up = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/Button.png"), 12, 12, 12, 12))
		buttonStyle.over = (buttonStyle.up as NinePatchDrawable).tint(Color(0.9f, 0.9f, 0.9f, 1.0f))
		skin.add("default", buttonStyle)

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
		scrollPaneStyle.vScrollKnob = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/Button.png"), 12, 12, 12, 12))
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
		sliderStyle.knob = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/Button.png"), 12, 12, 12, 12))
		sliderStyle.knobOver = (sliderStyle.knob as NinePatchDrawable).tint(Color(0.9f, 0.9f, 0.9f, 1.0f))
		sliderStyle.knobDown = (sliderStyle.knob as NinePatchDrawable).tint(Color.LIGHT_GRAY)
		skin.add("default-horizontal", sliderStyle)

		val tabPanelStyle = TabPanel.TabPanelStyle()
		tabPanelStyle.font = skin.getFont("default")
		tabPanelStyle.fontColor = Color.LIGHT_GRAY
		tabPanelStyle.overFontColor = Color.WHITE
		tabPanelStyle.bodyBackground = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/TextField.png"), 6, 6, 6, 6)).tint(Color(1f, 1f, 1f, 0.2f))
		tabPanelStyle.titleButtonUnselected = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/Button.png"), 12, 12, 12, 12))
		tabPanelStyle.titleButtonSelected = (tabPanelStyle.titleButtonUnselected as NinePatchDrawable).tint(Color(0.8f, 0.8f, 0.8f, 1.0f))
		skin.add("default", tabPanelStyle)

		return skin
	}
}