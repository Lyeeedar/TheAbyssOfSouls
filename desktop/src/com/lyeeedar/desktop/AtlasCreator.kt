package com.lyeeedar.desktop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.tools.texturepacker.TexturePacker
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Direction
import com.lyeeedar.Renderables.Sprite.TilingSprite
import com.lyeeedar.Util.EnumBitflag
import com.lyeeedar.Util.ImageUtils
import com.lyeeedar.Util.getChildrenByAttributeRecursively

import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.HashSet

/**
 * Created by Philip on 17-Jan-16.
 */
class AtlasCreator
{
	private val packer: TexturePacker

	private val packedPaths = ObjectSet<String>()

	init
	{
		buildTilingMasksArray()

		val settings = TexturePacker.Settings()
		settings.combineSubdirectories = true
		settings.duplicatePadding = true
		settings.maxWidth = 2048
		settings.maxHeight = 2048
		settings.paddingX = 4
		settings.paddingY = 4
		settings.useIndexes = false
		settings.filterMin = Texture.TextureFilter.MipMapLinearLinear
		settings.filterMag = Texture.TextureFilter.MipMapLinearLinear

		packer = TexturePacker(File("Sprites"), settings)

		findFilesRecursive(File("").absoluteFile)
		parseCodeFilesRecursive(File("../../core/src").absoluteFile)

		// pack default stuff

		// pack GUI
//		val guiDir = File("Sprites/GUI")
//		val guiFiles = guiDir.listFiles()
//		for (file in guiFiles)
//		{
//			if (file.path.endsWith(".png"))
//			{
//				packer.addImage(file)
//			}
//		}

		val outDir = File("Atlases")
		val contents = outDir.listFiles()
		if (contents != null)
			for (file in contents)
			{
				if (file.path.endsWith(".png"))
				{
					file.delete()
				}
				else if (file.path.endsWith(".atlas"))
				{
					file.delete()
				}
			}

		packer.pack(outDir, "SpriteAtlas")
	}

	private fun findFilesRecursive(dir: File)
	{
		val contents = dir.listFiles() ?: return

		for (file in contents)
		{
			if (file.isDirectory)
			{
				findFilesRecursive(file)
			}
			else if (file.path.endsWith(".xml"))
			{
				parseXml(file.path)
			}
		}
	}

	private fun parseCodeFilesRecursive(dir: File)
	{
		val contents = dir.listFiles() ?: return

		for (file in contents)
		{
			if (file.isDirectory)
			{
				parseCodeFilesRecursive(file)
			}
			else
			{
				parseCodeFile(file.path)
			}
		}
	}

	private fun parseCodeFile(file: String)
	{
		val contents = File(file).readText()
		val regex = Regex("AssetManager.loadSprite\\(\".*?\"")//(\".*\")")

		val occurances = regex.findAll(contents)

		for (occurance in occurances)
		{
			var path = occurance.value
			path = path.replace("AssetManager.loadSprite(\"", "")
			path = path.replace("\"", "")

			processSprite(path)
		}

		val regex2 = Regex("AssetManager.loadTextureRegion\\(\".*?\"")//(\".*\")")

		val occurances2 = regex2.findAll(contents)

		for (occurance in occurances2)
		{
			var path = occurance.value
			path = path.replace("AssetManager.loadTextureRegion(\"", "")
			path = path.replace("\"", "")

			processSprite(path)
		}

		val tilingRegex = Regex("TilingSprite\\(\".*?\", \".*?\", \".*?\"")
		val occurances3 = tilingRegex.findAll(contents)

		for (occurance in occurances3)
		{
			val split = occurance.value.split("\", \"")
			val baseName = split[1]
			val maskName = split[2].subSequence(0, split[2].length-1).toString()

			processTilingSprite(baseName, maskName, false)
		}
	}

	private fun parseXml(file: String)
	{
		val reader = XmlReader()
		var xml: XmlReader.Element? = null

		try
		{
			xml = reader.parse(Gdx.files.internal(file))
		} catch (e: Exception)
		{
			return
		}

		if (xml == null)
		{
			return
		}

		val spriteElements = Array<XmlReader.Element>()

		spriteElements.addAll(xml.getChildrenByAttributeRecursively("RefKey", "Sprite"))

		for (el in spriteElements)
		{
			val found = processSprite(el)
			if (!found)
			{
				throw RuntimeException("Failed to find sprite for file: " + file)
			}
		}

		val tilingSpriteElements = xml.getChildrenByAttributeRecursively("RefKey", "TilingSprite")

		for (el in tilingSpriteElements)
		{
			val succeed = processTilingSprite(el)
			if (!succeed)
			{
				throw RuntimeException("Failed to process tiling sprite in file: " + file)
			}
		}

		val particleElements = xml.getChildrenByNameRecursively("TextureKeyframes")

		for (el in particleElements)
		{
			val succeed = processParticle(el)
			if (!succeed)
			{
				throw RuntimeException("Failed to process particle in file: " + file)
			}
		}
	}

