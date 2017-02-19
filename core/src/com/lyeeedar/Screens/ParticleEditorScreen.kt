package com.lyeeedar.Screens

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.HDRColourSpriteBatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Global
import com.lyeeedar.Renderables.Animation.MoveAnimation
import com.lyeeedar.Renderables.Particle.ParticleEffect
import com.lyeeedar.Renderables.Particle.Particle
import com.lyeeedar.Renderables.Renderable
import com.lyeeedar.Renderables.SortedRenderer
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Renderables.Sprite.TilingSprite
import com.lyeeedar.Util.*
import com.lyeeedar.UI.addClickListener
import javax.swing.JColorChooser
import javax.swing.JFileChooser
import ktx.collections.get
import ktx.collections.set

/**
 * Created by Philip on 14-Aug-16.
 */

class ParticleEditorScreen : AbstractScreen()
{
	var currentPath: String? = null
	lateinit var particle: ParticleEffect
	val batch = HDRColourSpriteBatch()
	lateinit var background: Array2D<Symbol>
	lateinit var collision: Array2D<Boolean>
	val tileSize = 32f
	val spriteRender = SortedRenderer(tileSize, 100f, 100f, 2)
	val shape = ShapeRenderer()
	var colour: java.awt.Color = java.awt.Color.WHITE
	val crossedTiles = ObjectSet<Point>()

	override fun create()
	{
		val browseButton = TextButton("...", Global.skin)
		val updateButton = TextButton("Update", Global.skin)
		val playbackSpeedBox = SelectBox<Float>(Global.skin)
		playbackSpeedBox.setItems(0.01f, 0.05f, 0.1f, 0.25f, 0.5f, 0.75f, 1f, 1.5f, 2f, 3f, 4f, 5f)
		playbackSpeedBox.selected = 1f

		val colourButton = TextButton("Colour", Global.skin)

		playbackSpeedBox.addListener(object : ChangeListener()
		{
			override fun changed(event: ChangeEvent?, actor: Actor?)
			{
				particle.speedMultiplier = playbackSpeedBox.selected
			}

		})

		colourButton.addClickListener {
			colour = JColorChooser.showDialog(null, "Particle Colour", colour)
			particle.colour.set(colour.red / 255f, colour.green / 255f, colour.blue / 255f, colour.alpha / 255f)
			colourButton.color = particle.colour.color()
		}

		browseButton.addClickListener {
			val fc = JFileChooser()
			fc.currentDirectory = Gdx.files.internal("Particles").file().absoluteFile
			val returnVal = fc.showOpenDialog(null)

			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				val file = fc.selectedFile

				currentPath = file.name

				val nparticle = ParticleEffect.Companion.load(currentPath!!)
				nparticle.killOnAnimComplete = false
				nparticle.setPosition(particle.position.x, particle.position.y)
				nparticle.rotation = particle.rotation
				nparticle.speedMultiplier = playbackSpeedBox.selected
				nparticle.colour.set(colour.red / 255f, colour.green / 255f, colour.blue / 255f, colour.alpha / 255f)
				particle = nparticle
			}
		}

		updateButton.addClickListener {

			val nparticle = ParticleEffect.Companion.load(currentPath!!)
			nparticle.killOnAnimComplete = false
			nparticle.setPosition(particle.position.x, particle.position.y)
			nparticle.rotation = particle.rotation
			nparticle.speedMultiplier = playbackSpeedBox.selected
			nparticle.colour.set(colour.red / 255f, colour.green / 255f, colour.blue / 255f, colour.alpha / 255f)
			particle = nparticle
		}

		mainTable.add(browseButton).expandY().top()
		mainTable.add(updateButton).expandY().top()
		mainTable.add(playbackSpeedBox).expandY().top()
		mainTable.add(colourButton).expandY().top()

		particle = ParticleEffect()

		loadLevel()
	}

	fun loadLevel()
	{
		val xml = getXml("Particles/ParticleTestLevel")

		val symbolsEl = xml.getChildByName("Symbols")
		val symbolMap = ObjectMap<Char, Symbol>()

		for (i in 0..symbolsEl.childCount-1)
		{
			val el = symbolsEl.getChild(i)
			val symbol = Symbol.load(el)
			symbolMap[symbol.char] = symbol
		}

		val rowsEl = xml.getChildByName("Rows")
		val width = rowsEl.getChild(0).text.length
		val height = rowsEl.childCount

		background = Array2D(width, height) { x, y -> symbolMap[rowsEl.getChild(height - y - 1).text[x]].copy() }
		collision = Array2D(width, height) { x, y -> background[x, y].isWall }
	}

	val tempPoint = Point()
	override fun doRender(delta: Float)
	{
		particle.collisionGrid = collision

		spriteRender.begin(delta, 0f, 0f)

		for (x in 0..background.xSize-1)
		{
			for (y in 0..background.ySize-1)
			{
				val symbol = background[x, y]
				var i = 0
				for (renderable in symbol.sprites)
				{
					tempPoint.set(x, y)
					val col = if (crossedTiles.contains(tempPoint)) Color.GOLD else Color.WHITE

					if (renderable is Sprite)
					{
						spriteRender.queueSprite(renderable, x.toFloat(), y.toFloat(), 0, i++, Colour(col))
					}
					else if (renderable is TilingSprite)
					{
						spriteRender.queueSprite(renderable, x.toFloat(), y.toFloat(), 0, i++, Colour(col))
					}
				}
			}
		}
		spriteRender.queueParticle(particle, 0f, 0f, 1, 0)

		batch.color = Color.WHITE
		batch.begin()
		spriteRender.flush(batch)
		batch.end()

		shape.projectionMatrix = stage.camera.combined
		shape.setAutoShapeType(true)
		shape.begin()

		particle.debug(shape, 0f, 0f, tileSize)

		shape.end()
	}

	override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean
	{
		val p1 = Vector2(particle.position)
		val p2 = Vector2(screenX / tileSize, (stage.height - screenY) / tileSize)

		particle.position.set(p2)

		val dist = p1.dst(p2)

		particle.animation = MoveAnimation.obtain().set(dist * particle.moveSpeed, arrayOf(p1, p2), Interpolation.linear)
		if (particle.moveSpeed > 0f) particle.rotation = getRotation(p1, p2)

		Point.freeAll(crossedTiles)
		crossedTiles.clear()
		particle.collisionFun = fun(x:Int, y:Int) { crossedTiles.add(Point.obtain().set(x, y)) }

		particle.start()

		return true
	}
}

class Symbol
{
	var char: Char = ' '
	val sprites: Array<Renderable> = Array()
	var isWall: Boolean = false

	fun copy(): Symbol
	{
		val symbol = Symbol()
		symbol.char = char
		for (sprite in sprites)
		{
			symbol.sprites.add(sprite.copy())
		}
		symbol.isWall = isWall

		return symbol
	}

	companion object
	{
		fun load(xml: XmlReader.Element) : Symbol
		{
			val symbol = Symbol()
			symbol.isWall = xml.getBooleanAttribute("IsWall", false)

			for (i in 0..xml.childCount-1)
			{
				val el = xml.getChild(i)
				if (el.name == "Char") symbol.char = el.text[0]
				else
				{
					if (el.name == "Sprite")
					{
						symbol.sprites.add(AssetManager.loadSprite(el))
					}
					else if (el.name == "TilingSprite")
					{
						symbol.sprites.add(AssetManager.loadTilingSprite(el))
					}
					else
					{
						throw RuntimeException("Invalid symbol data type '${el.name}'!")
					}
				}
			}

			return symbol
		}
	}
}