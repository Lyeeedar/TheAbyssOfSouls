package com.lyeeedar.Util

import com.badlogic.gdx.Input
import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.controllers.PovDirection
import com.badlogic.gdx.controllers.mappings.Ouya.AXIS_LEFT_X
import com.badlogic.gdx.controllers.mappings.Ouya.AXIS_LEFT_Y
import com.badlogic.gdx.utils.ObjectSet
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
		WAIT,
		DEFENSE,
		ATTACKNORMAL,
		ATTACKSPECIAL,
	}

	private val keyMap = FastEnumMap<Keys, ObjectSet<KeyMapping>>(Keys::class.java)

	private val keyDownMap = FastEnumMap<Keys, Boolean>(Keys::class.java)
	private val keyPressMap = FastEnumMap<Keys, Boolean>(Keys::class.java)

	val onInput = Event1Arg<KeyMapping>()

	init
	{
		for (key in Keys.values())
		{
			keyPressMap[key] = false
			keyDownMap[key] = false
			keyMap[key] = ObjectSet()
		}

		defaultArrow()
		defaultWASD()
		defaultNumPad()
		defaultXboxController()
	}

	fun addKeyMapping(key: Keys, source: KeySource, code: Int)
	{
		keyMap[key].add(KeyMapping(source, code))
	}

	fun defaultXboxController()
	{
		addKeyMapping(Keys.LEFT, KeySource.CONTROLLERDPAD, Xbox360Controller.BUTTON_DPAD_WEST)
		addKeyMapping(Keys.RIGHT, KeySource.CONTROLLERDPAD, Xbox360Controller.BUTTON_DPAD_EAST)
		addKeyMapping(Keys.UP, KeySource.CONTROLLERDPAD, Xbox360Controller.BUTTON_DPAD_NORTH)
		addKeyMapping(Keys.DOWN, KeySource.CONTROLLERDPAD, Xbox360Controller.BUTTON_DPAD_SOUTH)

		addKeyMapping(Keys.LEFT, KeySource.CONTROLLERSTICK, Xbox360Controller.AXIS_LEFT_X_N)
		addKeyMapping(Keys.RIGHT, KeySource.CONTROLLERSTICK, Xbox360Controller.AXIS_LEFT_X_P)
		addKeyMapping(Keys.UP, KeySource.CONTROLLERSTICK, Xbox360Controller.AXIS_LEFT_Y_P)
		addKeyMapping(Keys.DOWN, KeySource.CONTROLLERSTICK, Xbox360Controller.AXIS_LEFT_Y_N)

		addKeyMapping(Keys.CANCEL, KeySource.CONTROLLERBUTTON, Xbox360Controller.BUTTON_B)
		addKeyMapping(Keys.ACCEPT, KeySource.CONTROLLERBUTTON, Xbox360Controller.BUTTON_A)
		addKeyMapping(Keys.WAIT, KeySource.CONTROLLERBUTTON, Xbox360Controller.BUTTON_A)
		addKeyMapping(Keys.DEFENSE, KeySource.CONTROLLERBUTTON, Xbox360Controller.BUTTON_B)
		addKeyMapping(Keys.ATTACKNORMAL, KeySource.CONTROLLERBUTTON, Xbox360Controller.BUTTON_X)
		addKeyMapping(Keys.ATTACKSPECIAL, KeySource.CONTROLLERBUTTON, Xbox360Controller.BUTTON_Y)
	}

	fun defaultArrow()
	{
		addKeyMapping(Keys.LEFT, KeySource.KEYBOARD, Input.Keys.LEFT)
		addKeyMapping(Keys.RIGHT, KeySource.KEYBOARD, Input.Keys.RIGHT)
		addKeyMapping(Keys.UP, KeySource.KEYBOARD, Input.Keys.UP)
		addKeyMapping(Keys.DOWN, KeySource.KEYBOARD, Input.Keys.DOWN)
		addKeyMapping(Keys.CANCEL, KeySource.KEYBOARD, Input.Keys.ESCAPE)
		addKeyMapping(Keys.ACCEPT, KeySource.KEYBOARD, Input.Keys.ENTER)
		addKeyMapping(Keys.WAIT, KeySource.KEYBOARD, Input.Keys.SPACE)
		addKeyMapping(Keys.DEFENSE, KeySource.KEYBOARD, Input.Keys.SHIFT_RIGHT)
		addKeyMapping(Keys.ATTACKNORMAL, KeySource.KEYBOARD, Input.Keys.CONTROL_RIGHT)
		addKeyMapping(Keys.ATTACKSPECIAL, KeySource.KEYBOARD, Input.Keys.NUMPAD_0)
	}

	fun defaultWASD()
	{
		addKeyMapping(Keys.LEFT, KeySource.KEYBOARD, Input.Keys.A)
		addKeyMapping(Keys.RIGHT, KeySource.KEYBOARD, Input.Keys.D)
		addKeyMapping(Keys.UP, KeySource.KEYBOARD, Input.Keys.W)
		addKeyMapping(Keys.DOWN, KeySource.KEYBOARD, Input.Keys.S)
		addKeyMapping(Keys.CANCEL, KeySource.KEYBOARD, Input.Keys.ESCAPE)
		addKeyMapping(Keys.ACCEPT, KeySource.KEYBOARD, Input.Keys.ENTER)
		addKeyMapping(Keys.WAIT, KeySource.KEYBOARD, Input.Keys.SPACE)
		addKeyMapping(Keys.DEFENSE, KeySource.KEYBOARD, Input.Keys.SHIFT_LEFT)
		addKeyMapping(Keys.ATTACKNORMAL, KeySource.KEYBOARD, Input.Keys.Q)
		addKeyMapping(Keys.ATTACKSPECIAL, KeySource.KEYBOARD, Input.Keys.E)
	}

	fun defaultNumPad()
	{
		addKeyMapping(Keys.LEFT, KeySource.KEYBOARD, Input.Keys.NUMPAD_4)
		addKeyMapping(Keys.RIGHT, KeySource.KEYBOARD, Input.Keys.NUMPAD_6)
		addKeyMapping(Keys.UP, KeySource.KEYBOARD, Input.Keys.NUMPAD_8)
		addKeyMapping(Keys.DOWN, KeySource.KEYBOARD, Input.Keys.NUMPAD_2)
		addKeyMapping(Keys.CANCEL, KeySource.KEYBOARD, Input.Keys.PERIOD)
		addKeyMapping(Keys.ACCEPT, KeySource.KEYBOARD, Input.Keys.ENTER)
		addKeyMapping(Keys.WAIT, KeySource.KEYBOARD, Input.Keys.NUMPAD_5)
		addKeyMapping(Keys.DEFENSE, KeySource.KEYBOARD, Input.Keys.NUMPAD_0)
		addKeyMapping(Keys.ATTACKNORMAL, KeySource.KEYBOARD, Input.Keys.NUMPAD_7)
		addKeyMapping(Keys.ATTACKSPECIAL, KeySource.KEYBOARD, Input.Keys.NUMPAD_9)
	}

	fun setKeyMap(key: Keys, source: KeySource, keycode: Int)
	{
		addKeyMapping(key, source, keycode)
	}

	fun getKeyCodes(key: Keys): ObjectSet<KeyMapping>
	{
		return keyMap.get(key)
	}

	fun getKey(source: KeySource, keycode: Int): Keys?
	{
		for (key in Keys.values())
		{
			if (keyMap[key].any { it.source == source && it.code == keycode }) return key
		}

		return null
	}

	fun isKey(key: Keys, source: KeySource, keycode: Int): Boolean
	{
		return keyMap.get(key).any { it.source == source && it.code == keycode }
	}

	fun isKeyDown(key: Keys): Boolean
	{
		return keyDownMap[key]
	}

	fun isKeyDownAndNotConsumed(key: Keys): Boolean
	{
		return keyPressMap[key]
	}

	fun keyPressed(source: KeySource, code: Int)
	{
		for (k in Keys.values())
		{
			if (keyMap[k].any { it.source == source && it.code == code })
			{
				keyPressMap[k] = true
				keyDownMap[k] = true
			}
		}
	}

	fun keyReleased(source: KeySource, code: Int)
	{
		for (k in Keys.values())
		{
			if (keyMap[k].any { it.source == source && it.code == code })
			{
				keyPressMap[k] = false
				keyDownMap[k] = false
			}
		}
	}

	fun consumeKeyPress(key: Keys): Boolean
	{
		val pressed = keyPressMap[key] ?: false
		keyPressMap[key] = false

		return pressed
	}

	fun isDirectionDownAndNotConsumed(): Boolean = keyPressMap[Keys.UP] || keyPressMap[Keys.DOWN] || keyPressMap[Keys.LEFT] || keyPressMap[Keys.RIGHT]

	fun isDirectionDown(): Boolean = Keys.UP.isDown() || Keys.DOWN.isDown() || Keys.LEFT.isDown() || Keys.RIGHT.isDown()
}

