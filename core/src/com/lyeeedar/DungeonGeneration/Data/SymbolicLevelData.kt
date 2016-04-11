package com.lyeeedar.DungeonGeneration.Data

import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader

/**
 * Created by Philip on 08-Apr-16.
 */

class SymbolicLevelData()
{
	val symbolMap: ObjectMap<Char, Symbol> = ObjectMap()
	val rooms: com.badlogic.gdx.utils.Array<SymbolicRoomData> = com.badlogic.gdx.utils.Array()
	val roomGenerators: com.badlogic.gdx.utils.Array<XmlReader.Element> = com.badlogic.gdx.utils.Array()
	var preprocessor: XmlReader.Element? = null
	val corridor: SymbolicCorridorData = SymbolicCorridorData()
}
