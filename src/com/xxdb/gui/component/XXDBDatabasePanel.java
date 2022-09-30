package com.xxdb.gui.component;

import com.xxdb.DBConnection;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xxdb.data.*;
import com.xxdb.gui.data.*;

public class XXDBDatabasePanel extends JPanel {
	private static final long serialVersionUID = 1L;
    private XXDBEditor parent;
	
	DBConnection conn= null;
    XXDBEditor editor;
    Server currentServer;
    DefaultMutableTreeNode rootNode;
    JPopupMenu refreshMenu;
    JPopupMenu databaseMenu;
    JPopupMenu menu;
    JPopupMenu commentMenu;
    JTree jtree;
    public XXDBDatabasePanel(XXDBEditor main){
        this.editor = main;
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.setBackground(Color.WHITE);
        this.setBorder(BorderFactory.createTitledBorder("Database Explorer"));

        rootNode = new DefaultMutableTreeNode("databases");
        jtree = new JTree(rootNode);
        ToolTipManager.sharedInstance().registerComponent(jtree);
        jtree.setCellRenderer(new HintRender());
        refreshMenu=new JPopupMenu();
        JMenuItem refreshItem=new JMenuItem("refresh");
        refreshMenu.add(refreshItem);
        refreshItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshDatabaseTree();
            }
        });

        databaseMenu = new JPopupMenu();
        JMenuItem newTableItem = new JMenuItem("new table");
//        databaseMenu.add(newTableItem);
        newTableItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                JPopupMenu m = (JPopupMenu)((JMenuItem)e.getSource()).getParent();
                JTree t = (JTree)m.getInvoker();
                DefaultMutableTreeNode n = (DefaultMutableTreeNode)t.getLastSelectedPathComponent();
                createNewTable(n);
            }
        });

        menu = new JPopupMenu();
        JMenuItem menuItem=new JMenuItem("schema");
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                JPopupMenu m = (JPopupMenu)((JMenuItem)e.getSource()).getParent();
                JTree t = (JTree)m.getInvoker();
                DefaultMutableTreeNode n = (DefaultMutableTreeNode)t.getLastSelectedPathComponent();
                showSchema(n);
            }
        });
        JMenuItem colItem = new JMenuItem("add column");
        menu.add(colItem);
        colItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                JPopupMenu m = (JPopupMenu)((JMenuItem)e.getSource()).getParent();
                JTree t = (JTree)m.getInvoker();
                DefaultMutableTreeNode n = (DefaultMutableTreeNode)t.getLastSelectedPathComponent();
                addColumn(n);
            }
        });
        JMenuItem recordItem = new JMenuItem("show first 1000 rows");
        menu.add(recordItem);
        recordItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                JPopupMenu m = (JPopupMenu)((JMenuItem)e.getSource()).getParent();
                JTree t = (JTree)m.getInvoker();
                DefaultMutableTreeNode n = (DefaultMutableTreeNode)t.getLastSelectedPathComponent();
                showRecords(n);
            }
        });
        JMenuItem deleteItem = new JMenuItem("delete");
//        menu.add(deleteItem);
        deleteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                JPopupMenu m = (JPopupMenu)((JMenuItem)e.getSource()).getParent();
                JTree t = (JTree)m.getInvoker();
                DefaultMutableTreeNode n = (DefaultMutableTreeNode)t.getLastSelectedPathComponent();
                confirmDeletion(n);
            }
        });

        //FIXME: dropPartition method
