package com.xxdb.gui.component;

import java.util.List;

import com.xxdb.DBConnection;
import com.xxdb.data.Entity;
import com.xxdb.data.Table;

public class DfsTableBasedDynamicXXDBTableModel extends DynamicXXDBTableModel {
	private static final long serialVersionUID = 1L;

	protected DfsObjectNode node;
	public DfsTableBasedDynamicXXDBTableModel(DBConnection conn, DfsObjectNode node, int totalRows) {
		super(conn, node.getName(), totalRows);
		this.node = node;
		moveToPage(0);
	}

	@Override
	public String prepareScript(int startRow, int rows, String name) {
		String site = '"' + node.getSite() + '"';
		String chunk = '"' + node.getChunk() + '"';
		String dbPath = '"' + node.getDbPath() + '"';
		String partitionPath = '"' + node.getPartitionPath() + '"';
		String tableName = '"' + node.getTableName() + '"';
		if(rows<=0){
			throw new IndexOutOfBoundsException("There is no data in selected chunk.");
		}
		String script = "rpc(" + site + ", readTabletChunk" + "," + chunk + "," +
				dbPath + "," + partitionPath + "," + tableName + "," + startRow + "," + rows + ")";
		return script;
	}

	@Override
	public List<TableColumnMeta> getTableColumnMeta(Entity entity) {
		return generateTableColumn((Table)entity);
	}

	@Override
	public Object getObject(Entity data, int row, int col) {
		return ((Table)data).getColumn(col).get(row);
	}

}
