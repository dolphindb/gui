package com.xxdb.gui.component;

import com.xxdb.DBConnection;
import com.xxdb.data.BasicTable;
import com.xxdb.data.Entity;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class shows a dialog that allows users to add/delete/modify function views.
 */
public class XXDBFunctionViewEditor extends JFrame implements ActionListener {
    private XXDBEditor editor;
    private String[][] functionViewList;
    DefaultTableModel model;
    DBConnection conn;
    JTable table;
    XXDBEditorPane funcPane;
    int selectedRow;

    public XXDBFunctionViewEditor(BasicTable table, DBConnection conn, XXDBEditor editor) throws Exception {
        super("Function Views Manager");

        this.conn = conn;
        this.editor = editor;

        int rows = table.rows();
        functionViewList = new String[rows][2];
        for (int i = 0; i < rows; i++) {
            String name = table.getColumn(0).get(i).getString();
            String body = table.getColumn(1).get(i).getString();
            functionViewList[i][0] = name;
            functionViewList[i][1] = body;
        }

        displayDlg();

    }

    private void refresh() throws Exception {
        BasicTable table = (BasicTable) conn.run("getFunctionViews()");
        int rows = table.rows();
        functionViewList = new String[rows][2];
        for (int i = 0; i < rows; i++) {
            String name = table.getColumn(0).get(i).getString();
            String body = table.getColumn(1).get(i).getString();
            functionViewList[i][0] = name;
            functionViewList[i][1] = body;
        }
        model.setDataVector(functionViewList, new String[] {"name", "body"});
    }

    private void displayDlg() throws Exception {

        JButton addBtn = new JButton("Add");

        JPanel findBtnPane = new JPanel();

        JTextField findInputTxt = new JTextField(30);
        JButton findBtn = new JButton("Find");
        JButton clearBtn = new JButton("Clear");

        findBtnPane.add(new JLabel("Filter: "));
        findBtnPane.add(findInputTxt);
        findBtnPane.add(findBtn);
        findBtnPane.add(clearBtn);

        JPanel tableBtnPanel = new JPanel();

        tableBtnPanel.add(addBtn);
        tableBtnPanel.add(findBtnPane);



        model = new DefaultTableModel(functionViewList, new String[]{"Name", "Body"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.setRowHeight(30);

        JScrollPane jsp = new JScrollPane(table);

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem editItem = new JMenuItem("Edit");
        JMenuItem delItem = new JMenuItem("Delete");
        popupMenu.add(editItem);
        popupMenu.addSeparator();
        popupMenu.add(delItem);


        JButton confirmBtn = new JButton("Close");
        JPanel btnPanel = new JPanel();
        btnPanel.add(confirmBtn);


        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout(20, 10));
        pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        pane.add(tableBtnPanel, BorderLayout.NORTH);
        pane.add(jsp, BorderLayout.CENTER);
        pane.add(btnPanel, BorderLayout.SOUTH);

        this.add(pane);
        this.setSize(700, 400);
        this.setLocationRelativeTo(editor);
        this.setVisible(true);


        funcPane = new XXDBEditorPane(editor);
//        funcTxt = new JTextArea();
//        funcJsp = new JScrollPane(funcTxt);
//        funcJsp.setPreferredSize(new Dimension(500, 400));




        addBtn.addActionListener(actionEvent -> {
            funcPane.clearText();
            int result = JOptionPane.showConfirmDialog(this, funcPane, "Add new function view",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                String defScript = funcPane.getText().trim();
                Pattern p=Pattern.compile("(?<=def )[0-9a-zA-Z_]+");
                Matcher m=p.matcher(defScript);
                if (!m.find()){
                    JOptionPane.showMessageDialog(this, "Invalid Script");
                    return;
                }
                String name = m.group(0);
                System.out.println(name);
                try {
                    conn.run(defScript);
                    conn.run("addFunctionView(" + name + ")");
                    refresh();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, e.getMessage());
                }
            }
        });

        findBtn.addActionListener(actionEvent -> {
            String inputStr = findInputTxt.getText().trim();
            if (!inputStr.isEmpty()) {
                sorter.setRowFilter(RowFilter.regexFilter(inputStr));
            } else {
                sorter.setRowFilter(null);
            }
        });

        clearBtn.addActionListener(actionEvent -> {
            sorter.setRowFilter(null);
            findInputTxt.setText("");
        });

        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2 || SwingUtilities.isRightMouseButton(mouseEvent)) {
                    selectedRow = table.getSelectedRow();
                    popupMenu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
                }
            }

        });

        delItem.addActionListener(this);
        editItem.addActionListener(this);

        confirmBtn.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String action = actionEvent.getActionCommand();
        if (action.equalsIgnoreCase("close")) this.dispose();
        //FIXME: show error when execute modified function with editorPane
        else if (action.equalsIgnoreCase("Edit")) {
            funcPane.setText((String) model.getValueAt(selectedRow, 1));
            int result = JOptionPane.showConfirmDialog(this, funcPane, "Edit function view",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                String defScript = funcPane.getText().trim();
                Pattern p=Pattern.compile("(?<=def )[0-9a-zA-Z_]+");
                Matcher m=p.matcher(defScript);
                if (!m.find()){
                    JOptionPane.showMessageDialog(this, "Invalid Script");
                    return;
                }
                String name = m.group(0);
                try {
                    conn.run("dropFunctionView(\"" + (String) model.getValueAt(selectedRow, 0) + "\")");
                    conn.run(defScript);
                    conn.run("addFunctionView(" + name + ")");
                    refresh();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, e.getMessage());
                }
            }
        }
        else if (action.equalsIgnoreCase("Delete")) {
            String funcName = (String) model.getValueAt(selectedRow, 0);
            int result = JOptionPane.showConfirmDialog(this, "Are you sure to delete " + funcName + "?", "Delete function view", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    conn.run("dropFunctionView(\"" + funcName +"\")");
                    refresh();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, e.getMessage());
                }
            }
        }
    }
}
