package com.xxdb.gui.component;

import com.xxdb.data.*;
import com.xxdb.data.Entity.DATA_TYPE;
import com.xxdb.data.Void;

public class VectorBasedXXDBTableModel extends AbstractXXDBTableModel{
	private static final long serialVersionUID = 1L;
	private Vector data;
	private DATA_TYPE type;
	private int startRow; //it refers to table row, not vector element index
	private int rows; //it refers to table rows, not vector element counts
	private int tableRows;
	private int pageCount;
	private int elementsPerRow;
	private Scalar nullObject;
	
	public VectorBasedXXDBTableModel(Vector vector, int elementsPerRow) {
		super(generateTableColumn(vector, elementsPerRow));
		
    	this.data = vector;
    	this.type = vector.getDataType();
    	this.elementsPerRow = elementsPerRow;
    	if(vector.rows()>0){
	    	if(vector.getDataType() == DATA_TYPE.DT_ANY || vector.getDataCategory() == Entity.DATA_CATEGORY.ARRAY)
	    		nullObject = new Void();
	    	else{
	    		nullObject = (Scalar)vector.get(0);
	    		nullObject.setNull();
	    	}
    	}
    	else
    		nullObject = null;
    	tableRows = data.rows()/elementsPerRow + (data.rows() % elementsPerRow != 0 ? 1 : 0);
    	pageCount = tableRows / getPageSize() + (tableRows % getPageSize() != 0 ? 1 : 0);
    	moveToPage(0);
	}
	
	public VectorBasedXXDBTableModel(BasicSet set, int elementsPerRow){
		this(set.keys(), elementsPerRow);
	}
    
    public int getPageCount(){
    	return pageCount == 0 ? 1 : pageCount;
    }
    
    public int getCurrentPage(){
    	return startRow/getPageSize();
    }
    
    public void moveToPage(int pageIndex){
    	if(pageIndex > getPageCount())
    		return;
    	startRow = pageIndex * getPageSize();
    	rows = Math.min(getPageSize(), tableRows - startRow);
    }
    
    public int getPageSize(){
    	return 1000;
    }
    
    public int getRowCount() {
        return rows;
    }
	
    @Override
    public Object getValueAt(int row, int col) {
    	final int rowOffset = (startRow+row) * elementsPerRow;
		if(col==0)
			return new BasicLong(rowOffset);
		else if(rowOffset + col>data.rows())
			return nullObject;
		else if(type != DATA_TYPE.DT_ANY && type.getValue() < 65)
			return data.get(rowOffset + col - 1);
		else if (type.getValue() >= 65)
			return new BasicString(((BasicArrayVector)data).getString(rowOffset + col - 1));
		else
			return ((BasicAnyVector)data).getEntity(rowOffset + col - 1);
    }
    
    @Override
    public void setValueAt(Object value, int row, int col) {
        throw new IllegalArgumentException("Read only table model.");
    }
    
    @Override
    public boolean isColumnReorderingAllowed(){
    	return false;
    }
    
    @Override
    public boolean isRowReorderingAllowed(){
    	return false;
    }
}
