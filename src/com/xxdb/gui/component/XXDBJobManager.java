package com.xxdb.gui.component;

import com.xxdb.DBConnection;
import com.xxdb.data.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class XXDBJobManager extends JFrame {
    JobManagerTableDisplayTab currentTab;
    private XXDBEditor editor;
    private DBConnection conn;
    private AbstractXXDBTableModel currentModel;
    private JTable currentTable;

    public XXDBJobManager(XXDBEditor editor) throws Exception {
        super("Job Manager");
        this.editor = editor;
        this.conn = editor.getConnection();
        if (conn == null) throw new Exception("Fail to open job manager: No connection to server");

        JTabbedPane tabbedPane = null;
        tabbedPane = createTabbedPane();

        JButton okBtn = new JButton("Close");
        okBtn.addActionListener(actionEvent -> this.dispose());
        JPanel okPane = new JPanel();
        okPane.add(okBtn);

        this.setLayout(new BorderLayout(10, 10));
        this.add(tabbedPane, BorderLayout.CENTER);
        this.add(okPane, BorderLayout.SOUTH);

        this.setSize(1300, 550);
        this.setLocationRelativeTo(editor);
        this.setVisible(true);
    }

    private JTabbedPane createTabbedPane() throws Exception {


        //submit job pane
        JobManagerTableDisplayTab submitPane = new JobManagerTableDisplayTab(JobManagerTableDisplayTab.SUBMIT_TAB);

        //scheduled job pane
        JobManagerTableDisplayTab schedulePane = new JobManagerTableDisplayTab(JobManagerTableDisplayTab.SCHEDULE_TAB);

        //console job pane
        JobManagerTableDisplayTab consolePane = new JobManagerTableDisplayTab(JobManagerTableDisplayTab.CONSOLE_TAB);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Submit Jobs", submitPane);
        tabbedPane.addTab("Scheduled Jobs", schedulePane);
        tabbedPane.addTab("Console Jobs", consolePane);

        tabbedPane.addChangeListener(changeEvent -> {
            currentTab = (JobManagerTableDisplayTab) tabbedPane.getSelectedComponent();
            currentTable = currentTab.table;
            currentModel = currentTab.tableModel;
        });

        currentTab = submitPane;
        currentTable = currentTab.table;
        currentModel = currentTab.tableModel;

        return tabbedPane;
    }

    /**
     * This inner class creates a tab pane that displays a table of recent submitted/scheduled/console jobs
     */
    private class JobManagerTableDisplayTab extends JPanel implements ActionListener {
        final public static int SUBMIT_TAB = 1;
        final public static int SCHEDULE_TAB = 2;
        final public static int CONSOLE_TAB = 3;
        private JCheckBox selectAllCheckBox = new JCheckBox("Select All");
        private JComboBox<String> statusComboBox;
        private ArrayList<RowFilter<Object, Object>> rowFilterList = new ArrayList<>();
        private TableRowSorter<ArrayBasedXXDBTableModel> sorter;
        private JTextField findInputTxt = new JTextField(30);
        private ArrayBasedXXDBTableModel tableModel;
        private XXDBJTable table;
        private JPopupMenu popupMenu;
        private int tabType;

        public JobManagerTableDisplayTab(JPopupMenu popupMenu, int tabType) throws Exception {
            if (popupMenu == null) {
                this.popupMenu = new JPopupMenu();
                JMenuItem cancelMnu = new JMenuItem("terminate");
                this.popupMenu.add(cancelMnu);
            } else
                this.popupMenu = popupMenu;
            this.tabType = tabType;
            if (!refresh()) {
                throw new Exception("Fail to open job manager: Cannot load the tables");
            }
            createUI();
        }

        public JobManagerTableDisplayTab(int tabType) throws Exception {
            this(null, tabType);
        }

        public boolean refresh() {
            BasicTable data = null;
            try {
                if (tabType == SUBMIT_TAB)
                    data = (BasicTable) conn.run("select * from getRecentJobs()");
                else if (tabType == SCHEDULE_TAB)
                    data = (BasicTable) conn.run("select * from getScheduledJobs()");
                else if (tabType == CONSOLE_TAB)
                    data = (BasicTable) conn.run("select * from getConsoleJobs()");
                if (data == null) throw new Exception("Unknown tab");
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
            updateData(data);
            if (table != null) {
                findInputTxt.setText("");
                if (statusComboBox != null)
                    statusComboBox.setSelectedIndex(0);
                sorter.setRowFilter(null);
                table.validate();
                table.updateUI();
            }
            return true;
        }

        private void updateData(BasicTable data) {
            List<TableColumnMeta> columnMetas = AbstractXXDBTableModel.generateTableColumn(data);
            Object[][] columnValues;
            int rows = data.rows();
            int columns = data.columns();
            columnValues = new Object[rows][columns + 1];
            columnMetas.add(0, new TableColumnMeta("terminate", Boolean.class, Entity.DATA_CATEGORY.LOGICAL, null, true));
            for (int i = 0; i < rows; i++) {
                columnValues[i][0] = false;
                for (int j = 0; j < columns; j++)
                    columnValues[i][j + 1] = data.getColumn(j).get(i);
            }
            selectAllCheckBox.setSelected(false);
            if (tableModel == null) {
                tableModel = new ArrayBasedXXDBTableModel(columnMetas) {
                    private static final long serialVersionUID = 1L;

                    public boolean isColumnReorderingAllowed() { return false; }

                    public boolean isRowReorderingAllowed() { return false; }

                    public boolean showPopupMenu() { return false; }
                };
            } else {
                tableModel.updateStructure(columnMetas);
            }
            tableModel.updateData(columnValues);
        }

        private void createUI() {
            selectAllCheckBox = new JCheckBox("Select All");
            selectAllCheckBox.setActionCommand("SelectAll");

            if (tabType == SUBMIT_TAB) {
                statusComboBox = new JComboBox<>(new String[]{"All", "Complete", "Incomplete"});
                statusComboBox.setActionCommand("SwitchStatus");
            } else {
                statusComboBox = new JComboBox<>(new String[]{"All"});
            }

            JButton cancelBtn = new JButton("Terminate Jobs");
            cancelBtn.setActionCommand("terminate");
            JButton addBtn = new JButton("Add");

            JButton refreshBtn = new JButton("Refresh");

            JPanel btnPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
            btnPane.add(selectAllCheckBox);
            btnPane.add(statusComboBox);
            btnPane.add(cancelBtn);
            if (tabType == SCHEDULE_TAB) {
                btnPane.add(addBtn);
            }
            btnPane.add(refreshBtn);

            JButton findBtn = new JButton("Find");
            JButton clearBtn = new JButton("Clear");

            JPanel findBtnPane = new JPanel();
            findBtnPane.add(new JLabel("Filter: "));
            findBtnPane.add(findInputTxt);
            findBtnPane.add(findBtn);
            findBtnPane.add(clearBtn);

            JPanel actionBtnPane = new JPanel(new GridLayout(1, 2));
            actionBtnPane.add(btnPane);
            actionBtnPane.add(findBtnPane);

            table = new XXDBJTable(tableModel, null);
            table.setAutoCreateRowSorter(true);
            if (tabType == SUBMIT_TAB) {
                rowFilterList.add(null);
                rowFilterList.add(null);
            }
            sorter = new TableRowSorter<>(tableModel);
            table.setRowSorter(sorter);
            JScrollPane tableJsp = new JScrollPane(table);
            tableJsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            this.setLayout(new BorderLayout(5, 5));
            this.add(actionBtnPane, BorderLayout.NORTH);
            this.add(tableJsp, BorderLayout.CENTER);

            selectAllCheckBox.addActionListener(this);
            statusComboBox.addActionListener(this);
            cancelBtn.addActionListener(this);
            addBtn.addActionListener(this);
            refreshBtn.addActionListener(this);
            findBtn.addActionListener(this);
            clearBtn.addActionListener(this);

            PopupListener popupListener = new PopupListener(popupMenu);
            table.addMouseListener(popupListener);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String action = actionEvent.getActionCommand();
            if (action.equalsIgnoreCase("terminate")) {
                int result = JOptionPane.showConfirmDialog(this, "Are you sure to terminate selected jobs?", "Terminate Jobs", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    for (int i = 0; i < table.getRowCount(); i++) {
                        if ((Boolean) table.getValueAt(i, 0)) {
                            String jobID, script;
                            if (tabType == JobManagerTableDisplayTab.SUBMIT_TAB) {
                                jobID = ((BasicString) table.getValueAt(i, 3)).getString().trim();
                                script = "cancelJob(\"" + jobID + "\")";
                            } else if (tabType == JobManagerTableDisplayTab.SCHEDULE_TAB) {
                                jobID = ((BasicString) table.getValueAt(i, 2)).getString().trim();
                                script = "deleteScheduledJob(\"" + jobID + "\")";
                            } else {
                                jobID = ((BasicString) table.getValueAt(i, 3)).getString().trim();
                                script = "cancelConsoleJob(\"" + jobID + "\")";
                            }
                            try {
                                conn.run(script);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(this, ex.getMessage());
                            }
                        }
                    }
                    try {
                        Thread.sleep(530);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!refresh()) JOptionPane.showMessageDialog(this, "Fail to refresh the tables");
                }
            } else if (action.equalsIgnoreCase("add")) {
                if (tabType == SCHEDULE_TAB) {
                    XXDBJobManager.this.setEnabled(false);
                    CreateJobDlg createScheduleJobDlg = new CreateJobDlg("Create scheduled job");
                    createScheduleJobDlg.setLocationRelativeTo(this);
                    createScheduleJobDlg.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent windowEvent) {
                            XXDBJobManager.this.setEnabled(true);
                        }
                    });
                }
            } else if (action.equalsIgnoreCase("refresh")) {
                if (!refresh()) JOptionPane.showMessageDialog(this, "Fail to refresh the table");
            } else if (action.equalsIgnoreCase("SelectAll")) {
                Boolean select = selectAllCheckBox.isSelected();
                int tableRows = table.getRowCount();
                for (int i = 0; i < tableRows; i++) {
//                    System.out.println("select row " + i + " value: " + table.getValueAt(i, 3));
                    table.setValueAt(select, i, 0);
                }
                table.validate();
                table.updateUI();
            } else if (action.equalsIgnoreCase("SwitchStatus")) {
                int status = statusComboBox.getSelectedIndex();
                for (int i = 0; i < table.getRowCount(); i++) {
                    table.setValueAt(false, i, 0);
                }
                selectAllCheckBox.setSelected(false);
                RowFilter<Object, Object> rowFilter = null;
                if (status == 1) {
                    rowFilter = RowFilter.regexFilter(".+", 9);
                } else if (status == 2) {
                    rowFilter = RowFilter.regexFilter("^$", 9);
                }
                rowFilterList.set(1, rowFilter);
                updateSorter();
            } else if (action.equalsIgnoreCase("Find")) {

                String inputStr = findInputTxt.getText().trim();
                RowFilter<Object, Object> rowFilter = null;
                if (!inputStr.isEmpty()) {
                    rowFilter = RowFilter.regexFilter(inputStr);
                    for (int i = 0; i < table.getRowCount(); i++) {
                        table.setValueAt(false, i, 0);
                    }
                }
                selectAllCheckBox.setSelected(false);
                if (rowFilterList.isEmpty()) {
                    sorter.setRowFilter(rowFilter);
                } else {
                    rowFilterList.set(0, rowFilter);
                    updateSorter();
                }
            } else if (action.equalsIgnoreCase("Clear")) {
                for (int i = 0; i < table.getRowCount(); i++) {
                    table.setValueAt(false, i, 0);
                }
                findInputTxt.setText("");
                selectAllCheckBox.setSelected(false);
                if (rowFilterList.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    rowFilterList.set(0, null);
                    updateSorter();
                }
            } else if (action.equalsIgnoreCase("")) {
                JOptionPane.showMessageDialog(this, "Unknown Operation");
            }
        }

        private void updateSorter() {
            if (rowFilterList.get(0) != null && rowFilterList.get(1) != null)
                sorter.setRowFilter(RowFilter.andFilter(rowFilterList));
            else if (rowFilterList.get(0) != null || rowFilterList.get(1) != null) {
                RowFilter<Object, Object> rowFilter = (rowFilterList.get(0) != null) ? rowFilterList.get(0) : rowFilterList.get(1);
                sorter.setRowFilter(rowFilter);
            } else {
                sorter.setRowFilter(null);
            }


        }
    }

    private class CreateJobDlg extends JFrame implements ActionListener {
        JTabbedPane createDlgTabPane;
        frequencyDetailPane dailyPane, weeklyPane, monthlyPane;
        String script;
        JTextField jobIDTxt, jobDescTxt, funcActionTxt, onCompleteName;
        XXDBRSyntaxTextArea functionScript, onCompleteBodyScript;
        frequencyDetailPane jobFrequencyPane;
        CardLayout triggerDetailCard;
        JPanel triggerDetailPane;

        public CreateJobDlg(String title) {
            super(title);
            createDlgTabPane = new JTabbedPane();

            JPanel mainInfoPane = new JPanel();
            JPanel triggerPane = new JPanel();
            JPanel actionPane = new JPanel();
            JPanel completePane = new JPanel();

            JLabel jobIDLbl = new JLabel("Job ID: ");
            JLabel jobDescLbl = new JLabel("Job Desc: ");

            jobIDTxt = new JTextField();
            jobDescTxt = new JTextField();

            GroupLayout mainInfoLayout = new GroupLayout(mainInfoPane);
            mainInfoPane.setLayout(mainInfoLayout);
            mainInfoLayout.setAutoCreateGaps(true);
            mainInfoLayout.setAutoCreateContainerGaps(true);
            mainInfoLayout.setHorizontalGroup(
                    mainInfoLayout.createSequentialGroup()
                            .addGroup(mainInfoLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                    .addComponent(jobIDLbl)
                                    .addComponent(jobDescLbl))
                            .addGroup(mainInfoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(jobIDTxt)
                                    .addComponent(jobDescTxt))
            );
            mainInfoLayout.setVerticalGroup(
                    mainInfoLayout.createSequentialGroup()
                            .addGroup(mainInfoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(jobIDLbl)
                                    .addComponent(jobIDTxt))
                            .addGroup(mainInfoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(jobDescLbl)
                                    .addComponent(jobDescTxt))
            );


            // trigger pane
            JRadioButton dailyBtn = new JRadioButton("Daily");
            JRadioButton weeklyBtn = new JRadioButton("Weekly");
            JRadioButton monthlyBtn = new JRadioButton("Monthly");

            ButtonGroup freqBtnGroup = new ButtonGroup();
            freqBtnGroup.add(dailyBtn);
            freqBtnGroup.add(weeklyBtn);
            freqBtnGroup.add(monthlyBtn);

            JPanel freqBtnPane = new JPanel();
            freqBtnPane.add(dailyBtn);
            freqBtnPane.add(weeklyBtn);
            freqBtnPane.add(monthlyBtn);
            dailyBtn.setSelected(true);

            JPanel triggerFreqPane = new JPanel(new GridLayout(2, 1));
            triggerFreqPane.add(new JLabel("What's the frequency of the job?"));
            triggerFreqPane.add(freqBtnPane);

            dailyPane = new frequencyDetailPane("daily");
            weeklyPane = new frequencyDetailPane("weekly");
            monthlyPane = new frequencyDetailPane("monthly");

            triggerDetailCard = new CardLayout();
            triggerDetailPane = new JPanel();
            triggerDetailPane.setLayout(triggerDetailCard);
            triggerDetailPane.add("daily", dailyPane);
            triggerDetailPane.add("weekly", weeklyPane);
            triggerDetailPane.add("monthly", monthlyPane);
            jobFrequencyPane = dailyPane;

            triggerPane.setLayout(new BoxLayout(triggerPane, BoxLayout.Y_AXIS));
            triggerPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));
            triggerPane.add(triggerFreqPane);
            triggerPane.add(triggerDetailPane);

            // action pane
            JLabel funcActionLbl = new JLabel("Job action function: ");
            funcActionTxt = new JTextField(30);
            funcActionTxt.setToolTipText("Name of an existing function or your customized function");
            JPanel funcActionPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
            funcActionPane.add(funcActionLbl);
            funcActionPane.add(funcActionTxt);

            JLabel customFuncLbl = new JLabel("Define new function (optional)");

            functionScript = new XXDBRSyntaxTextArea(editor);
            functionScript.setToolTipText("Press `Execute to check if syntax is valid");
            XXDBRTextScrollPane textScrollPane = new XXDBRTextScrollPane(functionScript);
            JTextField textField = new JTextField(35);
            textField.setEditable(false);
            textField.setAutoscrolls(true);
            JButton exeButton = new JButton("Execute");
            JPanel exePane = new JPanel();
            exePane.add(textField);
            exePane.add(exeButton);
            JPanel customFuncPane = new JPanel(new BorderLayout(10, 10));
            customFuncPane.setBorder(new EmptyBorder(10, 10, 10, 10));
            customFuncPane.add(textScrollPane, BorderLayout.CENTER);
            customFuncPane.add(exePane, BorderLayout.SOUTH);

            exeButton.addActionListener(actionEvent -> {
                System.out.println(functionScript.getText());
                try {
                    conn.run(functionScript.getText());
                    textField.setText("Run succeeds");
                } catch (Exception ex) {
                    textField.setText(ex.getMessage());
                    textField.setToolTipText(textField.getText());
                }
            });

            actionPane.setLayout(new BoxLayout(actionPane, BoxLayout.Y_AXIS));
            actionPane.setBorder(new EmptyBorder(10, 10, 10, 10));
            funcActionPane.setAlignmentX(Component.LEFT_ALIGNMENT);
            actionPane.add(funcActionPane);
            actionPane.add(customFuncLbl);
            customFuncLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            actionPane.add(customFuncPane);
            customFuncPane.setAlignmentX(Component.LEFT_ALIGNMENT);

            //onComplete
            JLabel onCompleteHeader = new JLabel(" def ");
            onCompleteName = new JTextField(10);
            onCompleteName.setToolTipText("function name");
            JLabel onCompleteParameters = new JLabel("(jobId, jobDesc, success, result) {");
            JPanel onCompleteHeaderPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
            onCompleteHeaderPane.add(onCompleteHeader);
            onCompleteHeaderPane.add(onCompleteName);
            onCompleteHeaderPane.add(onCompleteParameters);
            onCompleteBodyScript = new XXDBRSyntaxTextArea(editor);
            onCompleteBodyScript.setToolTipText("Press `Execute to check if syntax is valid");
            XXDBRTextScrollPane onCompleteBody = new XXDBRTextScrollPane(onCompleteBodyScript);
            completePane.setLayout(new BorderLayout(10, 10));
            completePane.setBorder(new EmptyBorder(10, 10, 10, 10));
            completePane.add(onCompleteHeaderPane, BorderLayout.NORTH);
            completePane.add(onCompleteBody, BorderLayout.CENTER);
            completePane.add(new JLabel("}"), BorderLayout.SOUTH);

            createDlgTabPane.addTab("Property", mainInfoPane);
            createDlgTabPane.addTab("Date & Time", triggerPane);
            createDlgTabPane.addTab("Action Function", actionPane);
            createDlgTabPane.addTab("On Complete", completePane);

            JButton prevPageBtn = new JButton("<-");
            prevPageBtn.setActionCommand("prev");
            prevPageBtn.setEnabled(false);
            JButton nextPageBtn = new JButton("->");
            nextPageBtn.setActionCommand("next");
            JButton confirmBtn = new JButton("Confirm");
            confirmBtn.setEnabled(false);

            JPanel createDlgBtnPane = new JPanel(new GridLayout(1, 3));
            createDlgBtnPane.add(prevPageBtn);
            createDlgBtnPane.add(nextPageBtn);
            createDlgBtnPane.add(confirmBtn);


            this.setLayout(new BorderLayout());
            this.add(createDlgTabPane, BorderLayout.CENTER);
            this.add(createDlgBtnPane, BorderLayout.SOUTH);
            createDlgBtnPane.setAlignmentX(Component.RIGHT_ALIGNMENT);

            this.setSize(550, 400);
            this.setAlwaysOnTop(true);
            this.setVisible(true);

            dailyBtn.addActionListener(this);
            weeklyBtn.addActionListener(this);
            monthlyBtn.addActionListener(this);

            prevPageBtn.addActionListener(this);
            nextPageBtn.addActionListener(this);
            confirmBtn.addActionListener(this);

            createDlgTabPane.addChangeListener(changeEvent -> {
                if (createDlgTabPane.getSelectedIndex() == 0) {
                    prevPageBtn.setEnabled(false);
                    nextPageBtn.setEnabled(true);

                } else if (createDlgTabPane.getSelectedIndex() == 3) {
                    prevPageBtn.setEnabled(true);
                    nextPageBtn.setEnabled(false);
                    confirmBtn.setEnabled(true);
                } else {
                    prevPageBtn.setEnabled(true);
                    nextPageBtn.setEnabled(true);
                }
            });
        }

        public String getJobAction() {
            if (funcActionTxt.getText() == null || funcActionTxt.getText().trim().equals(""))
                return null;
            try {
                conn.run(functionScript.getText());
            } catch (IOException ex) {
                return null;
            }
            return funcActionTxt.getText().trim();
        }

        public String getOnComplete() {
            if (onCompleteName.getText() == null || onCompleteName.getText().trim().equals(""))
                return null;
            try {
                System.out.println("def " + onCompleteName.getText().trim()
                        + " (jobId, jobDesc, success, result) {\n" + onCompleteBodyScript.getText() + "}");
                conn.run("def " + onCompleteName.getText().trim() + " (jobId, jobDesc, success, result) {\n"
                        + onCompleteBodyScript.getText() + "}");
            } catch (IOException ex) {
                return null;
            }
            return onCompleteName.getText().trim();
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String action = actionEvent.getActionCommand();
            if (action.equalsIgnoreCase("prev")) {
                int current = createDlgTabPane.getSelectedIndex();
                createDlgTabPane.setSelectedIndex(current - 1);
            } else if (action.equalsIgnoreCase("next")) {
                int current = createDlgTabPane.getSelectedIndex();
                createDlgTabPane.setSelectedIndex(current + 1);
            } else if (action.equalsIgnoreCase("confirm")) {
                try {
                    if (jobFrequencyPane.getType().equalsIgnoreCase("D")) {
                        script = String.format("scheduleJob(`%s, \"%s\", %s, [%s], %s, %s, 'D'"
                                        + (getOnComplete() == null? ");":",,%s);"),
                                jobIDTxt.getText().trim(), jobDescTxt.getText().trim(), getJobAction(),
                                jobFrequencyPane.getJobTimeTxt(), jobFrequencyPane.getJobStartDateTxt(),
                                jobFrequencyPane.getJobEndDateTxt(), getOnComplete());
                    } else {
                        script = String.format("scheduleJob(`%s, \"%s\", %s, [%s], %s, %s, '%s',[%s]"
                                        + (getOnComplete() == null? ");":",%s);"),
                                jobIDTxt.getText().trim(), jobDescTxt.getText().trim(), getJobAction(),
                                jobFrequencyPane.getJobTimeTxt(), jobFrequencyPane.getJobStartDateTxt(),
                                jobFrequencyPane.getJobEndDateTxt(), jobFrequencyPane.getType(),
                                jobFrequencyPane.getDateList(), getOnComplete());
                    }
                    System.out.println(script);
                    conn.run(script);
                    currentTab.refresh();
                    this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
                    dispose();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, ex.getMessage());
                }
            } else if (action.equalsIgnoreCase("daily")) {
                jobFrequencyPane = dailyPane;
                triggerDetailCard.show(triggerDetailPane, "daily");
            } else if (action.equalsIgnoreCase("weekly")) {
                jobFrequencyPane = weeklyPane;
                triggerDetailCard.show(triggerDetailPane, "weekly");
            } else if (action.equalsIgnoreCase("monthly")) {
                jobFrequencyPane = monthlyPane;
                triggerDetailCard.show(triggerDetailPane, "monthly");
            }

        }

        private class frequencyDetailPane extends JPanel {
            JTextField jobStartDateTxt, jobEndDateTxt, jobTimeTxt, jobDateListTxt;
            JPanel weeklyTablePane;
            String type;

            public frequencyDetailPane(String type) {
                this.type = type;
                JLabel jobStartDateLbl = new JLabel("Start Date (YYYY.MM.DD): ");
                JLabel jobEndDateLbl = new JLabel("End Date (YYYY.MM.DD): ");
                JLabel jobTimeLbl = new JLabel("Scheduled Time (HH:MM,HH:MM,...): ");


                jobStartDateTxt = new JTextField(20);
                jobEndDateTxt = new JTextField(20);
                jobTimeTxt = new JTextField(20);

                JPanel basicInfoPane = new JPanel();
                GroupLayout basicInfoLayout = new GroupLayout(basicInfoPane);
                basicInfoPane.setLayout(basicInfoLayout);
                basicInfoLayout.setAutoCreateGaps(true);
                basicInfoLayout.setAutoCreateContainerGaps(true);
                basicInfoLayout.setHorizontalGroup(basicInfoLayout.createSequentialGroup()
                        .addGroup(basicInfoLayout.createParallelGroup()
                                .addComponent(jobStartDateLbl)
                                .addComponent(jobEndDateLbl)
                                .addComponent(jobTimeLbl))
                        .addGroup(basicInfoLayout.createParallelGroup()
                                .addComponent(jobStartDateTxt)
                                .addComponent(jobEndDateTxt)
                                .addComponent(jobTimeTxt)));
                basicInfoLayout.setVerticalGroup(basicInfoLayout.createSequentialGroup()
                        .addGroup(basicInfoLayout.createParallelGroup().addComponent(jobStartDateLbl).addComponent(jobStartDateTxt))
                        .addGroup(basicInfoLayout.createParallelGroup().addComponent(jobEndDateLbl).addComponent(jobEndDateTxt))
                        .addGroup(basicInfoLayout.createParallelGroup().addComponent(jobTimeLbl).addComponent(jobTimeTxt)));

                JPanel advanceInfoPane = new JPanel();
                if (type.equalsIgnoreCase("weekly")) {
                    JLabel scheduleDateLbl = new JLabel("Scheduled Date: ");

                    JCheckBox dateCBox1 = new JCheckBox("MON");
                    JCheckBox dateCBox2 = new JCheckBox("TUE");
                    JCheckBox dateCBox3 = new JCheckBox("WED");
                    JCheckBox dateCBox4 = new JCheckBox("THU");
                    JCheckBox dateCBox5 = new JCheckBox("FRI");
                    JCheckBox dateCBox6 = new JCheckBox("SAT");
                    JCheckBox dateCBox7 = new JCheckBox("SUN");

                    weeklyTablePane = new JPanel(new GridLayout(2, 4));
                    weeklyTablePane.add(dateCBox1);
                    weeklyTablePane.add(dateCBox2);
                    weeklyTablePane.add(dateCBox3);
                    weeklyTablePane.add(dateCBox4);
                    weeklyTablePane.add(dateCBox5);
                    weeklyTablePane.add(dateCBox6);
                    weeklyTablePane.add(dateCBox7);

                    scheduleDateLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                    weeklyTablePane.setAlignmentX(Component.LEFT_ALIGNMENT);

                    advanceInfoPane.setLayout(new BoxLayout(advanceInfoPane, BoxLayout.Y_AXIS));
                    advanceInfoPane.add(scheduleDateLbl);
                    advanceInfoPane.add(weeklyTablePane);

                } else if (type.equalsIgnoreCase("monthly")) {
                    jobDateListTxt = new JTextField();
                    advanceInfoPane.setLayout(new GridLayout(2, 1));
                    advanceInfoPane.add(new JLabel("Scheduled Date (1-31, separate by comma): "));
                    advanceInfoPane.add(jobDateListTxt);
                }
                basicInfoPane.setAlignmentX(Component.LEFT_ALIGNMENT);
                advanceInfoPane.setAlignmentX(Component.LEFT_ALIGNMENT);

                this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                this.add(basicInfoPane);
                this.add(advanceInfoPane);
            }

            public String getType() {
                if (type.equalsIgnoreCase("daily")) return "D";
                if (type.equalsIgnoreCase("weekly")) return "W";
                else return "M";
            }

            public String getJobStartDateTxt() {
                return jobStartDateTxt.getText().trim();
            }

            public String getJobEndDateTxt() {
                return jobEndDateTxt.getText().trim();
            }

            public String getJobTimeTxt() {
                StringBuilder jobTimeScript = new StringBuilder();
                String[] jobTimeList = jobTimeTxt.getText().trim().split(",");
                for (int i = 0; i < jobTimeList.length; i++) {
                    String jobTime = jobTimeList[i].trim();
                    if (!jobTime.equals("")) {
                        if (i == 0)
                            jobTimeScript.append(jobTime).append("m");
                        else
                            jobTimeScript.append(",").append(jobTime).append("m");
                    }
                }
                return jobTimeScript.toString();
            }

            public String getDateList() {
                if (type.equalsIgnoreCase("weekly")) {
                    StringBuilder dateScript = new StringBuilder();
                    for (int i = 0; i < 7; i++) {
                        if (weeklyTablePane.getComponent(i) instanceof JCheckBox) {
                            JCheckBox dateSelectCBox = (JCheckBox) weeklyTablePane.getComponent(i);
                            if (dateSelectCBox.isSelected()) {
                                dateScript.append((i + 1) % 7).append(",");
                            }
                        }
                    }
                    if (dateScript.length() >= 1) {
                        return dateScript.substring(0, dateScript.length() - 1);
                    }
                    return null;
                } else {
                    return this.jobDateListTxt.getText().trim();
                }
            }
        }
    }


    private class PopupListener extends MouseAdapter implements ActionListener {
        private JPopupMenu menu;
        private int selectedRow;

        public PopupListener(JPopupMenu menu) {
            this.menu = menu;
            Component[] menuItems = menu.getComponents();
            for (Component mnuItem : menuItems) {
                if (mnuItem instanceof JMenuItem) ((JMenuItem) mnuItem).addActionListener(this);
            }
        }

        public void mousePressed(MouseEvent e) {
            showPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            showPopup(e);
        }

        private void showPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                selectedRow = currentTable.getSelectedRow();
                menu.show(e.getComponent(), e.getX(), e.getY());
            }
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
//            String action = actionEvent.getActionCommand();
            String jobID;
            String script;
            try {
                if (currentTab.tabType == JobManagerTableDisplayTab.SUBMIT_TAB) {
                    jobID = ((BasicString) currentTable.getValueAt(selectedRow, 3)).getString().trim();
                    script = "cancelJob(\"" + jobID + "\")";
                } else if (currentTab.tabType == JobManagerTableDisplayTab.SCHEDULE_TAB) {
                    jobID = ((BasicString) currentTable.getValueAt(selectedRow, 2)).getString().trim();
                    script = "deleteScheduledJob(\"" + jobID + "\")";
                } else {
                    jobID = ((BasicString) currentTable.getValueAt(selectedRow, 3)).getString().trim();
                    script = "cancelConsoleJob(\"" + jobID + "\")";
                }
                conn.run(script);
                Thread.sleep(350);
                currentTab.refresh();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, ex.getMessage());
            }
        }
    }

}
