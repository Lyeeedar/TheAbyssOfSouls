package com.lyeeedar

import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Sprite.Sprite

/**
 * Created by Philip on 30-Mar-16.
 */

fun loadSprite(xml: XmlReader.Element?): Sprite?
{
	if (xml == null) return null
	return AssetManager.loadSprite(xml)
}