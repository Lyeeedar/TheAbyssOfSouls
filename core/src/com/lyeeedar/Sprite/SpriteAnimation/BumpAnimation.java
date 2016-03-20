package com.lyeeedar.Sprite.SpriteAnimation;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.lyeeedar.Enums;
import com.lyeeedar.GlobalData;

public class BumpAnimation extends AbstractSpriteAnimation
{
	private Enums.Direction direction;

	private float[] offset = { 0, 0 };

	public BumpAnimation()
	{

	}

	public BumpAnimation( float duration, Enums.Direction direction )
	{
		duration *= GlobalData.Global.animationSpeed;

		this.duration = duration;
		this.direction = direction;
	}

	@Override
	public boolean update( float delta )
	{
		time += delta;

		float alpha = MathUtils.clamp( Math.abs( ( time - duration / 2 ) / ( duration / 2 ) ), 0, 1 );

		offset[0] = (int) ( ( GlobalData.Global.tileSize / 3 ) * alpha * direction.x );
		offset[1] = (int) ( ( GlobalData.Global.tileSize / 3 ) * alpha * direction.y );

		return time > duration;
	}

	@Override
	public float[] getRenderOffset()
	{
		return offset;
	}

	@Override
	public float[] getRenderScale()
	{
		return null;
	}

	@Override
	public void set( float duration, float[] diff )
	{
		this.duration = duration;
		this.direction = Enums.Direction.getDirection( diff );
		this.time = 0;
	}

	@Override
	public void parse( Element xml )
	{
	}

	@Override
	public AbstractSpriteAnimation copy()
	{
		BumpAnimation anim = new BumpAnimation();
		anim.direction = direction;
		anim.duration = duration;

		return anim;
	}
}
