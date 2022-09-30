package com.xxdb.gui.component;

import com.xxdb.data.Entity;
import com.xxdb.data.Entity.DATA_CATEGORY;

public class TableColumnMeta {
	private String name;
	private boolean editable;
	private Class<?> cls;
	private String format;
	private Entity.DATA_CATEGORY category;
	
	public TableColumnMeta(String name, Class<?> cls, DATA_CATEGORY category, String format, boolean editable){
		this.name = name;
		this.cls = cls;
		this.format = format;
		this.editable = editable;
		this.category = category;
	}
	
	public TableColumnMeta(String name, Class<?> cls, DATA_CATEGORY category){
		this(name, cls, category, null, false);
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public Class<?> getObjectClass(){
		return cls;
	}
	
	public boolean isEditable(){
		return editable;
	}
	
	public void setEditable(boolean editable){
		this.editable = editable;
	}
	
	public String getFormat(){
		return format;
	}
	
	public boolean isFormatSet(){
		return format!=null && !format.isEmpty();
	}
	
	public void setFormat(String format){
		this.format = format;
	}
	
	public boolean isNumber(){
		return category==DATA_CATEGORY.FLOATING || category==DATA_CATEGORY.INTEGRAL;
	}

	public boolean isInteger(){
		return category==DATA_CATEGORY.INTEGRAL;
	}

	public boolean isTemporal(){
		return category==DATA_CATEGORY.TEMPORAL;
	}

	public  boolean isTuple() {return category==DATA_CATEGORY.MIXED || category == DATA_CATEGORY.ARRAY;}
}
