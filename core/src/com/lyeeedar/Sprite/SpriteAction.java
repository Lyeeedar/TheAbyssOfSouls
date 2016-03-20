package com.lyeeedar.Sprite;

/**
 * Created by Philip on 27-Feb-16.
 */
public abstract class SpriteAction
{
	public enum FirePoint
	{
		Start,
		Middle,
		End
	}

	public FirePoint firePoint;

	public SpriteAction(FirePoint firePoint)
	{
		this.firePoint = firePoint;
	}

	public abstract void evaluate();
}
