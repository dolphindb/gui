package com.xxdb.gui.component;

import com.xxdb.DBConnection;

public class DimensionTableBasedDynamicXXDBTableModel extends DfsTableBasedDynamicXXDBTableModel{
	private static final long serialVersionUID = 1L;
	
	public DimensionTableBasedDynamicXXDBTableModel(DBConnection conn, DimensionObjectNode node, int totalRows) {
        super(conn, node, totalRows);
    }
	
    @Override
    public String prepareScript(int startRow, int rows, String name) {
        if(rows<=0){
            throw new IndexOutOfBoundsException("There is no data in selected chunk.");
        }
        String script = "rpc('" + node.getSite() + "', readTabletChunk,'" + node.getChunk() + "','" +
                node.getDbPath() + "','" + node.getPartitionPath() + "','" + node.getTableName() .substring(2) + "'," + startRow + "," + rows + ")";
        return script;
    }
}
