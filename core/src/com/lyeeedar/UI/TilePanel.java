package com.lyeeedar.UI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.lyeeedar.AssetManager;
import com.lyeeedar.Sprite.Sprite;

public abstract class TilePanel extends Widget
{
	private final TilePanel thisRef = this;

	protected int tileSize;

	protected int targetWidth;
	protected int targetHeight;

	protected int viewWidth;
	protected int viewHeight;

	protected int dataWidth;
	protected int dataHeight;

	protected int scrollX;
	protected int scrollY;

	protected final Skin skin;
	protected final Stage stage;

	public int padding = 10;

	public boolean canBeExamined = true;

	protected NinePatch tilePanelBackgroundH;
	protected NinePatch tilePanelBackgroundV;
	protected boolean drawHorizontalBackground = true;

	protected Sprite tileBackground;
	protected Sprite tileBorder;

	protected Array<Object> tileData = new Array<Object>();
	protected Object mouseOver;

	protected boolean expandVertically;

	public TilePanel( Skin skin, Stage stage, Sprite tileBackground, Sprite tileBorder, int viewWidth, int viewHeight, int tileSize, boolean expandVertically )
	{
		this.skin = skin;
		this.stage = stage;
		this.tileBackground = tileBackground;
		this.tileBorder = tileBorder;
		this.targetWidth = viewWidth;
		this.targetHeight = viewHeight;
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		this.tileSize = tileSize;
		this.expandVertically = expandVertically;

		this.tilePanelBackgroundH = new NinePatch( AssetManager.loadTextureRegion( "Sprites/GUI/PanelHorizontal.png" ), 21, 21, 21, 21 );
		this.tilePanelBackgroundV = new NinePatch( AssetManager.loadTextureRegion( "Sprites/GUI/PanelVertical.png" ), 21, 21, 21, 21 );

		TilePanelListener listener = new TilePanelListener();

		this.addListener( listener );
		this.setWidth( getPrefWidth() );
	}

	public abstract void populateTileData();

	public abstract Sprite getSpriteForData( Object data );

	public abstract void handleDataClicked( Object data, InputEvent event, float x, float y );

	public abstract Table getToolTipForData( Object data );

	public abstract Color getColourForData( Object data );

	public abstract void onDrawItemBackground( Object data, Batch batch, int x, int y, int width, int height );

	public abstract void onDrawItem( Object data, Batch batch, int x, int y, int width, int height );

	public abstract void onDrawItemForeground( Object data, Batch batch, int x, int y, int width, int height );

	public boolean isPointInThis( int x, int y )
	{
		Vector2 vec2 = Pools.obtain(Vector2.class).set( x, y );
		vec2 = this.screenToLocalCoordinates( vec2 );

		if ( vec2.x >= 0 && vec2.y >= 0 && vec2.x <= getWidth() && vec2.y <= getHeight() )
		{
			Pools.free( vec2 );
			return true;
		}
		Pools.free( vec2 );
		return false;
	}

	@Override
	public void invalidate()
	{
		super.invalidate();

		if ( expandVertically )
		{
			viewHeight = (int) ( ( getHeight() - padding ) / ( tileSize + padding ) );
		}
	}

	@Override
	public float getMinWidth()
	{
		return ( tileSize + padding ) * targetWidth + padding;
	}

	@Override
	public float getMinHeight()
	{
		return ( tileSize + padding ) * targetHeight + padding;
	}

	private void validateScroll()
	{
		int scrollableX = Math.max( 0, dataWidth - viewWidth );
		int scrollableY = Math.max( 0, dataHeight - viewHeight );

		scrollX = MathUtils.clamp( scrollX, 0, scrollableX );
		scrollY = MathUtils.clamp( scrollY, 0, scrollableY );
	}

	@Override
	public void draw( Batch batch, float parentAlpha )
	{
		populateTileData();
		validateScroll();

		int height = viewHeight * ( tileSize + padding ) + padding;
		int width = viewWidth * ( tileSize + padding ) + padding;

		if ( dataHeight > viewHeight )
		{
			width += 25;
		}

		batch.setColor( Color.WHITE );

		if (drawHorizontalBackground)
		{
			tilePanelBackgroundH.draw( batch, getX(), getY() + getHeight() - height, width, height );
		}
		else
		{
			tilePanelBackgroundV.draw( batch, getX(), getY() + getHeight() - height, width, height );
		}

		int xOffset = (int) getX() + padding;
		int top = (int) ( getY() - padding + getHeight() ) - tileSize;

		if ( dataHeight > viewHeight )
		{
			tileBackground.render( batch, xOffset + tileSize + 5, top - height, 10, height );
		}

		int x = 0;
		int y = 0;

		batch.setColor( Color.DARK_GRAY );
		for ( y = 0; y < viewHeight; y++ )
		{
			for ( x = 0; x < viewWidth; x++ )
			{
				if ( tileBackground != null )
				{
					tileBackground.render( batch, x * ( tileSize + padding ) + xOffset, top - y * ( tileSize + padding ), tileSize, tileSize );
				}
			}
		}

		x = 0;
		y = 0;

		int scrollOffset = scrollY * viewWidth + scrollX;

		for ( int i = scrollOffset; i < tileData.size; i++ )
		{
			Object item = tileData.get( i );

			Color baseColour = item != null && item == mouseOver ? Color.WHITE : Color.LIGHT_GRAY;
			Color itemColour = getColourForData( item );
			if ( itemColour != null )
			{
				itemColour = new Color( baseColour ).mul( itemColour );
			}
			else
			{
				itemColour = baseColour;
			}

			batch.setColor( itemColour );
			if ( tileBackground != null )
			{
				tileBackground.render( batch, x * ( tileSize + padding ) + xOffset, top - y * ( tileSize + padding ), tileSize, tileSize );
			}
			onDrawItemBackground( item, batch, x * ( tileSize + padding ) + xOffset, top - y * ( tileSize + padding ), tileSize, tileSize );

			batch.setColor( Color.WHITE );
			Sprite sprite = getSpriteForData( item );
			if ( sprite != null )
			{
				sprite.render( batch, x * ( tileSize + padding ) + xOffset, top - y * ( tileSize + padding ), tileSize, tileSize );
			}
			onDrawItem( item, batch, x * ( tileSize + padding ) + xOffset, top - y * ( tileSize + padding ), tileSize, tileSize );

			batch.setColor( itemColour );
			if ( tileBorder != null && item != null )
			{
				tileBorder.render( batch, x * ( tileSize + padding ) + xOffset, top - y * ( tileSize + padding ), tileSize, tileSize );
			}
			onDrawItemForeground( item, batch, x * ( tileSize + padding ) + xOffset, top - y * ( tileSize + padding ), tileSize, tileSize );

			x++;
			if ( x == viewWidth )
			{
				x = 0;
				y++;
				if ( y == viewHeight )
				{
					break;
				}
			}
		}
	}

