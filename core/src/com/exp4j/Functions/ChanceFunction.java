package com.exp4j.Functions;

import com.badlogic.gdx.math.MathUtils;
import net.objecthunter.exp4j.function.Function;

import java.util.Random;

/**
 * Created by Philip on 03-Jan-16.
 */
public class ChanceFunction extends Function
{
	private Random ran;

	public ChanceFunction()
	{
		super( "chance", 2 );
	}

	public ChanceFunction( Random ran )
	{
		super( "chance", 2 );

		this.ran = ran;
	}

	@Override
	public double apply( double... arg0 )
	{
		double chanceVal = 0;

		if ( ran != null )
		{
			chanceVal = ran.nextFloat() * arg0[1];
		}
		else
		{
			chanceVal = MathUtils.random() * arg0[1];
		}

		return chanceVal <= arg0[0] ? 1 : 0;
	}

}
