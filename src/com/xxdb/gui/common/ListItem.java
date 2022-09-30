package com.xxdb.gui.common;

public class ListItem {
	private int value;
	private String text;
	private String dtype;
	private boolean selected;
	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getDataType() {
		return dtype;
	}

	public void setDataType(String text) {
		this.dtype = text;
	}
	
	public void setSelected(boolean val){
		selected = val;
	}
	
	public boolean getSelected() {
		return selected;
	}
	public ListItem(int value, String text, String dType) {
		this.value = value;
		this.text = text;
		this.dtype = dType;
	}

	public void reverseChecked(){
		this.selected = !this.selected;
	}
	
	public String toString() {
		return this.text;
	}
	
	public String toDebugString() {
		return "value: " + String.valueOf(this.value) + " text: " + this.text + " dataType : " + this.dtype;
	}
	
	public boolean equals(Object obj) {
        if (obj instanceof ListItem) {
        	return this.value == ((ListItem) obj).getValue() && this.text.equals(((ListItem) obj).getText());
        }else{
        	return false;
        }
    }
        
    public int hashCode() {
        return this.text.length();
    }
}
