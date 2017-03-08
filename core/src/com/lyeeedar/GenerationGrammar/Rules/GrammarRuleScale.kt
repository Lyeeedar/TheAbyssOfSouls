package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.evaluate
import com.lyeeedar.Direction
import com.lyeeedar.Util.Random
import com.lyeeedar.Util.freeTS

class GrammarRuleScale : AbstractGrammarRule()
{
	enum class Mode
	{
		ADDITIVE,
		MULTIPLICATIVE,
		ABSOLUTE
	}

	lateinit var mode: Mode
	lateinit var xEqn: String
	lateinit var yEqn: String

	lateinit var snap: Direction

	suspend override fun execute(args: RuleArguments)
	{
		val oldWidth = args.area.width
		val oldHeight = args.area.height

		val rng = Random.obtainTS(args.seed)

		args.area.writeVariables(args.variables)
		val x = xEqn.evaluate(args.variables, rng.nextLong())
		val y = yEqn.evaluate(args.variables, rng.nextLong())

		rng.freeTS()

		if (mode == Mode.ADDITIVE)
		{
			args.area.width += x.toInt()
			args.area.height += y.toInt()
		}
		else if (mode == Mode.MULTIPLICATIVE)
		{
			args.area.width = (args.area.width * x).toInt()
			args.area.height = (args.area.height * y).toInt()
		}
		else if (mode == Mode.ABSOLUTE)
		{
			args.area.width = x.toInt()
			args.area.height = y.toInt()
		}
		else throw Exception("Unhandled scale mode '$mode'!")

		val diffX = args.area.width - oldWidth
		val diffY = args.area.height - oldHeight

		if (snap.x == 0)
		{
			args.area.x -= diffX / 2
		}
		else if (snap.x < 0)
		{

		}
		else if (snap.x > 0)
		{
			args.area.x = (args.area.x + oldWidth) - args.area.width
		}

		if (snap.y == 0)
		{
			args.area.y -= diffY / 2
		}
		else if (snap.y < 0)
		{

		}
		else if (snap.y > 0)
		{
			args.area.y = (args.area.y + oldHeight) - args.area.height
		}

		if (args.area.isPoints)
		{
			for (point in args.area.points.toList())
			{
				if (point.x < args.area.x || point.y < args.area.y || point.x >= args.area.x+args.area.width || point.y >= args.area.y+args.area.height)
				{
					args.area.points.removeValue(point, true)
				}
			}
		}
	}

	override fun parse(xml: XmlReader.Element)
	{
		mode = Mode.valueOf(xml.get("Mode", "Additive").toUpperCase())
		xEqn = xml.get("X")
		yEqn = xml.get("Y")
		snap = Direction.valueOf(xml.get("Snap", "Center").toUpperCase())
	}

}
