/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.lyeeedar.UI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Pools;

/** A {@code TabbedPane} widget not full featured but somewhat reusable.
 *
 * @author davebaol */
public class TabPanel extends Table {
	private TabPanelStyle style;

	public Table tabTitleTable;
	Stack tabBodyStack;
	int selectedIndex;
	int tabTitleAlign = Align.left;

	/** Creates a {@code TabbedPane} using the specified skin.
	 * @param skin the skin */
	public TabPanel( Skin skin ) {
		super(skin);
		initialize(skin.get(TabPanelStyle.class));
	}

	/** Creates a {@code TabbedPane} using the specified skin and alignment.
	 * @param skin the skin
	 * @param tabTitleAlign the alignment for tab titles. Must be one of {@link Align#left}, {@link Align#center} or
	 *           {@link Align#right}. */
	public TabPanel( Skin skin, int tabTitleAlign ) {
		super(skin);
		this.tabTitleAlign = tabTitleAlign;
		initialize(skin.get(TabPanelStyle.class));
	}

	/** Creates a {@code TabbedPane} using the specified skin and style name.
	 * @param skin the skin
	 * @param styleName the style name */
	public TabPanel( Skin skin, String styleName ) {
		super(skin);
		initialize(skin.get(styleName, TabPanelStyle.class));
	}

	/** Creates a {@code TabbedPane} using the specified skin, style name and alignment.
	 * @param skin the skin
	 * @param styleName the style name
	 * @param tabTitleAlign the alignment for tab titles. Must be one of {@link Align#left}, {@link Align#center} or
	 *           {@link Align#right}. */
	public TabPanel( Skin skin, String styleName, int tabTitleAlign ) {
		super(skin);
		this.tabTitleAlign = tabTitleAlign;
		initialize(skin.get(styleName, TabPanelStyle.class));
	}

	public TabPanel( TabPanelStyle style ) {
		initialize(style);
	}

	/** Creates a {@code TabbedPane} using the specified style and alignment.
	 * @param style the style
	 * @param tabTitleAlign the alignment for tab titles. Must be one of {@link Align#left}, {@link Align#center} or
	 *           {@link Align#right}. */
	public TabPanel( TabPanelStyle style, int tabTitleAlign ) {
		this.tabTitleAlign = tabTitleAlign;
		initialize(style);
	}

	/** Creates a {@code TabbedPane} without setting the style or size. At least a style must be set before using this tabbed pane. */
	public TabPanel() {
		initialize();
	}

	private void initialize (TabPanelStyle style) {
		setStyle(style);
		setSize(getPrefWidth(), getPrefHeight());
		initialize();
	}

	private void initialize () {

		setTouchable(Touchable.enabled);
		tabTitleTable = new Table();
		tabBodyStack = new Stack();
		selectedIndex = -1;

		// Create 1st row
		Cell<?> leftCell = add(new Image(style.titleBegin));
		Cell<?> midCell = add(tabTitleTable);
		Cell<?> rightCell = add(new Image(style.titleEnd));
		switch (tabTitleAlign) {
			case Align.left:
				leftCell.width( leftCell.getActor().getWidth() ).bottom();
				midCell.left();
				rightCell.expandX().fillX().bottom();
				break;
			case Align.right:
				leftCell.expandX().fillX().bottom();
				midCell.right();
				rightCell.width( rightCell.getActor().getWidth() ).bottom();
				break;
			case Align.center:
				leftCell.expandX().fillX().bottom();
				midCell.center();
				rightCell.expandX().fillX().bottom();
				break;
			default:
				throw new IllegalArgumentException("TabbedPane align must be one of left, center, right");
		}

		// Create 2nd row
		row();
		Table t = new Table();
		t.setBackground(style.bodyBackground);
		t.add(tabBodyStack).expand().fill();
		add(t).colspan(3).expand().fill();
	}

	@Override
	public Table debug () {
		tabTitleTable.debug();
		return super.debug();
	}

	public void setStyle (TabPanelStyle style) {
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		this.style = style;

		invalidateHierarchy();
	}

	/** Returns the tabbed pane's style. Modifying the returned style may not have an effect until
	 * {@link #setStyle(TabPanelStyle)} is called. */
	public TabPanelStyle getStyle () {
		return style;
	}

