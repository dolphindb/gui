package com.xxdb.gui.component;

import java.util.List;

public class ArrayBasedXXDBTableModel extends AbstractXXDBTableModel{
	private static final long serialVersionUID = 1L;
	protected Object[][] data;
	
	public ArrayBasedXXDBTableModel(List<TableColumnMeta> cols) {
		super(cols);
    	data=new Object[0][];
	}

    public void updateData(Object[][] data){
    	if(data==null || data.length==0)
    		this.data=new Object[0][cols.size()];
    	else if(getColumnCount()==data[0].length)
    		this.data=data;
    }
    
    public int getRowCount() {
        return data.length;
    }
	
    public Object getValueAt(int row, int col) {
        return data[row][col];
    }
    
    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value;
        if(listener!=null && cols.get(col).isEditable())
        	listener.tableCellChanged(this, row, col, value);
    }
    
    public int getPageCount(){
    	return 1;
    }
    
    public int getCurrentPage(){
    	return 0;
    }
    
    public void moveToPage(int num){
    }
    
    public int getPageSize(){
    	return getRowCount();
    }
}
