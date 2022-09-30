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

public class BasicTableDeleteDlg extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;

    private JComboBox<String> cboTemporalFormat, cboNumFormat;
    private XXDBJTable table, schemaTable;
    private ArrayBasedXXDBTableModel schemaModel;
    private boolean[] arrTemporal;
    private boolean[] arrNumber;
    private DefaultCellEditor numEditor, temporalEditor;
    private String userAction;

    public BasicTableDeleteDlg(XXDBJTable table) {

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
        setTitle("Confirm Table Deletion");
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

        List<TableColumnMeta> cols = new ArrayList<>();
        cols.add(new TableColumnMeta("name", String.class, DATA_CATEGORY.LITERAL, null, true));
        cols.add(new TableColumnMeta("typeString", String.class, DATA_CATEGORY.LITERAL, null, true));
        cols.add(new TableColumnMeta("typeInt", String.class, DATA_CATEGORY.LITERAL, null, true));
        cols.add(new TableColumnMeta("comment", String.class, DATA_CATEGORY.LITERAL, null, true));

        schemaModel = new ArrayBasedXXDBTableModel(cols){
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
        schemaModel.updateData(getSchema(table));

        schemaTable = new XXDBJTable(schemaModel, null){
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

        schemaTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        final JScrollPane sPanel = new JScrollPane();
        sPanel.setViewportView(schemaTable);
        splitPane.setRightComponent(sPanel);
    }

    public void actionPerformed(ActionEvent event){
        String action;

        action=event.getActionCommand();
        if(action.equalsIgnoreCase("Cancel")){
            userAction = "Cancel";
            this.dispose();
        }
        else if(action.equalsIgnoreCase("OK")){
            userAction = "OK";
            this.dispose();
        }
    }

    public String getUserAction() {
        return userAction;
    }

    private Object[][] getSchema(XXDBJTable table){
        AbstractXXDBTableModel model  = (AbstractXXDBTableModel)table.getModel();
        int rows = table.getRowCount();
        Object[][] arrData = new String[rows][4];

        for(int i=0;i<rows;i++) {
            arrData[i][0] = table.getValueAt(i, 0).toString();
            arrData[i][1] = table.getValueAt(i, 1).toString();
            arrData[i][2] = table.getValueAt(i, 2).toString();
            arrData[i][3] = table.getValueAt(i, 3).toString();
        }
        return arrData;
    }
}