//        JMenuItem partitionItem = new JMenuItem("partitions");
//        menu.add(partitionItem);
//        partitionItem.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                // TODO Auto-generated method stub
//                JPopupMenu m = (JPopupMenu)((JMenuItem)e.getSource()).getParent();
//                JTree t = (JTree)m.getInvoker();
//                DefaultMutableTreeNode n = (DefaultMutableTreeNode)t.getLastSelectedPathComponent();
//                handlePartitions(n);
//            }
//        });

        commentMenu = new JPopupMenu();
        JMenuItem commentItem = new JMenuItem("edit comment");
        commentMenu.add(commentItem);
        commentItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                JPopupMenu m = (JPopupMenu)((JMenuItem)e.getSource()).getParent();
                JTree t = (JTree)m.getInvoker();
                DefaultMutableTreeNode n = (DefaultMutableTreeNode)t.getLastSelectedPathComponent();
                editComment(n);
            }
        });

        jtree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO Auto-generated method stub
                super.mouseClicked(e);
                int x = e.getX();
                int y = e.getY();
                if (e.getClickCount() == 2) {
                    JTree t = (JTree) e.getSource();
                    TreePath selectedPath = t.getSelectionPath();
                    if (!t.hasBeenExpanded(selectedPath)) {
                        DefaultMutableTreeNode obj = (DefaultMutableTreeNode) t.getLastSelectedPathComponent();
                        DatabaseUserObject uo = (DatabaseUserObject) obj.getUserObject();
                        if (uo.Type == DatabaseNodeType.DATABASE) {
                            try {
                                String dbName = uo.DBPath;
                                BasicStringVector tb = (BasicStringVector) conn.run("getTables(database('" + dbName + "'))");
                                for (int j = 0; j < tb.rows(); j++) {
                                    String tbName = tb.getString(j);
                                    DefaultMutableTreeNode tbNode = new DefaultMutableTreeNode(tbName);
                                    tbNode.setUserObject(new DatabaseUserObject(DatabaseNodeType.TABLE, dbName, tbName));
                                    String script = "select * from schema(loadTable('" + dbName + "','" + tbName + "')).colDefs";
                                    try {
                                        BasicTable schemaTb = (BasicTable) conn.run(script);
                                        for (int k = 0; k < schemaTb.rows(); k++) {
                                            String colName = schemaTb.getColumn(0).get(k).getString();
                                            String colType = schemaTb.getColumn(1).get(k).toString();
                                            String colComm = "";
                                            if (schemaTb.columns() > 3)
                                                colComm = schemaTb.getColumn(3).get(k).toString();
                                            String schemaString = colName + String.format(" [%s]", colType);

                                            DefaultMutableTreeNode schemaNode = new DefaultMutableTreeNode(schemaString);
                                            schemaNode.setUserObject(new DatabaseUserObject(DatabaseNodeType.COLUMN, dbName, tbName, colName, colComm, colType));
                                            tbNode.add(schemaNode);
                                        }
                                        obj.add(tbNode);
                                    } catch (IOException e3) {
                                        System.out.println(e3.getMessage());
                                        editor.displayError(e3);
                                    }
                                }
                            } catch (IOException e2) {
                                e2.printStackTrace();
                                editor.displayError(e2);
                            }

                        }
                        t.expandPath(selectedPath);
                    }

                    return;
                }
                if (e.getButton() == MouseEvent.BUTTON3) {
                    JTree t = (JTree)e.getSource();
                    DefaultMutableTreeNode obj = (DefaultMutableTreeNode)t.getLastSelectedPathComponent();
                    if(obj.getUserObject() instanceof String || obj.getUserObject().toString()=="Database") {
                        refreshMenu.show(jtree,x,y);
                        return;
                    }
                    DatabaseUserObject uo = (DatabaseUserObject)obj.getUserObject();
                    if(uo.Type == DatabaseNodeType.TABLE) {
                        menu.show(jtree, x, y);
                        return;
                    }
                    if(uo.Type == DatabaseNodeType.DATABASE) {
                        databaseMenu.show(jtree, x, y);
                        return;
                    }
                    if(uo.Type == DatabaseNodeType.COLUMN) {
                        commentMenu.show(jtree, x, y);
                        return;
                    }
                }
            }
        });

        this.add(jtree);
    }

    //FIXME: code cleanup
    private void createNewTable(DefaultMutableTreeNode n){
        conn = editor.getConnection();
        if(conn==null) return;
        DatabaseUserObject obj =  (DatabaseUserObject)n.getUserObject();
        try {
            Object[] opts = {"Dimensional Table", "Partitioned Table"};
            int response = JOptionPane.showOptionDialog(parent, "Type of table to be created", "Create New Table",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, opts, opts[0]);
            boolean isPartitioned = response == 1;

            JLabel tableNameLabel = new JLabel("Table Name:");
            JTextField tableNameText = new JTextField();
            Box tableNameBox = Box.createHorizontalBox();
            tableNameBox.add(tableNameLabel);
            tableNameBox.add(tableNameText);

            final Object[] columnTypes = new Object[] {
                    "BOOL", "CHAR", "SHORT", "INT",
                    "LONG", "DATE", "MONTH", "TIME",
                    "MINUTE", "SECOND", "DATETIME", "TIMESTAMP",
                    "NANOTIME", "NANOTIMESTAMP", "FLOAT", "DOUBLE",
                    "SYMBOL", "STRING", "UUID", "IPADDR",
                    "INT128"
            };

            DefaultTableModel model = new DefaultTableModel(3, 3);
            String[] tableHeaders = new String[]{"Column Name", "Type", "Comments"};
            model.setColumnIdentifiers(tableHeaders);
            JTable table = new JTable(model);
            table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JComboBox(columnTypes)));

            JButton addButton = new JButton("+");
            JButton removeButton = new JButton("-");
            Box tableBtnBox = Box.createHorizontalBox();
            tableBtnBox.add(addButton);
            tableBtnBox.add(removeButton);

            Box tableBox = Box.createVerticalBox();
            tableBox.add(new JScrollPane(table));
            tableBox.add(tableBtnBox);

            JPanel panel = new JPanel(new BorderLayout(5, 10));
            JComboBox<String>[] colTxtList = null;
            if (isPartitioned) {
                BasicDictionary schema = (BasicDictionary) conn.run("schema(database('" + obj.DBPath + "'))");
                JLabel dbLbl = new JLabel("<html><body>Database: <font color=\"blue\">" + obj.DBPath + "</font></body></html>");
                JLabel partitionTypeLbl = new JLabel("<html><body>Partition Type: <font color=\"blue\">"
                        + schema.get(new BasicString("partitionTypeName")).getString() + "</font></body></html>");
                JLabel partitionSchemaLbl = new JLabel("Partition Schema: ");
                Box partitionSchemaBox = Box.createVerticalBox();
                Box partitionCols = Box.createVerticalBox();
                if (schema.get(new BasicString("partitionTypeName")).isVector()) {
                    BasicStringVector typeVector = (BasicStringVector) schema.get(new BasicString("partitionTypeName"));
                    BasicAnyVector schemaVector = (BasicAnyVector) schema.get(new BasicString("partitionSchema"));
                    colTxtList = new JComboBox[typeVector.rows()];
                    for (int i = 0; i < typeVector.rows(); i++) {
                        JLabel schemaLbl = new JLabel("<html><font color=\"blue\">" + schemaVector.getEntity(i).getString() + "</font></html>");
                        partitionSchemaBox.add(schemaLbl);

                        JLabel typeLbl = new JLabel(typeVector.getString(i) + ": ");
                        colTxtList[i] = new JComboBox<>();
                        Box colBox = Box.createHorizontalBox();
                        colBox.add(typeLbl);
                        colBox.add(colTxtList[i]);
                        partitionCols.add(colBox);
                    }
                } else {
                    partitionSchemaBox.add(new JLabel("<html><font color=\"blue\">" + schema.get(new BasicString("partitionSchema")).getString() + "</font></html>"));
                    JLabel typeLbl = new JLabel(schema.get(new BasicString("partitionTypeName")).getString() + ": ");
                    colTxtList = new JComboBox[1];
                    colTxtList[0] = new JComboBox<>();
                    Box colBox = Box.createHorizontalBox();
                    colBox.add(typeLbl);
                    colBox.add(colTxtList[0]);
                    partitionCols.add(colBox);
                }

                Box dbInfoBox = Box.createVerticalBox();
                dbInfoBox.add(dbLbl);
                dbInfoBox.add(partitionTypeLbl);
                dbInfoBox.add(partitionSchemaLbl);
                dbInfoBox.add(partitionSchemaBox);
                dbInfoBox.add(partitionCols);
                dbInfoBox.setBorder(BorderFactory.createTitledBorder("Partition Setting"));
                dbInfoBox.setPreferredSize(new Dimension(100, 40));
                JPanel partitionPane = new JPanel(new GridLayout(1, 2, 5, 5));
                partitionPane.add(tableBox);
                partitionPane.add(dbInfoBox);

                panel.add(tableNameBox, BorderLayout.NORTH);
                panel.add(partitionPane);
            } else {
               panel.add(tableNameBox, BorderLayout.NORTH);
               panel.add(tableBox, BorderLayout.CENTER);
            }
            JButton confirmBtn = new JButton("Confirm");
            JButton cancelBtn = new JButton("Cancel");
            JPanel btnPane = new JPanel();
            btnPane.add(confirmBtn);
            btnPane.add(cancelBtn);
            panel.add(btnPane, BorderLayout.SOUTH);

            JFrame createTableFrame = new JFrame("Create New Table");
            createTableFrame.add(panel);
            createTableFrame.setSize(500, 250);
            createTableFrame.setVisible(true);

            addButton.addActionListener(actionEvent -> {
                model.addRow(new java.util.Vector());
            });
            removeButton.addActionListener(actionEvent -> model.removeRow(model.getRowCount() - 1));
            JComboBox<String>[] finalColTxtList = colTxtList;
            for (JComboBox<String> comboBox:finalColTxtList) {
                comboBox.addPopupMenuListener(new PopupMenuListener() {
                    @Override
                    public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
                        comboBox.removeAllItems();
                        for (int i = 0; i < model.getRowCount(); i++) {
                            if (model.getValueAt(i, 0) != null && !model.getValueAt(i, 0).equals("")) {
                                comboBox.addItem((String) model.getValueAt(i, 0));
                            }
                        }
                    }

                    @Override
                    public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {
                    }

                    @Override
                    public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {
                    }
                });
            }
            confirmBtn.addActionListener(actionEvent -> {
                try {
                    if (tableNameText.getText().trim().equals(""))
                        throw new Exception("Invalid Input: Must enter a table name");
                    String names = "";
                    String types = "";
                    String comment = "";
                    String partition = "";
                    String[] partitionLv = new String[4];
                    int rowNum = table.getRowCount();
                    for (int i = 0; i < table.getRowCount(); i++) {
                        if (table.getValueAt(i, 0) == null) {
                            rowNum = i;
                            break;
                        } else if (table.getValueAt(i, 1) == null) {
                           throw new Exception("Invalid Input: Must choose a datatype for the column");
                        }
                        names += String.format("\"%s\", ", ((String) table.getValueAt(i, 0)).trim());
                        types += String.format("%s,", ((String) table.getValueAt(i, 1)).trim());
                        if (table.getValueAt(i, 2) != null && !((String) table.getValueAt(i, 2)).trim().equals(""))
                            comment += String.format("%s: \"%s\", ", ((String) table.getValueAt(i, 0)).trim(),
                                    ((String) table.getValueAt(i, 2)).trim());
                    }

                    if (rowNum == 0) throw new Exception("Invalid Input: No column entered");
                    types = types.substring(0, types.length() - 1);
                    conn.run(String.format("t = table(200:10, [%s], [%s])", names.substring(0, names.length() - 2), types));
                    conn.run("db = database('" + obj.DBPath + "')");
                    if (isPartitioned) {
                        for (JComboBox comboBox : finalColTxtList) {
                            partition += "`" + comboBox.getSelectedItem().toString();
                        }
                        conn.run(String.format("pt = createPartitionedTable(db, t, '%s', %s)",
                                tableNameText.getText().trim(), partition));
                    } else
                        conn.run("pt = createTable(db, t, '" + tableNameText.getText().trim() + "')");
                    if (!comment.equals("")) {
                        comment = "setColumnComment(pt, {" + comment.substring(0, comment.length() - 2) + "})";
                        conn.run(comment);
                    }
                    refreshDatabaseTree();
                    editor.displayMessage("Table created successfully.");
                    conn.run("lt = loadTable(db, '" + tableNameText.getText().trim() + "')");
                    conn.run("schema(lt)");
                    createTableFrame.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                    ex.printStackTrace();
                }
            });
            cancelBtn.addActionListener(actionEvent -> createTableFrame.dispose());
        } catch (Exception ex) {
            editor.displayError(ex);
        }
  }

    private void showSchema(DefaultMutableTreeNode n){
        conn = editor.getConnection();
        if(conn==null) return;

        DatabaseUserObject obj =  (DatabaseUserObject)n.getUserObject();
        try {
            Table schema = (Table) conn.run("select * from schema(loadTable('" + obj.DBPath + "','" + obj.TableName + "')).colDefs");
            XXDBJTable displayTable = new XXDBJTable(new TableBasedXXDBTableModel(schema), null, null);
            XXDBTablePane pane = new XXDBTablePane(displayTable);
            new XXDBDataBrowser(editor, pane, schema.rows(), "Schema of " + obj.TableName);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addColumn(DefaultMutableTreeNode n) {
        conn = editor.getConnection();
        if (conn == null) {
            return;
        }
        DatabaseUserObject obj =  (DatabaseUserObject)n.getUserObject();
        try {
            final String[] dataTypes = new String[] {
                    "BOOL", "CHAR", "SHORT", "INT",
                    "LONG", "DATE", "MONTH", "TIME",
                    "MINUTE", "SECOND", "DATETIME", "TIMESTAMP",
                    "NANOTIME", "NANOTIMESTAMP", "FLOAT", "DOUBLE",
                    "SYMBOL", "STRING", "UUID", "IPADDR",
                    "INT128"
            };

            JLabel namelbl = new JLabel("Enter column name:");
            JTextField nametxt = new JTextField();
            JLabel typelbl = new JLabel("Enter column type:");
            JComboBox<String> typecombo = new JComboBox<>(dataTypes);

            JPanel pane = new JPanel(new GridLayout(2, 2, 10, 5));
            pane.add(namelbl);
            pane.add(nametxt);
            pane.add(typelbl);
            pane.add(typecombo);

            int result = JOptionPane.showConfirmDialog(parent, pane, "Add new column",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String colName = nametxt.getText().trim();
                String colType = (String) typecombo.getSelectedItem();
                if (colName.equals("")|| colType == null) {
                    editor.displayMessage("Action cancelled: column name cannot be null");
                    return;
                }
                conn.run("db=database('" + obj.DBPath + "')");
                conn.run("pt=loadTable(db, '" + obj.TableName + "')");
                conn.run("addColumn(pt, '" + colName + "'," + colType + ")");
                refreshDatabaseTree();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showRecords(DefaultMutableTreeNode n) {
        conn = editor.getConnection();
        if(conn==null) return;
        DatabaseUserObject obj =  (DatabaseUserObject)n.getUserObject();
        try {
            String tableName = obj.TableName;
            editor.executeCode("db = database('" + obj.DBPath + "');"
                    + "pt = loadTable(db, '" + tableName + "');"
                    + "select top 1000 * from pt");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void confirmDeletion(DefaultMutableTreeNode n){
        conn = editor.getConnection();
        if(conn==null) return;
        DatabaseUserObject obj =  (DatabaseUserObject)n.getUserObject();
        try {
            conn.run("db=database('" + obj.DBPath + "')");
            conn.run("pt = loadTable(db, \"" + obj.TableName + "\")");
            String rcnt = conn.run("exec count(*) from pt").getString();
            JLabel namelbl = new JLabel("<html><body>Table Name: <font color=\"blue\">" + obj.TableName + "</font></body></html>");
            JLabel dblbl = new JLabel("<html><body>Database path: <font color=\"blue\">" + obj.DBPath + "</font></body></html>");
            JLabel rcntlbl = new JLabel("<html><body>Row count: <font color=\"blue\">" + rcnt + "</font></body></html>");

            JPanel lblPane = new JPanel(new GridLayout(3, 1));
            lblPane.add(namelbl);
            lblPane.add(dblbl);
            lblPane.add(rcntlbl);

            BasicTable schema = (BasicTable) conn.run("schema(loadTable('" + obj.DBPath + "','" + obj.TableName + "')).colDefs");
            XXDBJTable displayTable = new XXDBJTable(new TableBasedXXDBTableModel(schema), null, null);
            JScrollPane jsp = new JScrollPane(displayTable);
            jsp.setPreferredSize(new Dimension(300, 150));

            JPanel panel = new JPanel(new GridLayout(2, 1));
            panel.add(lblPane);
            panel.add(jsp);

            int result = JOptionPane.showConfirmDialog(parent, panel, "Confirm Deletion",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                if (JOptionPane.showConfirmDialog(parent, new JLabel("There're "+ rcnt + " rows in the table. Are you sure to delete all of them?"),
                        "Deletion Confirm", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                    conn.run("dropTable(db, '" + obj.TableName + "')");
                    refreshDatabaseTree();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handlePartitions(DefaultMutableTreeNode n) {
        conn = editor.getConnection();
        if(conn==null) return;
        DatabaseUserObject obj =  (DatabaseUserObject)n.getUserObject();
        try {
            System.out.println("drop partition");
            String chunckPath = obj.DBPath.replaceFirst("dfs://", "/");
            System.out.println("select path from getTabletsMeta(\"" + chunckPath + "%\", `" + obj.TableName + ") where rowNum > 0");
            Table plist = (Table) conn.run("select path from getTabletsMeta(\"" + chunckPath + "%\", `" + obj.TableName + ") where rowNum > 0");
            //convert to vector;
            if (plist != null) {
                Object[][] data = new Object[plist.rows()][2];
                Pattern pattern = Pattern.compile("(?<=" + chunckPath + ").*");
                String line = "";
                Matcher m;
                for (int i = 0; i < plist.rows(); i++) {
                    data[i][0] = false;
                    line = plist.getColumn(0).get(i).getString();
                    m = pattern.matcher(line);
                    if (m.find())
                        data[i][1] = m.group(0);
                }
                DefaultTableModel model = new DefaultTableModel(data, new String[]{"Select", "Partition Path"}) {
                    @Override
                    public Class<?> getColumnClass(int i) {
                        if (i == 0) return Boolean.class;
                        return super.getColumnClass(i);
                    }
                };
                JTable table = new JTable(model);
                table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JCheckBox()));
                JScrollPane panel = new JScrollPane(table);
                int result = JOptionPane.showConfirmDialog(parent, panel, "Select to delete partitions",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    for (int i = 0; i < data.length; i++) {
                        if (table.getValueAt(i, 0) != null && (Boolean) table.getValueAt(i, 0))
                            //conn.run("dropPartition(database(\"" + obj.DBPath +"\"), \"" + table.getValueAt(i, 1) + "\")");
                            System.out.println("dropPartition(database(\"" + obj.DBPath +"\"), \"" + table.getValueAt(i, 1) + "\")");
                    }

                    editor.displayMessage("Delete partition(s) successfully");
                } else
                    editor.displayMessage("Action cancelled");
            }
            else
                throw new Exception("no valid partition found");
        } catch (Exception ex) {
            ex.printStackTrace();
            editor.displayMessage("Fail to drop partition: " + ex.getMessage());
        }
    }

    private void editComment(DefaultMutableTreeNode n) {
        conn = editor.getConnection();
        if(conn==null) return;
        DatabaseUserObject obj = (DatabaseUserObject)n.getUserObject();
        try {
            Object edit = JOptionPane.showInputDialog(parent, "Edit comment for the selected column", "Edit Comment", JOptionPane.QUESTION_MESSAGE, null, null, obj.ColumnComment
            );
            if (edit == null) {
                return;
            }
            String newComment = edit.toString().trim();
            conn.run("db=database('" + obj.DBPath + "')");
            conn.run("pt=loadTable(db, '" + obj.TableName + "')");
            conn.run("setColumnComment(pt, {" + obj.ColumnName + ":'" + newComment + "'})");
            refreshDatabaseTree();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setServer(Server s){
        this.currentServer = s;
    }
    public void refreshDatabaseTree(){
        rootNode.removeAllChildren();
        this.conn = editor.getConnection();
        if(conn==null) return;

        try{
            if(this.conn.isConnected() == false){
                //this.conn.connect(svr.getHost(),svr.getPort(),svr.getUsername(),svr.getPassword());
                return;
            }
            BasicStringVector db = (BasicStringVector)conn.run("getClusterDFSDatabases()");
            for (int i=0;i <db.rows();i++ ) {
                String dbName = db.getString(i);
                DefaultMutableTreeNode dbNode = new DefaultMutableTreeNode(dbName);
                dbNode.setUserObject(new DatabaseUserObject(DatabaseNodeType.DATABASE, dbName));
                rootNode.add(dbNode);
            }
            jtree.expandPath(new TreePath(rootNode));
            jtree.updateUI();
        }catch(IOException ex){
            this.editor.displayMessage("Failed to get database schema : "  + ex.getMessage());
        }
    }


    class HintRender extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = 1L;

		public Component getTreeCellRendererComponent(
                JTree tree,
                Object value,
                boolean sel,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {

            super.getTreeCellRendererComponent(
                    tree, value, sel,
                    expanded, leaf, row,
                    hasFocus);
            if (leaf) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
                if(node.getUserObject() instanceof DatabaseUserObject){
                    DatabaseUserObject obj = (DatabaseUserObject)node.getUserObject();
                    if(obj.Type==DatabaseNodeType.COLUMN)
                        setToolTipText(obj.ColumnComment);
                }

            } else {
                setToolTipText(null); //no tool tip
            }

            return this;
        }
    }
    private class DatabaseUserObject{
        public DatabaseNodeType Type;
        public String DBPath;
        public String TableName;
        public String ColumnName;
        public String ColumnComment;
        public String ColumnType;

        @Override
        public String toString() {
            switch(this.Type){
                case DATABASE:
                    return DBPath;
                case COLUMN:
                    return ColumnName + "[" + ColumnType + "]";
                case TABLE:
                    return TableName;
            }
            return  "";
        }
        private DatabaseUserObject(DatabaseNodeType type, String dbpath, String tbname, String colname, String colcomment, String coltype){
            this.Type = type;
            this.DBPath = dbpath;
            if(tbname!=null) this.TableName = tbname;
            if(colname!=null) this.ColumnName = colname;
            if(colcomment!=null) this.ColumnComment = colcomment;
            if(coltype!=null) this.ColumnType = coltype;
        }
        private DatabaseUserObject(DatabaseNodeType type, String dbpath){
            this.Type = type;
            this.DBPath = dbpath;
            this.TableName = "";
            this.ColumnName = "";
            this.ColumnComment = "";
            this.ColumnType = "";
        }

        private DatabaseUserObject(DatabaseNodeType type, String dbpath, String tbname){
            this.Type = type;
            this.DBPath = dbpath;
            this.TableName = tbname;
            this.ColumnName = "";
            this.ColumnComment = "";
            this.ColumnType = "";
        }

    }
    private enum DatabaseNodeType{
        DATABASE,
        TABLE,
        COLUMN
    }
}
