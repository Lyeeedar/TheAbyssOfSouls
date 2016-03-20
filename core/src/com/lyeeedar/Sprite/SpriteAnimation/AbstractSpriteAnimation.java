package com.lyeeedar.Sprite.SpriteAnimation;

import java.util.HashMap;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public abstract class AbstractSpriteAnimation
{
	public float duration;
	public float time;

	public abstract void set( float duration, float[] diff );

	public abstract float[] getRenderOffset();

	public abstract float[] getRenderScale();

	public abstract boolean update( float delta );

	public abstract void parse( Element xml );

	public abstract AbstractSpriteAnimation copy();

	public static AbstractSpriteAnimation load( Element xml )
	{
		Class<AbstractSpriteAnimation> c = ClassMap.get( xml.getName().toUpperCase() );
		AbstractSpriteAnimation type = null;

		try
		{
			type = ClassReflection.newInstance( c );
		}
		catch ( Exception e )
		{
			System.err.println(xml.getName());
			e.printStackTrace();
		}

		type.parse( xml );

		return type;
	}

	public static final HashMap<String, Class> ClassMap = new HashMap<String, Class>();
	static
	{
		ClassMap.put( "BUMP", BumpAnimation.class );
		ClassMap.put( "MOVE", MoveAnimation.class );
		ClassMap.put( "STRETCH", StretchAnimation.class );
	}
}
