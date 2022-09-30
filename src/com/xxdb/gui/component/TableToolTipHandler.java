package com.xxdb.gui.component;

import java.awt.event.MouseEvent;

/**
 * 
 * The class defines the interface of showing a tip text when a mouse event occurs on the data point.
 *
 */
public interface TableToolTipHandler {
	public String getToolTipText(MouseEvent e);

}
