package com.lyeeedar.Sprite;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.HDRColourSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.lyeeedar.Enums;
import com.lyeeedar.GlobalData;
import com.lyeeedar.Sound.SoundInstance;
import com.lyeeedar.Sprite.SpriteAnimation.AbstractSpriteAnimation;
import com.lyeeedar.Util.Colour;
import com.lyeeedar.Util.Point;

public final class Sprite
{
	public enum AnimationStage
	{
		INVALID,
		START,
		MIDDLE,
		END
	}

	public enum AnimationMode
	{
		NONE, TEXTURE, SHRINK, SINE
	}

	public String fileName;

	private static final Colour tempColour = new Colour();

	public Colour colour = new Colour( 1f );

	public float renderDelay = -1;
	public boolean showBeforeRender = false;

	public float repeatDelay = 0;
	public float repeatAccumulator;

	public float animationDelay;
	public float animationAccumulator;

	public float rotation;
	public boolean fixPosition;

	public boolean flipX;
	public boolean flipY;

	public final int[] size = { 1, 1 };

	public Array<TextureRegion> textures;

	public AbstractSpriteAnimation spriteAnimation;

	public AnimationStage animationStage = AnimationStage.INVALID;
	public AnimationState animationState;

	public SoundInstance sound;

	public boolean drawActualSize;

	public float[] baseScale = { 1, 1 };

	private static final Vector3 tempVec = new Vector3(  );
	private static final Matrix3 tempMat = new Matrix3(  );

	public Sprite( String fileName, float animationDelay, Array<TextureRegion> textures, Colour colour, AnimationMode mode, SoundInstance sound, boolean drawActualSize )
	{
		this.fileName = fileName;
		this.textures = textures;
		this.animationDelay = animationDelay;
		this.sound = sound;
		this.drawActualSize = drawActualSize;

		animationState = new AnimationState();
		animationState.mode = mode;

		this.colour = colour;
	}

	public float getLifetime()
	{
		return spriteAnimation != null ? spriteAnimation.duration : animationDelay * textures.size;
	}

	public float getRemainingLifetime()
	{
		return spriteAnimation != null ? spriteAnimation.duration - spriteAnimation.time : animationDelay * (textures.size - animationState.texIndex);
	}

	public AnimationStage getAnimationStage()
	{
		return animationStage;
	}

	public boolean update( float delta )
	{
		if ( renderDelay > 0 )
		{
			renderDelay -= delta;

			if ( renderDelay > 0 ) { return false; }
		}

		if (repeatAccumulator > 0)
		{
			repeatAccumulator -= delta;
		}

		boolean looped = false;
		if (repeatAccumulator <= 0)
		{
			if (animationStage == AnimationStage.INVALID) animationStage = AnimationStage.START;
			animationAccumulator += delta;

			while ( animationAccumulator >= animationDelay )
			{
				animationAccumulator -= animationDelay;

				if ( animationState.mode == AnimationMode.TEXTURE )
				{
					if (spriteAnimation == null && animationState.texIndex == textures.size/2)
					{
						animationStage = AnimationStage.MIDDLE;
					}

					animationState.texIndex++;
					if ( animationState.texIndex >= textures.size )
					{
						animationState.texIndex = 0;
						looped = true;
						repeatAccumulator = repeatDelay;
					}
				}
				else if ( animationState.mode == AnimationMode.SHRINK )
				{
					animationState.isShrunk = !animationState.isShrunk;
					looped = animationState.isShrunk;
				}
				else if ( animationState.mode == AnimationMode.SINE )
				{
					looped = true;
				}
			}
		}

		if ( animationState.mode == AnimationMode.SINE )
		{
			animationState.sinOffset = (float) Math.sin( animationAccumulator / ( animationDelay / ( 2 * Math.PI ) ) );
		}

		if ( spriteAnimation != null )
		{
			if (animationStage == AnimationStage.INVALID) animationStage = AnimationStage.START;
			looped = spriteAnimation.update( delta );

			if (spriteAnimation.time >= spriteAnimation.duration / 2)
			{
				if (spriteAnimation == null && animationState.texIndex == textures.size/2)
				{
					animationStage = AnimationStage.MIDDLE;
				}
			}

			if ( looped )
			{
				spriteAnimation = null;
			}
		}

		if (looped)
		{
			animationStage = AnimationStage.END;
		}

		return looped;
	}

	public void render( HDRColourSpriteBatch batch, float x, float y, float width, float height )
	{
		float scaleX = baseScale[0];
		float scaleY = baseScale[1];

		if ( spriteAnimation != null )
		{
			float[] scale = spriteAnimation.getRenderScale();
			if ( scale != null )
			{
				scaleX *= scale[0];
				scaleY *= scale[1];
			}
		}

		render( batch, x, y, width, height, scaleX, scaleY, animationState );
	}

	public void render( HDRColourSpriteBatch batch, float x, float y, float width, float height, float scaleX, float scaleY, AnimationState animationState )
	{
		Colour oldCol = null;
		if ( colour.a == 0 )
		{
			return;
		}

		oldCol = batch.getColour();

		Colour col = tempColour.set( oldCol );
		col.timesAssign( colour );
		batch.setColor( col );

		drawTexture( batch, textures.items[animationState.texIndex], x, y, width, height, scaleX, scaleY, animationState );

		if ( oldCol != null )
		{
			batch.setColor( oldCol );
		}
	}

