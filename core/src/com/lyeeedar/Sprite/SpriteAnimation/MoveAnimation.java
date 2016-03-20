package com.lyeeedar.Sprite.SpriteAnimation;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.lyeeedar.GlobalData;

public class MoveAnimation extends AbstractSpriteAnimation
{
	public enum MoveEquation
	{
		LINEAR, SMOOTHSTEP, EXPONENTIAL, LEAP
	}

	private float[] diff;
	private MoveEquation eqn;

	public float leapHeight = 3;

	private float[] offset = { 0, 0 };

	public MoveAnimation()
	{

	}

	public MoveAnimation( float duration, float[] diff, MoveEquation eqn )
	{
		//duration *= GlobalData.Global.animationSpeed;

		this.duration = duration;
		this.diff = diff;
		this.eqn = eqn;
	}

	@Override
	public boolean update( float delta )
	{
		time += delta;

		float alpha = MathUtils.clamp( ( duration - time ) / duration, 0, 1 );

		if ( eqn == MoveEquation.SMOOTHSTEP )
		{
			alpha = alpha * alpha * ( 3 - 2 * alpha ); // smoothstep
		}
		else if ( eqn == MoveEquation.EXPONENTIAL )
		{
			alpha = 1 - ( 1 - alpha ) * ( 1 - alpha ) * ( 1 - alpha ) * ( 1 - alpha );
		}

		offset[0] = (int) ( diff[0] * alpha );
		offset[1] = (int) ( diff[1] * alpha );

		if ( eqn == MoveEquation.LEAP )
		{
			// B2(t) = (1 - t) * (1 - t) * p0 + 2 * (1-t) * t * p1 + t*t*p2
			alpha = ( 1 - alpha ) * ( 1 - alpha ) * 0 + 2 * ( 1 - alpha ) * alpha * 1 + alpha * alpha * 0;
			offset[1] += ( GlobalData.Global.tileSize * leapHeight ) * alpha;
		}

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
		this.time = 0;
		this.diff = diff;
	}

	@Override
	public void parse( Element xml )
	{
		eqn = MoveEquation.valueOf( xml.get( "Equation", "SmoothStep" ).toUpperCase() );
		leapHeight = xml.getFloat( "LeapHeight", leapHeight );
	}

	@Override
	public AbstractSpriteAnimation copy()
	{
		MoveAnimation anim = new MoveAnimation();
		anim.eqn = eqn;
		anim.leapHeight = leapHeight;
		anim.duration = duration;
		anim.diff = diff;

		return anim;
	}
}
