package box2dLight;

import com.badlogic.gdx.graphics.Color;

/**
 * Created by Philip on 21-Mar-16.
 */
public class FovLight extends PointLight
{
	public FovLight( RayHandler rayHandler, int rays )
	{
		super( rayHandler, rays );
	}

	public FovLight( RayHandler rayHandler, int rays, Color color, float distance, float x, float y )
	{
		super( rayHandler, rays, color, distance, x, y );
	}

	@Override
	public void add(RayHandler rayHandler)
	{
		this.rayHandler = rayHandler;
		rayHandler.fovLightsList.add( this );
	}

	@Override
	public void remove()
	{
		rayHandler.fovLightsList.removeValue( this, true );
		rayHandler = null;
	}
}