	private void drawTexture( HDRColourSpriteBatch batch, TextureRegion texture, float x, float y, float width, float height, float scaleX, float scaleY, AnimationState animationState )
	{
		if ( renderDelay > 0 && !showBeforeRender ) { return; }

		if ( drawActualSize )
		{
			float widthRatio = width / 32.0f;
			float heightRatio = height / 32.0f;

			float trueWidth = texture.getRegionWidth() * widthRatio;
			float trueHeight = texture.getRegionHeight() * heightRatio;

			float widthOffset = ( trueWidth - width ) / 2;

			x -= widthOffset;
			width = trueWidth;
			height = trueHeight;
		}

		width = width * size[0];
		height = height * size[1];

		if ( animationState.mode == AnimationMode.SHRINK && animationState.isShrunk )
		{
			height *= 0.85f;
		}
		else if ( animationState.mode == AnimationMode.SINE )
		{
			y += ( height / 15f ) * animationState.sinOffset;
		}

		if (rotation != 0 && fixPosition)
		{
			Vector3 offset = getPositionCorrectionOffsets( x, y, width / 2.0f, height / 2.0f, width, height, scaleX, scaleY, rotation );
			x -= offset.x;
			y -= offset.y;
		}

		// Check if not onscreen
		if ( x + width < 0 || y + height < 0 || x > GlobalData.Global.resolution[0] || y > GlobalData.Global.resolution[1] )
		{
			return; // skip drawing
		}

		batch.draw( texture, x, y, width / 2.0f, height / 2.0f, width, height, scaleX, scaleY, rotation, flipX, flipY );
	}

	private static Vector3 getPositionCorrectionOffsets(float x, float y, float originX, float originY, float width, float height,
												float scaleX, float scaleY, float rotation)
	{
		// bottom left and top right corner points relative to origin
		final float worldOriginX = x + originX;
		final float worldOriginY = y + originY;
		float fx = -originX;
		float fy = -originY;
		float fx2 = width - originX;
		float fy2 = height - originY;

		// scale
		if (scaleX != 1 || scaleY != 1) {
			fx *= scaleX;
			fy *= scaleY;
			fx2 *= scaleX;
			fy2 *= scaleY;
		}

		// construct corner points, start from top left and go counter clockwise
		final float p1x = fx;
		final float p1y = fy;
		final float p2x = fx;
		final float p2y = fy2;
		final float p3x = fx2;
		final float p3y = fy2;
		final float p4x = fx2;
		final float p4y = fy;

		float x1;
		float y1;
		float x2;
		float y2;
		float x3;
		float y3;
		float x4;
		float y4;

		// rotate
		if (rotation != 0) {
			final float cos = MathUtils.cosDeg( rotation );
			final float sin = MathUtils.sinDeg(rotation);

			x1 = cos * p1x - sin * p1y;
			y1 = sin * p1x + cos * p1y;

			x2 = cos * p2x - sin * p2y;
			y2 = sin * p2x + cos * p2y;

			x3 = cos * p3x - sin * p3y;
			y3 = sin * p3x + cos * p3y;

			x4 = x1 + (x3 - x2);
			y4 = y3 - (y2 - y1);
		} else {
			x1 = p1x;
			y1 = p1y;

			x2 = p2x;
			y2 = p2y;

			x3 = p3x;
			y3 = p3y;

			x4 = p4x;
			y4 = p4y;
		}

		tempVec.set( x1, y1, 0 );

		if (x2 < tempVec.x) tempVec.x = x2;
		if (x3 < tempVec.x) tempVec.x = x3;
		if (x4 < tempVec.x) tempVec.x = x4;

		if (y2 < tempVec.y) tempVec.y = y2;
		if (y3 < tempVec.y) tempVec.y = y3;
		if (y4 < tempVec.y) tempVec.y = y4;

		tempVec.x += worldOriginX;
		tempVec.y += worldOriginY;

		tempVec.x -= x;
		tempVec.y -= y;

		return tempVec;
	}

	public TextureRegion getCurrentTexture()
	{
		return textures.get( animationState.texIndex );
	}

	public Sprite copy()
	{
		Sprite sprite = new Sprite( fileName, animationDelay, textures, colour, animationState.mode, sound, drawActualSize );
		if ( spriteAnimation != null )
		{
			sprite.spriteAnimation = spriteAnimation.copy();
		}

		sprite.flipX = flipX;
		sprite.flipY = flipY;

		return sprite;
	}

	public static final class AnimationState
	{
		public AnimationMode mode;

		public int texIndex;
		public boolean isShrunk;
		public float sinOffset;

		public AnimationState copy()
		{
			AnimationState as = new AnimationState();

			as.mode = mode;
			as.texIndex = texIndex;
			as.isShrunk = isShrunk;
			as.sinOffset = sinOffset;

			return as;
		}

		public void set( AnimationState other )
		{
			mode = other.mode;
			texIndex = other.texIndex;
			isShrunk = other.isShrunk;
			sinOffset = other.sinOffset;
		}
	}
}
