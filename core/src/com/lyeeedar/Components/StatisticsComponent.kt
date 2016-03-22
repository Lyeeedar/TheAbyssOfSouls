package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.lyeeedar.Enums
import com.lyeeedar.Util.FastEnumMap
import java.util.*

/**
 * Created by Philip on 21-Mar-16.
 */

class StatisticsComponent: Component
{
	constructor()

	val stats: FastEnumMap<Enums.Statistic, Float> = Enums.Statistic.getStatisticsBlock(10f)
	val variableMap: HashMap<String, Float> = HashMap<String, Float>()
	val factions: HashSet<String> = HashSet<String>()
	var hp: Float = 1f
	var stamina: Float = 1f
}