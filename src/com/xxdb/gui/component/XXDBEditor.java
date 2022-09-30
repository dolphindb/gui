package com.xxdb.gui.component;

import com.xxdb.DBConnection;
import com.xxdb.data.*;
import com.xxdb.data.Entity.DATA_FORM;
import com.xxdb.data.Entity.DATA_TYPE;
import com.xxdb.gui.common.DolphinSyntaxItem;
import com.xxdb.gui.common.FileLog;
import com.xxdb.gui.common.Utility;
import com.xxdb.gui.data.*;
import com.xxdb.io.ProgressListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.Plot;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

//在类的声明中，通过关键字extends来创建一个类的子类。一个类通过关键字implements声明自己使用一个或者多个接口。
public class XXDBEditor extends JFrame
        implements ActionListener, TableSelectionListener, TablePageSwitchListener, VariableBrowseListener {
    private static final long serialVersionUID = 1L;
    private static final String build = "2022.09.28 V1.30.20.1";
//    用public修饰的static成员变量和成员方法本质是全局变量和全局方法，当声明它类的对象时，不生成static变量的副本，而是类的所有实例共享同一个static变量
    public static final int SYNC_FAIL = 0;
    public static final int SYNC_SUCCESS = 1;
    public static final int SYNC_NOT_MODIFIED = 2;
    public static final int DEFAULT_PORT = 8848;


    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static String SERVER_OS = "linux";
    public static String SERVER_VERSION = "";
    private JPanel topPanel = new JPanel();
    private JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    private XXDBJTabbedPane tabbedWorkSpace = new XXDBJTabbedPane(this);
    private JSplitPane curWorkbook = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

    private JTabbedPane jExplorerPane = new JTabbedPane();
    private XXDBDatabasePanel databasePanel =  new XXDBDatabasePanel(this);
    private String mainTitle = "DolphinDB Workspace";

    private JMenuBar menuBar = new JMenuBar();
    private JMenu file = new JMenu("File");

    private JMenuItem openFile = XXDBMenuBar.createMenuItem(
            this.file, XXDBMenuBar.ITEM_PLAIN, "Open", null,
            KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            "Open a file on filesystem");
    private JMenuItem saveFile = XXDBMenuBar.createMenuItem(
            this.file, XXDBMenuBar.ITEM_PLAIN, "Save", null,
            KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            "Save a file on filesystem");
    private JMenuItem saveAsFile = XXDBMenuBar.createMenuItem(
            this.file, XXDBMenuBar.ITEM_PLAIN, "Save As", "SaveAs", null, null, "Save a file on filesystem");

    private JMenuItem switchWS = XXDBMenuBar.createMenuItem(
            this.file, XXDBMenuBar.ITEM_PLAIN, "Change Workspace...", "SwitchWS", null,
            null, "Switch to another workspace");

    private JMenuItem mnuPreferences = XXDBMenuBar.createMenuItem(
            this.file, XXDBMenuBar.ITEM_PLAIN, "Preferences", "Prefs", null,
            null, "Preferences");

    private JMenuItem exit = XXDBMenuBar.createMenuItem(
            this.file, XXDBMenuBar.ITEM_PLAIN, "Exit", null,
            KeyStroke.getKeyStroke(KeyEvent.VK_F4, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            "Close the session");

    private JMenu edit = new JMenu("Edit");
    private JMenuItem mnuUndo = XXDBMenuBar.createMenuItem(
            this.edit, XXDBMenuBar.ITEM_PLAIN, "Undo", null,
            KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            null);
    private JMenuItem mnuRedo = XXDBMenuBar.createMenuItem(
            this.edit, XXDBMenuBar.ITEM_PLAIN, "Redo", null,
            KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            null);
    private JMenuItem mnuCut = XXDBMenuBar.createMenuItem(
            this.edit, XXDBMenuBar.ITEM_PLAIN, "Cut", null,
            KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            null);
    private JMenuItem mnuCopy = XXDBMenuBar.createMenuItem(
            this.edit, XXDBMenuBar.ITEM_PLAIN, "Copy", null,
            KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            null);
    private JMenuItem mnuPaste = XXDBMenuBar.createMenuItem(
            this.edit, XXDBMenuBar.ITEM_PLAIN, "Paste", null,
            KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            null);
    private JMenuItem mnuFindReplace = XXDBMenuBar.createMenuItem(
            this.edit, XXDBMenuBar.ITEM_PLAIN, "Find/Replace...", "FindReplace", null,
            KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            null);

    private JMenu run = new JMenu("Run");
    private JMenuItem execute = XXDBMenuBar.createMenuItem(
            this.file, XXDBMenuBar.ITEM_PLAIN, "Execute", null,
            KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            "Execute selected commands");

    private JMenuItem cancel = XXDBMenuBar.createMenuItem(
            this.file, XXDBMenuBar.ITEM_PLAIN, "Cancel", null,
            null, "Cancel running job");

    private JMenuItem exportTable = XXDBMenuBar.createMenuItem(
            this.file, XXDBMenuBar.ITEM_PLAIN, "Export table", "ExTable",  null,
            null, "Export table to CSV");

    private JMenu mnuServer = new JMenu("Server");
    private JMenuItem mnuAddServer = XXDBMenuBar.createMenuItem(
            this.mnuServer, XXDBMenuBar.ITEM_PLAIN, "Add Server", "AddServer", null,
            null, "Add a DolphinDB server");
    private JMenuItem mnuEditServer = XXDBMenuBar.createMenuItem(
            this.mnuServer, XXDBMenuBar.ITEM_PLAIN, "Edit Server", "EditServer", null,
            null, "Delete or edit existing servers");

    private JMenu mnuSys = new JMenu("System");
//    private JMenuItem mnuFuncView = XXDBMenuBar.createMenuItem(
//            this.mnuSys, XXDBMenuBar.ITEM_PLAIN, "Function View", "FuncView", null,
//            null, "Edit function view");
    private JMenuItem mnuJobMgr = XXDBMenuBar.createMenuItem(
            this.mnuSys, XXDBMenuBar.ITEM_PLAIN, "Manage Jobs..", "JobMgr", null,
            null, "Manage submit/scheduled/console jobs");
//    private JMenuItem mnuUserAdmin = XXDBMenuBar.createMenuItem(
//            this.mnuSys, XXDBMenuBar.ITEM_PLAIN, "Users & Privileges", "UserAdmin", null,
//            null, "Administrate users and groups");
//    //FIXME: tooltip -- yitong
//    private JMenuItem mnuStreamMonitor = XXDBMenuBar.createMenuItem(
//            this.mnuSys, XXDBMenuBar.ITEM_PLAIN, "Stream Processing", "MonitorStream", null,
//            null, "Monitor sub/pub connections and working threads");


    private JMenu mnuHelp = new JMenu("Help");
    private JMenuItem mnuSysHelp = XXDBMenuBar.createMenuItem(
            this.mnuHelp, XXDBMenuBar.ITEM_PLAIN, "Developer Documentation", "SysHelp", null,
            null, "Developer Documentation");
    private JMenuItem mnuGuiHelp = XXDBMenuBar.createMenuItem(
            this.mnuHelp, XXDBMenuBar.ITEM_PLAIN, "GUI Workbench Documentation", "GuiHelp", null,
            null, "GUI Workbench Documentation");

    public JToolBar toolBar = new JToolBar();
    private JButton executeIcon;
    private JLabel runningIcon;
    private JComboBox<String> cboServer;
    public JComboBox<String> cboLanguage;
    private String language = "DolphinDB";

    // Add server
    private XXDBJDialog dialogServerForm;
    private XXDBServerForm serverForm;
    private JButton insertServer = new JButton("Add");
    private JButton cancelBtn = new JButton("Cancel");
    private JButton testServer = new JButton("Test");

    private XXDBJDialog dialogFindReplace = null;
    private FindReplacePanel findReplacePanel;

    private JSplitPane display = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    private JTabbedPane tabbedPane = new JTabbedPane();
    private JScrollPane displayTextScrollPane;
    private XXDBTextArea displayText = new XXDBTextArea();
    private XXDBTablePane dataBrowser = new XXDBTablePane();
    private XXDBVariablePane displayVariables;
    private final XXDBStatusBar statusBar = new XXDBStatusBar();
    public Workspace sd = null;
    private DBConnection conn_ = null;
    private Server server = null;
    private JPanel pad = new JPanel();
    private XXDBRSyntaxTextArea textArea = null;
    private XXDBFileTree explorerTree = new XXDBFileTree(new File(Utility.getLastUsedWorkspace()), this, tabbedWorkSpace);
    double adjrate = Utility.getAdjustRate();
    RealTimeDataChartDlg frm = null;
    ExportTableDlg frm_ex = null;
    // XXDBEditor类的构造方法 构造方法的特点：方法名和类名相同，没有返回值，可以重载
    public XXDBEditor() {
//        dimension是Java的一个类，封装了一个构件的高度和宽度
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        pack();
        this.setSize(new Double(0.75 * screenSize.width).intValue(),
                new Double(0.75 * screenSize.height).intValue());
        setLocation((screenSize.width - getSize().width) / 2,
                (screenSize.height - getSize().height) / 2);

//        获取相对ClassPath的资源,此方式需要注意class要使用自己定义的类，因为自定义的类所在路径是项目内的路径，才能得到根目录路径。
        this.setIconImage(new ImageIcon(XXDBEditor.class.getResource("/logo.jpg")).getImage());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                handleWindowClosing();
            }
        });

        topPanel.setLayout(new BorderLayout());
        this.getContentPane().add(topPanel, BorderLayout.CENTER);

        //Standard toolbar for icons
        toolBar.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        createToolbarIcon("Open", "/open.png", "Open");
        toolBar.addSeparator();
        createToolbarIcon("Save", "/save.png", "Save");
        toolBar.addSeparator();
        createToolbarIcon("SaveAll", "/saveAll.png", "Save All");
        toolBar.addSeparator();
        createToolbarIcon("Undo", "/undo.png", "Undo");
        toolBar.addSeparator();
        createToolbarIcon("Cut", "/cut.png", "Cut");
        toolBar.addSeparator();
        createToolbarIcon("Copy", "/copy.png", "Copy");
        toolBar.addSeparator();
        createToolbarIcon("Paste", "/paste.png", "Paste");
        toolBar.addSeparator();
        createToolbarIcon("FindReplace", "/search.png", "Find/Replace");
        toolBar.addSeparator();
        createToolbarIcon("Login", "/login.png", "Login");
        toolBar.addSeparator();
        createToolbarIcon("ExTable", "/export.png", "Export Table");
        toolBar.addSeparator();
        executeIcon = createToolbarIcon("Execute", "/execute.png", "Execute");
        ImageIcon runningImage = new ImageIcon(XXDBEditor.class.getResource("/running.gif"));
        int imageHeight = executeIcon.getIcon().getIconHeight();
        runningImage.setImage(runningImage.getImage().getScaledInstance(imageHeight, imageHeight, Image.SCALE_DEFAULT));
        runningIcon = new JLabel(runningImage);
        runningIcon.setVisible(false);
        toolBar.add(runningIcon);
        toolBar.addSeparator();
        createToolbarIcon("Cancel", "/delete.png", "Cancel");
        toolBar.addSeparator();

        insertServer.setActionCommand("InsertServer");
        insertServer.addActionListener(this);
        cancelBtn.setActionCommand("cancelServer");
        cancelBtn.addActionListener(this);
        testServer.setActionCommand("TestServer");
        testServer.addActionListener(this);
        
        cboServer = new JComboBox<String>();
        cboServer.setEditable(false);
        cboServer.setMaximumSize(new Dimension((int) (adjrate * 150), imageHeight + 12));
        cboServer.setActionCommand("ActiveServer");
        cboServer.addActionListener(this);
        toolBar.add(cboServer);
        toolBar.addSeparator();
        
        cboLanguage = new JComboBox<String>();
        cboLanguage.addItem("DolphinDB");
        cboLanguage.addItem("Python");
        cboLanguage.setEditable(false);
        cboLanguage.setMaximumSize(new Dimension((int) (adjrate * 100), imageHeight + 12));
        cboLanguage.setActionCommand("SelectLanguage");
        cboLanguage.addActionListener(this);
        if (
            // always show language dropdown
            Boolean.valueOf(
                Utility.getUserSetting(
                    PreferenceSettingItem.AlwaysShowLanguageDropdown.toString(), "false"
                )
            )
        )
            toolBar.add(cboLanguage);
        
        topPanel.add(toolBar, BorderLayout.NORTH);
        topPanel.add(mainSplit, BorderLayout.CENTER);
        JScrollPane explorerPane = new JScrollPane();
        explorerPane.setViewportView(explorerTree);
        explorerPane.setBackground(Color.WHITE);
        explorerPane.setBorder(BorderFactory.createTitledBorder("Project Explorer"));

        JScrollPane databasePane = new JScrollPane();
        databasePane.setViewportView(databasePanel);
        databasePane.setBackground(Color.WHITE);

        jExplorerPane.addTab("Project", explorerPane);
        jExplorerPane.addTab("Database",databasePane);

        jExplorerPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JTabbedPane source = (JTabbedPane) e.getSource();
                String title = source.getTitleAt(source.getSelectedIndex());
                if(title == "Database"){
                    databasePanel.refreshDatabaseTree();
                }
            }
        });

        mainSplit.add(jExplorerPane);
        mainSplit.add(curWorkbook);
        mainSplit.setDividerLocation((int) (getSize().width * 0.2));

        curWorkbook.setLeftComponent(pad);
        curWorkbook.setRightComponent(display);
        curWorkbook.setDividerLocation((int) (this.getSize().getHeight() * 0.75 * 0.8 * 0.67));
        curWorkbook.setResizeWeight(0.75);

        pad.setLayout(new GridLayout(1, 1));
        pad.add(tabbedWorkSpace, BorderLayout.CENTER);
        if (tabbedWorkSpace.getSelectedComponent() != null)
            textArea = ((XXDBRTextScrollPane) tabbedWorkSpace.getSelectedComponent()).getTextArea();
        ChangeListener changeListener = new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
                XXDBRTextScrollPane pane = (XXDBRTextScrollPane) sourceTabbedPane.getSelectedComponent();
                if (pane != null) resetWorkbook((XXDBRSyntaxTextArea) pane.getTextArea());
            }
        };
        tabbedWorkSpace.addChangeListener(changeListener);

        displayText.setEditable(false);
        displayText.setBackground(Color.WHITE);
        displayTextScrollPane = new JScrollPane(displayText);
        //displayTextScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        //set horizontal when needed
        displayTextScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        tabbedPane.add("Log", displayTextScrollPane);
        tabbedPane.add("Data Browser", dataBrowser);

        displayVariables = new XXDBVariablePane(conn_, this, this);
        display.setResizeWeight(0.8);
        display.setDividerLocation((int) (this.getSize().width * 0.75 * 0.8));
        display.setLeftComponent(tabbedPane);
        display.setRightComponent(displayVariables);

        statusBar.addSection("server", "No Server Selected", (int) (200 * adjrate));
        statusBar.addSection("timeconsume", "", (int) (200 * adjrate));
        statusBar.addSection("page", "page: 0/0", (int) (150 * adjrate));
        statusBar.addSection("row", "rows: 0", (int) (200 * adjrate));
        statusBar.addSection("sum", "sum: 0", (int) (175 * adjrate));
        statusBar.addSection("license", "", (int) (300 * adjrate));
        statusBar.addSection("build", "gui version:" + build, (int) (220 * adjrate));
        //session
        statusBar.addSection("session", "", (int) (300 * adjrate));
        add(statusBar, BorderLayout.SOUTH);
        explorerTree.setCellRenderer(new XXDBTreeCellRenderer());
        sd = explorerTree.getWorkspace();
        this.setJMenuBar(menuBar);
        this.menuBar.add(this.file);
        this.menuBar.add(this.edit);
        this.menuBar.add(this.run);
        this.menuBar.add(this.mnuServer);
        this.menuBar.add(this.mnuSys);
        this.menuBar.add(this.mnuHelp);
        // File Menu
        this.openFile.addActionListener(this);
        this.file.add(this.openFile);
        this.saveFile.addActionListener(this);
        this.file.add(this.saveFile);
        this.saveAsFile.addActionListener(this);
        this.file.add(this.saveAsFile);
        this.file.addSeparator();
        this.switchWS.addActionListener(this);
        this.file.add(switchWS);
        this.file.addSeparator();
        this.mnuPreferences.addActionListener(this);
        this.file.add(mnuPreferences);
        this.file.addSeparator();
        this.exit.addActionListener(this);
        this.file.add(this.exit);

        // Edit Menu
        this.mnuUndo.addActionListener(this);
        this.mnuRedo.addActionListener(this);
        this.edit.add(mnuUndo);
        this.edit.addSeparator();
        this.mnuCut.addActionListener(this);
        this.edit.add(mnuCut);
        this.mnuCopy.addActionListener(this);
        this.edit.add(mnuCopy);
        this.mnuPaste.addActionListener(this);
        this.edit.add(mnuPaste);
        this.edit.addSeparator();
        this.mnuFindReplace.addActionListener(this);
        this.edit.add(this.mnuFindReplace);

        //Server Menu
        this.mnuAddServer.addActionListener(this);
        this.mnuServer.add(mnuAddServer);
        this.mnuServer.addSeparator();
        this.mnuEditServer.addActionListener(this);
        this.mnuServer.add(mnuEditServer);

        // Add server menu items
        Server activeServer = sd.getActiveServer();
        for (final Server s : sd.getServers()) {
            listServer(s, s.equals(activeServer));
        }
        //Execute Menu
        this.execute.addActionListener(this);
        this.cancel.addActionListener(this);
        this.run.add(this.execute);
        this.run.add(this.cancel);
        this.run.addSeparator();
        this.exportTable.addActionListener(this);
        this.run.add(exportTable);

        //Sys Menu
