package com.xxdb.gui.component;

public class DimensionObjectNode extends DfsObjectNode {

	public DimensionObjectNode(String name, String site, String chunk, String dbPath, String partitionPath, String tableName, String version) {
		super(name,site,chunk,dbPath,partitionPath,tableName,version);
	}

	public String  getLogicalTableName(){
		return getTableName().substring(2);
	}
}