package com.xxdb.gui.component;

import java.util.List;

import com.xxdb.DBConnection;
import com.xxdb.data.Entity;
import com.xxdb.data.Table;

public class TableBasedDynamicXXDBTableModel extends DynamicXXDBTableModel{
	private static final long serialVersionUID = 1L;

	public TableBasedDynamicXXDBTableModel(DBConnection conn, String name, int totalRows){
		super(conn, name, totalRows);
    	moveToPage(0);
	}

	@Override
	public String prepareScript(int startRow, int rows, String name) {
		return name +"[" + startRow + ":" + (startRow + rows) + "]";
	}

	@Override
	public List<TableColumnMeta> getTableColumnMeta(Entity entity) {
		return generateTableColumn((Table)entity);
	}

	@Override
	public Object getObject(Entity data, int row, int col) {
		if (((Table)data).getColumn(col).getDataType().getValue() < 65)
			return ((Table)data).getColumn(col).get(row);
		else
			return ((Table)data).getColumn(col).getString(row);
	}
}
