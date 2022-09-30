package com.xxdb.gui.component;

import javax.swing.event.ListSelectionEvent;

/**
  * An event that characterizes a change in the current selection.
  * @author Jan-Friedrich Mutter (jmutter@bigfoot.de)
  */
public class TableSelectionEvent extends ListSelectionEvent {
	private static final long serialVersionUID = 1L;
	
	/**
    * The index of the column whose selection has changed.
    */
	protected int columnIndex;
	
	public TableSelectionEvent(Object source, int firstRowIndex, int lastRowIndex,
			      int columnIndex, boolean isAdjusting) {

		super(source, firstRowIndex, lastRowIndex, isAdjusting);
		this.columnIndex = columnIndex;
	}

	/**
	* Returns the index of the column whose selection has changed.
	* @return The last column whose selection value has changed, where zero is the first column.
	*/
	public int getColumnIndex() {
	    return columnIndex;
	}
}
