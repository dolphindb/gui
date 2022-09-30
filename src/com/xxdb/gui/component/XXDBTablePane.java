package com.xxdb.gui.component;

import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class XXDBTablePane extends JSplitPane{
	private static final long serialVersionUID = 1L;
	
	private JScrollPane displayTblScrollPane = new JScrollPane();
	private JSlider pageSlider = new JSlider(1,10,1);

	public XXDBTablePane(XXDBJTable table){
		super(JSplitPane.VERTICAL_SPLIT);
		setBottomComponent(displayTblScrollPane);
		setDividerSize(0);	
		pageSlider.setPaintLabels(true);
		pageSlider.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
		        if (!source.getValueIsAdjusting()) {
		            int page = (int)source.getValue() - 1;
		            XXDBJTable table = (XXDBJTable)displayTblScrollPane.getViewport().getView();
		            if(table != null)
		            	table.moveToPage(page);
		        }
			}
		});
		setTable(table);
	}
	
	public XXDBTablePane(){
		this(null);
	}
	
	public void setTable(XXDBJTable table){
		if(table != null){
			AbstractXXDBTableModel model = (AbstractXXDBTableModel)table.getModel();
			if(model.getPageCount()>=2){
				pageSlider.setMaximum(model.getPageCount());
				pageSlider.setValue(1);
				setTopComponent(pageSlider);
			}
			else
				setTopComponent(null);
		}
		displayTblScrollPane.setViewportView(table);
	}
	
	public XXDBJTable getTable(){
		return (XXDBJTable)displayTblScrollPane.getViewport().getView();
	}
}