fun Controls.Keys.isDown() = Global.controls.isKeyDown(this)
fun Controls.Keys.isDownAndNotConsumed() = Global.controls.isKeyDownAndNotConsumed(this)
fun Controls.Keys.consumePress() = Global.controls.consumeKeyPress(this)

enum class KeySource
{
	KEYBOARD,
	CONTROLLERBUTTON,
	CONTROLLERSTICK,
	CONTROLLERDPAD
}
data class KeyMapping(val source: KeySource, val code: Int)

object Xbox360Controller
{
	val N_BUTTONS = 10
	val N_TRIGGERS = 2
	val N_DPAD = 8
	val N_AXIS = 4

	// Xbox Controller Wrapper mappings
	val BUTTON_DPAD_NORTH = 0
	val BUTTON_DPAD_NORTHEAST = 1
	val BUTTON_DPAD_EAST = 2
	val BUTTON_DPAD_SOUTHEAST = 3
	val BUTTON_DPAD_SOUTH = 4
	val BUTTON_DPAD_SOUTHWEST = 5
	val BUTTON_DPAD_WEST = 6
	val BUTTON_DPAD_NORTHWEST = 7

	// Xbox Controller mappings
	val BUTTON_A = 0
	val BUTTON_B = 1
	val BUTTON_X = 2
	val BUTTON_Y = 3
	val BUTTON_BACK = 6
	val BUTTON_START = 7
	val BUTTON_LB = 4
	val BUTTON_L3 = 8
	val BUTTON_RB = 5
	val BUTTON_R3 = 9
	val AXIS_LEFT_X = 1
	val AXIS_LEFT_Y = 0
	val AXIS_LEFT_TRIGGER = 4
	val AXIS_RIGHT_X = 3
	val AXIS_RIGHT_Y = 2
	val AXIS_RIGHT_TRIGGER = 4

