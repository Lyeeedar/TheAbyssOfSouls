package com.lyeeedar.AI.BehaviourTree

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.JsonReader
import com.badlogic.gdx.utils.JsonValue
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.badlogic.gdx.utils.reflect.ClassReflection
import com.lyeeedar.AI.BehaviourTree.Actions.*
import com.lyeeedar.AI.BehaviourTree.Conditionals.ConditionalCheckValue
import com.lyeeedar.AI.BehaviourTree.Decorators.*
import com.lyeeedar.AI.BehaviourTree.Selectors.*
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 21-Mar-16.
 */

abstract class AbstractTreeNode()
{
	//----------------------------------------------------------------------
	lateinit var parent: AbstractNodeContainer
	var state: ExecutionState = ExecutionState.NONE
	var data: ObjectMap<String, Any>? = null

	//----------------------------------------------------------------------
	open fun setData(key:String, value:Any?)
	{
		val oldVal = data?.get(key)
		if (oldVal != value && oldVal is Point && oldVal.javaClass == Point::class.java)
		{
			oldVal.free()
		}

		data?.put(key, value)
	}

	fun getData(key:String, fallback:Any? = null) = data?.get(key) ?: fallback

	//----------------------------------------------------------------------
	companion object
	{
		fun load(path: String, root: Boolean = false): AbstractTreeNode
		{
			val reader = XmlReader()
			val xml = reader.parse(Gdx.files.internal("AI/$path.xml"))

			val rootEl = xml.getChildByName("Root")

			val node = AbstractTreeNode.get(rootEl.getAttribute("meta:RefKey").toUpperCase())
			if (root) node.data = ObjectMap()

			node.parse(xml)

			return node
		}

		fun get(name: String): AbstractTreeNode
		{
			val node = when(name.toUpperCase()) {

			// Selectors
				"ANY" -> SelectorAny()
				"PRIORITY" -> SelectorPriority()
				"RANDOM" -> SelectorRandom()
				"SEQUENCE" -> SelectorSequence()
				"UNTIL" -> SelectorUntil()

			// Decorators
				"DATASCOPE" -> DecoratorDataScope()
				"IMPORT" -> DecoratorImport()
				"INVERT" -> DecoratorInvert()
				"REPEAT" -> DecoratorRepeat()
				"SETSTATE" -> DecoratorSetState()

			// Actions
				"COMBO" -> ActionCombo()
				"CLEARVALUE" -> ActionClearValue()
				"CONVERTTO" -> ActionConvertTo()
				"GETALLVISIBLE" -> ActionGetAllVisible()
				"MOVETO" -> ActionMoveTo()
				"PICK" -> ActionPick()
				"PROCESSINPUT" -> ActionProcessInput()
				"SETVALUE" -> ActionSetValue()
				"WAIT" -> ActionWait()

			// Conditionals
				"CONDITIONAL" -> ConditionalCheckValue()

			// ARGH everything broke
				else -> throw RuntimeException("Invalid node type: $name")
			}

			return node
		}

	}

	//----------------------------------------------------------------------
	abstract fun evaluate(entity: Entity): ExecutionState
	abstract fun parse(xml: XmlReader.Element)
	abstract fun cancel(entity: Entity)
}
