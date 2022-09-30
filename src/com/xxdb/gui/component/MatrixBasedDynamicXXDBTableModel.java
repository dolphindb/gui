package com.xxdb.gui.component;

import java.util.List;

import com.xxdb.DBConnection;
import com.xxdb.data.Entity;
import com.xxdb.data.Matrix;

public class MatrixBasedDynamicXXDBTableModel  extends DynamicXXDBTableModel{
	private static final long serialVersionUID = 1L;
	private boolean displayRowLabels;

	public MatrixBasedDynamicXXDBTableModel(DBConnection conn, String name, int totalRows){
		super(conn, name, totalRows);
    	moveToPage(0);
	}

	@Override
	public String prepareScript(int startRow, int rows, String name) {
		return name +"[" + startRow + ":" + (startRow + rows) + ",]";
	}

	@Override
	public List<TableColumnMeta> getTableColumnMeta(Entity entity) {
		displayRowLabels = ((Matrix)entity).hasRowLabel();
		return generateTableColumn((Matrix)entity, displayRowLabels);
	}

	@Override
	public Object getObject(Entity data, int row, int col) {
		if(!displayRowLabels)
    		return ((Matrix)data).get(row, col);
    	else if(col==0)
    		return ((Matrix)data).getRowLabel(row);
    	else
    		return ((Matrix)data).get(row, col -1);
	}
}