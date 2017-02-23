package com.lyeeedar.Util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.lyeeedar.Global

class Controls
{
	enum class Keys
	{
		LEFT,
		RIGHT,
		UP,
		DOWN,
		CANCEL,
		ACCEPT,
		DEFENSE,
		ATTACKNORMAL,
		ATTACKSPECIAL,
	}

	private val keyMap = FastEnumMap<Keys, Int>(Keys::class.java)

	init
	{
		defaultArrow()
	}

	fun defaultArrow()
	{
		keyMap.put(Keys.LEFT, Input.Keys.LEFT)
		keyMap.put(Keys.RIGHT, Input.Keys.RIGHT)
		keyMap.put(Keys.UP, Input.Keys.UP)
		keyMap.put(Keys.DOWN, Input.Keys.DOWN)
		keyMap.put(Keys.CANCEL, Input.Keys.ESCAPE)
		keyMap.put(Keys.ACCEPT, Input.Keys.ENTER)
		keyMap.put(Keys.DEFENSE, Input.Keys.SPACE)
		keyMap.put(Keys.ATTACKNORMAL, Input.Keys.CONTROL_RIGHT)
		keyMap.put(Keys.ATTACKSPECIAL, Input.Keys.NUMPAD_0)
	}

	fun defaultWASD()
	{
		keyMap.put(Keys.LEFT, Input.Keys.A)
		keyMap.put(Keys.RIGHT, Input.Keys.D)
		keyMap.put(Keys.UP, Input.Keys.W)
		keyMap.put(Keys.DOWN, Input.Keys.S)
		keyMap.put(Keys.CANCEL, Input.Keys.ESCAPE)
		keyMap.put(Keys.ACCEPT, Input.Keys.ENTER)
		keyMap.put(Keys.DEFENSE, Input.Keys.SPACE)
		keyMap.put(Keys.ATTACKNORMAL, Input.Keys.Q)
		keyMap.put(Keys.ATTACKSPECIAL, Input.Keys.E)
	}

	fun defaultNumPad()
	{
		keyMap.put(Keys.LEFT, Input.Keys.NUMPAD_4)
		keyMap.put(Keys.RIGHT, Input.Keys.NUMPAD_6)
		keyMap.put(Keys.UP, Input.Keys.NUMPAD_8)
		keyMap.put(Keys.DOWN, Input.Keys.NUMPAD_2)
		keyMap.put(Keys.CANCEL, Input.Keys.PERIOD)
		keyMap.put(Keys.ACCEPT, Input.Keys.ENTER)
		keyMap.put(Keys.DEFENSE, Input.Keys.NUMPAD_5)
		keyMap.put(Keys.ATTACKNORMAL, Input.Keys.NUMPAD_7)
		keyMap.put(Keys.ATTACKSPECIAL, Input.Keys.NUMPAD_9)
	}

	fun setKeyMap(key: Keys, keycode: Int)
	{
		keyMap.put(key, keycode)
	}

	fun getKeyCode(key: Keys): Int
	{
		return keyMap.get(key)
	}

	fun isKey(key: Keys, keycode: Int): Boolean
	{
		return keyMap.get(key) === keycode
	}

	fun isKeyDown(key: Keys): Boolean
	{
		return Gdx.input.isKeyPressed(keyMap.get(key))
	}

	fun isDirectionDown(): Boolean = Keys.UP.isDown() || Keys.DOWN.isDown() || Keys.LEFT.isDown() || Keys.RIGHT.isDown()
}

fun Controls.Keys.isDown() = Global.controls.isKeyDown(this)