	// Axis
	val AXIS_LEFT_X_N = 0
	val AXIS_LEFT_X_P = 1
	val AXIS_LEFT_Y_N = 2
	val AXIS_LEFT_Y_P = 3
}

class ControllerWrapper(private val controller: Controller)
{
	private val buttonPressed: BooleanArray
	private val buttonPressedPrevious: BooleanArray

	private val triggerPressed: BooleanArray
	private val triggerPressedPrevious: BooleanArray

	private val dpadPressed: BooleanArray
	private val dpadPressedPrevious: BooleanArray

	private val axisPressed: BooleanArray
	private val axisPressedPrevious: BooleanArray

	val buttonPressedEvent = Event1Arg<Int>()
	val buttonReleasedEvent = Event1Arg<Int>()
	val dpadPressedEvent = Event1Arg<Int>()
	val dpadReleasedEvent = Event1Arg<Int>()
	val axisPressedEvent = Event1Arg<Int>()
	val axisReleasedEvent = Event1Arg<Int>()

	init
	{
		buttonPressed = BooleanArray(Xbox360Controller.N_BUTTONS)
		buttonPressedPrevious = BooleanArray(Xbox360Controller.N_BUTTONS)

		triggerPressed = BooleanArray(Xbox360Controller.N_TRIGGERS)
		triggerPressedPrevious = BooleanArray(Xbox360Controller.N_TRIGGERS)

		dpadPressed = BooleanArray(Xbox360Controller.N_DPAD)
		dpadPressedPrevious = BooleanArray(Xbox360Controller.N_DPAD)

		axisPressed = kotlin.BooleanArray(Xbox360Controller.N_AXIS)
		axisPressedPrevious = kotlin.BooleanArray(Xbox360Controller.N_AXIS)

		register(this)
	}

