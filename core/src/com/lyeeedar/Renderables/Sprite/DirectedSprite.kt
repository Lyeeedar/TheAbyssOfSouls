package com.lyeeedar.Renderables.Sprite

import com.badlogic.gdx.utils.IntMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Direction
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.EnumBitflag

class DirectedSprite(val name: String)
{
	val sprites: IntMap<Sprite> = IntMap()

	init
	{
		val bitflag = EnumBitflag<Direction>()

		for (dir in allDirs)
		{
			val path = name + "_" + dir

			try
			{
				val sprite = AssetManager.loadSprite(path)

				bitflag.clear()
				if (dir.contains("C")) bitflag.setBit(Direction.CENTRE)
				if (dir.contains("N")) bitflag.setBit(Direction.NORTH)
				if (dir.contains("S")) bitflag.setBit(Direction.SOUTH)
				if (dir.contains("E")) bitflag.setBit(Direction.EAST)
				if (dir.contains("W")) bitflag.setBit(Direction.WEST)

				sprites.put(bitflag.bitFlag, sprite)
			}
			catch (e: Exception) {System.err.println("failed to load sprite $path")}
		}
	}

	fun getSprite(bitflag: EnumBitflag<Direction>): Sprite? = sprites.get(bitflag.bitFlag)

	companion object
	{
		val allDirs = arrayOf(
				"C",
				"N", "S", "E", "W",
				"NS", "EW", "NE", "NW", "SE", "SW",
				"NEW", "SEW", "NSE", "NSW",
				"NSEW")

		fun load(xml: XmlReader.Element): DirectedSprite = DirectedSprite(xml.text)
	}
}