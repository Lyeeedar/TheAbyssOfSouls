package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.lyeeedar.Renderables.Sprite.DirectionalSprite

class DirectionalSpriteComponent(val directionalSprite: DirectionalSprite) : Component
{
	var currentAnim: String = "idle"
	var lastV: DirectionalSprite.VDir = DirectionalSprite.VDir.DOWN
	var lastH: DirectionalSprite.HDir = DirectionalSprite.HDir.RIGHT
}
