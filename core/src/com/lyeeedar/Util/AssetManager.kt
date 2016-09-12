package com.lyeeedar.Util

import java.nio.IntBuffer
import java.util.HashMap

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Pixmap.Format
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.PixmapPacker
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.BufferUtils
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader.Element
import com.lyeeedar.Renderables.Animation.AbstractAnimation
import com.lyeeedar.Renderables.Particle.ParticleEffect
import com.lyeeedar.Renderables.Sprite.DirectionalSprite
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Renderables.Sprite.TilingSprite

class AssetManager
{
	companion object
	{
		private val loadedFonts = HashMap<String, BitmapFont>()

		@JvmOverloads fun loadFont(name: String, size: Int, colour: Color = Color.WHITE, borderWidth: Int = 1, borderColour: Color = Color.BLACK, shadow: Boolean = false): BitmapFont?
		{
			val key = name + size + colour.toString() + borderWidth + borderColour.toString()

			if (loadedFonts.containsKey(key))
			{
				return loadedFonts[key]
			}

			val fgenerator = FreeTypeFontGenerator(Gdx.files.internal(name))
			val parameter = FreeTypeFontParameter()
			parameter.size = size
			parameter.borderWidth = borderWidth.toFloat()
			parameter.kerning = true
			parameter.borderColor = borderColour
			parameter.borderStraight = true
			parameter.color = colour

			if (shadow)
			{
				parameter.shadowOffsetX = -1
				parameter.shadowOffsetY = 1
			}

			val font = fgenerator.generateFont(parameter)
			font.data.markupEnabled = true
			fgenerator.dispose() // don't forget to dispose to avoid memory leaks!

			loadedFonts.put(key, font)

			return font
		}

		private val loadedSounds = HashMap<String, Sound?>()

		fun loadSound(path: String): Sound?
		{
			if (loadedSounds.containsKey(path))
			{
				return loadedSounds[path]
			}

			var file = Gdx.files.internal("Sounds/$path.mp3")
			if (!file.exists())
			{
				file = Gdx.files.internal("Sounds/$path.ogg")

				if (!file.exists())
				{
					loadedSounds.put(path, null)
					return null
				}
			}

			val sound = Gdx.audio.newSound(file)

			loadedSounds.put(path, sound)

			return sound
		}

		private val prepackedAtlas = TextureAtlas(Gdx.files.internal("Atlases/SpriteAtlas.atlas"))

		private val loadedTextureRegions = HashMap<String, TextureRegion?>()

		fun loadTextureRegion(path: String): TextureRegion?
		{
			if (loadedTextureRegions.containsKey(path))
			{
				return loadedTextureRegions[path]
			}

			var atlasName = path
			atlasName = atlasName.replaceFirst("Sprites/".toRegex(), "")
			atlasName = atlasName.replace(".png", "")

			val region = prepackedAtlas.findRegion(atlasName)
			if (region != null)
			{
				val textureRegion = TextureRegion(region)
				loadedTextureRegions.put(path, textureRegion)
				return textureRegion
			} else
			{
				loadedTextureRegions.put(path, null)
				return null
			}
		}

		private val loadedTextures = HashMap<String, Texture?>()

		fun loadTexture(path: String, filter: TextureFilter = TextureFilter.Linear, wrapping: Texture.TextureWrap = Texture.TextureWrap.ClampToEdge): Texture?
		{
			if (loadedTextures.containsKey(path))
			{
				return loadedTextures[path]
			}

			val file = Gdx.files.internal(path)
			if (!file.exists())
			{
				loadedTextures.put(path, null)
				return null
			}

			val region = Texture(path)
			region.setFilter(filter, filter)
			region.setWrap(wrapping, wrapping)
			loadedTextures.put(path, region)

			return region
		}

		fun loadParticleEffect(name: String): ParticleEffect
		{
			val effect = ParticleEffect.load(name)
			return effect
		}

		fun loadParticleEffect(xml: Element): ParticleEffect
		{
			val effect = ParticleEffect.load(xml.get("Name"))

			val colourElement = xml.getChildByName("Colour")
			var colour = Colour(1f, 1f, 1f, 1f)
			if (colourElement != null)
			{
				colour = loadColour(colourElement)
			}

			effect.colour.set(colour)

			effect.speedMultiplier = xml.getFloat("SpeedMultiplier", 1f)

			effect.flipX = xml.getBoolean("FlipX", false)
			effect.flipY = xml.getBoolean("FlipY", false)

			return effect
		}

		fun loadSprite(name: String, drawActualSize: Boolean): Sprite
		{
			return loadSprite(name, 0.5f, Colour(1f, 1f, 1f, 1f), Sprite.AnimationMode.TEXTURE, drawActualSize)
		}

		fun loadSprite(name: String, updateTime: Float, reverse: Boolean): Sprite
		{
			return loadSprite(name, updateTime, Colour(1f, 1f, 1f, 1f), Sprite.AnimationMode.TEXTURE, false, reverse)
		}

		@JvmOverloads fun loadSprite(name: String, updateTime: Float = 0.5f, colour: Colour = Colour(1f, 1f, 1f, 1f), mode: Sprite.AnimationMode = Sprite.AnimationMode.TEXTURE, drawActualSize: Boolean = false, reverse: Boolean = false): Sprite
		{
			var updateTime = updateTime
			val textures = Array<TextureRegion>(false, 1, TextureRegion::class.java)

			// Try 0 indexed sprite
			var i = 0
			while (true)
			{
				val tex = loadTextureRegion("Sprites/" + name + "_" + i + ".png")

				if (tex == null)
				{
					break
				} else
				{
					textures.add(tex)
				}

				i++
			}

			// Try 1 indexed sprite
			if (textures.size == 0)
			{
				i = 1
				while (true)
				{
					val tex = loadTextureRegion("Sprites/" + name + "_" + i + ".png")

					if (tex == null)
					{
						break
					} else
					{
						textures.add(tex)
					}

					i++
				}
			}

			// Try sprite without indexes
			if (textures.size == 0)
			{
				val tex = loadTextureRegion("Sprites/$name.png")

				if (tex != null)
				{
					textures.add(tex)
				}
			}

			if (textures.size == 0)
			{
				throw RuntimeException("Cant find any textures for $name!")
			}

			if (reverse)
			{
				textures.reverse()
			}

			if (updateTime <= 0)
			{
				if (mode === Sprite.AnimationMode.SINE)
				{
					updateTime = 4f
				} else
				{
					updateTime = 0.5f
				}
			}

			val sprite = Sprite(name, updateTime, textures, colour, mode, drawActualSize)

			return sprite
		}

		fun tryLoadSpriteWithResources(xml: Element, resources: ObjectMap<String, Element>): Sprite
		{
			if (xml.childCount == 0) return loadSprite(resources[xml.text])
			else return loadSprite(xml)
		}

		fun tryLoadSprite(xml: Element?): Sprite?
		{
			if (xml == null) return null
			else if (xml.childCount == 0) return null
			else return loadSprite(xml)
		}

		fun loadSprite(xml: Element): Sprite
		{
			val colourElement = xml.getChildByName("Colour")
			var colour = Colour(1f, 1f, 1f, 1f)
			if (colourElement != null)
			{
				colour = loadColour(colourElement)
			}

			val sprite = loadSprite(
					xml.get("Name"),
					xml.getFloat("UpdateRate", 0f),
					colour,
					Sprite.AnimationMode.valueOf(xml.get("AnimationMode", "Texture").toUpperCase()),
					xml.getBoolean("DrawActualSize", false))

			sprite.repeatDelay = xml.getFloat("RepeatDelay", 0f)

			val animationElement = xml.getChildByName("Animation")
			if (animationElement != null)
			{
				sprite.animation = AbstractAnimation.load(animationElement.getChild(0))
			}

			return sprite
		}

		fun loadSprite(xml: Element, texture: TextureRegion): Sprite
		{
			val colourElement = xml.getChildByName("Colour")
			var colour = Colour(1f, 1f, 1f, 1f)
			if (colourElement != null)
			{
				colour = loadColour(colourElement)
			}

			val textures = Array<TextureRegion>(false, 1, TextureRegion::class.java)
			textures.add(texture)

			var updateTime = xml.getFloat("UpdateRate", 0f)
			val mode = Sprite.AnimationMode.valueOf(xml.get("AnimationMode", "Texture").toUpperCase())

			if (updateTime <= 0)
			{
				if (mode === Sprite.AnimationMode.SINE)
				{
					updateTime = 4f
				} else
				{
					updateTime = 0.5f
				}
			}

			val sprite = Sprite(xml.get("Name", ""),
					updateTime,
					textures,
					colour,
					mode,
					xml.getBoolean("DrawActualSize", false))

			sprite.repeatDelay = xml.getFloat("RepeatDelay", 0f)

			val animationElement = xml.getChildByName("Animation")
			if (animationElement != null)
			{
				sprite.animation = AbstractAnimation.load(animationElement.getChild(0))
			}


			return sprite
		}

		fun loadColour(stringCol: String, colour: Colour = Colour()): Colour
		{
			val cols = stringCol.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
			colour.r = java.lang.Float.parseFloat(cols[0]) / 255.0f
			colour.g = java.lang.Float.parseFloat(cols[1]) / 255.0f
			colour.b = java.lang.Float.parseFloat(cols[2]) / 255.0f
			colour.a = if (cols.size > 3) cols[3].toFloat() / 255.0f else 1f

			return colour
		}

		fun loadColour(xml: Element): Colour
		{
			return loadColour(xml.text)
		}

		fun loadTilingSprite(xml: Element): TilingSprite
		{
			return TilingSprite.load(xml)
		}

		fun loadDirectionalSprite(xml: Element): DirectionalSprite
		{
			val directionalSprite = DirectionalSprite()

			val anims = xml.getChildByName("Animations")
			for (i in 0.. anims.childCount-1)
			{
				val el = anims.getChild(i)
				val name = el.name
				val up = AssetManager.loadSprite(el.getChildByName("Up"))
				val down = AssetManager.loadSprite(el.getChildByName("Down"))

				directionalSprite.addAnim(name, up, down)
			}

			return directionalSprite
		}
	}
}