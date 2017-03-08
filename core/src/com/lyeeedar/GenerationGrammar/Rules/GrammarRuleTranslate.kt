package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.evaluate
import com.lyeeedar.GenerationGrammar.Pos
import com.lyeeedar.Util.Random
import com.lyeeedar.Util.freeTS

class GrammarRuleTranslate : AbstractGrammarRule()
{
	enum class Mode
	{
		RELATIVE,
		ABSOLUTE
	}

	lateinit var xEqn: String
	lateinit var yEqn: String
	lateinit var mode: Mode

	suspend override fun execute(args: RuleArguments)
	{
		val rng = Random.obtainTS(args.seed)

		args.area.writeVariables(args.variables)
		val x = xEqn.evaluate(args.variables, rng.nextLong()).toInt()
		val y = yEqn.evaluate(args.variables, rng.nextLong()).toInt()

		rng.freeTS()

		if (mode == Mode.RELATIVE)
		{
			args.area.x += x
			args.area.y += y

			if (args.area.isPoints)
			{
				args.area.points.forEachIndexed { i, pos -> args.area.points[i] = Pos(pos.x + x, pos.y + y) }
			}
		}
		else if (mode == Mode.ABSOLUTE)
		{
			val dx = x - args.area.x
			val dy = y - args.area.y

			args.area.x += dx
			args.area.y += dy

			if (args.area.isPoints)
			{
				args.area.points.forEachIndexed { i, pos -> args.area.points[i] = Pos(pos.x + dx, pos.y + dy) }
			}
		}
	}

	override fun parse(xml: XmlReader.Element)
	{
		xEqn = xml.get("X", "0")
		yEqn = xml.get("Y", "0")
		mode = Mode.valueOf(xml.get("Mode", "Relative").toUpperCase())
	}

}
