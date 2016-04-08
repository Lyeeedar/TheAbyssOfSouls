package com.lyeeedar.DungeonGeneration.Data

import com.badlogic.gdx.utils.ObjectMap

/**
 * Created by Philip on 08-Apr-16.
 */

class SymbolicLevelData()
{
	val symbolMap: ObjectMap<Char, Symbol> = ObjectMap()
	val rooms: com.badlogic.gdx.utils.Array<SymbolicRoomData> = com.badlogic.gdx.utils.Array()
}
