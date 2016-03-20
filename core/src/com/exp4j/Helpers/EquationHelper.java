package com.exp4j.Helpers;

import java.util.HashMap;
import java.util.Random;

import com.badlogic.gdx.math.MathUtils;
import com.exp4j.Functions.ChanceFunction;
import com.exp4j.Functions.MathUtilFunctions;
import com.exp4j.Functions.RandomFunction;
import com.exp4j.Operators.BooleanOperators;
import com.lyeeedar.Enums;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class EquationHelper
{
	public static Expression tryBuild( ExpressionBuilder expB )
	{
		Expression exp = null;

		// try
		// {
		exp = expB.build();
		// }
		// catch (Exception e) { }

		return exp;
	}

	public static void setVariableNames( ExpressionBuilder expB, HashMap<String, Float> variableMap, String prefix )
	{
		for ( String key : variableMap.keySet() )
		{
			expB.variable( prefix + key );
		}
	}

	public static void setVariableValues( Expression exp, HashMap<String, Float> variableMap, String prefix )
	{
		for ( String key : variableMap.keySet() )
		{
			exp.setVariable( prefix + key, variableMap.get( key ) );
		}
	}

	public static ExpressionBuilder createEquationBuilder( String eqn )
	{
		return createEquationBuilder( eqn, MathUtils.random );
	}

	public static ExpressionBuilder createEquationBuilder( String eqn, Random ran )
	{
		ExpressionBuilder expB = new ExpressionBuilder( eqn );
		BooleanOperators.applyOperators( expB );
		expB.function( new RandomFunction( ran ) );
		expB.function( new ChanceFunction( ran ) );
		MathUtilFunctions.applyFunctions( expB );

		return expB;
	}

	// ----------------------------------------------------------------------
	public static boolean isNumber( String string )
	{
		if ( string == null || string.isEmpty() ) { return false; }
		int i = 0;
		if ( string.charAt( 0 ) == '-' )
		{
			if ( string.length() > 1 )
			{
				i++;
			}
			else
			{
				return false;
			}
		}
		for ( ; i < string.length(); i++ )
		{
			if ( !Character.isDigit( string.charAt( i ) ) ) { return false; }
		}
		return true;
	}

	public static float evaluate( String eqn )
	{
		return evaluate( eqn, Enums.Statistic.emptyMap, MathUtils.random );
	}

	public static float evaluate( String eqn, Random ran )
	{
		return evaluate( eqn, Enums.Statistic.emptyMap, ran );
	}

	public static float evaluate( String eqn, HashMap<String, Float> variableMap )
	{
		return evaluate( eqn, variableMap, MathUtils.random );
	}

	public static float evaluate( String eqn, HashMap<String, Float> variableMap, Random ran )
	{
		if ( isNumber( eqn ) )
		{
			return Integer.parseInt( eqn );
		}
		else
		{
			ExpressionBuilder expB = createEquationBuilder( eqn, ran );
			setVariableNames( expB, variableMap, "" );
			Expression exp = tryBuild( expB );

			if ( exp == null )
			{
				return 0;
			}
			else
			{
				setVariableValues( exp, variableMap, "" );
				double rawVal = exp.evaluate();

				return (float)rawVal;
			}
		}
	}
}
