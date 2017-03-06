package com.lyeeedar.GenerationGrammar

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.evaluate
import com.exp4j.Helpers.unescapeCharacters
import com.lyeeedar.Components.*
import com.lyeeedar.GenerationGrammar.Rules.AbstractGrammarRule
import com.lyeeedar.GenerationGrammar.Rules.DeferredRule
import com.lyeeedar.Global
import com.lyeeedar.Level.Level
import com.lyeeedar.Level.Tile
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import ktx.collections.set

class GenerationGrammar
{
	val ruleTable = ObjectMap<String, AbstractGrammarRule>()
	lateinit var width: String
	lateinit var height: String
	lateinit var rootRule: String
	lateinit var ambient: Colour

	fun generate(seed: Long, engine: Engine): Level
	{
		SceneTimelineComponent.sharedTimelines.clear()

		val rng = Random.obtainTS(seed)

		val width = width.evaluate(seed = rng.nextLong()).round()
		val height = height.evaluate(seed = rng.nextLong()).round()

		val symbolGrid = Array2D<GrammarSymbol>(width, height) { x, y -> GrammarSymbol(' ') }
		val area = Area()
		area.allowedBoundsWidth = width
		area.allowedBoundsHeight = height
		area.width = width
		area.height = height
		area.grid = symbolGrid

		val rule = ruleTable[rootRule]

		var deferred = Array<DeferredRule>()
		DeferredRule.reset()

		runBlocking {
			rule.execute(area, ruleTable, ObjectMap(), ObjectFloatMap(), ObjectMap(), rng.nextLong(), deferred)
		}

		rng.freeTS()

		while (deferred.size > 0)
		{
			val newDeferred = Array<DeferredRule>()
			for (deferredRule in deferred)
			{
				runBlocking {
					deferredRule.execute(ruleTable, newDeferred)
				}
			}

			deferred = newDeferred
		}

		val level = Level()
		level.grid = Array2D(width, height) { x, y -> Tile() }

		runBlocking {
			val jobs = Array<Job>(width * height)
			for (x in 0..width-1)
			{
				for (y in 0..height-1)
				{
					val job = launch(CommonPool)
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

								synchronized(engine)
								{
									engine.addEntity(entity)
								}
							}
						}
					}

					jobs.add(job)
				}
			}

			for (job in jobs) job.join()
		}

		if (!Global.release)
		{
			for (y in 1..height)
			{
				for (x in 0..width-1)
				{
					val char = symbolGrid[x, height-y].char
					print(char)
				}
				print('\n')
			}
		}

		val namedEntities = engine.getEntitiesFor(Family.one(NameComponent::class.java).get())
		val player = namedEntities.firstOrNull { it.name()!!.isPlayer } ?: throw Exception("No player on level!")

		level.player = player
		level.ambient.set(ambient)

		return level
	}

	companion object
	{
		fun load(xml: XmlReader.Element): GenerationGrammar
		{
			val grammar = GenerationGrammar()
			grammar.width = xml.get("Width").unescapeCharacters()
			grammar.height = xml.get("Height").unescapeCharacters()
			grammar.rootRule = xml.get("Root")

			val rulesEl = xml.getChildByName("Rules")
			for (el in rulesEl.children())
			{
				val rule = AbstractGrammarRule.load(el)
				val guid = el.getAttribute("GUID")
				grammar.ruleTable[guid] = rule
			}

			grammar.ambient = AssetManager.loadColour(xml.getChildByName("Ambient"))

			return grammar
		}

		fun load(path: String): GenerationGrammar
		{
			val xml = getXml("Grammars/$path")
			return load(xml)
		}
	}
}
