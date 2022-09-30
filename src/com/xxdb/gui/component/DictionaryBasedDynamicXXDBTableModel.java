package com.xxdb.gui.component;

import java.util.List;

import com.xxdb.DBConnection;
import com.xxdb.data.BasicAnyVector;
import com.xxdb.data.Entity;
import com.xxdb.data.Vector;
import com.xxdb.data.Entity.DATA_TYPE;

public class DictionaryBasedDynamicXXDBTableModel  extends DynamicXXDBTableModel{
	private static final long serialVersionUID = 1L;

	public DictionaryBasedDynamicXXDBTableModel(DBConnection conn, String name, int totalRows){
		super(conn, name, totalRows);
    	moveToPage(0);
	}

	@Override
	public String prepareScript(int startRow, int rows, String name) {
		return "[" + name + ".keys()[" +startRow + ":" + (startRow + rows) + "]," + name + ".values()[" +startRow + ":" + (startRow + rows) + "]]";
	}

	@Override
	public List<TableColumnMeta> getTableColumnMeta(Entity entity) {
		return generateTableColumn((Vector)(((BasicAnyVector)entity).getEntity(0)), (Vector)(((BasicAnyVector)entity).getEntity(1)));
	}

	@Override
	public Object getObject(Entity data, int row, int col) {
		Entity entity = ((BasicAnyVector)data).getEntity(col);
		if(entity.getDataType() == DATA_TYPE.DT_ANY)
			return ((BasicAnyVector)entity).getEntity(row);
		else
			return ((Vector)entity).get(row);
	}
}
