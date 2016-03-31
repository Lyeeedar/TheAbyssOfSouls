package com.exp4j.Helpers;

import java.util.HashMap;
import java.util.Random;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ObjectFloatMap;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.exp4j.Functions.ChanceFunction;
import com.exp4j.Functions.MathUtilFunctions;
import com.exp4j.Functions.RandomFunction;
import com.exp4j.Operators.BooleanOperators;
import com.lyeeedar.Enums;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class EquationHelper
{
	private static final ObjectFloatMap<String> emptyMap = Enums.Statistic.getEmptyMap();

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

	public static void setVariableNames( ExpressionBuilder expB, ObjectFloatMap<String> variableMap, String prefix )
	{
		for ( String key : variableMap.keys() )
		{
			expB.variable( prefix + key );
		}
	}

	public static void setVariableValues( Expression exp, ObjectFloatMap<String> variableMap, String prefix )
	{
		for ( String key : variableMap.keys() )
		{
			exp.setVariable( prefix + key, variableMap.get( key, 0f ) );
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
		return evaluate( eqn, emptyMap, MathUtils.random );
	}

	public static float evaluate( String eqn, Random ran )
	{
		return evaluate( eqn, emptyMap, ran );
	}

	public static float evaluate( String eqn, ObjectFloatMap<String> variableMap )
	{
		return evaluate( eqn, variableMap, MathUtils.random );
	}

	public static float evaluate( String eqn, ObjectFloatMap<String> variableMap, Random ran )
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
