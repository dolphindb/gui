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
import javax.swing.table.TableCellEditor;
import com.xxdb.data.Entity.DATA_CATEGORY;
import com.xxdb.gui.common.Utility;

public class BasicTableFormatSetupDlg extends JDialog implements ActionListener{
	private static final long serialVersionUID = 1L;
	
	private JComboBox<String> cboTemporalFormat, cboNumFormat;
	private XXDBJTable table, formatTable;
	private ArrayBasedXXDBTableModel formatModel;
	private boolean[] arrTemporal;
	private boolean[] arrNumber;
	private DefaultCellEditor numEditor, temporalEditor;

	public BasicTableFormatSetupDlg(XXDBJTable table) {
		
		double adjrate = Utility.getAdjustRate();
		
		this.table=table;
		AbstractXXDBTableModel model=(AbstractXXDBTableModel)table.getModel();
		arrTemporal = new boolean[table.getColumnCount()];
		arrNumber = new boolean[table.getColumnCount()];
		for(int i=0; i<table.getColumnCount(); ++i){
			int modelIndex = table.convertColumnIndexToModel(i);
			arrTemporal[i] = model.getColumnMeta(modelIndex).isTemporal();
			arrNumber[i] = model.getColumnMeta(modelIndex).isNumber();
		}
		
		setModalityType(ModalityType.APPLICATION_MODAL);
		setModal(true);
		setResizable(true);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Table Column Format Setup");
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
		cmdClear.setText("Reset");
		cmdClear.setBounds(10, 10, adjWidth, adjHeight);
		cmdClear.addActionListener(this);
		panel.add(cmdClear,BorderLayout.WEST);
		
		List<TableColumnMeta> cols = new ArrayList<>();
		cols.add(new TableColumnMeta("Column Name", String.class, DATA_CATEGORY.LITERAL, null, true));
		cols.add(new TableColumnMeta("Format", String.class, DATA_CATEGORY.LITERAL, null, true));
		formatModel = new ArrayBasedXXDBTableModel(cols){
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int row, int col) {
		       if(col==0)
		    	   return false;
		       else
		    	   return arrTemporal[row] || arrNumber[row];
		    }
		    
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
		formatModel.updateData(getFormat(table));
		
		cboTemporalFormat = new JComboBox<>();
		cboTemporalFormat.setEditable(true);
		cboTemporalFormat.setModel(new DefaultComboBoxModel<>(new String[] {"", "yyyy.MM.dd", "MM/dd/yyyy", "yyyy.MM.dd HH:mm:ss.SSS", "HH:mm:ss.SSS", "HH:mm:ss", "HH:mm", "yyyy/MM", "yyyy.MM.dd HH:mm:ss.SSSSSSSSS"}));
		cboNumFormat = new JComboBox<>();
		cboNumFormat.setEditable(true);
		cboNumFormat.setModel(new DefaultComboBoxModel<>(new String[] {"", "0", "#,##0.###", "#,##0.###;(#,##0.###)", "0.0#####E0", "0.######", "0.#########"}));
		numEditor = new DefaultCellEditor(cboNumFormat){
			private static final long serialVersionUID = 1L;
			public Object getCellEditorValue() {
		       return cboNumFormat.getEditor().getItem();
		    }
		};
		temporalEditor = new DefaultCellEditor(cboTemporalFormat){
			private static final long serialVersionUID = 1L;
			public Object getCellEditorValue() {
		       return cboTemporalFormat.getEditor().getItem();
		    }
		};
		
		formatTable = new XXDBJTable(formatModel, null){
			private static final long serialVersionUID = 1L;

			public TableCellEditor getCellEditor(int row, int column) {
				if(arrNumber[row])
					return numEditor;
				else if(arrTemporal[row])
					return temporalEditor;
				else
					return null;
		    }
		};
		//formatTable.setRowHeight(20);
		
		formatTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		
		final JScrollPane sPanel = new JScrollPane();
		sPanel.setViewportView(formatTable);
		splitPane.setRightComponent(sPanel);
	}
	
	public void actionPerformed(ActionEvent event){
		String action;
		
		action=event.getActionCommand();
		if(action.equalsIgnoreCase("Cancel")){
			this.dispose();
		}
		else if(action.equalsIgnoreCase("OK")){
			setFormat();
			this.dispose();
		}
		else if(action.equalsIgnoreCase("Reset")){
			formatModel.updateData(getFormat(table));
			formatTable.updateUI();
		}
	}
	
	private void setFormat(){
		AbstractXXDBTableModel model = (AbstractXXDBTableModel)table.getModel();
		for(int i=0; i<formatModel.getRowCount(); ++i){
			int modelIndex = table.convertColumnIndexToModel(i);
			String format = (String)formatModel.getValueAt(i, 1);
			if(format == null)
				continue;
			model.getColumnMeta(modelIndex).setFormat(format.trim());
		}
		table.updateUI();
	}
	
	private Object[][] getFormat(XXDBJTable table){
		AbstractXXDBTableModel model  = (AbstractXXDBTableModel)table.getModel();
		int columns = table.getColumnCount();
		Object[][] arrData = new String[columns][2];

		for(int i=0;i<columns;i++){
			int modelIndex = table.convertColumnIndexToModel(i);
			arrData[i][0] = model.getColumnMeta(modelIndex).getName();
			arrData[i][1] = model.getColumnMeta(modelIndex).getFormat();
		}
		return arrData;
	}
}