	fun update()
	{
		// buttons
		for (i in 0..Xbox360Controller.N_BUTTONS - 1)
		{
			buttonPressedPrevious[i] = buttonPressed[i]
			buttonPressed[i] = isButtonPressed(i)
		}

		// triggers
		triggerPressedPrevious[LEFT_TRIGGER] = triggerPressed[LEFT_TRIGGER]
		triggerPressedPrevious[RIGHT_TRIGGER] = triggerPressed[RIGHT_TRIGGER]
		triggerPressed[LEFT_TRIGGER] = isLeftTriggerPressed
		triggerPressed[RIGHT_TRIGGER] = isRightTriggerPressed

		// dpad
		var code = -1
		val currentDirection = controller.getPov(0)
		if (currentDirection == PovDirection.north) code = Xbox360Controller.BUTTON_DPAD_NORTH
		if (currentDirection == PovDirection.south) code = Xbox360Controller.BUTTON_DPAD_SOUTH
		if (currentDirection == PovDirection.west) code = Xbox360Controller.BUTTON_DPAD_WEST
		if (currentDirection == PovDirection.east) code = Xbox360Controller.BUTTON_DPAD_EAST
		if (currentDirection == PovDirection.northWest) code = Xbox360Controller.BUTTON_DPAD_NORTHWEST
		if (currentDirection == PovDirection.northEast) code = Xbox360Controller.BUTTON_DPAD_NORTHEAST
		if (currentDirection == PovDirection.southWest) code = Xbox360Controller.BUTTON_DPAD_SOUTHWEST
		if (currentDirection == PovDirection.southEast) code = Xbox360Controller.BUTTON_DPAD_SOUTHEAST

		for (i in 0..Xbox360Controller.N_DPAD - 1)
		{
			val pressed = i == code
			dpadPressedPrevious[i] = dpadPressed[i]
			dpadPressed[i] = pressed
		}

		// axis
		for (i in 0..Xbox360Controller.N_AXIS - 1)
		{
			axisPressedPrevious[i] = axisPressed[i]
			axisPressed[i] = false
		}

		val yAxis = controller.getAxis(AXIS_LEFT_X)
		val xAxis = controller.getAxis(AXIS_LEFT_Y)

		if(Math.abs(xAxis) > AXIS_CUTOFF)
		{
			if (xAxis < 0) axisPressed[Xbox360Controller.AXIS_LEFT_X_N] = true
			if (xAxis > 0) axisPressed[Xbox360Controller.AXIS_LEFT_X_P] = true
		}
		if(Math.abs(yAxis) > AXIS_CUTOFF)
		{
			if (yAxis > 0) axisPressed[Xbox360Controller.AXIS_LEFT_Y_N] = true
			if (yAxis < 0) axisPressed[Xbox360Controller.AXIS_LEFT_Y_P] = true
		}

		// fire events
		for (i in 0..Xbox360Controller.N_BUTTONS - 1)
		{
			if (!buttonPressedPrevious[i] && buttonPressed[i])
			{
				buttonPressedEvent(i)
			}
			else if (buttonPressedPrevious[i] && !buttonPressed[i])
			{
				buttonReleasedEvent(i)
			}
		}

		for (i in 0..Xbox360Controller.N_DPAD - 1)
		{
			if (!dpadPressedPrevious[i] && dpadPressed[i])
			{
				dpadPressedEvent(i)
			}
			else if (dpadPressedPrevious[i] && !dpadPressed[i])
			{
				dpadReleasedEvent(i)
			}
		}

		for (i in 0..Xbox360Controller.N_AXIS - 1)
		{
			if (!axisPressedPrevious[i] && axisPressed[i])
			{
				axisPressedEvent(i)
			}
			else if (axisPressedPrevious[i] && !axisPressed[i])
			{
				axisReleasedEvent(i)
			}
		}
	}

