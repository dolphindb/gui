package com.xxdb.gui.component;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.xxdb.data.Entity.DATA_CATEGORY;
import com.xxdb.gui.common.SortedArray;
import com.xxdb.gui.common.Utility;

public class BasicTableSortSetupDlg extends JDialog implements ActionListener{
	private static final long serialVersionUID = 1L;
	
	private JComboBox<String> cboOrientation, cboColumn;
	private XXDBJTable table, keyTable;
	private ArrayBasedXXDBTableModel keyModel;

	public BasicTableSortSetupDlg(XXDBJTable table) {
		
		double adjrate = Utility.getAdjustRate();

		
		AbstractXXDBTableModel model;
		int i;
		
		this.table=table;
		model=(AbstractXXDBTableModel)table.getModel();
		setModalityType(ModalityType.APPLICATION_MODAL);
		setModal(true);
		setResizable(false);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Table Sorting Setup");
		setSize((int)(adjrate*430), (int)(adjrate*300));
		setLocationRelativeTo(null);


		
		final JSplitPane splitPane = new JSplitPane();
		splitPane.setDividerSize(2);
		splitPane.setDividerLocation((int)(40*adjrate));
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		getContentPane().add(splitPane, BorderLayout.CENTER);

		final JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		splitPane.setLeftComponent(panel);

		
		int adjWidth  = (int)(84*adjrate);
		int adjHeight  = (int)(20*adjrate);
		
		final JButton cmdOK = new JButton();
		cmdOK.setText("OK");
		cmdOK.setBounds(241, 10, adjWidth, adjHeight);
		cmdOK.addActionListener(this);
		panel.add(cmdOK,BorderLayout.EAST);

		final JButton cmdCancel = new JButton();
		cmdCancel.setText("Cancel");
		cmdCancel.setBounds(331, 10, adjWidth, adjHeight);
		cmdCancel.addActionListener(this);
		panel.add(cmdCancel,BorderLayout.EAST);

		final JButton cmdClear = new JButton();
		cmdClear.setText("Clear All");
		cmdClear.setBounds(10, 10, adjWidth, adjHeight);
		cmdClear.addActionListener(this);
		panel.add(cmdClear,BorderLayout.WEST);
		
		List<TableColumnMeta> cols = new ArrayList<>();
		cols.add(new TableColumnMeta("Column Name", String.class, DATA_CATEGORY.LITERAL, null, true));
		cols.add(new TableColumnMeta("Order", String.class, DATA_CATEGORY.LITERAL, null, true));
		keyModel = new ArrayBasedXXDBTableModel(cols){
			private static final long serialVersionUID = 1L;
		    
		    public boolean isColumnReorderingAllowed(){
		    	return false;
		    }
		    
		    public boolean isRowReorderingAllowed(){
		    	return false;
		    }
		    
		    public boolean showPopupMenu(){
		    	return false;
		    }
		};
		keyModel.updateData(getSortKeys(this.table));
		keyTable = new XXDBJTable(keyModel, null);
		
		String[] arrHeader=new String[model.getColumnCount()+1];
		arrHeader[0]="";
		for(i=0;i<model.getColumnCount();i++)
			arrHeader[i+1]=String.valueOf(i)+". "+model.getColumnName(i);
		cboColumn = new JComboBox<>();
		cboColumn.setModel(new DefaultComboBoxModel<>(arrHeader));
		cboOrientation = new JComboBox<>();
		cboOrientation.setModel(new DefaultComboBoxModel<>(new String[] {"Ascending", "Descending"}));
		
		keyTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(cboColumn));
		keyTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(cboOrientation));
		keyTable.setRowHeight((int)(adjrate*20));
		keyTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		
		final JScrollPane sPanel = new JScrollPane();
		sPanel.setViewportView(keyTable);
		splitPane.setRightComponent(sPanel);
	}
	
	public void actionPerformed(ActionEvent event){
		String action;
		
		action=event.getActionCommand();
		if(action.equalsIgnoreCase("Cancel")){
			this.dispose();
		}
		else if(action.equalsIgnoreCase("OK")){
			setSortKeys();
			this.dispose();
		}
		else if(action.equalsIgnoreCase("Clear All")){
			keyModel.updateData(new Object[10][2]);
			keyTable.updateUI();
		}
	}
	
	private void setSortKeys(){
		SortedArray<Integer> colList;
		RowSorter<? extends TableModel> sorter;
		ArrayList<SortKey> keyList;
		String str;
		int i, column;
		
		keyList=new ArrayList<>();
		colList=new SortedArray<>();
		for(i=0;i<keyModel.getRowCount();i++){
			str=(String)keyModel.getValueAt(i,0);
			if(str==null || str.length()==0)
				continue;
			column=Integer.parseInt(str.substring(0, str.indexOf('.')));
			str=(String)keyModel.getValueAt(i,1);
			if(str==null || str.length()==0)
				continue;
			if(!colList.add(new Integer(column)))
				continue;
			if(str.equalsIgnoreCase("Ascending"))
				keyList.add(new SortKey(column,SortOrder.ASCENDING));
			else
				keyList.add(new SortKey(column,SortOrder.DESCENDING));
		}
		sorter=table.getRowSorter();
		if(sorter==null){
			sorter=new TableRowSorter<AbstractXXDBTableModel>();
			table.setRowSorter(sorter);
		}
		sorter.setSortKeys(keyList);
	}
	
	private Object[][] getSortKeys(XXDBJTable table){
		List<? extends SortKey> keyList;
		SortKey key;
		Object[][] arrData;
		int i;
		
		try{
			if(table.getRowSorter()==null)
				return new Object[10][2];
			
			keyList=table.getRowSorter().getSortKeys();
			if(keyList==null || keyList.size()==0)
				return new Object[10][2];
			
			arrData=new Object[keyList.size()+10][2];
			for(i=0;i<keyList.size();i++){
				key=(SortKey)keyList.get(i);
				arrData[i][0]=String.valueOf(key.getColumn())+". "+table.getModel().getColumnName(key.getColumn());
				arrData[i][1]=(key.getSortOrder()==SortOrder.ASCENDING?"Ascending":"Descending");
			}
			return arrData;
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
}
