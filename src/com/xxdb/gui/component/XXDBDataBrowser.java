package com.xxdb.gui.component;

import java.awt.BorderLayout;
import java.text.DecimalFormat;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.event.ListSelectionEvent;

import com.xxdb.gui.common.Utility;

public class XXDBDataBrowser extends JFrame  implements TableSelectionListener, TablePageSwitchListener{
	private static final long serialVersionUID = 1L;
	
	final XXDBStatusBar statusBar = new XXDBStatusBar();
	final DecimalFormat df = new DecimalFormat("#,##0");
	
	public XXDBDataBrowser(JFrame parent, XXDBTablePane tablePane, int rows, String title){
		
		this.setLocationRelativeTo(null);
		this.setSize(Utility.getScaledSize(600), Utility.getScaledSize(450));
		
		this.setIconImage(new ImageIcon(XXDBDataBrowser.class.getResource("/logo.jpg")).getImage());
		add(tablePane, BorderLayout.CENTER);
		add(statusBar, BorderLayout.SOUTH);
		
		int pageCount = ((AbstractXXDBTableModel)tablePane.getTable().getModel()).getPageCount();
		statusBar.addSection("page", "page: 1/" + df.format(pageCount), Utility.getScaledSize(200));
		statusBar.addSection("row", "rows: " + df.format(rows), Utility.getScaledSize(150));
		statusBar.addSection("sum", "sum: 0", Utility.getScaledSize(175));
		
		tablePane.getTable().setTableSelectionListener(this);
		tablePane.getTable().setPageSwitchListener(this);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setTitle(title);
		pack();
		setVisible(true);
	}

	@Override
	public void handleTableSelection(ListSelectionEvent event, XXDBJTable table){	
		if(event.getValueIsAdjusting())
			return;
		double sum = table.getSelectionSum();
		if(sum != 0 && Math.abs(sum)<0.000001)
			statusBar.setSectionText("sum","sum: " + new DecimalFormat("0.######E0").format(sum));
		else
			statusBar.setSectionText("sum","sum: " + new DecimalFormat("#,##0.######").format(sum));
	}
	
	@Override
	public void handlePageSwitch(int currentPage, int totalPage){
		statusBar.setSectionText("page","page: " + df.format(currentPage+1) +"/" + df.format(totalPage));
	}
}
