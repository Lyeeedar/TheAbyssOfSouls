package com.lyeeedar.SceneTimeline

import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.evaluate
import com.lyeeedar.Components.task
import com.lyeeedar.Global
import com.lyeeedar.Pathfinding.ShadowCastCache
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Systems.task

abstract class AbstractBlockerAction : AbstractTimelineAction()
{
	var isBlocked = false
}

class BlockerAction() : AbstractBlockerAction()
{
	var blockCount = 0

	lateinit var countEqn: String

	override fun enter()
	{
		blockCount = countEqn.evaluate().toInt()

		Global.engine.task().onTurnEvent += fun(): Boolean {

			blockCount--
			if (blockCount == 0)
			{
				isExited = true
				exit()
				return true
			}
			else
			{
				return false
			}
		}

		isBlocked = true
	}

	override fun exit()
	{
		isBlocked = false
	}

	override fun copy(parent: SceneTimeline): AbstractTimelineAction
	{
		val action = BlockerAction()
		action.parent = parent
		action.startTime = startTime
		action.duration = duration
		action.countEqn = countEqn
		return action
	}

	override fun parse(xml: XmlReader.Element)
	{
		countEqn = xml.get("Count", "1")
	}
}

class ProximityAction() : AbstractBlockerAction()
{
	enum class Type
	{
		ALL,
		PLAYERONLY
	}

	lateinit var type: Type
	var range: Int = 1

	var shadowCast: ShadowCastCache? = null

	override fun enter()
	{
		if (shadowCast == null)
		{
			shadowCast = ShadowCastCache()
		}

		isBlocked = true

		Global.engine.task().onTurnEvent += fun(): Boolean {

			if (parent.sourceTile == null) return false

			val visible = shadowCast!!.getShadowCast(parent.sourceTile!!.level.grid, parent.sourceTile!!.x, parent.sourceTile!!.y, range, parent.parentEntity)

			if (type == Type.PLAYERONLY)
			{
				for (pos in visible)
				{
					val tile = parent.sourceTile!!.level.getTile(pos) ?: continue
					if (tile.contents[SpaceSlot.ENTITY] == tile.level.player)
					{
						isExited = true
						exit()
						return true
					}
				}
			}
			else
			{
				for (pos in visible)
				{
					val tile = parent.sourceTile!!.level.getTile(pos) ?: continue
					if (tile.contents[SpaceSlot.ENTITY]?.task() != null)
					{
						isExited = true
						exit()
						return true
					}
				}
			}

			return false
		}
	}

	override fun exit()
	{
		isBlocked = false
	}

	override fun copy(parent: SceneTimeline): AbstractTimelineAction
	{
		val action = ProximityAction()
		action.parent = parent
		action.startTime = startTime

		action.type = type
		action.range = range

		return action
	}

	override fun parse(xml: XmlReader.Element)
	{
		range = xml.getInt("Range", 1)
		type = Type.valueOf(xml.get("Type", "All").toUpperCase())
	}
}

class SignalAction() : AbstractBlockerAction()
{
	lateinit var key: String

	override fun enter()
	{
		isBlocked = true

		Global.engine.task().signalEvent += fun(key: String): Boolean {

			if (key == this.key)
			{
				isExited = true
				exit()
				return true
			}

			return false
		}
	}

	override fun exit()
	{
		isBlocked = false
	}

	override fun copy(parent: SceneTimeline): AbstractTimelineAction
	{
		val action = SignalAction()
		action.parent = parent
		action.startTime = startTime

		action.key = key

		return action
	}

	override fun parse(xml: XmlReader.Element)
	{
		key = xml.get("Key")
	}
}