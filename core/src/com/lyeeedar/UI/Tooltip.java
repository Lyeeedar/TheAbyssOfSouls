package com.lyeeedar.UI;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class Tooltip extends Table
{
	public static Tooltip openTooltip;

	public Table Content;
	private TooltipStyle m_style;
	private Stage storedstage;

	public Tooltip( Table Content, Skin skin, Stage stage )
	{
		super( skin );

		this.storedstage = stage;

		this.Content = Content;
		add( Content ).expand().fill();

		setVisible( false );

		stage.addActor( this );

		setStyle( skin.get( "default", TooltipStyle.class ) );

		pack();
	}

	public void setStyle( TooltipStyle style )
	{
		m_style = style;
		setBackground( m_style.background );
	}

	public void show( InputEvent event, float x, float y )
	{
		Vector2 tmp = new Vector2( x, y );
		event.getListenerActor().localToStageCoordinates( tmp );
		show( tmp.x, tmp.y );
	}

	public void show( float x, float y )
	{
		if (getStage() == null)
		{
			storedstage.addActor( this );
		}

		if ( openTooltip != null && openTooltip != this )
		{
			openTooltip.setVisible( false );
			openTooltip.remove();
			openTooltip = null;
		}

		setVisible( true );

		Vector2 tmp = new Vector2( x, y );
		tmp.add( 10, 10 );

		if ( getWidth() > getStage().getWidth() - 10 )
		{
			setWidth( getStage().getWidth() - 10 );
		}

		if ( getHeight() > getStage().getHeight() - 10 )
		{
			setHeight( getStage().getHeight() - 10 );
		}

		// Fit within stage

		if ( tmp.x < 5 )
		{
			tmp.x = 5;
		}
		else if ( tmp.x + getWidth() > getStage().getWidth() - 5 )
		{
			tmp.x = getStage().getWidth() - getWidth() - 5;
		}

		if ( tmp.y < 5 )
		{
			tmp.y = 5;
		}
		else if ( tmp.y + getHeight() > getStage().getHeight() - 5 )
		{
			tmp.y = getStage().getHeight() - getHeight() - 5;
		}

		setPosition( tmp.x, tmp.y );
		toFront();

		openTooltip = this;
	}

	public static class TooltipStyle
	{

		/** Optional */
		public Drawable background;

		public TooltipStyle()
		{

		}

	}
}
