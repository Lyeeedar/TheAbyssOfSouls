package com.exp4j.Functions;

import com.badlogic.gdx.math.MathUtils;

import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;

public class MathUtilFunctions
{
	public static class RoundFunction extends Function
	{
		public RoundFunction()
		{
			super("round", 1);
		}

		@Override
		public double apply(double... arg0)
		{
			return MathUtils.round((float) arg0[0]);
		}
	}

	public static class MinFunction extends Function
	{
		public MinFunction()
		{
			super("min", 2);
		}

		@Override
		public double apply(double... arg0)
		{
			return Math.min(arg0[0], arg0[1]);
		}
	}

	public static class Min3Function extends Function
	{
		public Min3Function()
		{
			super("min3", 3);
		}

		@Override
		public double apply(double... arg0)
		{
			return
					Math.min(arg0[0],
					Math.min(arg0[1], arg0[2])
					);
		}
	}

	public static class Min4Function extends Function
	{
		public Min4Function()
		{
			super("min4", 4);
		}

		@Override
		public double apply(double... arg0)
		{
			return
					Math.min(arg0[0],
					Math.min(arg0[1],
					Math.min(arg0[2], arg0[3])
					));
		}
	}

	public static class Min5Function extends Function
	{
		public Min5Function()
		{
			super("min5", 5);
		}

		@Override
		public double apply(double... arg0)
		{
			return
					Math.min(arg0[0],
					Math.min(arg0[1],
					Math.min(arg0[2],
					Math.min(arg0[3], arg0[4])
					)));
		}
	}

	public static class MaxFunction extends Function
	{
		public MaxFunction()
		{
			super("max", 2);
		}

		@Override
		public double apply(double... arg0)
		{
			return Math.max(arg0[0], arg0[1]);
		}
	}

	public static class Max3Function extends Function
	{
		public Max3Function()
		{
			super("max3", 3);
		}

		@Override
		public double apply(double... arg0)
		{
			return
					Math.max(arg0[0],
					Math.max(arg0[1], arg0[2])
					);
		}
	}

	public static class Max4Function extends Function
	{
		public Max4Function()
		{
			super("max4", 4);
		}

		@Override
		public double apply(double... arg0)
		{
			return
					Math.max(arg0[0],
					Math.max(arg0[1],
					Math.max(arg0[2], arg0[3])
					));
		}
	}

	public static class Max5Function extends Function
	{
		public Max5Function()
		{
			super("max5", 5);
		}

		@Override
		public double apply(double... arg0)
		{
			return
					Math.max(arg0[0],
					Math.max(arg0[1],
					Math.max(arg0[2],
					Math.max(arg0[3], arg0[4])
					)));
		}
	}

	public static class ClampFunction extends Function
	{
		public ClampFunction()
		{
			super("clamp", 3);
		}

		@Override
		public double apply(double... arg0)
		{
			return MathUtils.clamp(arg0[0], arg0[1], arg0[2]);
		}
	}

	private final static RoundFunction round = new RoundFunction();
	private final static ClampFunction clamp = new ClampFunction();

	private final static MinFunction min = new MinFunction();
	private final static Min3Function min3 = new Min3Function();
	private final static Min4Function min4 = new Min4Function();
	private final static Min5Function min5 = new Min5Function();

	private final static MaxFunction max = new MaxFunction();
	private final static Max3Function max3 = new Max3Function();
	private final static Max4Function max4 = new Max4Function();
	private final static Max5Function max5 = new Max5Function();

	public static void applyFunctions(ExpressionBuilder expB)
	{
		expB.function(round);
		expB.function(clamp);

		expB.function(min);
		expB.function(min3);
		expB.function(min4);
		expB.function(min5);

		expB.function(max);
		expB.function(max3);
		expB.function(max4);
		expB.function(max5);
	}
}
