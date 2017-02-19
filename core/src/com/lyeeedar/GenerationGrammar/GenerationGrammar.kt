package com.lyeeedar.GenerationGrammar

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.evaluate
import com.lyeeedar.Components.*
import com.lyeeedar.GenerationGrammar.Rules.AbstractGrammarRule
import com.lyeeedar.GenerationGrammar.Rules.DeferredRule
import com.lyeeedar.Level.Level
import com.lyeeedar.Level.Tile
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.Array2D
import com.lyeeedar.Util.children
import com.lyeeedar.Util.getXml
import com.lyeeedar.Util.round
import ktx.collections.set
import java.util.*

class GenerationGrammar
{
	val ruleTable = ObjectMap<String, AbstractGrammarRule>()
	lateinit var width: String
	lateinit var height: String
	lateinit var rootRule: String

	fun generate(seed: Long, engine: Engine): Level
	{
		val ran = Random(seed)
		val width = width.evaluate(ran = ran).round()
		val height = height.evaluate(ran = ran).round()

		val symbolGrid = Array2D<GrammarSymbol>(width, height) { x, y -> GrammarSymbol(' ') }
		val area = Area()
		area.width = width
		area.height = height
		area.grid = symbolGrid

		val rule = ruleTable[rootRule]

		var deferred = Array<DeferredRule>()
		DeferredRule.reset()

		rule.execute(area, ruleTable, ObjectMap(), ObjectFloatMap(), ObjectMap(), ran, deferred)

		while (deferred.size > 0)
		{
			val newDeferred = Array<DeferredRule>()
			for (deferredRule in deferred)
			{
				deferredRule.execute(ruleTable, ran, newDeferred)
			}

			deferred = newDeferred
		}

		val level = Level()
		level.grid = Array2D(width, height) { x, y -> Tile() }

		for (x in 0..width-1)
		{
			for (y in 0..height-1)
			{
				for (slot in SpaceSlot.Values)
				{
					if (symbolGrid[x, y].contents.containsKey(slot))
					{
						val xml = symbolGrid[x, y].contents[slot]

						val entity = EntityLoader.load(xml)
						if (entity.pos() == null)
						{
							val pos = PositionComponent()
							pos.slot = slot

							entity.add(pos)
						}

						level.grid[x, y].contents[slot] = entity
						entity.pos().position = level.grid[x, y]

						engine.addEntity(entity)
					}
				}
			}
		}

		val namedEntities = engine.getEntitiesFor(Family.one(NameComponent::class.java).get())
		val player = namedEntities.firstOrNull { it.name()!!.isPlayer } ?: throw Exception("No player on level!")

		level.player = player

		return level
	}

	companion object
	{
		fun load(xml: XmlReader.Element): GenerationGrammar
		{
			val grammar = GenerationGrammar()
			grammar.width = xml.get("Width")
			grammar.height = xml.get("Height")
			grammar.rootRule = xml.get("Root")

			val rulesEl = xml.getChildByName("Rules")
			for (el in rulesEl.children())
			{
				val rule = AbstractGrammarRule.load(el)
				val guid = el.getAttribute("GUID")
				grammar.ruleTable[guid] = rule
			}

			return grammar
		}

		fun load(path: String): GenerationGrammar
		{
			val xml = getXml("Grammars/$path")
			return load(xml)
		}
	}
}
