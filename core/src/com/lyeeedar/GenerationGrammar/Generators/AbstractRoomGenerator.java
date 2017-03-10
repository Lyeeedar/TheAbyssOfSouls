package com.lyeeedar.GenerationGrammar.Generators;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.lyeeedar.GenerationGrammar.Area;
import com.lyeeedar.GenerationGrammar.GrammarSymbol;
import squidpony.squidmath.LightRNG;

public abstract class AbstractRoomGenerator
{
	// ----------------------------------------------------------------------
	public abstract void process( Area area, GrammarSymbol floor, GrammarSymbol wall, LightRNG ran );

	// ----------------------------------------------------------------------
	public abstract void parse( Element xml );
}