	public class TilePanelListener extends InputListener
	{
		boolean longPressed = false;

		boolean dragged = false;
		float dragX;
		float dragY;

		float lastX;
		float lastY;

		private Object pointToItem( float x, float y )
		{
			if ( x < padding || y < padding || x > getWidth() - padding || y > getHeight() - padding ) { return null; }

			y = getHeight() - y;

			int xIndex = (int) ( ( x - padding ) / ( tileSize + padding ) );
			int yIndex = (int) ( ( y - padding ) / ( tileSize + padding ) );

			if ( xIndex >= viewWidth || yIndex >= viewHeight ) { return null; }

			int xpos = (int) ( x - ( tileSize + padding ) * xIndex );
			int ypos = (int) ( y - ( tileSize + padding ) * yIndex );

			if ( xpos > tileSize + padding || ypos > tileSize + padding ) { return null; }

			xIndex += scrollX;
			yIndex += scrollY;

			int index = yIndex * viewWidth + xIndex;
			if ( index >= tileData.size || index < 0 ) { return null; }
			return tileData.get( index );
		}

		@Override
		public void touchDragged( InputEvent event, float x, float y, int pointer )
		{
			if ( !dragged && ( Math.abs( x - dragX ) > 10 || Math.abs( y - dragY ) > 10 ) )
			{
				dragged = true;

				lastX = x;
				lastY = y;
			}

			if ( dragged )
			{
				int xdiff = (int) ( ( x - lastX ) / tileSize );
				int ydiff = (int) ( ( y - lastY ) / tileSize );

				if ( xdiff != 0 )
				{
					scrollX -= xdiff;
					lastX = x;
				}

				if ( ydiff != 0 )
				{
					scrollY += ydiff;
					lastY = y;
				}
			}
		}

		@Override
		public boolean scrolled( InputEvent event, float x, float y, int amount )
		{
			if ( dataWidth > viewWidth )
			{
				scrollX += amount;
			}
			else
			{
				scrollY += amount;
			}

			return true;
		}

		@Override
		public boolean mouseMoved( InputEvent event, float x, float y )
		{
			if ( Tooltip.openTooltip != null )
			{
				Tooltip.openTooltip.setVisible( false );
				Tooltip.openTooltip.remove();
				Tooltip.openTooltip = null;
			}

			Object item = pointToItem( x, y );

			if ( item != null )
			{
				Table table = getToolTipForData( item );

				if ( table != null )
				{
					Tooltip tooltip = new Tooltip( table, skin, stage );
					tooltip.show( event, x, y, false );
				}
			}

			mouseOver = item;

			stage.setScrollFocus( thisRef );

			return true;
		}

		@Override
		public boolean touchDown( InputEvent event, float x, float y, int pointer, int button )
		{
			if ( Tooltip.openTooltip != null )
			{
				Tooltip.openTooltip.setVisible( false );
				Tooltip.openTooltip.remove();
				Tooltip.openTooltip.openTooltip.openTooltip = null;
				Tooltip.openTooltip = null;
			}

			dragged = false;
			dragX = x;
			dragY = y;

			return true;
		}

		@Override
		public void touchUp( InputEvent event, float x, float y, int pointer, int button )
		{
			if ( Tooltip.openTooltip != null )
			{
				Tooltip.openTooltip.setVisible( false );
				Tooltip.openTooltip.remove();
				Tooltip.openTooltip = null;
			}

			Object item = pointToItem( x, y );

			if ( !longPressed && !dragged && item != null )
			{
				if ( canBeExamined )
				{
					Table table = getToolTipForData( item );

					if ( table != null )
					{
						Tooltip tooltip = new Tooltip( table, skin, stage );
						tooltip.show( event, x, y, false );
					}
				}
				else
				{
					handleDataClicked( item, event, x, y );
				}
			}

			dragged = false;
		}

		@Override
		public void enter( InputEvent event, float x, float y, int pointer, Actor toActor )
		{
			stage.setScrollFocus( thisRef );
		}

		@Override
		public void exit( InputEvent event, float x, float y, int pointer, Actor toActor )
		{
			mouseOver = null;

			if ( Tooltip.openTooltip != null )
			{
				Tooltip.openTooltip.setVisible( false );
				Tooltip.openTooltip.remove();
				Tooltip.openTooltip.openTooltip.openTooltip = null;
				Tooltip.openTooltip = null;
			}
		}
	}
}