	private fun processParticle(xml: XmlReader.Element) : Boolean
	{
		val streamsEl = xml.getChildrenByName("Stream")
		if (streamsEl.size == 0)
		{
			return processParticleStream(xml)
		}
		else
		{
			for (el in streamsEl)
			{
				if (!processParticleStream(el)) return false
			}
		}

		return true
	}

	private fun processParticleStream(xml: XmlReader.Element) : Boolean
	{
		for (i in 0..xml.childCount-1)
		{
			val el = xml.getChild(i)
			var path: String

			if (el.text != null)
			{
				val split = el.text.split("|")
				path = split[1]
			}
			else
			{
				path = el.get("Value")
			}

			val found = processSprite(path)
			if (!found) return false
		}

		return true
	}

	private fun processTilingSprite(spriteElement: XmlReader.Element): Boolean
	{
		val topElement = spriteElement.getChildByName("Top")
		if (topElement != null)
		{
			// Predefined sprite

			val overhangElement = spriteElement.getChildByName("Overhang")
			val frontElement = spriteElement.getChildByName("Front")

			var exists = tryPackSprite(topElement)
			if (!exists)
			{
				return false
			}

			exists = tryPackSprite(frontElement)
			if (!exists)
			{
				return false
			}

			if (overhangElement != null)
			{

				// pack top overhang
				exists = packOverhang(topElement.get("Name"), overhangElement.get("Name"))
				if (!exists)
				{
					return false
				}

				// pack front overhang
				exists = packOverhang(frontElement.get("Name"), overhangElement.get("Name"))
				if (!exists)
				{
					return false
				}
			}
		} else
		{
			// Auto masking sprites
			val spriteDataElement = spriteElement.getChildByName("Sprite")

			val texName = spriteDataElement.get("Name")
			val maskName = spriteElement.get("Mask")
			val additive = spriteElement.getBoolean("Additive", false)

			val succeed = processTilingSprite(texName, maskName, additive)
			if (!succeed)
			{
				return false
			}
		}

		return true
	}

	private fun processTilingSprite(baseName: String, maskBaseName: String, additive: Boolean): Boolean
	{
		for (mask in tilingMasks)
		{
			val succeed = maskSprite(baseName, maskBaseName, mask, additive)

			if (!succeed)
			{
				return false
			}
		}

		return true
	}

	private fun packOverhang(topName: String, overhangName: String) : Boolean
	{
		val composedName = topName + ": Overhang :" + overhangName

		// File exists on disk, no need to compose
		if (tryPackSprite(composedName))
		{
			println("Added Overhang sprite: " + composedName)
			return true
		}

		val topHandle = Gdx.files.internal("Sprites/$topName.png")
		if (!topHandle.exists())
		{
			System.err.println("Failed to find sprite for: " + topName)
			return false
		}

		val overhangHandle = Gdx.files.internal("Sprites/$overhangName.png")
		if (!overhangHandle.exists())
		{
			System.err.println("Failed to find sprite for: " + overhangName)
			return false
		}

		val top = Pixmap(topHandle)
		val overhang = Pixmap(overhangHandle)
		val composed = ImageUtils.composeOverhang(top, overhang)
		top.dispose()
		overhang.dispose()

		val image = ImageUtils.pixmapToImage(composed)
		composed.dispose()

		val path = "Sprites/$composedName.png"
		packer.addImage(image, composedName)
		packedPaths.add(path)
		return true
	}

