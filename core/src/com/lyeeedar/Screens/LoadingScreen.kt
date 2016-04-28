package com.lyeeedar.Screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.HDRColourSpriteBatch
import com.badlogic.gdx.utils.XmlReader

import com.lyeeedar.DungeonGeneration.LevelGenerators.AbstractLevelGenerator
import com.lyeeedar.GlobalData
import com.lyeeedar.MainGame
import com.lyeeedar.Sprite.Sprite
import java.util.concurrent.Callable
import java.util.concurrent.Future

/**
 * Created by Philip on 15-Apr-16.
 */

class LoadingScreen: AbstractScreen
{
	constructor()
		: super()
	{
		Instance = this
	}

	companion object
	{
		lateinit var Instance: LoadingScreen
	}

	var generator: AbstractLevelGenerator? = null
	var future: Future<Unit?>? = null
	lateinit var sprite: Sprite
	lateinit var batch: HDRColourSpriteBatch

	fun load(level: String)
	{
		val handle = Gdx.files.internal("Levels/$level/$level.xml")
		val reader = XmlReader()
		val xml = reader.parse(handle)

		generator = AbstractLevelGenerator.load(xml)

		val callable = Callable { generator?.generate() }

		future = GlobalData.Global.threadpool.submit ( callable )
	}

	override fun create()
	{
		sprite = AssetManager.loadSprite("GUI/loading")
		batch = HDRColourSpriteBatch()
	}

	override fun doRender(delta: Float)
	{
		if (future?.isDone ?: false)
		{
			try
			{
				future?.get()
			}
			catch (e: Exception)
			{
				throw e
			}

			GlobalData.Global.currentLevel = generator!!.create(GlobalData.Global.engine)
			GlobalData.Global.game.switchScreen(MainGame.ScreenEnum.GAME)
		}

		sprite.rotation += delta * 200f

		batch.begin()
		sprite.render(batch, 50f, 50f, 200f, 200f)
		batch.end()
	}
}