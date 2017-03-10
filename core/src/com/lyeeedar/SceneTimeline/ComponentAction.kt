package com.lyeeedar.SceneTimeline

import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.LightComponent
import com.lyeeedar.Components.PitComponent
import com.lyeeedar.Components.sceneTimeline

enum class ComponentType
{
	PIT,
	LIGHT
}

class AddComponentAction : AbstractTimelineAction()
{
	lateinit var type: ComponentType

	var lightData: XmlReader.Element? = null

	override fun enter()
	{
		val source = parent.parentEntity ?: parent.sourceTile!!.contents.firstOrNull{ it.sceneTimeline()?.sceneTimeline == parent } ?: return

		if (type == ComponentType.PIT)
		{
			source.add(PitComponent())
		}
		else if (type == ComponentType.LIGHT)
		{
			val light = LightComponent()
			light.parse(lightData!!, source)
			source.add(light)
		}
	}

	override fun exit()
	{

	}

	override fun copy(parent: SceneTimeline): AbstractTimelineAction
	{
		val action = AddComponentAction()
		action.parent = parent

		action.startTime = startTime

		action.type = type
		action.lightData = lightData

		return action
	}

	override fun parse(xml: XmlReader.Element)
	{
		type = ComponentType.valueOf(xml.get("Type", "Pit").toUpperCase())
		lightData = xml.getChildByName("LightData")
	}
}

class RemoveComponentAction : AbstractTimelineAction()
{
	lateinit var type: ComponentType

	override fun enter()
	{
		val source = parent.parentEntity ?: parent.sourceTile!!.contents.firstOrNull{ it.sceneTimeline()?.sceneTimeline == parent } ?: return

		if (type == ComponentType.PIT)
		{
			source.remove(PitComponent::class.java)
		}
		else if (type == ComponentType.LIGHT)
		{
			source.remove(LightComponent::class.java)
		}
	}

	override fun exit()
	{

	}

	override fun copy(parent: SceneTimeline): AbstractTimelineAction
	{
		val action = RemoveComponentAction()
		action.parent = parent

		action.startTime = startTime

		action.type = type

		return action
	}

	override fun parse(xml: XmlReader.Element)
	{
		type = ComponentType.valueOf(xml.get("Type", "Pit").toUpperCase())
	}
}
