package com.xxdb.gui.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.xxdb.DBConnection;
import com.xxdb.data.Entity;

public abstract class DynamicXXDBTableModel extends AbstractXXDBTableModel{
	private static final long serialVersionUID = 1L;
	private DBConnection conn;
	private Entity data;
	private String varName;
	private int totalRows;
	private int pageCount;
	private int rows;
	private int currentPage;
	
	public abstract String prepareScript(int startRow, int rows, String name);
	public abstract List<TableColumnMeta> getTableColumnMeta(Entity entity);
	public abstract Object getObject(Entity data, int row, int col);
	
	public DynamicXXDBTableModel(DBConnection conn, String name, int totalRows)  {
		super(new ArrayList<TableColumnMeta>());
		this.conn = conn;
    	this.totalRows = totalRows;
    	this.varName = name;
    	currentPage = -1;
    	pageCount = totalRows / getPageSize() + (totalRows % getPageSize() != 0 ? 1 : 0);
	}
    
    public int getPageCount(){
    	return pageCount == 0 ? 1 : pageCount;
    }
    
    public int getCurrentPage(){
    	return currentPage;
    }
    
    public int getTotalRows(){
    	return totalRows;
    }
    
    public void moveToPage(int num){
    	if(num > pageCount)
    		return;
    	if(conn.isBusy())
    		throw new RuntimeException("The connection to dolphindb is in use. Please try again later.");
    	int startRow = num * getPageSize();
    	rows = Math.min(getPageSize(), totalRows - startRow);
    	String script = prepareScript(startRow, rows, varName);
    	try{
    		data = conn.tryRun(script);
    		if(data == null){
    			throw new RuntimeException("The connection to dolphindb is in use. Please try again later.");
    		}
    		if(currentPage == -1)
    			updateStructure(getTableColumnMeta(data));
    		currentPage = num;
    	}
    	catch(IOException ex){
    		throw new RuntimeException("The data retrieval request returns error message: " + ex.getMessage());
    	}
    }
    
    public int getPageSize(){
    	return 1000;
    }
    
    public int getRowCount() {
        return rows;
    }
    
    public Object getValueAt(int row, int col) {
        return getObject(data, row, col);
    }
    
    public void setValueAt(Object value, int row, int col) {
        throw new IllegalArgumentException("Read only table model.");
    }
}