	fun isButtonPressed(buttonCode: Int): Boolean
	{
		return controller.getButton(buttonCode)
	}

	fun isButtonJustPressed(buttonCode: Int): Boolean
	{
		if (buttonCode < 0 || buttonCode >= Xbox360Controller.N_BUTTONS)
			return false
		else
			return buttonPressed[buttonCode] && !buttonPressedPrevious[buttonCode]
	}

	fun getAxis(axisCode: Int): Float
	{
		return controller.getAxis(axisCode)
	}

	val isRightTriggerPressed: Boolean
		get() = rightTriggerValue > TRIGGER_CUTOFF

	val isLeftTriggerPressed: Boolean
		get() = leftTriggerValue > TRIGGER_CUTOFF

	val isRightTriggerJustPressed: Boolean
		get() = triggerPressed[RIGHT_TRIGGER] && !triggerPressedPrevious[RIGHT_TRIGGER]

	val isLeftTriggerJustPressed: Boolean
		get() = triggerPressed[LEFT_TRIGGER] && !triggerPressedPrevious[LEFT_TRIGGER]

	val leftTriggerValue: Float
		get() = controller.getAxis(Xbox360Controller.AXIS_LEFT_TRIGGER)

	val rightTriggerValue: Float
		get() = controller.getAxis(Xbox360Controller.AXIS_RIGHT_TRIGGER)

	fun isDirectionalPadPressed(direction: Int): Boolean
	{
		return dpadPressed[direction]
	}

	fun isDirectionalPadJustPressed(direction: Int): Boolean
	{
		return dpadPressed[direction] && !dpadPressedPrevious[direction]
	}

	companion object
	{
		val TRIGGER_CUTOFF = 0.2f
		val AXIS_CUTOFF = 0.15f
		private val LEFT_TRIGGER = 0
		private val RIGHT_TRIGGER = 1

		val buttonPressedEvent = Event1Arg<Int>()
		val buttonReleasedEvent = Event1Arg<Int>()
		val dpadPressedEvent = Event1Arg<Int>()
		val dpadReleasedEvent = Event1Arg<Int>()
		val axisPressedEvent = Event1Arg<Int>()
		val axisReleasedEvent = Event1Arg<Int>()

		val pressedEvent = Event2Arg<KeySource, Int>()
		val releasedEvent = Event2Arg<KeySource, Int>()

		fun register(controllerWrapper: ControllerWrapper)
		{
			controllerWrapper.buttonPressedEvent += fun (code: Int): Boolean { buttonPressedEvent(code); pressedEvent(KeySource.CONTROLLERBUTTON, code); return false }
			controllerWrapper.buttonReleasedEvent += fun (code: Int): Boolean { buttonReleasedEvent(code); releasedEvent(KeySource.CONTROLLERBUTTON, code); return false }
			controllerWrapper.dpadPressedEvent += fun (code: Int): Boolean { dpadPressedEvent(code); pressedEvent(KeySource.CONTROLLERDPAD, code); return false }
			controllerWrapper.dpadReleasedEvent += fun (code: Int): Boolean { dpadReleasedEvent(code); releasedEvent(KeySource.CONTROLLERDPAD, code); return false }
			controllerWrapper.axisPressedEvent += fun (code: Int): Boolean { axisPressedEvent(code); pressedEvent(KeySource.CONTROLLERSTICK, code); return false }
			controllerWrapper.axisReleasedEvent += fun (code: Int): Boolean { axisReleasedEvent(code); releasedEvent(KeySource.CONTROLLERSTICK, code); return false }
		}
	}

}