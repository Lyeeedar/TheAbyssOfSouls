package com.exp4j.Functions;

import java.util.Random;

import net.objecthunter.exp4j.function.Function;

import com.badlogic.gdx.math.MathUtils;

public class RandomFunction extends Function
{
	private Random ran;

	public RandomFunction()
	{
		super( "rnd", 1 );
	}

	public RandomFunction( Random ran )
	{
		super( "rnd", 1 );

		this.ran = ran;
	}

	@Override
	public double apply( double... arg0 )
	{
		if ( ran != null )
		{
			return ran.nextFloat() * arg0[0];
		}
		else
		{
			return MathUtils.random() * arg0[0];
		}
	}

}
