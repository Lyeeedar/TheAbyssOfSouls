package com.lyeeedar.UI;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.lyeeedar.UI.Tooltip;

/**
 * Makes a given tooltip actor visible when the actor this listener is attached
 * to was hovered. It will also hide the tooltip when the mouse is not hovering
 * anymore.
 *
 * @author Daniel Holderbaum
 */
public class TooltipListener extends InputListener
{
	private Tooltip tooltip;

	public TooltipListener(Tooltip tooltip)
	{
		this.tooltip = tooltip;
	}

	@Override
	public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor)
	{
		tooltip.show( event, x, y );
	}

	@Override
	public boolean mouseMoved (InputEvent event, float x, float y)
	{
		tooltip.show( event, x, y );

		return false;
	}

	@Override
	public void exit(InputEvent event, float x, float y, int pointer, Actor toActor)
	{
		tooltip.setVisible(false);
		if (Tooltip.openTooltip == tooltip) Tooltip.openTooltip = null;
	}

	@Override
	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button)
	{
		return false;
	}

	@Override
	public void touchUp (InputEvent event, float x, float y, int pointer, int button)
	{

	}
}