	private fun maskSprite(baseName: String, maskBaseName: String, masks: Array<String>, additive: Boolean): Boolean
	{
		// Build the mask suffix
		var mask = ""
		for (m in masks)
		{
			mask += "_" + m
		}

		val maskedName = baseName + "_" + maskBaseName + mask + "_" + additive

		// File exists on disk, no need to mask
		if (tryPackSprite(maskedName))
		{
			println("Added Tiling sprite: " + maskedName)
			return true
		}

		val baseHandle = Gdx.files.internal("Sprites/$baseName.png")
		if (!baseHandle.exists())
		{
			System.err.println("Failed to find sprite for: " + baseName)
			return false
		}

		val base = Pixmap(baseHandle)

		var merged = if (additive) Pixmap(base.width, base.height, Pixmap.Format.RGBA8888) else base
		for (maskSuffix in masks)
		{
			var maskHandle = Gdx.files.internal("Sprites/" + maskBaseName + "_" + maskSuffix + ".png")
			if (!maskHandle.exists())
			{
				maskHandle = Gdx.files.internal("Sprites/" + maskBaseName + "_C.png")
			}

			if (!maskHandle.exists())
			{
				maskHandle = Gdx.files.internal("Sprites/$maskBaseName.png")
			}

			if (!maskHandle.exists())
			{
				System.err.println("Failed to find mask for: " + maskBaseName + "_" + maskSuffix)
				return false
			}

			val maskPixmap = Pixmap(maskHandle)
			val currentPixmap = if (additive) base else merged

			val maskedTex = ImageUtils.multiplyPixmap(currentPixmap, maskPixmap)

			if (additive)
			{
				val addedText = ImageUtils.addPixmap(merged, maskedTex)
				merged.dispose()
				maskedTex.dispose()

				merged = addedText
			} else
			{
				if (merged !== base)
				{
					merged.dispose()
				}
				merged = maskedTex
			}
		}

		val image = ImageUtils.pixmapToImage(merged)
		merged.dispose()

		val path = "Sprites/$maskedName.png"
		packer.addImage(image, maskedName)
		packedPaths.add(path)

		println("Added Tiling sprite: " + maskedName)

		return true
	}

	private fun tryPackSprite(element: XmlReader.Element): Boolean
	{
		val name = element.get("Name")
		val exists = tryPackSprite(name)
		if (!exists)
		{
			System.err.println("Could not find sprites with name: " + name)
			return false
		} else
		{
			println("Added sprites for name: " + name)
			return true
		}
	}

	private fun tryPackSprite(name: String): Boolean
	{
		var path = name
		if (!path.startsWith("Sprites/")) path = "Sprites/" + path
		if (!path.endsWith(".png")) path += ".png"

		if (packedPaths.contains(path))
		{
			return true
		}

		val handle = Gdx.files.internal(path)

		if (handle.exists())
		{
			packer.addImage(handle.file())
			packedPaths.add(path)
			return true
		}
		else
		{
			return false
		}
	}

	private fun processSprite(spriteElement: XmlReader.Element): Boolean
	{
		val name = spriteElement.get("Name", null) ?: return true

		return processSprite(name)
	}

	private fun processSprite(name: String): Boolean
	{
		var foundCount = 0

		// Try 0 indexed sprite
		var i = 0
		while (true)
		{
			val exists = tryPackSprite(name + "_" + i)
			if (!exists)
			{
				break
			} else
			{
				foundCount++
			}

			i++
		}

		// Try 1 indexed sprite
		if (foundCount == 0)
		{
			i = 1
			while (true)
			{
				val exists = tryPackSprite(name + "_" + i)
				if (!exists)
				{
					break
				} else
				{
					foundCount++
				}

				i++
			}
		}

		// Try sprite without indexes
		if (foundCount == 0)
		{
			val exists = tryPackSprite(name)
			if (exists)
			{
				foundCount++
			}
		}

		if (foundCount == 0)
		{
			System.err.println("Could not find sprites with name: " + name)
		} else
		{
			println("Added sprites for name: " + name)
		}

		return foundCount > 0
	}

	companion object
	{

		var tilingMasks = Array<Array<String>>()
		fun buildTilingMasksArray()
		{
			val directions = HashSet<Direction>()
			for (dir in Direction.Values)
			{
				directions.add(dir)
			}

			val powerSet = powerSet(directions)

			val alreadyAdded = HashSet<String>()

			for (set in powerSet)
			{
				val bitflag = EnumBitflag<Direction>()
				for (dir in set)
				{
					bitflag.setBit(dir)
				}

				val masks = TilingSprite.getMasks(bitflag)
				var mask = ""
				for (m in masks)
				{
					mask += "_" + m
				}

				if (!alreadyAdded.contains(mask))
				{
					tilingMasks.add(masks)
					alreadyAdded.add(mask)
				}
			}
		}

		fun <T> powerSet(originalSet: Set<T>): Set<Set<T>>
		{
			val sets = HashSet<Set<T>>()
			if (originalSet.isEmpty())
			{
				sets.add(HashSet<T>())
				return sets
			}
			val list = ArrayList(originalSet)
			val head = list[0]
			val rest = HashSet(list.subList(1, list.size))
			for (set in powerSet(rest))
			{
				val newSet = HashSet<T>()
				newSet.add(head)
				newSet.addAll(set)
				sets.add(newSet)
				sets.add(set)
			}
			return sets
		}
	}
}
