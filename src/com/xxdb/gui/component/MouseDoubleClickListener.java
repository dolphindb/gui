package com.xxdb.gui.component;

import java.awt.event.MouseEvent;
/**
 * 
 * an interface defines the operation when a mouse event occurs on a BasicTable.
 *
 */
public interface MouseDoubleClickListener {
	
	/**
	 * Execute the default operation when a double-click event occurs.
	 * @param e a mouse double-click event
	 * @param table a BasicTable
	 */
	public void doubleClicked(MouseEvent e, XXDBJTable table);
}
