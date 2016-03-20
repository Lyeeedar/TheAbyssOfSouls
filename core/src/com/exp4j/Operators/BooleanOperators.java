package com.exp4j.Operators;

import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.operator.Operator;

public class BooleanOperators
{
	public static class LessThanOperator extends Operator
	{
		public LessThanOperator()
		{
			super("<", 2, true,  Operator.PRECEDENCE_ADDITION - 1);
		}

		@Override
		public double apply(double... args)
		{
			return args[0] < args[1] ? 1 : 0;
		}
	}

	public static class LessThanOrEqualOperator extends Operator
	{
		public LessThanOrEqualOperator()
		{
			super("<=", 2, true,  Operator.PRECEDENCE_ADDITION - 1);
		}

		@Override
		public double apply(double... args)
		{
			return args[0] <= args[1] ? 1 : 0;
		}
	}

	public static class GreaterThanOperator extends Operator
	{
		public GreaterThanOperator()
		{
			super(">", 2, true,  Operator.PRECEDENCE_ADDITION - 1);
		}

		@Override
		public double apply(double... args)
		{
			return args[0] > args[1] ? 1 : 0;
		}
	}

	public static class GreaterThanOrEqualOperator extends Operator
	{
		public GreaterThanOrEqualOperator()
		{
			super(">=", 2, true,  Operator.PRECEDENCE_ADDITION - 1);
		}

		@Override
		public double apply(double... args)
		{
			return args[0] >= args[1] ? 1 : 0;
		}
	}

	public static class EqualsOperator extends Operator
	{
		public EqualsOperator()
		{
			super("==", 2, true,  Operator.PRECEDENCE_ADDITION - 1);
		}

		@Override
		public double apply(double... args)
		{
			return args[0] == args[1] ? 1 : 0;
		}
	}

	public static class NotEqualsOperator extends Operator
	{
		public NotEqualsOperator()
		{
			super("!=", 2, true,  Operator.PRECEDENCE_ADDITION - 1);
		}

		@Override
		public double apply(double... args)
		{
			return args[0] != args[1] ? 1 : 0;
		}
	}

	public static class AndOperator extends Operator
	{
		public AndOperator()
		{
			super("&&", 2, true,  Operator.PRECEDENCE_ADDITION - 2);
		}

		@Override
		public double apply(double... args)
		{
			boolean b1 = args[0] != 0;
			boolean b2 = args[1] != 0;

			return b1 && b2 ? 1 : 0;
		}
	}

	public static class OrOperator extends Operator
	{
		public OrOperator()
		{
			super("||", 2, true,  Operator.PRECEDENCE_ADDITION - 2);
		}

		@Override
		public double apply(double... args)
		{
			boolean b1 = args[0] != 0;
			boolean b2 = args[1] != 0;

			return b1 || b2 ? 1 : 0;
		}
	}

	private static final LessThanOperator lessThan = new LessThanOperator();
	private static final LessThanOrEqualOperator lessThanOrEqual = new LessThanOrEqualOperator();

	private static final GreaterThanOperator greaterThan = new GreaterThanOperator();
	private static final GreaterThanOrEqualOperator greaterThanOrEqual = new GreaterThanOrEqualOperator();

	private static final EqualsOperator equal = new EqualsOperator();
	private static final NotEqualsOperator notEqual = new NotEqualsOperator();

	private static final AndOperator and = new AndOperator();
	private static final OrOperator or = new OrOperator();

	public static void applyOperators(ExpressionBuilder expB)
	{
		expB.operator(lessThan);
		expB.operator(lessThanOrEqual);

		expB.operator(greaterThan);
		expB.operator(greaterThanOrEqual);

		expB.operator(equal);
		expB.operator(notEqual);

		expB.operator(and);
		expB.operator(or);
	}
}
