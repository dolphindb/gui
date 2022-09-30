package com.xxdb.gui.component;

import java.util.Vector;
import java.util.Enumeration;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import javax.swing.ListSelectionModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.table.TableModel;
import javax.swing.event.*;

/**
 * 
 * A model class that handles the table selection event.
 *
 */
public class TableSelectionModel
      implements PropertyChangeListener, ListSelectionListener, TableModelListener {

  /** List of Listeners which will be notified when the selection value changes */
  protected EventListenerList listenerList = new EventListenerList();
  /** contains a ListSelectionModel for each column */
  protected Vector<ListSelectionModel> listSelectionModels = new Vector<>();
  private XXDBJTable myTable;
  private int lastCol;
  private int lastRow;
  public int getLastCol(){
	  return this.lastCol;
  }
  public int getLastRow(){
	  return this.lastRow;
  }
  public TableSelectionModel(XXDBJTable tb) {
	  this.myTable = tb;
	  lastCol = -1;
	  lastRow = -1;
  }
  public int getColCount(){
	  return listSelectionModels.size();
  }

  /**
    * Forwards the request to the ListSelectionModel
    * at the specified column.
    */
  public void addSelection(int row, int column) {
    ListSelectionModel lsm = getListSelectionModelAt(column);
    lsm.addSelectionInterval(row, row);
    lastCol = column;
    lastRow = row;
  }

  /**
    * Forwards the request to the ListSelectionModel
    * at the specified column.
    */
  public void setSelection(int row, int column) {
    ListSelectionModel lsm = getListSelectionModelAt(column);
    lsm.setSelectionInterval(row, row);
    lastCol = column;
    lastRow = row;
  }

  /**
    * Forwards the request to the ListSelectionModel
    * at the specified column.
    */
  public void setSelectionInterval(int row1, int row2, int column) {
    ListSelectionModel lsm = getListSelectionModelAt(column);
    lsm.setSelectionInterval(row1, row2);
  }

  /**
    * Forwards the request to the ListSelectionModel
    * at the specified column.
    */
  public void setLeadSelectionIndex(int row, int column) {
    ListSelectionModel lsm = getListSelectionModelAt(column);
    if (lsm.isSelectionEmpty())
      lsm.setSelectionInterval(row, row);
    else
      //calling that method throws an IndexOutOfBoundsException when selection is empty (?, JDK 1.1.8, Swing 1.1)
      lsm.setLeadSelectionIndex(row);
  }

  /**
    * Forwards the request to the ListSelectionModel
    * at the specified column.
    */
  public void removeSelection(int row, int column) {
    ListSelectionModel lsm = getListSelectionModelAt(column);
    lsm.removeSelectionInterval(row, row);
  }

  /**
    * Calls clearSelection() of all ListSelectionModels.
    */
  public void clearSelection() {
    for(Enumeration<ListSelectionModel> enumen=listSelectionModels.elements(); enumen.hasMoreElements();) {
      ListSelectionModel lm = (ListSelectionModel)(enumen.nextElement());
      lm.clearSelection();
    }
  }

  /**
    * @return true, if the specified cell is selected.
    */
  public boolean isSelected(int row, int column) {
    ListSelectionModel lsm = getListSelectionModelAt(column);
    return lsm.isSelectedIndex(row);
  }

  /**
    * Returns the ListSelectionModel at the specified column
    * @param index the column
    */
  public ListSelectionModel getListSelectionModelAt(int index) {
	  return (ListSelectionModel)(listSelectionModels.elementAt(index));
  }

  /**
    * Set the number of columns.
    * @param count the number of columns
    */
  public void setColumns(int count) {
    listSelectionModels = new Vector<>();
    for (int i=0; i<count; i++) {
      addColumn();
    }
  }

  /**
    * Add a column to the end of the model.
    */
  protected void addColumn() {
    DefaultListSelectionModel newListModel = new DefaultListSelectionModel();
    listSelectionModels.addElement(newListModel);
    newListModel.addListSelectionListener(this);
  }

  /**
    * Remove last column from model.
    */
  protected void removeColumn() {
    //get last element
    DefaultListSelectionModel removedModel = (DefaultListSelectionModel)listSelectionModels.lastElement();
    removedModel.removeListSelectionListener(this);
    listSelectionModels.removeElement(removedModel);

  }

  /**
    * When the TableModel changes, the TableSelectionModel
    * has to adapt to the new Model. This method is called
    * if a new TableModel is set to the JTable.
    */
  // implements PropertyChangeListener
  public void propertyChange(PropertyChangeEvent evt) {
    if ("model".equals(evt.getPropertyName())) {
      TableModel newModel = (TableModel)(evt.getNewValue());
      setColumns(newModel.getColumnCount());
      TableModel oldModel = (TableModel)(evt.getOldValue());
      if (oldModel != null)
        oldModel.removeTableModelListener(this);
      //TableSelectionModel must be aware of changes in the TableModel
      newModel.addTableModelListener(this);
    }
  }

  /**
    * Add a listener to the list that's notified each time a
    * change to the selection occurs.
    */
  public void addTableSelectionListener(TableSelectionListener l) {
 	  listenerList.add(TableSelectionListener.class, l);
  }

  /**
    * Remove a listener from the list that's notified each time a
    * change to the selection occurs.
    */
  public void removeTableSelectionListener(TableSelectionListener l) {
 	  listenerList.remove(TableSelectionListener.class, l);
  }

  /**
    * Is called when the TableModel changes. If the number of columns
    * had changed this class will adapt to it.
    */
  //implements TableModelListener
  public void tableChanged(TableModelEvent e) {
    TableModel tm = (TableModel)e.getSource();
    int count = listSelectionModels.size();
    int tmCount = tm.getColumnCount();
    //works, because you can't insert columns into a TableModel (only add/romove(?)):
    //if columns were removed from the TableModel
    while (count-- > tmCount) {
      removeColumn();
    }
    //count == tmCount if was in the loop, else count < tmCount
    //if columns were added to the TableModel
    while (tmCount > count++) {
      addColumn();
    }
  }

  /**
    * Is called when the selection of a ListSelectionModel
    * of a column has changed.
    * @see #fireValueChanged(Object source, int firstIndex, int lastIndex, int columnIndex, boolean isAdjusting)
    */
  //implements ListSelectionListener
  public void valueChanged(ListSelectionEvent e) {
    ListSelectionModel lsm = (ListSelectionModel)e.getSource();
    int columnIndex = listSelectionModels.lastIndexOf(lsm);
    if (columnIndex > -1) {
      fireValueChanged(this, e.getFirstIndex(), e.getLastIndex(), columnIndex, e.getValueIsAdjusting());
    }
  }

  /**
    * Notify listeners that we have ended a series of adjustments.
    */
  protected void fireValueChanged(Object source, int firstIndex, int lastIndex, int columnIndex, boolean isAdjusting) {
	  Object[] listeners = listenerList.getListenerList();
	  TableSelectionEvent e = null;

	  for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == TableSelectionListener.class) {
		    if (e == null) {
		      e = new TableSelectionEvent(source, firstIndex, lastIndex, columnIndex, false);
		    } ((TableSelectionListener)listeners[i+1]).handleTableSelection(e, this.myTable);
      }
	  }
  }

  public String toString() {
    String ret = "[\n";
    for (int col=0; col<listSelectionModels.size(); col++) {
      ret += "\'"+col+"\'={";
      ListSelectionModel lsm = getListSelectionModelAt(col);
      int startRow = lsm.getMinSelectionIndex();
      int endRow = lsm.getMaxSelectionIndex();
      for (int row=startRow; row<endRow; row++) {
        if (lsm.isSelectedIndex(row))
          ret += row + ", ";
      }
      if (lsm.isSelectedIndex(endRow))
        ret += endRow;
      ret += "}\n";
    }
    ret += "]";
    return ret;
  }

}