//        this.mnuFuncView.addActionListener(this);
//        this.mnuSys.add(this.mnuFuncView);
//        this.mnuSys.addSeparator();
        this.mnuJobMgr.addActionListener(this);
        this.mnuSys.add(mnuJobMgr);
//        this.mnuSys.addSeparator();
//        this.mnuUserAdmin.addActionListener(this);
//        this.mnuSys.add(mnuUserAdmin);
//        this.mnuSys.addSeparator();
//        this.mnuStreamMonitor.addActionListener(this);
//        this.mnuSys.add(this.mnuStreamMonitor);

        //Help Menu
        this.mnuSysHelp.addActionListener(this);
        this.mnuHelp.add(mnuSysHelp);
        this.mnuHelp.addSeparator();
        this.mnuGuiHelp.addActionListener(this);
        this.mnuHelp.add(mnuGuiHelp);

        setTitle();
        revalidate();
    }

    public Server getServer() {
        return server;
    }

    private void setServer(Server s) {
        server = s;
        sd.addServer(s, true);
    }

    public void listServer(final Server s, boolean active) {
        cboServer.addItem(s.getName());
        if (active) {
            cboServer.setSelectedItem(s.getName());
            setServer(s);
            statusBar.setSectionText("server", s.getHost() + ":" + s.getPort());
        }
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        try {
            if (command.equalsIgnoreCase("Exit")) {
                handleWindowClosing();
            } else if (command.equalsIgnoreCase("SwitchWS")) {
                handleSwitchWorkspace();
            } else if (command.equalsIgnoreCase("Prefs")) {
                handlePreferences();
            } else if (command.equalsIgnoreCase("Open")) {
                XXDBRSyntaxTextArea newbook = new XXDBRSyntaxTextArea(this);
                if (XXDBMenuBar.openFile(this, newbook)) {
                    resetWorkbook(newbook);
                    XXDBRTextScrollPane textScrollPane = new XXDBRTextScrollPane(newbook);
                    tabbedWorkSpace.addWorkBook(newbook.getName(), textScrollPane);
                    tabbedWorkSpace.setSelectedComponent(textScrollPane);
                }
            } else if (command.equalsIgnoreCase("Save")) {
                XXDBMenuBar.saveFile(this, this.textArea);
                setTitle();
            } else if (command.equalsIgnoreCase("SaveAs")) {
                XXDBMenuBar.saveAsFile(this, this.textArea);
                setTitle();
            } else if (command.equalsIgnoreCase("ExTable")) {
                openExportTableDialog();
            } else if (command.equalsIgnoreCase("FuncView")) {
                try {
                    if (conn_ == null) throw new Exception("Fail to open function views editor: No connection to server");
                    Entity obj = this.conn_.run("getFunctionViews()");
                    new XXDBFunctionViewEditor((BasicTable) obj, conn_, this);
                } catch (Exception ex) {
                    displayError(ex);
                }
            } else if (command.equalsIgnoreCase("JobMgr")) {
                try {
                    new XXDBJobManager(this);
                } catch (Exception ex) {
                    displayError(ex);
                }
            } else if (command.equalsIgnoreCase("UserAdmin")) {
                new XXDBUserAdministration(this);
            } else if (command.equalsIgnoreCase("MonitorStream")) {
                new XXDBStreamMonitor(this);
            } else if (command.equalsIgnoreCase("AddServer")) {
                JPanel form = new JPanel();
                form.setLayout(new BorderLayout());
                serverForm = new XXDBServerForm();
                form.add(serverForm, BorderLayout.CENTER);

                JPanel buttonGroup = new JPanel();
                buttonGroup.setLayout(new FlowLayout(FlowLayout.CENTER));

                buttonGroup.add(insertServer);
                buttonGroup.add(cancelBtn);
                buttonGroup.add(testServer);

                form.add(buttonGroup, BorderLayout.SOUTH);
                if (dialogServerForm == null || dialogServerForm.getOpenState() == false) {
                    dialogServerForm = new XXDBJDialog(this, "Add Server", form);
                    dialogServerForm.setApplicationModal();    // disable the rest components
                }
            } else if (command.equalsIgnoreCase("FindReplace")) {
                if (findReplacePanel == null) {
                    findReplacePanel = new FindReplacePanel(textArea);
                } else {
                    findReplacePanel.setTextArea(textArea);
                    findReplacePanel.setDefaultSearchText();
                }

                if (dialogFindReplace == null || dialogFindReplace.getOpenState() == false)
                    dialogFindReplace = new XXDBJDialog(this, "Find/Replace", findReplacePanel);
                findReplacePanel.getSearchField().selectAll();
            } else if (command.equalsIgnoreCase("InsertServer")) {
                Server newServer = serverForm.getServer();
                if (newServer != null) {
                    if (sd.getServer(newServer.getName()) != null) {
                        displayMessage(newServer.getName() + " Already exist");
                        return;
                    }
                    sd.addServer(newServer, server == null);
                    listServer(newServer, server == null);
                    sd.saveWorkspace(explorerTree.getWorkspaceFile());
                    dialogServerForm.dispose();
                }
            } else if (command.equalsIgnoreCase("EditServer")) {
                if (cboServer.getItemCount() == 0)
                    return;
                List<String> servers = new ArrayList<String>(cboServer.getItemCount());
                for (int i = 0; i < cboServer.getItemCount(); ++i)
                    servers.add(cboServer.getItemAt(i));
                XXDBServerEditor serverEditor = new XXDBServerEditor(this, sd, explorerTree.getWorkspaceFile(), cboServer);
                serverEditor.setVisible(true);
            } else if (command.equalsIgnoreCase("ActiveServer")) {
                String activeServer = (String) cboServer.getSelectedItem();
                if (activeServer != null) {
                    disconnectServer();
                    Server s = sd.getServer(activeServer);
                   // databasePanel.refreshDatabaseTree(s);
                    setServer(s);
                    statusBar.setSectionText("server", s.getHost() + ":" + s.getPort());
                }
                SERVER_VERSION = "";
                SERVER_OS = "";
                syntaxUpdatedCount = 0;
            } else if (command.equalsIgnoreCase("SelectLanguage")) {
                String language = (String)cboLanguage.getSelectedItem();
                if (!this.language.equals(language)) {
                    this.language = language;
                    disconnectServer();
                    this.conn_ = getDBConnection();
                }
            } else if (command.equalsIgnoreCase("TestServer")) {
                Server newServer = serverForm.getServer();
                if (newServer != null) {
                    testServer(true, newServer);
                }
            } else if (command.equalsIgnoreCase("Login")) {
                if (this.conn_ == null) {
                    this.conn_ = getDBConnection();
                }

                if (this.conn_.isConnected() == false) {
                    try {
                        if (!this.conn_.connect(this.getServer().getHost(), this.getServer().getPort())) {
                            JOptionPane.showMessageDialog(this, "connect server failed!", "warning", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(this, e1.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                LoginPanel frm = new LoginPanel(this);
                frm.setVisible(true);

            } else if (command.equalsIgnoreCase("Execute")) {
                execute();
            } else if (command.equalsIgnoreCase("Cancel")) {
            	cancelJob();
            } else if (command.equalsIgnoreCase("Cut")) {
                textArea.cut();
            } else if (command.equalsIgnoreCase("Copy")) {
                textArea.copy();
            } else if (command.equalsIgnoreCase("Paste")) {
                textArea.paste();
            } else if (command.equalsIgnoreCase("Undo")) {
                textArea.undoLastAction();
            } else if (command.equalsIgnoreCase("Redo")) {
                textArea.redoLastAction();
            } else if (command.equalsIgnoreCase("SaveAll")) {
                saveAllOpenDocuments();
            } else if (command.equalsIgnoreCase("RealtimePlotting")) {
                openRealTimePanelDialog();
            } else if (command.equalsIgnoreCase("cancelServer")) {
                dialogServerForm.dispose();
            }else if (command.equalsIgnoreCase("SysHelp")) {
                if(isZh()){
                    openUrl("https://www.dolphindb.cn/cn/help/index.html");
                }else{
                    openUrl("https://www.dolphindb.com/help/index.html");
                }
            }else if (command.equalsIgnoreCase("GuiHelp")) {
                if(isZh()){
                    openUrl("https://www.dolphindb.cn/cn/gui/index.html");
                } else {
                    openUrl("https://www.dolphindb.com/gui_help/index.html");
                }
            }
        } catch (Exception ex) {
            displayError(ex);
        }
    }
    
    private boolean isZh () {
        return System.getProperty("user.country").toLowerCase().equals("cn") && 
            System.getProperty("user.language").toLowerCase().equals("zh");
    }

    private void openUrl(String url) throws IOException {
        try {
            URI link = new URI(url);
            java.awt.Desktop.getDesktop().browse(link);
        } catch (IOException ex) {
            displayError(ex);
        } catch (URISyntaxException ex) {
            displayError(ex);
        }
    }
    public void execute() {
        if(this.textArea==null){
            displayMessage("Please open a script file.");
            return;
        }
        try {
            Boolean isAllowAutosave = PreferenceSettingUtil.getAutosave();
            if (!this.textArea.isSaved() && isAllowAutosave) {
                XXDBMenuBar.saveFile(this, this.textArea);
                setTitle();
            }
        } catch (XXDBException e) {
            e.printStackTrace();
        }

        String textToExec;
        int firstLine = 1;
        if (textArea.getSelectedText() == null)
            textToExec = textArea.getCaretLineText();
        else {
            textToExec = textArea.getSelectedText();
            try{
                firstLine = textArea.getLineOfOffset(textArea.getSelectionStart()) + 1;
            }
            catch(Exception ex){}
        }

        String openFilePath = textArea.getOpenFilePath();
        if (openFilePath != null && !openFilePath.isEmpty()) {
            if (textArea.getNode() != null)
                openFilePath = "file://" + getRemoteNodePath((ProjectNode) textArea.getNode().getParent());
            else //textArea.getNode() == null in mac os
                openFilePath = "file://" + openFilePath.substring(0,openFilePath.lastIndexOf(FILE_SEPARATOR)-1);
        }
        if(firstLine > 1 || (openFilePath != null && !openFilePath.isEmpty())){
            StringBuilder attr = new StringBuilder();
            //If SERVER_VERSION is empty, it means that server has not been connected
            if(firstLine > 1 && SERVER_VERSION.compareTo("1.10.0") >= 0) {
                attr.append("line://");
                    attr.append(firstLine);
            }
        	if(openFilePath != null && !openFilePath.isEmpty()){
        		if(attr.length() > 0) attr.append(',');
        		attr.append(openFilePath);
        	}
        	attr.append("\n");
        	attr.append(textToExec);
        	textToExec = attr.toString();
        }
        executeCode(textToExec);
    }
    
    public void cancelJob() {
    	if (executeIcon.isVisible())
    		return;
		DBConnection tmpConn = getDBConnection();
    	try {
        	String sessionID = conn_.getSessionID();
    		tmpConn.connect(conn_.getHostName(), conn_.getPort());
    		BasicStringVector jobIds = (BasicStringVector) tmpConn.run("exec rootJobId from getConsoleJobs() where sessionId = " + sessionID);
    		int jobCount = jobIds.rows();
    		for (int i = 0; i < jobCount; i++) {
    			try {
    				tmpConn.run("cancelConsoleJob(\"" + jobIds.getString(i) + "\")");
    			}
    			catch (Exception e) {
                    displayError(e);
    			}
    		}
    	}
    	catch (Exception ex) {
			displayMessage("Could not cancel currently running job.");
		}
    	finally {
			tmpConn.close();
		}
    }


    public void setDialogOpenState(XXDBJDialog dialog, boolean opened) {
        dialog.setOpenState(opened);
    }

    public void setTitle() {
        if (textArea == null)
            this.setTitle(mainTitle);
        else {
            this.setTitle(mainTitle + "  [" + textArea.getOpenFilePath()
                    + (textArea.isSaved() ? "" : "*") + "]");

            int selectedIndex = tabbedWorkSpace.getSelectedIndex();
            if (selectedIndex >= 0) {
                this.tabbedWorkSpace.setTitleAt(selectedIndex,
                        new String((textArea.isSaved() ? "" : "*") + textArea.getFileName())); // Extra space to accommodate unsaved mark *
                revalidate();
            }
        }
    }

    public void resetWorkbook(XXDBRSyntaxTextArea wb) {
        textArea = wb;
        if (findReplacePanel != null && wb != null) {
            findReplacePanel.setTextArea(wb);
        }
        setTitle();
    }

    public void displayError(Exception ex) {
        ex.printStackTrace();
        String msg = ex.getMessage();
        if (msg == null || msg.isEmpty()) {
            msg = ex.toString();
        }
        int scIndex = 0;
        if (msg.contains("Server response") && (msg.contains(" script: ") || msg.contains(" function: "))){
            if (msg.contains(" script: ")){
                scIndex = msg.indexOf(" script: ");
            }else if (msg.contains(" function: ")){
                scIndex = msg.indexOf(" function: ");
            }
            msg = msg.substring(0, scIndex);
        }

        msg = msg.replaceAll("<", "&lt;");
        msg = msg.replaceAll(
            "RefId:\\s*(\\w+)",
            (
                "<span class='link'><a target='_blank' href='" + 
                (isZh() ? "https://dolphindb.cn/cn/" : "https://dolphindb.com/") + 
                "help/" +
                (SERVER_VERSION.startsWith("1.30") ? "130/" : "") +
                "ErrorCodeList/$1/index.html'>RefId: $1</a></span>"
            )
        );
        StringBuilder sb = new StringBuilder();
        int paneWidth = displayText.getWidth();
        FontMetrics fm = displayText.getFontMetrics(displayText.getFont());
        for (int i = 0, cnt = 0; i < msg.length(); i++){
            if((cnt += fm.charWidth(msg.charAt(i))) >= paneWidth){
                cnt = 0;
                sb.append("\n");
                sb.append(msg.charAt(i));
                continue;
            }
            sb.append(msg.charAt(i));
        }
        this.displayText.append("<pre><span style=\"white-space:pre-wrap;\" class=error>" + sb.toString() + "</span></pre><br/>");
        this.displayText.append("<b><br/></b><br/>");
        this.displayText.setCaretPosition(displayText.getStyledDocument().getLength());
        //show log when error
        this.tabbedPane.setSelectedIndex(0);
    }

    public void displayMessage(String msg) {
        displayMessage(msg, "plain");
    }
    public void displayMessage(String msg, String styleClass) {
        if (styleClass != null) {
            msg = msg.replaceAll("<", "&lt;");
            msg = "<pre><span class=" + styleClass + ">" + msg + "</span></pre>";
        }
        this.displayText.append(msg + "<br/>");
        this.displayText.setCaretPosition(displayText.getStyledDocument().getLength());
    }


    public boolean testServer(boolean test, Server server) {
        if (server == null) {
            displayError(new XXDBException("Please select a server.."));
            return false;
        }

        if (!executeIcon.isVisible()) {
            JOptionPane.showMessageDialog(this, "The server connection is in use. Please try again later.", "DolphinDB Workspace", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (conn_ != null) {
            if (conn_.getHostName() != server.getHost() || conn_.getPort() != server.getPort()) {
                conn_.close();
                conn_ = getDBConnection();
            } else if (conn_.isConnected()) {
                return true;
            }else {
                conn_.close();
            }
        } else {
            conn_ = getDBConnection();
        }
        boolean connect_succ = false;
        try {
            if(test){
                try {
                    connect_succ = conn_.connect(server.getHost(), server.getPort(), 1000);
                }catch (java.net.SocketTimeoutException ste){
                    connect_succ = false;
                }
            }else{
                connect_succ = conn_.connect(server.getHost(), server.getPort());
                if (!connect_succ)
                    throw new RuntimeException("Failed to connect to server");
            }

            displayVariables.updateConnection(conn_);
            try {
                BasicDictionary lic = (BasicDictionary) conn_.run("license()");
                String clientName = lic.get(new BasicString("clientName")).toString();
                String expireDate = lic.get(new BasicString("expiration")).toString();
                SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
                Date expire = format.parse(expireDate);
                Date now = new Date();
                int days = (int) ((expire.getTime() - now.getTime()) / (1000*3600*24));
                if (days > 0) {
                    statusBar.setSectionText("license", "licensed to: " + clientName + "  exp: " + expireDate);
                    if (days < 16) {
                        JOptionPane.showMessageDialog(this, "Your DolphinDB license will expire in "
                                + days + " days. Please contact DolphinDB support team to renew it.");
                    }
                } else {
                    statusBar.setSectionText("license", "<html><font color=\"red\">licensed to: "
                            + clientName + "  exp: " + expireDate + "</font></html>");
                    JOptionPane.showMessageDialog(this, "Your DolphinDB license has expired. " +
                            "Please contact DolphinDB support team to renew it.");
                }
                BasicString osstring = (BasicString) conn_.run("getOS() + ';' +  version()");
                String[] t = osstring.getString().split(";");
                SERVER_OS = t[0];
                SERVER_VERSION = t[1].split(" ")[0];
                
                this.toolBar.remove(this.cboLanguage);
                if (
                    // always show language dropdown
                    Boolean.valueOf(
                        Utility.getUserSetting(
                            PreferenceSettingItem.AlwaysShowLanguageDropdown.toString(), "false"
                        )
                    ) ||
                    SERVER_VERSION.compareTo("2.10.0") >= 0
                ) {
                    this.toolBar.add(this.cboLanguage);
                } else {
                    this.cboLanguage.setSelectedItem("DolphinDB");
                    this.language = "DolphinDB";
                }
                this.toolBar.repaint();
                
                if(syntaxUpdatedCount==0){
                    BasicTable defsTable = (BasicTable) conn_.run("defs()");
                    DolphinSyntaxItem.save(defsTable);
                    refreshTextAreaAutoComplete();
                    syntaxUpdatedCount =1;
                }
            } catch (Exception e) {
                displayError(e);
            }
        } catch (Exception e) {
            conn_ = null;
            throw new RuntimeException("Failed to connect to selected server: " + server);
        }

        if(connect_succ){
            if(test){
                JOptionPane.showMessageDialog(this, "Succesfully connected to " + server);
                if(!server.getUsername().equals("")) {
                    if (!tryLogin(server.getUsername(), server.getPassword()))
                        JOptionPane.showMessageDialog(this, "Invalid user name or password.");
                }
            }else{
                if(!server.getUsername().equals("")) {
                    if(!tryLogin(server.getUsername(), server.getPassword())){
                        displayError(new Exception("Invalid user name or password."));
                    }
                }
            }
        }else{
            if(test) {
                JOptionPane.showMessageDialog(this, "failed connected to " + server);
            }
        }
        if(test)
            disconnectServer();
        return true;
    }

    static int syntaxUpdatedCount = 0;

    public void refreshTextAreaAutoComplete() {
        if (syntaxUpdatedCount == 0) {
            for (XXDBRTextScrollPane comp : tabbedWorkSpace.getAllWorkbooks()) {
                XXDBRSyntaxTextArea current = (XXDBRSyntaxTextArea) (comp.getTextArea());
                current.refreshAutoComplete();
            }
            syntaxUpdatedCount = 1;
        }
    }

    public void refreshTextArea() {
        for (XXDBRTextScrollPane comp : tabbedWorkSpace.getAllWorkbooks()) {
            XXDBRSyntaxTextArea current = (XXDBRSyntaxTextArea) (comp.getTextArea());
            current.setFont(new Font(PreferenceSettingUtil.getDefault_Font(),
                    Font.PLAIN,  Utility.getScaledSize(PreferenceSettingUtil.getFont_Size())));
            current.setTabSize(PreferenceSettingUtil.getEditor_Tab_Size());

        }
        if (displayText != null) {
            displayText.setFont(new Font(PreferenceSettingUtil.getDefault_Font(),
                    Font.PLAIN, PreferenceSettingUtil.getFont_Size()));
        }
    }
    
    public boolean tryLogin(String username, String password) {
        if (username.isEmpty())
            return true;
        try {
            conn_.login(username, password, true);
            server.setUsername(username);
            server.setPassword(password);
            statusBar.setSectionText("server", username + "@" + server.getHost() + ":" + server.getPort());
            return true;
        } catch (Exception e) {
            displayError(e);
            return false;
        }
    }

    public int syncFile(String remotePath, String fileContent, long lastModified) {
        if (!testServer(false, server))
            return SYNC_FAIL;

        List<Entity> args = new ArrayList<>();

        String parentFolder = remotePath.substring(0, remotePath.lastIndexOf(Utility.getPathSeperator(SERVER_OS)));
        String fileName = remotePath.substring(remotePath.lastIndexOf(Utility.getPathSeperator(SERVER_OS)) + 1);

        try {
            // Make sure the directory is created
            args.add(new BasicString(parentFolder));
            BasicBoolean folderExists = (BasicBoolean) conn_.run("exists", args);
            // Check lastModified and determine whether to synchronize file
            if (!folderExists.getBoolean()) {
                conn_.run("mkdir", args);
            }
            else {
            	String script = "exec lastModified from files('" + parentFolder.replace("\\","\\\\") + "') where filename = '" + fileName + "'";
            	BasicTimestampVector filesTable = (BasicTimestampVector) conn_.run(script);    // get files info in the folder
            	if (filesTable.rows() > 0) {
            		long targetLastModified = filesTable.getLong(0);
            		if (targetLastModified == lastModified)
            			return SYNC_NOT_MODIFIED;    // File not modified, finish
            	}
            }

            // Sync file
            args.clear();
            args.add(new BasicString(fileContent));
            args.add(new BasicString(remotePath));
            args.add(new BasicBoolean(false));
            args.add(new BasicLong(lastModified));
            int fileLength = fileContent.length();
            if (fileLength <= 65535)
	            conn_.run("saveTextFile", args);
            else {
            	String fragment = fileContent.substring(0, 65535);
            	args.set(0, new BasicString(fragment));
	            conn_.run("saveTextFile", args);
	            args.set(2, new BasicBoolean(true));
	            for (int start = 65535; start < fileLength; start += 65535) {
	            	fragment = fileContent.substring(start, Math.min(start + 65535, fileLength));
	            	args.set(0, new BasicString(fragment));
		            conn_.run("saveTextFile", args);
	            }
            }

            java.util.Date dtStart = new java.util.Date();
            displayMessage(Utility.formatDate(dtStart) + ": Successfully synchronized " + remotePath);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
            java.util.Date dtStart = new java.util.Date();
            FileLog.Log(ex.getMessage());
            FileLog.Log(Arrays.toString(ex.getStackTrace()));
            displayMessage(Utility.formatDate(dtStart) + ": Could not synchronize " + remotePath);
            return SYNC_FAIL;
        }
        return SYNC_SUCCESS;
    }

    public String getRemoteNodePath(ProjectNode node) {
        if (server.hasRemoteDir()) {
            TreeNode[] nodesInPath = node.getPath();
            String[] paths = new String[nodesInPath.length - 1];
            for (int i = 0; i < nodesInPath.length - 1; i++) {
                paths[i] = nodesInPath[i + 1].toString();
            }
            String filePath = String.join("/", paths);

            return server.getFormatedRemoteDir() + filePath;
        } else
            return node.getDirPath().replaceAll("\\\\", "/");
    }

    public String getRemoteModulePath(ProjectNode node){
        if(conn_ ==null){
            testServer(false, this.sd.getActiveServer());
        }
        try {
            String SEP = Utility.getPathSeperator(SERVER_OS);
            BasicString homeDir = (BasicString) conn_.run("getHomeDir()");
            int i = node.getDirPath().toLowerCase().lastIndexOf(FILE_SEPARATOR + "modules" + FILE_SEPARATOR);
            String path =  homeDir.getString() + node.getDirPath().substring(i).replace(FILE_SEPARATOR, SEP);
            return path;
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return "";
    }
    
    public void executeCode(String textToExec) {
        if (!testServer(false, server))
            return;
        final String script = textToExec.replaceAll("\r", "");
        ScriptExecutor executor = new ScriptExecutor(this, conn_, script);
        executeIcon.setVisible(false);
        runningIcon.setVisible(true);
        execute.setEnabled(false);

        java.util.Date dtStart = new java.util.Date();
        long startTime = System.currentTimeMillis();
        executor.setStartTime(startTime);

        displayMessage("<b class=time>" + Utility.formatDate(dtStart) + ": executing code ...</b>", null);

        String[] scriptLines = textToExec.split("\n+");
        if (scriptLines.length > 1) {
            if (!scriptLines[0].startsWith("line") && !scriptLines[0].startsWith("file"))
                displayMessage(scriptLines[0], "plain");
            displayMessage("> " + scriptLines[1], "plain");
            if (scriptLines.length >= 3) {
                if (scriptLines.length > 3)
                    displayMessage("...");
                displayMessage("> " + scriptLines[scriptLines.length - 1], "plain");
            }
        }

        executor.execute();
    }

    private DBConnection getDBConnection(){
//        FileLog.Log("getDBConnection");
        return new DBConnection(false, PreferenceSettingUtil.getSSLEnabled(), false, "Python".equals(this.language));
    }

    private void displayObjectInTable(Entity obj) {
        XXDBJTable displayTable = null;
        DATA_FORM form = obj.getDataForm();
        int total = obj.rows();
        try {
            if (form == DATA_FORM.DF_TABLE) {
                displayTable = new XXDBJTable(new TableBasedXXDBTableModel((Table) obj), this, this);
            } else if (form == DATA_FORM.DF_CHART) {
                Matrix data = ((BasicChart) obj).getData();
                displayTable = new XXDBJTable(new MatrixBasedXXDBTableModel(data, data.hasRowLabel()), this, this);
                total = data.rows();
            } else if (form == DATA_FORM.DF_MATRIX) {
                displayTable = new XXDBJTable(new MatrixBasedXXDBTableModel((Matrix) obj, ((Matrix) obj).hasRowLabel()), this, this);
            } else if (form == DATA_FORM.DF_DICTIONARY) {
                displayTable = new XXDBJTable(new DictionaryBasedXXDBTableModel((BasicDictionary) obj), this, this);
            } else if (form == DATA_FORM.DF_VECTOR) {
                displayTable = new XXDBJTable(new VectorBasedXXDBTableModel((Vector) obj, 10), this, this);
            } else if (form == DATA_FORM.DF_SET) {
                displayTable = new XXDBJTable(new VectorBasedXXDBTableModel((BasicSet) obj, 10), this, this);
            } else
                return;

            setDataBrowser(displayTable, total);
        }catch(Exception ex){
            displayError(ex);
        }
    }

    @Override
    public void handleTableSelection(ListSelectionEvent event, XXDBJTable table) {
        if (event.getValueIsAdjusting())
            return;
        double sum = table.getSelectionSum();
        if (sum != 0 && Math.abs(sum) < 0.000001)
            statusBar.setSectionText("sum", "sum: " + new DecimalFormat("0.######E0").format(sum));
        else
            statusBar.setSectionText("sum", "sum: " + new DecimalFormat("#,##0.######").format(sum));
    }

    @Override
    public void handlePageSwitch(int currentPage, int totalPage) {
        DecimalFormat df = new DecimalFormat("#,##0");
        statusBar.setSectionText("page", "page: " + df.format(currentPage + 1) + "/" + df.format(totalPage));
    }

    @Override
    public void handleVariableBrowse(String name, DATA_FORM form, DATA_TYPE type, int rows, int columns, long bytes) {
        XXDBJTable displayTable = null;
        try {
            if (form == DATA_FORM.DF_TABLE) {
                displayTable = new XXDBJTable(new TableBasedDynamicXXDBTableModel(conn_, name, rows), this, this);
            } else if (form == DATA_FORM.DF_MATRIX) {
                displayTable = new XXDBJTable(new MatrixBasedDynamicXXDBTableModel(conn_, name, rows), this, this);
            } else if (form == DATA_FORM.DF_DICTIONARY) {
                displayTable = new XXDBJTable(new DictionaryBasedDynamicXXDBTableModel(conn_, name, rows), this, this);
            } else if (form == DATA_FORM.DF_VECTOR || form == DATA_FORM.DF_SET) {
                displayTable = new XXDBJTable(new VectorBasedDynamicXXDBTableModel(conn_, name, rows, 10, type, form == DATA_FORM.DF_SET), this, this);
            } else
                return;
        } catch (Throwable t) {
            JOptionPane.showMessageDialog(null, t.getMessage());
            return;
        }

        setDataBrowser(displayTable, rows);
    }

    private void setDataBrowser(XXDBJTable table, int rows) {
        dataBrowser.setTable(table);
        tabbedPane.setSelectedComponent(dataBrowser);
        statusBar.setSectionText("row", "rows: " + new DecimalFormat("#,##0").format(rows));
    }

    JButton createToolbarIcon(String action, String imageUrl, String toolTipText) {
        ImageIcon image = new ImageIcon(XXDBEditor.class.getResource(imageUrl));
        image.setImage(image.getImage().getScaledInstance(Utility.getScaledSize(24), Utility.getScaledSize(24), Image.SCALE_DEFAULT));
        JButton icon = new JButton(image);
        icon.setActionCommand(action);
        icon.addActionListener(this);
        icon.setToolTipText(toolTipText);
        toolBar.add(icon);
        return icon;
    }

    private void handleSwitchWorkspace() {
        if (conn_ != null && conn_.isBusy()) {
            JOptionPane.showMessageDialog(this, "The application is running code on dolphindb. Please try again later.");
            return;
        }

        String curWorkspace = Utility.getLastUsedWorkspace();
        new XXDBWorkspaceBrowser().setVisible(true);
        String newWorkspace = Utility.getLastUsedWorkspace();
        if (newWorkspace.isEmpty()) {
            Utility.setLastUsedWorkspace(curWorkspace);
            return;
        } else if (curWorkspace.equalsIgnoreCase(newWorkspace))
            return;
        else if ((newWorkspace + File.separator).startsWith(curWorkspace + File.separator)) {
            Utility.setLastUsedWorkspace(curWorkspace);
            JOptionPane.showMessageDialog(this, "The new workspace is the sub folder of the current workspace.", "DolphinDB Workspace", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!handleUnsavedDocuments())
            return;

        disconnectServer();
        cboServer.removeAllItems();
        explorerTree.saveWorkspace();
        explorerTree.loadWorkspace(new File(newWorkspace));
        Server activeServer = sd.getActiveServer();
        for (final Server s : sd.getServers()) {
            listServer(s, s.equals(activeServer));
        }

        if (sd.getServers().size() == 0) {
            Server s = new Server("local" + DEFAULT_PORT, "localhost", DEFAULT_PORT, "", "", "");
            sd.addServer(s, true);
            listServer(s, true);
        }
    }

    private void handlePreferences() {
        new XXDBPreferencesDlg(this).setVisible(true);
    }

    private void handleWindowClosing() {
        if (conn_ != null && conn_.isBusy()) {
            int n = JOptionPane.showConfirmDialog(this, "The application is running script on dolphindb server. Are you sure to exit?", "DolphinDB Workspace", JOptionPane.YES_NO_OPTION);
            if (n == JOptionPane.NO_OPTION) {
                return;
            } else if(n==JOptionPane.YES_OPTION){
                if (!handleUnsavedDocuments())
                    return;
                cancelJob();
                explorerTree.saveWorkspace();
                System.exit(0);
            }
        } else {
            if (JOptionPane.showConfirmDialog(this,
                    "Are you sure to exit the application?", "DolphinDB Workspace",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                if (!handleUnsavedDocuments())
                    return;
                explorerTree.saveWorkspace();
                if (conn_ == null)
                    System.exit(0);
                else {
                    disconnectServer();
                    System.exit(0);
                }
            }
        }
    }

    private boolean saveAllOpenDocuments() {
        Component selectedComp = tabbedWorkSpace.getSelectedComponent();
        for (XXDBRTextScrollPane comp : tabbedWorkSpace.getAllWorkbooks()) {
            XXDBRSyntaxTextArea current = (XXDBRSyntaxTextArea) (comp.getTextArea());

            if (!current.isSaved()) {
                tabbedWorkSpace.setSelectedComponent(comp);
                displayMessage("Saving: " + current.getName());
                try {
                    XXDBMenuBar.saveFile(this, current);
                    setTitle();
                } catch (Exception e) {
                    displayMessage("Not able to save: " + e.getMessage());
                    tabbedWorkSpace.setSelectedComponent(selectedComp);
                    return false;
                }
            }
        }

        tabbedWorkSpace.setSelectedComponent(selectedComp);
        return true;
    }

    private boolean handleUnsavedDocuments() {
        for (XXDBRTextScrollPane comp : tabbedWorkSpace.getAllWorkbooks()) {
            XXDBRSyntaxTextArea current = (XXDBRSyntaxTextArea) (comp.getTextArea());

            if (!current.isSaved()) {
                tabbedWorkSpace.setSelectedComponent(comp);
                int selection = JOptionPane.showConfirmDialog(this,
                        "Do you want to save : " + current.getName() + "?",
                        "DolphinDB Workspace",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (selection == JOptionPane.YES_OPTION) {
                    displayMessage("Saving: " + current.getName());
                    try {
                        XXDBMenuBar.saveFile(this, current);
                        setTitle();
                    } catch (Exception e) {
                        displayMessage("Not able to save: " + e.getMessage());
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private void disconnectServer() {
//        FileLog.Log("disconnectServer");
        if (conn_ != null)
            conn_.close();
    }

    public DBConnection getConnection(){
        return this.conn_;
    }

    public XXDBFileTree getExplorerTree() {
        return explorerTree;
    }

    public void openRealTimePanelDialog() {
        frm = new RealTimeDataChartDlg(this);
        frm.setVisible(true);
    }

    public String getLocalAddress() {
        if (conn_ != null) {
            InetAddress address = conn_.getLocalAddress();
            return address.getHostAddress();
        } else
            return "";

    }

    public void openExportTableDialog() {
        frm_ex = new ExportTableDlg(this);
        frm_ex.setVisible(true);
    }

    private class ScriptExecutor extends SwingWorker<Entity, String> implements ProgressListener {
        private XXDBEditor parent;
        private DBConnection conn;
        private String script;

        private long startTime;

        public void setStartTime(long start) {
            this.startTime = start;
        }

        public ScriptExecutor(XXDBEditor parent, DBConnection conn, String script) {
            this.parent = parent;
            this.conn = conn;
            this.script = script;
           

        }

        @Override
        protected Entity doInBackground() throws Exception {
            try {
                if (!conn.isConnected()){
                    conn = new DBConnection(false, PreferenceSettingUtil.getSSLEnabled(), false, "Python".equals(language));
                    conn.connect(getServer().getHost(), getServer().getPort());
                    displayVariables.updateConnection(conn);
                    conn_ = this.conn;
                }
                Entity re = conn.run(script, this);
                System.out.print(re);
                return re;
            }catch(Exception ex){
                displayError(ex);
            }
            return null;
        }

        @Override
        protected void process(List<String> msgs) {
            for (String msg : msgs)
                displayMessage(msg);
        }

        @Override
        protected void done() {
            executeIcon.setVisible(true);
            runningIcon.setVisible(false);
            execute.setEnabled(true);
            try {
                Entity obj = get();
                if (obj == null)
                    return;

                java.util.Date dtEnd = new java.util.Date();

                long endTime = dtEnd.getTime();
                String showTime = Utility.formatTime(endTime - this.startTime);
                statusBar.setSectionText("timeconsume", "time elapsed: " + showTime);

                displayMessage(Utility.formatDate(dtEnd) + ": execution was completed [" + showTime + "]", "time");
                String sessionId = conn.getSessionID();
                statusBar.setSectionText("session","current sessionID : " + sessionId);
                try {
                    displayVariables.update();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    displayMessage("Error was raised when updating variables : " + ex.getMessage());
                }

                DATA_FORM form = obj.getDataForm();
                if (form == DATA_FORM.DF_SCALAR || form == DATA_FORM.DF_PAIR || form == DATA_FORM.DF_CHUNK || (form == DATA_FORM.DF_DICTIONARY && obj.getDataType() == DATA_TYPE.DT_ANY)) {
                    displayMessage(obj.getString(), "plain");
                    tabbedPane.setSelectedComponent(displayTextScrollPane);
                } else {
                    displayObjectInTable(obj);
                    if (form == DATA_FORM.DF_CHART) {
                        try {
                            BasicChart chart = (BasicChart) obj;
                            XXDBChartData t = new XXDBChartData();
                            //BasicBoolean s = (BasicBoolean)(((BasicDictionary)chart.get(new BasicString(new String("extras")))).get(new BasicString(new String("XY_MULTI_AXIS"))));
                            t.loadTable(chart);
                            try {
                                JFreeChart tchart = XXDBChart.getChart(chart, t);
                                Plot plot = tchart.getPlot();
                                plot.setDrawingSupplier(new ChartDrawingSupplier());
                                ChartPanel chartPanel = new ChartPanel(tchart);
                                chartPanel.setPreferredSize(new java.awt.Dimension(560, 367));
                                String windowTitle = "DolphinDB Chart";
                                if (chart.getTitle() != null && !chart.getTitle().isEmpty())
                                    windowTitle = windowTitle + " [" + chart.getTitle() + "]";
                                new XXDBJDialog(parent, windowTitle, chartPanel);
                            } catch (Exception ex){
                                displayMessage("Error was raised when drawing the chart : " + ex.getMessage());
                            }
                        } catch (org.jfree.data.general.SeriesException ex) {
                            displayMessage("Duplicated temporal data for x-axis are not permitted.");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            displayMessage("Error was raised when drawing the chart : " + ex.getMessage());
                        }
                    }
                }
                revalidate();
            } catch (ExecutionException t) {
                displayMessage(LocalDateTime.now().toString() + ": execution was completed with exception");
                if (t.getCause() != null) {
                    if (t.getCause() instanceof ConnectException)
                        displayVariables.initUI(true);
                    displayError(new XXDBException(t.getCause().getMessage()));
                } else
                    displayError(new XXDBException(t.getMessage()));

            } catch (InterruptedException e) {
                displayError(e);
            }
        }

        @Override
        public void progress(String message) {
            publish(message);
        }
    }

    public class ChartDrawingSupplier extends DefaultDrawingSupplier {
        private static final long serialVersionUID = 1L;

        private transient Paint[] paintSequence;
        private int paintIndex = 0;
        private transient Paint[] outlinePaintSequence;
        private int outlinePaintIndex = 0;
        private transient Stroke[] strokeSequence;
        private int strokeIndex;
        private transient Stroke[] outlineStrokeSequence;
        private int outlineStrokeIndex;
        private transient Shape[] shapeSequence;
        private int shapeIndex;

        public ChartDrawingSupplier() {
            this(DEFAULT_PAINT_SEQUENCE,
                    DEFAULT_OUTLINE_PAINT_SEQUENCE,
                    DEFAULT_STROKE_SEQUENCE,
                    DEFAULT_OUTLINE_STROKE_SEQUENCE,
                    DEFAULT_SHAPE_SEQUENCE);
        }

        public ChartDrawingSupplier(Paint[] paintSequence,
                                    Paint[] outlinePaintSequence,
                                    Stroke[] strokeSequence,
                                    Stroke[] outlineStrokeSequence,
                                    Shape[] shapeSequence) {

            this.paintSequence = paintSequence;
            this.outlinePaintSequence = outlinePaintSequence;
            this.strokeSequence = strokeSequence;
            this.outlineStrokeSequence = outlineStrokeSequence;
            this.shapeSequence = shapeSequence;
        }

        public Paint getNextPaint() {
            Paint result;
            if (this.paintIndex % this.paintSequence.length == 3)
                result = new Color(175, 175, 85);
            else
                result = this.paintSequence[this.paintIndex % this.paintSequence.length];
            this.paintIndex++;
            return result;
        }

        public Paint getNextOutlinePaint() {
            Paint result = this.outlinePaintSequence[
                    this.outlinePaintIndex % this.outlinePaintSequence.length];
            this.outlinePaintIndex++;
            return result;
        }

        public Stroke getNextStroke() {
            Stroke result = this.strokeSequence[
                    this.strokeIndex % this.strokeSequence.length];
            this.strokeIndex++;
            return result;
        }

        public Stroke getNextOutlineStroke() {
            Stroke result = this.outlineStrokeSequence[
                    this.outlineStrokeIndex % this.outlineStrokeSequence.length];
            this.outlineStrokeIndex++;
            return result;
        }

        public Shape getNextShape() {
            Shape result = this.shapeSequence[
                    this.shapeIndex % this.shapeSequence.length];
            this.shapeIndex++;
            return result;
        }
    }
}
