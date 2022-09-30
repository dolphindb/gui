package com.xxdb.gui.component;

public class DfsObjectNode {
	private String name;
	private String site;
	private String chunk;
	private String dbPath;
	private String partitionPath;
	private String tableName;
	private String version;

	public DfsObjectNode(String name, String site, String chunk, String dbPath, String partitionPath, String tableName,String version) {
		this.name = name;
		this.chunk = chunk;
		this.site = site;
		this.dbPath = dbPath;
		this.partitionPath = partitionPath;
		this.tableName = tableName;
		this.version = version;
	}
	
	public String toString() {
		return name;
	}
	
	public String getName() {
		return name;
	}
	
	public String getChunk() {
		return chunk;
	}
	
	public String getSite() {
		return site;
	}
	
	public String getVersion(){
		return version;
	}
	public String getDbPath() {
		return dbPath;
	}
	
	public String getPartitionPath() {
		return partitionPath;
	}
	
	public String getTableName() {
		return tableName;
	}
}