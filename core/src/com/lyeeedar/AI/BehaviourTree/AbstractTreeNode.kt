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
	open fun setData(key:String, value:Any?) = data?.put(key, value)
	fun getData(key:String, fallback:Any? = null) = data?.get(key) ?: fallback

	//----------------------------------------------------------------------
	companion object
	{
		fun load(path: String, root: Boolean = false): AbstractTreeNode
		{
			val reader = XmlReader();
			val xml = reader.parse(Gdx.files.internal("AI/$path.xml"));

			val node = AbstractTreeNode.get(xml.name.toUpperCase())
			if (root) node.data = ObjectMap()

			node.parse(xml)

			return node
		}

		fun get(name: String): AbstractTreeNode
		{
			val uname = name.toUpperCase()
			val c = getClass(uname)
			val instance = ClassReflection.newInstance(c)

			return instance
		}

		fun getClass(name: String): Class<out AbstractTreeNode>
		{
			val type = when(name) {
			// Selectors
				"ANY" -> SelectorAny::class.java
				"PRIORITY" -> SelectorPriority::class.java
				"RANDOM" -> SelectorRandom::class.java
				"SEQUENCE" -> SelectorSequence::class.java
				"UNTIL" -> SelectorUntil::class.java

			// Decorators
				"DATASCOPE" -> DecoratorDataScope::class.java
				"IMPORT" -> DecoratorImport::class.java
				"INVERT" -> DecoratorInvert::class.java
				"REPEAT" -> DecoratorRepeat::class.java
				"SETSTATE" -> DecoratorSetState::class.java

			// Actions
				"ATTACK" -> ActionAttack::class.java
				"CLEARVALUE" -> ActionClearValue::class.java
				"CONVERTTO" -> ActionConvertTo::class.java
				"GETALLVISIBLE" -> ActionGetAllVisible::class.java
				"GETROOM", "GETNEIGHBOURROOM" -> ActionGetRoom::class.java
				"MOVETO", "MOVEAWAY" -> ActionMoveTo::class.java
				"PICK" -> ActionPick::class.java
				"PROCESSINPUT" -> ActionProcessInput::class.java
				"SETVALUE" -> ActionSetValue::class.java
				"TELEGRAPHEDATTACK" -> ActionTelegraphedAttack::class.java
				"WAIT" -> ActionWait::class.java

			// Conditionals
				"CHECKVALUE" -> ConditionalCheckValue::class.java

			// ARGH everything broke
				else -> throw RuntimeException("Invalid node type: $name")
			}

			return type
		}
	}

	//----------------------------------------------------------------------
	abstract fun evaluate(entity: Entity): ExecutionState
	abstract fun parse(xml: XmlReader.Element)
	abstract fun cancel()
}
