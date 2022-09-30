package com.xxdb.gui.component;

import com.xxdb.data.Matrix;

public class MatrixBasedXXDBTableModel extends AbstractXXDBTableModel{
	private static final long serialVersionUID = 1L;
	private Matrix data;
	private int startRow;
	private int rows;
	private boolean displayRowLabels;
	
	public MatrixBasedXXDBTableModel(Matrix matrix, boolean displayRowLabels) {
		super(generateTableColumn(matrix, displayRowLabels));
		
    	this.data = matrix;
    	this.displayRowLabels = displayRowLabels;
		moveToPage(0);
	}
    
    public int getPageCount(){
    	int pageCount = data.rows()/ getPageSize() + (data.rows() % getPageSize() != 0 ? 1 : 0);
    	return pageCount == 0 ? 1 : pageCount;
    }
    
    public int getCurrentPage(){
    	return startRow/getPageSize();
    }
    
    public void moveToPage(int num){
    	if(num > getPageCount())
    		return;
    	startRow = num * getPageSize();
    	rows = Math.min(getPageSize(), data.rows() - startRow);
    }
    
    public int getPageSize(){
    	return 1000;
    }
    
    public int getRowCount() {
        return rows;
    }
	
    public Object getValueAt(int row, int col) {
    	if(!displayRowLabels)
    		return data.get(startRow + row, col);
    	else if(col==0)
    		return data.getRowLabel(startRow + row);
    	else
    		return data.get(startRow + row, col -1);
    }
    
    public void setValueAt(Object value, int row, int col) {
        throw new IllegalArgumentException("Read only table model.");
    }
    
    public void update(int startRow, int rows){
    	if(startRow<0 || rows<=0 || startRow + rows> data.rows())
			throw new IllegalArgumentException("Invalid startRow and rows.");
		this.startRow = startRow;
		this.rows = rows;
    }
}
