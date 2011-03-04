/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.ui;

import hu.openig.core.Act;
import hu.openig.render.GenericButtonRenderer;
import hu.openig.render.RenderTools;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.Timer;

/**
 * An Imperium Galactica styled button component.
 * @author karnok, 2011.03.04.
 */
public class UIGenericButton extends UIComponent {
	/** The disabled pattern to use for the button. */
	protected BufferedImage disabledPattern;
	/** The action to invoke when the button is clicked. */
	public Act onClick;
	/** 
	 * The action to invoke when the button is pressed down.
	 * Can be used to use this button as a tab.
	 */
	public Act onPress;
	/** 
	 * The optional delay to fire onClick events when
	 * the mouse is pressed over the button.
	 * Unit is in milliseconds.
	 */
	protected int holdDelay = -1;
	/** The timer to send pressed events periodically. */
	protected Timer holdTimer;
	/** Is the mouse pressed down on this component. */
	protected boolean down;
	/** The text to display. */
	protected String text;
	/** The buttom renderer. */
	protected final GenericButtonRenderer render;
	/**
	 * Constructor with the default images.
	 * @param text the text label
	 * @param fm the font metrics used to compute the size
	 * @param render the actual button renderer
	 */
	public UIGenericButton(String text, FontMetrics fm, GenericButtonRenderer render) {
		this.text = text;
		this.render = render;
		Dimension d = render.getPreferredSize(fm, text);
		this.width = d.width;
		this.height = d.height;
		this.holdTimer = new Timer(100, new Act() {
			@Override
			public void act() {
				doClick();
				if (holdDelay < 0 || !enabled || !visible) {
					holdTimer.stop();
				}
			}
		});
	}
	/**
	 * Set the mouse-hold delay to repeatedly fire the onClick
	 * event. Use -1 to turn of repetition.
	 * @param delayMillis the delay in milliseconds
	 */
	public void setHoldDelay(int delayMillis) {
		this.holdDelay = delayMillis;
		if (holdDelay >= 0) {
			holdTimer.setInitialDelay(holdDelay);
			holdTimer.setDelay(holdDelay);
		}
	}
	/**
	 * Compute the preferred size in respect to the given font metrics.
	 * @param fm the font metrics
	 * @return the preferred dimension
	 */
	public Dimension getPreferredSize(FontMetrics fm) {
		return render.getPreferredSize(fm, text);
	}
	/**
	 * Stop all internal timers to allow cleanup and thread exit.
	 */
	public void stop() {
		holdTimer.stop();
	}
	/**
	 * Call the click action if set.
	 */
	protected void doClick() {
		if (onClick != null) {
			onClick.act();
		}
	}
	@Override
	public void draw(Graphics2D g2) {
		if (!enabled && disabledPattern != null) {
			render.paintTo(g2, 0, 0, width, height, false, text);
			RenderTools.fill(g2, 0, 0, width, height, disabledPattern);
		} else
		if (down) {
			render.paintTo(g2, 0, 0, width, height, true, text);
		} else {
			render.paintTo(g2, 0, 0, width, height, false, text);
		}
	}
	@Override
	public boolean mouse(UIMouse e) {
		switch (e.type) {
		case DOWN:
			down = true;
			if (holdDelay >= 0) {
				holdTimer.start();
			}
			if (onPress != null) {
				onPress.act();
			}
			return true;
		case CLICK:
		case DOUBLE_CLICK:
			for (int i = 0; i < e.z; i++) {
				doClick();
			}
			return false;
		case UP:
		case LEAVE:
			down = false;
			holdTimer.stop();
			return true;
		case ENTER:
			return true;
		default:
			return false;
		}
	}
	/**
	 * Set the disabled pattern for this button.
	 * @param pattern the pattern to fill with the area of the button when it is disabled
	 * @return this
	 */
	public UIGenericButton disabledPattern(BufferedImage pattern) {
		this.disabledPattern = pattern;
		return this;
	}
	/**
	 * Change the text of the button.
	 * @param text the new text
	 * @return this
	 */
	public UIGenericButton text(String text) {
		this.text = text;
		return this;
	}
}
