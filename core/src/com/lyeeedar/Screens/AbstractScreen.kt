package com.lyeeedar.Screens

import com.badlogic.gdx.*
import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.controllers.ControllerListener
import com.badlogic.gdx.controllers.Controllers
import com.badlogic.gdx.controllers.PovDirection
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.ArrayMap
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.lyeeedar.Global
import com.lyeeedar.UI.ButtonKeyboardHelper
import com.lyeeedar.UI.DebugConsole
import com.lyeeedar.Util.ControllerWrapper
import com.lyeeedar.Util.Future
import com.lyeeedar.Util.KeyMapping
import com.lyeeedar.Util.KeySource
import ktx.actors.setKeyboardFocus

/**
 * Created by Philip on 20-Mar-16.
 */

abstract class AbstractScreen() : Screen, InputProcessor, ControllerListener
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
		val start = System.nanoTime()

        stage.act()
		Future.update(delta)

		for (controller in connectedControllers)
		{
			controller.value.update()
		}

        Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        doRender(delta)

        stage.draw()

		val end = System.nanoTime()

		val diff = (end - start) / 1000000000f
		frameDuration = (frameDuration + diff) / 2f

		fpsAccumulator += delta
		if (fpsAccumulator > 0.5f)
		{
			fpsAccumulator = 0f

			fps = (1f / frameDuration).toInt()
		}

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

    //endregion
	//############################################################################
	//region ControllerListener

	override fun connected (controller: Controller)
	{
		connectedControllers.put(controller, ControllerWrapper(controller))
	}

	override fun disconnected(controller: Controller)
	{
		connectedControllers.removeKey(controller)
	}

	override fun buttonDown(controller: Controller, buttonCode: Int): Boolean = false

	override fun buttonUp(controller: Controller, buttonCode: Int): Boolean = false

	override fun axisMoved(controller: Controller, axisCode: Int, value: Float): Boolean = false

	override fun povMoved(controller: Controller, povCode: Int, value: PovDirection): Boolean = false

	override fun xSliderMoved(controller: Controller, sliderCode: Int, value: Boolean): Boolean = false

	override fun ySliderMoved(controller: Controller, sliderCode: Int, value: Boolean): Boolean = false

	override fun accelerometerMoved(controller: Controller, accelerometerCode: Int, value: Vector3): Boolean = false

	//endregion
	//############################################################################
    //region InputProcessor

    // ----------------------------------------------------------------------
    override fun keyDown( keycode: Int ): Boolean
	{
		if (keycode == Input.Keys.GRAVE && !Global.release)
		{
			debugConsole.isVisible = !debugConsole.isVisible
			debugConsole.text.setKeyboardFocus(true)

			return true
		}
		else
		{
			Global.controls.keyPressed(KeySource.KEYBOARD, keycode)
		}

		Global.controls.onInput(KeyMapping(KeySource.KEYBOARD, keycode))

		val key = Global.controls.getKey(KeySource.KEYBOARD, keycode)
		if (key != null) keyboardHelper?.keyDown(key)

		return false
	}

    // ----------------------------------------------------------------------
    override fun keyUp( keycode: Int ): Boolean
	{
		Global.controls.keyReleased(KeySource.KEYBOARD, keycode)

		return false
	}

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
        stage = Stage(ScalingViewport(Scaling.fit, Global.resolution.x.toFloat(), Global.resolution.y.toFloat()), SpriteBatch())

        mainTable = Table()
        mainTable.setFillParent(true)
        stage.addActor(mainTable)

		if (!Global.release)
		{
			val debugConsoleTable = Table()
			debugConsoleTable.setFillParent(true)
			stage.addActor(debugConsoleTable)

			debugConsole = DebugConsole()
			debugConsoleTable.add(debugConsole).width(300f).expand().left().top().pad(5f)

			debugConsole.isVisible = false
		}

        inputMultiplexer = InputMultiplexer()

        val inputProcessorOne = this
        val inputProcessorTwo = stage

        inputMultiplexer.addProcessor(inputProcessorTwo)
        inputMultiplexer.addProcessor(inputProcessorOne)

		for (controller in Controllers.getControllers())
		{
			connectedControllers.put(controller, ControllerWrapper(controller))
		}

		Controllers.addListener(this)

		ControllerWrapper.pressedEvent += fun (source: KeySource, code: Int): Boolean
		{
			Global.controls.keyPressed(source, code)

			Global.controls.onInput(KeyMapping(source, code))

			val key = Global.controls.getKey(source, code)
			if (key != null) keyboardHelper?.keyDown(key)

			return false
		}

		ControllerWrapper.releasedEvent += fun (source: KeySource, code: Int): Boolean
		{
			Global.controls.keyReleased(source, code)

			return false
		}

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

	val connectedControllers = ArrayMap<Controller, ControllerWrapper>()

    var created: Boolean = false

    lateinit var stage: Stage
    lateinit var mainTable: Table

    lateinit var inputMultiplexer: InputMultiplexer

    var diff: Long = 0
    var start: Long = System.currentTimeMillis()
	var frametime: Float = -1f
	var frameDuration: Float = 0f
	var fps: Int = 0
	var fpsAccumulator: Float = 0f

	var keyboardHelper: ButtonKeyboardHelper? = null

	lateinit var debugConsole: DebugConsole

    //endregion
    //############################################################################
}