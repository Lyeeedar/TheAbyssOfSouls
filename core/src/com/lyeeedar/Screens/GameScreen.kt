package com.lyeeedar.Screens

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.lyeeedar.AssetManager
import com.lyeeedar.Components.*
import com.lyeeedar.GlobalData
import com.lyeeedar.Level.Level
import com.lyeeedar.Level.Tile
import com.lyeeedar.Sprite.SpriteAnimation.MoveAnimation
import com.lyeeedar.Systems.*
import com.lyeeedar.UI.ButtonKeyboardHelper
import com.lyeeedar.Util.Array2D
import com.lyeeedar.Util.Colour

/**
 * Created by Philip on 20-Mar-16.
 */

class GameScreen(): AbstractScreen()
{
	// ----------------------------------------------------------------------
	override fun create()
	{

	}

	// ----------------------------------------------------------------------
	override fun doRender(delta: Float)
	{
		GlobalData.Global.engine.update(delta)
	}

	// ----------------------------------------------------------------------
	override fun scrolled(amount: Int): Boolean
	{
		GlobalData.Global.tileSize -= amount * 5;
		if ( GlobalData.Global.tileSize < 2 )
		{
			GlobalData.Global.tileSize = 2f;
		}

		return true
	}

}