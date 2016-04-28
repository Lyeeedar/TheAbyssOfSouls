package com.lyeeedar

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
import com.badlogic.gdx.utils.XmlReader.Element
import com.lyeeedar.Sound.SoundInstance
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Sprite.SpriteAnimation.AbstractSpriteAnimation
import com.lyeeedar.Sprite.TilingSprite
import com.lyeeedar.Util.Colour

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

fun loadTexture(path: String): Texture?
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
	region.setFilter(TextureFilter.Linear, TextureFilter.Linear)
	loadedTextures.put(path, region)

	return region
}

fun loadSprite(name: String, drawActualSize: Boolean): Sprite
{
	return loadSprite(name, 0.5f, Colour(1f), Sprite.AnimationMode.TEXTURE, null, drawActualSize)
}

fun loadSprite(name: String, updateTime: Float, sound: String): Sprite
{
	return loadSprite(name, updateTime, Colour(1f), Sprite.AnimationMode.TEXTURE, SoundInstance.getSound(sound), false)
}

@JvmOverloads fun loadSprite(name: String, updateTime: Float = 0.5f, colour: Colour = Colour(1f), mode: Sprite.AnimationMode = Sprite.AnimationMode.TEXTURE, sound: SoundInstance? = null, drawActualSize: Boolean = false): Sprite
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

	val sprite = Sprite(name, updateTime, textures, colour, mode, sound, drawActualSize)

	return sprite
}

fun loadSprite(xml: Element): Sprite
{
	val colourElement = xml.getChildByName("Colour")
	var colour = Colour(1f)
	if (colourElement != null)
	{
		colour = loadColour(colourElement)
	}

	val soundElement = xml.getChildByName("Sound")
	var sound: SoundInstance? = null
	if (soundElement != null)
	{
		sound = SoundInstance.load(soundElement)
	}

	val sprite = loadSprite(
			xml.get("Name"),
			xml.getFloat("UpdateRate", 0f),
			colour,
			Sprite.AnimationMode.valueOf(xml.get("AnimationMode", "Texture").toUpperCase()),
			sound,
			xml.getBoolean("DrawActualSize", false))

	sprite.repeatDelay = xml.getFloat("RepeatDelay", 0f)

	sprite.flipX = xml.getBoolean("FlipX", false)
	sprite.flipY = xml.getBoolean("FlipY", false)

	val animationElement = xml.getChildByName("Animation")
	if (animationElement != null)
	{
		sprite.spriteAnimation = AbstractSpriteAnimation.load(animationElement.getChild(0))
	}

	return sprite
}

fun loadSprite(xml: Element, texture: TextureRegion): Sprite
{
	val colourElement = xml.getChildByName("Colour")
	var colour = Colour(1f)
	if (colourElement != null)
	{
		colour = loadColour(colourElement)
	}

	val soundElement = xml.getChildByName("Sound")
	var sound: SoundInstance? = null
	if (soundElement != null)
	{
		sound = SoundInstance.load(soundElement)
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

	val sprite = Sprite(xml.get("Name", null),
			updateTime,
			textures,
			colour,
			mode,
			sound,
			xml.getBoolean("DrawActualSize", false))

	sprite.repeatDelay = xml.getFloat("RepeatDelay", 0f)

	sprite.flipX = xml.getBoolean("FlipX", false)
	sprite.flipY = xml.getBoolean("FlipY", false)

	val animationElement = xml.getChildByName("Animation")
	if (animationElement != null)
	{
		sprite.spriteAnimation = AbstractSpriteAnimation.load(animationElement.getChild(0))
	}


	return sprite
}

fun loadColour(xml: Element): Colour
{
	val colour = Colour()
	colour.a = 1f

	val rgb = xml.get("RGB", null)
	if (rgb != null)
	{
		val cols = rgb.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
		colour.r = java.lang.Float.parseFloat(cols[0]) / 255.0f
		colour.g = java.lang.Float.parseFloat(cols[1]) / 255.0f
		colour.b = java.lang.Float.parseFloat(cols[2]) / 255.0f
	}

	colour.r = xml.getFloat("Red", colour.r)
	colour.g = xml.getFloat("Green", colour.g)
	colour.b = xml.getFloat("Blue", colour.b)
	colour.a = xml.getFloat("Alpha", colour.a)

	return colour
}

fun loadTilingSprite(xml: Element): TilingSprite
{
	return TilingSprite.load(xml)
}
