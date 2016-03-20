package com.lyeeedar.UI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * Created by Philip on 17-Dec-15.
 */
public class Seperator extends Widget
{
	private SeperatorStyle style;

	public Seperator( Skin skin )
	{
		this(skin, false);
	}

	public Seperator( Skin skin, boolean vertical )
	{
		style = skin.get(vertical ? "vertical" : "horizontal", SeperatorStyle.class);
	}

	public Seperator( Skin skin, String styleName )
	{
		style = skin.get(styleName, SeperatorStyle.class);
	}

	public Seperator( SeperatorStyle style )
	{
		this.style = style;
	}

	@Override
	public float getPrefHeight()
	{
		return style.thickness;
	}

	@Override
	public float getPrefWidth()
	{
		return style.thickness;
	}

	@Override
	public void draw( Batch batch, float parentAlpha )
	{
		Color c = getColor();
		batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);
		style.background.draw(batch, getX(), getY(), getWidth(), getHeight());
	}

	public SeperatorStyle getStyle()
	{
		return style;
	}

	static public class SeperatorStyle
	{
		public Drawable background;
		public int thickness;
		public boolean vertical;

		public SeperatorStyle()
		{
		}

		public SeperatorStyle( Drawable bg, int thickness )
		{
			this(bg, thickness, false);
		}

		public SeperatorStyle( Drawable bg, int thickness, boolean vertical )
		{
			this.background = bg;
			this.thickness = thickness;
			this.vertical = vertical;
		}
	}
}