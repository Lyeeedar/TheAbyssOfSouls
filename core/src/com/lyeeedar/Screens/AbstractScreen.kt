package com.lyeeedar.Screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.lyeeedar.GlobalData
import com.lyeeedar.UI.ButtonKeyboardHelper
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
    override fun show() {
        if ( !created ) {
            baseCreate();
            created = true;
        }

        Gdx.input.inputProcessor = inputMultiplexer;

        camera = OrthographicCamera(GlobalData.Global.resolution[0], GlobalData.Global.resolution[1]);
        camera.translate(GlobalData.Global.resolution[0] / 2, GlobalData.Global.resolution[1] / 2);
        camera.setToOrtho(false, GlobalData.Global.resolution[0], GlobalData.Global.resolution[1]);
        camera.update();

        batch.projectionMatrix = camera.combined;
        stage.viewport.camera = camera;
        stage.viewport.worldWidth = GlobalData.Global.resolution[0];
        stage.viewport.worldHeight = GlobalData.Global.resolution[1];
        stage.viewport.screenWidth = GlobalData.Global.screenSize[0];
        stage.viewport.screenHeight = GlobalData.Global.screenSize[1];
    }

    // ----------------------------------------------------------------------
    override fun resize(width: Int, height: Int) {
        GlobalData.Global.screenSize[0] = width;
        GlobalData.Global.screenSize[1] = height;

        var w = 360.0f;
        var h = 480.0f;

        if ( width < height ) {
            h = w * height.toFloat() / width.toFloat();
        } else {
            w = h * width.toFloat() / height.toFloat();
        }

        GlobalData.Global.resolution[0] = w;
        GlobalData.Global.resolution[1] = h;

        camera = OrthographicCamera(GlobalData.Global.resolution[0], GlobalData.Global.resolution[1]);
        camera.translate(GlobalData.Global.resolution[0] / 2, GlobalData.Global.resolution[1] / 2);
        camera.setToOrtho(false, GlobalData.Global.resolution[0], GlobalData.Global.resolution[1]);
        camera.update();

        batch.projectionMatrix = camera.combined;
        stage.viewport.camera = camera;
        stage.viewport.worldWidth = GlobalData.Global.resolution[0];
        stage.viewport.worldHeight = GlobalData.Global.resolution[1];
        stage.viewport.screenWidth = GlobalData.Global.screenSize[0];
        stage.viewport.screenHeight = GlobalData.Global.screenSize[1];
    }

    // ----------------------------------------------------------------------
    override fun render(delta: Float)
	{
        stage.act();

        Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        doRender(delta)

        stage.draw();

		if (!GlobalData.Global.release)
		{
			debugAccumulator += delta
			if (debugAccumulator >= 0f)
			{
				debugAccumulator = -0.5f

				System.out.println("FPS: " + (1f / frametime))
			}
		}

        // limit fps
        sleep();
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
    override fun keyDown( keycode: Int ) = keyboardHelper.keyDown(keycode)

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
    override fun mouseMoved( screenX: Int, screenY: Int ): Boolean
    {
        keyboardHelper.clear();
        return false;
    }

    // ----------------------------------------------------------------------
    override fun scrolled(amount: Int) = false

    //endregion
    //############################################################################
    //region Methods

    // ----------------------------------------------------------------------
    fun baseCreate() {
        stage = Stage(ScreenViewport());
        batch = SpriteBatch();

        mainTable = Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        inputMultiplexer = InputMultiplexer();

        var inputProcessorOne = this;
        var inputProcessorTwo = stage;

        inputMultiplexer.addProcessor(inputProcessorTwo);
        inputMultiplexer.addProcessor(inputProcessorOne);

        create()
    }

    // ----------------------------------------------------------------------
    fun sleep() {
		diff = System.currentTimeMillis() - start;
        if ( GlobalData.Global.fps > 0 ) {

            var targetDelay = 1000 / GlobalData.Global.fps
            if ( diff < targetDelay ) {
                try {
                    Thread.sleep(targetDelay - diff);
                } catch (e: InterruptedException) {
                }
            }
        }
		start = System.currentTimeMillis();

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

    lateinit var camera: OrthographicCamera
    lateinit var batch: SpriteBatch
    lateinit var stage: Stage
    lateinit var mainTable: Table

    lateinit var inputMultiplexer: InputMultiplexer
    lateinit var keyboardHelper: ButtonKeyboardHelper

    var diff: Long = 0
    var start: Long = System.currentTimeMillis()
	var frametime: Float = -1f

	var debugAccumulator: Float = 0f

    //endregion
    //############################################################################
}