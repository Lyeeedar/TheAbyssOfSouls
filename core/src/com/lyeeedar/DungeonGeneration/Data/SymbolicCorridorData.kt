package com.lyeeedar.DungeonGeneration.Data

/**
 * Created by Philip on 11-Apr-16.
 */

class SymbolicCorridorData()
{
	enum class Style
	{
		NORMAL,
		WANDERING
	}

	lateinit var style: Style
	var width: Int = 1
}