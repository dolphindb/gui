package com.xxdb.gui.component;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.xxdb.data.BasicLong;
import com.xxdb.data.Entity;
import com.xxdb.data.Matrix;
import com.xxdb.data.Table;
import com.xxdb.data.Vector;

public abstract class AbstractXXDBTableModel extends AbstractTableModel{
	private static final long serialVersionUID = 1L;
	
	protected TableDataChangeListener listener;
	protected List<TableColumnMeta> cols;
    protected int topFixed, bottomFixed;
    private String key;
    
    public abstract int getPageCount();
    public abstract int getCurrentPage();
    public abstract void moveToPage(int num);
    public abstract int getPageSize();
    
    public AbstractXXDBTableModel(List<TableColumnMeta> cols){
    	this.cols = new ArrayList<TableColumnMeta>(cols);
    	this.listener = null;
    }
    
    public String getKey(){
    	return key;
    }
    
    public void setKey(String key){
    	this.key=key;
    }
     
    
    public int getTopFixedRowCount(){
    	return Math.min(topFixed,getRowCount());
    }
    
    public void setTopFixedRowCount(int rows){
    	this.topFixed=rows;
    }
    
    public int getBottomFixedRowCount(){
    	return Math.min(bottomFixed,getRowCount());
    }
    
    public void setBottomFixedRowCount(int rows){
    	this.bottomFixed=rows;
    }
    
    public void setTableDataChangeListener(TableDataChangeListener listener){
    	this.listener=listener;
    }
    
    public TableDataChangeListener getTableDataChangeListener(){
    	return listener;
    }
    
    public void updateStructure(List<TableColumnMeta> cols){
		this.cols.clear();
		this.cols.addAll(cols);

    }
    
    public void setNumberFormat(int column, String format){
    	cols.get(column).setFormat(format);
    }
    
    public String getNumberFormat(int column){
    	return cols.get(column).getFormat();
    }  
        
    public int getColumnCount() {
        return cols.size();
    }

    public String getColumnName(int col) {
        return cols.get(col).getName();
    }
    
    public void setColumnName(int col, String newName){
    	cols.get(col).setName(newName);
    }

    public Class<?> getColumnClass(int c) {
    	return  cols.get(c).getObjectClass();
    }

    public boolean isCellEditable(int row, int col) {
       return cols.get(col).isEditable();
    }
    
    public boolean isColumnReorderingAllowed(){
    	return true;
    }
    
    public boolean isRowReorderingAllowed(){
    	return true;
    }
    
    public boolean showPopupMenu(){
    	return true;
    }
    
    public TableColumnMeta getColumnMeta(int col){
    	return cols.get(col);
    }
    
    public static List<TableColumnMeta> generateTableColumn(Matrix matrix, boolean displayRowLabel){
    	if(displayRowLabel && !matrix.hasRowLabel())
    		throw new IllegalArgumentException("The matrix doesn't contain row labels.");
    	List<TableColumnMeta> cols = new ArrayList<>(matrix.columns() + (displayRowLabel? 1 : 0));
    	if(displayRowLabel)
    		cols.add(new TableColumnMeta("label", matrix.getRowLabels().getElementClass(), matrix.getRowLabels().getDataCategory()));
    	for(int i=0; i<matrix.columns(); ++i){
    		String name;
    		if(matrix.hasColumnLabel())
    			name = matrix.getColumnLabel(i).toString();
    		else
    			name = "col" + (i + 1);
    		cols.add(new TableColumnMeta(name, matrix.getElementClass(), matrix.getDataCategory()));
    	}
    	return cols;
    }
    
    public static List<TableColumnMeta> generateTableColumn(Table table){
    	List<TableColumnMeta> cols = new ArrayList<>(table.columns());
    	for(int i=0; i<table.columns(); ++i){
    		cols.add(new TableColumnMeta(table.getColumnName(i), table.getColumn(i).getElementClass(), table.getColumn(i).getDataCategory()));
    	}
    	return cols;
    }
    
    public static List<TableColumnMeta> generateTableColumn(Vector keys, Vector values){
    	List<TableColumnMeta> cols = new ArrayList<>(2);
   		cols.add(new TableColumnMeta("key", keys.getElementClass(), keys.getDataCategory()));
   		cols.add(new TableColumnMeta("value", values.getElementClass(), values.getDataCategory()));
   		return cols;
    }
    
    public static List<TableColumnMeta> generateTableColumn(Vector data, int columns){
    	List<TableColumnMeta> cols = new ArrayList<>(1 + Math.min(data.rows(), columns));
   		cols.add(new TableColumnMeta("offset", BasicLong.class, Entity.DATA_CATEGORY.INTEGRAL));
   		columns = Math.min(data.rows(), columns);

   		for(int i=0; i<columns; ++i)
   			cols.add(new TableColumnMeta(String.valueOf(i), data.getElementClass(), data.getDataCategory()));
   		return cols;
    }
}
