package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader

abstract class AbstractComponent : Component
{
	abstract fun parse(xml: XmlReader.Element, entity: Entity)
}