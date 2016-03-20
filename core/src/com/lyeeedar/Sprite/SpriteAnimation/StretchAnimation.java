package com.lyeeedar.Sprite.SpriteAnimation;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.lyeeedar.GlobalData;

public class StretchAnimation extends AbstractSpriteAnimation
{
	public enum StretchEquation
	{
		EXTEND, REVERSEEXTEND, EXPAND
	}

	private float[] diff;
	private float finalScale;
	private StretchEquation eqn;
	private float trueDuration;
	private float animSpeed = 0.2f;
	private float padding = 0.5f;

	private float[] offset = { 0, 0 };
	private float[] scale = { 1, 1 };

	public StretchAnimation()
	{

	}

	public StretchAnimation( float duration, float[] diff, float padDuration, StretchEquation eqn )
	{
		duration *= GlobalData.Global.animationSpeed;
		padDuration *= GlobalData.Global.animationSpeed;

		this.duration = duration + padDuration;

		this.diff = diff;
		this.eqn = eqn;
		this.trueDuration = duration;

		if (diff != null)
		{
			float dist = (float) Math.sqrt( diff[ 0 ] * diff[ 0 ] + diff[ 1 ] * diff[ 1 ] ) + GlobalData.Global.tileSize * 2;
			finalScale = ( dist / GlobalData.Global.tileSize ) / 2.0f;
		}
	}

	@Override
	public boolean update( float delta )
	{
		time += delta;

		float alpha = MathUtils.clamp( ( trueDuration - time ) / trueDuration, 0, 1 );

		if ( eqn == StretchEquation.EXTEND )
		{
			offset[0] = (int) ( diff[0] / 2 + ( diff[0] / 2 ) * alpha );
			offset[1] = (int) ( diff[1] / 2 + ( diff[1] / 2 ) * alpha );

			scale[1] = 1 + finalScale * ( 1 - alpha );
		}
		else if ( eqn == StretchEquation.REVERSEEXTEND )
		{
			offset[0] = (int) ( ( diff[0] / 2 ) * ( 1 - alpha ) );
			offset[1] = (int) ( ( diff[1] / 2 ) * ( 1 - alpha ) );

			scale[1] = 1 + finalScale * ( 1 - alpha );
		}
		else if ( eqn == StretchEquation.EXPAND )
		{
			scale[0] = 1 - alpha ;
			scale[1] = 1 - alpha ;
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
		return scale;
	}

	@Override
	public void set( float duration, float[] diff )
	{
		duration = duration * animSpeed;
		this.duration = duration + padding;
		this.trueDuration = duration;
		this.diff = diff;
		this.time = 0;

		float dist = (float) Math.sqrt( diff[0] * diff[0] + diff[1] * diff[1] ) + GlobalData.Global.tileSize * 2;
		finalScale = ( dist / GlobalData.Global.tileSize ) / 2.0f;
	}

	@Override
	public AbstractSpriteAnimation copy()
	{
		StretchAnimation anim = new StretchAnimation();
		anim.duration = duration;
		anim.trueDuration = trueDuration;
		anim.diff = diff;
		anim.finalScale = finalScale;
		anim.eqn = eqn;

		return anim;
	}

	@Override
	public void parse( Element xml )
	{
		eqn = StretchEquation.valueOf( xml.get( "Equation", "Extend" ).toUpperCase() );
		animSpeed = xml.getFloat( "AnimationSpeed", 0.2f );
		padding = xml.getFloat( "Padding", 0.5f );
	}
}