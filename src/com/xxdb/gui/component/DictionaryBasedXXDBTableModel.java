package com.xxdb.gui.component;

import java.util.ArrayList;
import java.util.Map;

import com.xxdb.data.BasicAnyVector;
import com.xxdb.data.BasicDictionary;
import com.xxdb.data.BasicEntityFactory;
import com.xxdb.data.Entity;
import com.xxdb.data.Entity.DATA_TYPE;
import com.xxdb.data.Scalar;
import com.xxdb.data.Vector;

public class DictionaryBasedXXDBTableModel extends AbstractXXDBTableModel{
	private static final long serialVersionUID = 1L;
	private Vector keys;
	private Vector values;
	private int startRow;
	private int rows;
	
	public DictionaryBasedXXDBTableModel(BasicDictionary dict){
		super(new ArrayList<TableColumnMeta>());
		try{
			generateKeyAndValue(dict);
		}
		catch(Exception ex){
			throw new IllegalArgumentException("Failed to generate key vector and value vector from BasicDictionary object.");
		}
		updateStructure(generateTableColumn(keys, values));
    	moveToPage(0);
	}
    
    public int getPageCount(){
    	int pageCount =  keys.rows()/ getPageSize() + (keys.rows() % getPageSize() != 0 ? 1 : 0);
    	return pageCount == 0 ? 1 : pageCount; 
    }
    
    public int getCurrentPage(){
    	return startRow/getPageSize();
    }
    
    public void moveToPage(int num){
    	if(num > getPageCount())
    		return;
    	startRow = num * getPageSize();
    	rows = Math.min(getPageSize(), keys.rows() - startRow);
    }
    
    public int getPageSize(){
    	return 1000;
    }
    
    public int getRowCount() {
        return rows;
    }
	
    public Object getValueAt(int row, int col) {
    	if(col==0)
    		return keys.get(startRow + row);
    	else
    		return values.get(startRow + row);
    }
    
    public void setValueAt(Object value, int row, int col) {
        throw new IllegalArgumentException("Read only table model.");
    }
    
    private void generateKeyAndValue(BasicDictionary dict) throws Exception{
    	BasicEntityFactory factory = new BasicEntityFactory();
    	int size = dict.rows();
    	keys = factory.createVectorWithDefaultValue(dict.getKeyDataType(), size);
    	values = factory.createVectorWithDefaultValue(dict.getDataType(), size);
    	int count = 0;
    	if(dict.getDataType() != DATA_TYPE.DT_ANY){
	    	for(Map.Entry<Entity, Entity> entry : dict.entrySet()){
	    		keys.set(count, entry.getKey());
	    		values.set(count, (Scalar)entry.getValue());
	    		++count;
	    	}
    	}
    	else{
    		BasicAnyVector vec= (BasicAnyVector)values;
	    	for(Map.Entry<Entity, Entity> entry : dict.entrySet()){
	    		keys.set(count, entry.getKey());
	    		vec.setEntity(count, entry.getValue());
	    		++count;
	    	}
    	}
    }
}