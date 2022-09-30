package com.xxdb.gui.component;

import java.util.List;

import com.xxdb.DBConnection;
import com.xxdb.data.*;
import com.xxdb.data.Entity.DATA_TYPE;

public class VectorBasedDynamicXXDBTableModel extends DynamicXXDBTableModel{
	private static final long serialVersionUID = 1L;
	private boolean isSet;
	private int elementsPerRow;
	private int totalRows;
	private Entity nullObject;
	private Entity.DATA_TYPE type;

	public VectorBasedDynamicXXDBTableModel(DBConnection conn, String name, int totalRows, int elementsPerRow, Entity.DATA_TYPE type, boolean isSet){
		super(conn, name, totalRows/elementsPerRow +(totalRows % elementsPerRow > 0 ? 1 : 0));
		this.isSet = isSet;
		this.type = type;
		this.elementsPerRow = elementsPerRow;
		this.totalRows =totalRows;
		if (type.getValue() < 65){
			nullObject = new BasicEntityFactory().createScalarWithDefaultValue(type == DATA_TYPE.DT_ANY ? DATA_TYPE.DT_STRING : type);
			((Scalar)nullObject).setNull();
		}else {
			nullObject = new BasicEntityFactory().createVectorWithDefaultValue(DATA_TYPE.valueOf(type.getValue() - 64), elementsPerRow);
		}
    	moveToPage(0);
	}

	@Override
	public String prepareScript(int startRow, int rows, String name) {
		String script = name;
		if(isSet)
			script += ".keys()";
		if (type.getValue() < 65)
			return script +"[" + startRow*elementsPerRow+ ":" + Math.min(totalRows, (startRow + rows)*elementsPerRow) + "]";
		else
			return script +"[" + startRow*elementsPerRow+ ":" + Math.min(elementsPerRow, (startRow + rows)*elementsPerRow) + "]";
	}

	@Override
	public List<TableColumnMeta> getTableColumnMeta(Entity entity) {
		return generateTableColumn((Vector)entity, elementsPerRow);
	}

	@Override
	public Object getObject(Entity data, int row, int col) {
		if(col==0)
			return new BasicLong((getCurrentPage()*getPageSize()+row) * elementsPerRow);
		else if(row*elementsPerRow + col>((Vector)data).rows())
			return nullObject;
		else if(type != DATA_TYPE.DT_ANY && type.getValue() < 65)
			return ((Vector)data).get(row*elementsPerRow + col - 1);
		else if (type.getValue() >= 65)
			return new BasicString(((BasicArrayVector)data).getString(row*elementsPerRow + col - 1));
		else
			return ((BasicAnyVector)data).getEntity(row*elementsPerRow + col - 1);
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
