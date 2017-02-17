package com.lyeeedar.Screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.HDRColourSpriteBatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.lyeeedar.Global
import com.lyeeedar.Util.Future
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 20-Mar-16.
 */

abstract class AbstractScreen() : Screen, InputProcessor
{
    //############################################################################
    //region Abstract Methods

    abstract fun create()
    abstract fun doRender(delta: Float)

    //endregion
    //############################################################################
    //region Screen

	// ----------------------------------------------------------------------
	fun swapTo()
	{
		Global.game.switchScreen(this)
	}

    // ----------------------------------------------------------------------
    override fun show()
	{
        if ( !created )
		{
            baseCreate()
            created = true
        }

        Gdx.input.inputProcessor = inputMultiplexer
    }

    // ----------------------------------------------------------------------
    override fun resize(width: Int, height: Int)
	{
        stage.viewport.update(width, height, true)
    }

    // ----------------------------------------------------------------------
    override fun render(delta: Float)
	{
        stage.act()
		Future.update(delta)

        Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        doRender(delta)

        stage.draw()

		Point.freeTemp()

        // limit fps
        sleep()
    }

    // ----------------------------------------------------------------------
    override fun pause() {}

    // ----------------------------------------------------------------------
    override fun resume() {}

    // ----------------------------------------------------------------------
    override fun hide() {}

    // ----------------------------------------------------------------------
    override fun dispose() {}

    //enregion
    //############################################################################
    //region InputProcessor

    // ----------------------------------------------------------------------
    override fun keyDown( keycode: Int ) = false

    // ----------------------------------------------------------------------
    override fun keyUp( keycode: Int ) = false

    // ----------------------------------------------------------------------
    override fun keyTyped( character: Char ) = false

    // ----------------------------------------------------------------------
    override fun touchDown( screenX: Int, screenY: Int, pointer: Int, button: Int ) = false

    // ----------------------------------------------------------------------
    override fun touchUp( screenX: Int, screenY: Int, pointer: Int, button: Int ) = false

    // ----------------------------------------------------------------------
    override fun touchDragged( screenX: Int, screenY: Int, pointer: Int ) = false

    // ----------------------------------------------------------------------
    override fun mouseMoved( screenX: Int, screenY: Int ) = false

    // ----------------------------------------------------------------------
    override fun scrolled(amount: Int) = false

    //endregion
    //############################################################################
    //region Methods

    // ----------------------------------------------------------------------
    fun baseCreate()
	{
        stage = Stage(ScalingViewport(Scaling.fit, Global.resolution.x.toFloat(), Global.resolution.y.toFloat()), HDRColourSpriteBatch())

        mainTable = Table()
        mainTable.setFillParent(true)
        stage.addActor(mainTable)

        inputMultiplexer = InputMultiplexer()

        val inputProcessorOne = this
        val inputProcessorTwo = stage

        inputMultiplexer.addProcessor(inputProcessorTwo)
        inputMultiplexer.addProcessor(inputProcessorOne)

        create()
    }

    // ----------------------------------------------------------------------
    fun sleep() {
		diff = System.currentTimeMillis() - start
        if ( Global.fps > 0 ) {

            val targetDelay = 1000 / Global.fps
            if ( diff < targetDelay ) {
                try {
                    Thread.sleep(targetDelay - diff)
                } catch (e: InterruptedException) {
                }
            }
        }
		start = System.currentTimeMillis()

		if (frametime == -1f)
		{
			frametime = 1f / diff
		}
		else
		{
			frametime = (frametime + 1f/diff) / 2f
		}
    }

    //endregion
    //############################################################################
    //region Data

    var created: Boolean = false

    lateinit var stage: Stage
    lateinit var mainTable: Table

    lateinit var inputMultiplexer: InputMultiplexer

    var diff: Long = 0
    var start: Long = System.currentTimeMillis()
	var frametime: Float = -1f

	var debugAccumulator: Float = 0f

    //endregion
    //############################################################################
}