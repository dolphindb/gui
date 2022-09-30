package com.xxdb.gui.component;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.event.MouseInputListener;

/**
  * This class doesn't change the L&F of the JTable but
  * listens to mouseclicks and updates the TableSelectionModel.
  */
public class BasicTableSelectionUI extends BasicTableUI {
	private MouseDoubleClickListener mdcl;
  
	public static ComponentUI createUI(JComponent c) {
	    return new BasicTableSelectionUI();
	}
  
	public BasicTableSelectionUI(){
		this(null);
	}
	public BasicTableSelectionUI(MouseDoubleClickListener mdcl){
		this.mdcl = mdcl;
	}

	protected MouseInputListener createMouseInputListener() {
	    return new AnySelectionMouseInputHandler(mdcl);
	}

	/**
    * to get access to the table from the inner class MyMouseInputHandler
    */
	protected JTable getTable() {
		return table;
	}

  	/**
    * updates the TableSelectionModel.
    */
  	protected void updateTableSelectionModel(int row, int column,boolean ctrlDown, boolean shiftDown) {

	XXDBJTable t = (XXDBJTable)getTable();
    column = t.convertColumnIndexToModel(column);
    TableSelectionModel tsm = t.getTableSelectionModel();   
    
    if (t.cellSelectionMode()){
	    if (shiftDown) {
	    	int lastcol = tsm.getLastCol();
	    	int lastrow = tsm.getLastRow();
	    	if(lastrow >=0 && lastcol >=0){
	    		for(int i = Math.min(lastcol, column);i<=Math.max(lastcol, column);i++)
	    			for(int j = Math.min(lastrow, row);j<=Math.max(lastrow,row);j++){
	    				tsm.addSelection(j, i);
	    			}
	    	}
	    	else
	    		tsm.addSelection(row, column);
		    } else if ((ctrlDown)){
		    	tsm.addSelection(row, column);
		    } else {
		    	tsm.clearSelection();
		    	tsm.setSelection(row, column);
		    }
		} 
	    else{
	    	tsm.clearSelection();
	    	for(int i =0;i<tsm.getColCount();i++){
	    		tsm.addSelection(row, i);
	    	}
	    }
  	}

  	/**
    * Almost the same implementation as its super class.
    * Except updating the TableSelectionModel rather than the
    * default ListSelectionModel.
    */
  	//Some methods which are called in the super class are private.
  	//Thus I couldn't call them. Calling the method of the super
  	//class itself should do it, but you never know. Sideeffects may occur...
  	public class AnySelectionMouseInputHandler extends MouseInputHandler {
  		MouseDoubleClickListener doubleClickListener;
  		
  		public AnySelectionMouseInputHandler(MouseDoubleClickListener listener){
  			doubleClickListener = listener;
  		}	
  		
  		public AnySelectionMouseInputHandler(){
  			this(null);
  		}
  		
  		public void mousePressed(MouseEvent e) {
  			super.mousePressed(e);

  			if (!SwingUtilities.isLeftMouseButton(e)) {
  				return;
	    	}

  			Point p = e.getPoint();
  			int row = getTable().rowAtPoint(p);
  			int column = getTable().columnAtPoint(p);
	  
  			if ((column == -1) || (row == -1) || (column == -1)) {
  				return;
  			}

  			/* Adjust the selection if the event was not forwarded 
		     * to the editor above *or* the editor declares that it 
		     * should change selection even when events are forwarded 
		     * to it.
		     */
  			// PENDING(philip): Ought to convert mouse event, e, here. 
  			//if (!repostEvent || table.getCellEditor().shouldSelectCell(e)) {
  			TableCellEditor tce = getTable().getCellEditor();
  			if ((tce==null) || (tce.shouldSelectCell(e))) {
  				getTable().requestFocus();
  				updateTableSelectionModel(row, column, e.isControlDown(), e.isShiftDown());
  				
  				//need to do this because JTable is not painted properly when
  				//user does Shift-Click on a column where a not selected
  				//cell is the anchor ...
  				if (e.isShiftDown())
  					getTable().repaint();
  			}
  			if(doubleClickListener != null && e.getClickCount()==2){
  				doubleClickListener.doubleClicked(e, (XXDBJTable)getTable());
  			}
  		}//mousePressed()
  	}
}
