package com.xxdb.gui.component;

import javax.swing.event.ListSelectionEvent;
import java.util.EventListener;

public interface TableSelectionListener extends EventListener{
	void handleTableSelection(ListSelectionEvent event, XXDBJTable table);
}
