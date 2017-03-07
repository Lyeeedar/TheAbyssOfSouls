package com.lyeeedar.Systems

import com.badlogic.ashley.core.Family
import com.lyeeedar.Components.*
import com.lyeeedar.Util.Colour

class LightingSystem(): AbstractSystem(Family.all(PositionComponent::class.java, LightComponent::class.java).get())
{
	val temp: Colour = Colour()

	override fun doUpdate(deltaTime: Float)
	{
		for (x in 0.. level!!.width-1)
		{
			for (y in 0..level!!.height-1)
			{
				val tile = level!!.getTile(x, y) ?: continue
				tile.light.set(level!!.ambient)
				tile.isVisible = false
			}
		}

		// update visible/seen
		var shadow = Mappers.shadow.get(level!!.player)
		if (shadow == null)
		{
			shadow = ShadowCastComponent()
			level!!.player.add(shadow)
		}

		val visible = shadow.cache.currentShadowCast
		for (point in visible)
		{
			val tile = level!!.getTile(point) ?: continue
			tile.isVisible = true
			tile.isSeen = true
		}

		for (entity in entities)
		{
			val pos = Mappers.position.get(entity)

			if (pos.position.taxiDist(level!!.player.tile()!!) > 100)
			{
				continue
			}

			val light = Mappers.light.get(entity)
			val offset = entity.renderOffset()

			var x = pos.position.x.toFloat()
			var y = pos.position.y.toFloat()

			if (offset != null)
			{
				x += offset[0]
				y += offset[1]
			}

			val rawGrid = light.cache.rawOutput ?: continue

			for (ix in 0..rawGrid.size-1)
			{
				for (iy in 0..rawGrid[0].size-1)
				{
					val lightVal = rawGrid[ix][iy]
					if (lightVal <= 0) continue

					val gx = ix + light.cache.lastx - light.cache.lastrange
					val gy = iy + light.cache.lasty - light.cache.lastrange

					val tile = level!!.getTile(gx, gy) ?: continue
					val dst2 = tile.euclideanDist2(x.toFloat(), y.toFloat())

					var alpha = 1f - dst2 / ( light.dist * light.dist )
					if (alpha < 0.001f) alpha = 0f

					temp.set(light.col)
					temp *= alpha
					temp *= temp.a
					//temp *= lightVal.toFloat()
					temp.a = 1f

					tile.light += temp
				}
			}
		}
	}
}