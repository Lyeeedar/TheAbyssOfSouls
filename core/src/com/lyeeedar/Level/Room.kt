package com.lyeeedar.Level

import com.badlogic.gdx.utils.ObjectSet

/**
 * Created by Philip on 19-Apr-16.
 */

class Room(val x: Int, val y: Int, val width: Int, val height: Int)
{
	lateinit var level: Level
	val metavalues: ObjectSet<String> = ObjectSet()
	val neighbours: com.badlogic.gdx.utils.Array<Room> = com.badlogic.gdx.utils.Array()
}