	public void addTab (String title, Actor actor) {
		int index = tabTitleTable.getCells().size;
		final TabTitleButton button = new TabTitleButton(index, title, style);
		button.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				setSelectedIndex(button.index);
			}
		});
		tabTitleTable.add(button); // .uniform().fill(); // uniform gives tabs the same size
		tabBodyStack.add(actor);

		// Make sure the 1st tab is selected even after adding the tab
		// TODO
		// CAUTION: if you've added a ChangeListener before adding the tab
		// the following lines will fire 2 ChangeEvents.
		setSelectedIndex(index);
		setSelectedIndex(0);
	}

	public int getSelectedIndex () {
		return selectedIndex;
	}

	public void setSelectedIndex (int index) {
		if (selectedIndex == index) return;
		int tabs = tabTitleTable.getCells().size;
		if (selectedIndex >= 0 && selectedIndex < tabs) {
			setSelectedTab(false);
		}
		this.selectedIndex = index;
		if (selectedIndex >= 0 && selectedIndex < tabs) {
			setSelectedTab(true);
		}

		fireStateChanged();
	}

	private void setSelectedTab (boolean value) {
		TabTitleButton tabTitleButton = ((TabTitleButton)tabTitleTable.getCells().get(selectedIndex).getActor());
		tabTitleButton.setDisabled(value); // Can't toggle the selected tab
		tabTitleButton.setChecked(value);
		tabBodyStack.getChildren().get(selectedIndex).setVisible(value);
	}

	/** Sends a ChangeEvent, with this TabbedPane as the target, to each registered listener. This method is called each time there
	 * is a change to the selected index. */
	protected void fireStateChanged () {
		ChangeEvent changeEvent = Pools.obtain(ChangeEvent.class);
		changeEvent.setBubbles(false);
		fire(changeEvent);
		Pools.free(changeEvent);
	}

	private static class TabTitleButton extends TextButton {

		private int index;

		private TabTitleButton (int index, String text, TabPanelStyle style) {
			super(text, toTextButtonStyle(style));
			this.index = index;
		}

		private static TextButtonStyle toTextButtonStyle (TabPanelStyle style) {
			TextButtonStyle buttonStyle = new TextButtonStyle(style.titleButtonUnselected, null, style.titleButtonSelected,
															  style.font);
			buttonStyle.fontColor = style.fontColor;
			buttonStyle.downFontColor = style.downFontColor;
			buttonStyle.overFontColor = style.overFontColor;
			buttonStyle.checkedFontColor = style.checkedFontColor;
			buttonStyle.checkedOverFontColor = style.checkedOverFontColor;
			buttonStyle.disabledFontColor = style.disabledFontColor;
			return buttonStyle;
		}
	}

	/** The style for a {@link TabPanel}. **/
	static public class TabPanelStyle {
		public Drawable titleBegin, titleEnd, bodyBackground;
		public Drawable titleButtonSelected, titleButtonUnselected;
		public BitmapFont font;
		/** Optional. */
		public Color fontColor, downFontColor, overFontColor, checkedFontColor, checkedOverFontColor, disabledFontColor;

		public TabPanelStyle () {
		}

		public TabPanelStyle (Drawable titleBegin, Drawable titleEnd, Drawable bodyBackground, Drawable titleButtonSelected,
								Drawable titleButtonUnselected, BitmapFont font) {
			this.titleBegin = titleBegin;
			this.titleEnd = titleEnd;
			this.bodyBackground = bodyBackground;
			this.titleButtonSelected = titleButtonSelected;
			this.titleButtonUnselected = titleButtonUnselected;
			this.font = font;
		}

		public TabPanelStyle (TabPanelStyle style) {
			this.titleBegin = style.titleBegin;
			this.titleEnd = style.titleEnd;
			this.bodyBackground = style.bodyBackground;
			this.titleButtonSelected = style.titleButtonSelected;
			this.titleButtonUnselected = style.titleButtonUnselected;
			this.font = style.font;

			if (style.fontColor != null) this.fontColor = new Color(style.fontColor);
			if (style.downFontColor != null) this.downFontColor = new Color(style.downFontColor);
			if (style.overFontColor != null) this.overFontColor = new Color(style.overFontColor);
			if (style.checkedFontColor != null) this.checkedFontColor = new Color(style.checkedFontColor);
			if (style.checkedOverFontColor != null) this.checkedFontColor = new Color(style.checkedOverFontColor);
			if (style.disabledFontColor != null) this.disabledFontColor = new Color(style.disabledFontColor);
		}
	}
}
