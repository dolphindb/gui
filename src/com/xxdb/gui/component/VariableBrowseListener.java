package com.xxdb.gui.component;

import com.xxdb.data.Entity.DATA_FORM;
import com.xxdb.data.Entity.DATA_TYPE;

public interface VariableBrowseListener {
	void handleVariableBrowse(String name, DATA_FORM form, DATA_TYPE type, int rows, int columns, long bytes);
}
