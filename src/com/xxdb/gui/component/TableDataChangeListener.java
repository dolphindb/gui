package com.xxdb.gui.component;

/**
 * 
 * <code>Table Data Change Listener</code>
 *
 */

public interface TableDataChangeListener {
	/**
	 * Defines default the operations when a table cell value is changed.
	 * @param model BasicXMLTableModel
	 * @param row the row
	 * @param col the column
	 * @param value the value
	 */
	public void tableCellChanged(AbstractXXDBTableModel model, int row, int col, Object value);
}
