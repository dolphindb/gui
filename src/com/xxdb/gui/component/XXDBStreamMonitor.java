package com.xxdb.gui.component;

import com.xxdb.DBConnection;
import com.xxdb.data.BasicTable;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class XXDBStreamMonitor extends JFrame {
    XXDBStreamMonitor.StreamMonitorTab currentTab;
    private DBConnection conn;

    public XXDBStreamMonitor(XXDBEditor editor) throws Exception {
        super("Stream Processing Monitor");
        this.conn = editor.getConnection();
        if (conn == null) throw new Exception("Fail to open job manager: No connection");

        JTabbedPane tabbedPane = null;
        try {
            tabbedPane = createTabbedPane();
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    private JTabbedPane createTabbedPane() throws IOException {

        StreamMonitorTab subConnTab = new XXDBStreamMonitor.StreamMonitorTab(StreamMonitorTab.SUB_CONN_TAB);

        StreamMonitorTab subWorkerTab = new XXDBStreamMonitor.StreamMonitorTab(StreamMonitorTab.SUB_WORKER_TAB);

        StreamMonitorTab pubConnTab = new XXDBStreamMonitor.StreamMonitorTab(StreamMonitorTab.PUB_CONN_TAB);

        StreamMonitorTab pubTableTab = new XXDBStreamMonitor.StreamMonitorTab(StreamMonitorTab.PUB_TABLE_TAB);

        StreamMonitorTab tsAggregatorTab = new StreamMonitorTab(StreamMonitorTab.TS_AGGREGATOR_TAB);
        StreamMonitorTab rsEngineTab = new StreamMonitorTab(StreamMonitorTab.RS_ENGINE_TAB);
        StreamMonitorTab csAggregatorTab = new StreamMonitorTab(StreamMonitorTab.CS_AGGREGATOR_TAB);
        StreamMonitorTab adEngineTab = new StreamMonitorTab(StreamMonitorTab.AD_ENGINE_TAB);

        JTabbedPane aggregatorTab = new JTabbedPane();
        aggregatorTab.addTab("Time Series Aggregator", tsAggregatorTab);
        aggregatorTab.addTab("Reactive Stream Engine", rsEngineTab);
        aggregatorTab.addTab("Cross Sectional Aggregator", csAggregatorTab);
        aggregatorTab.addTab("Anomaly Detection Engine", adEngineTab);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Sub Connections", subConnTab);
        tabbedPane.addTab("Sub Workers", subWorkerTab);
        tabbedPane.addTab("Pub Connections", pubConnTab);
        tabbedPane.addTab("Pub Tables", pubTableTab);
        tabbedPane.addTab("Streaming Engines", aggregatorTab);

        tabbedPane.setSelectedIndex(2);

        return tabbedPane;
    }

    private class StreamMonitorTab extends JPanel implements ActionListener {
        final public static int SUB_CONN_TAB = 1;
        final public static int SUB_WORKER_TAB = 2;
        final public static int PUB_CONN_TAB = 3;
        final public static int PUB_TABLE_TAB = 4;
        final public static int TS_AGGREGATOR_TAB = 5;
        final public static int RS_ENGINE_TAB = 6;
        final public static int CS_AGGREGATOR_TAB = 7;
        final public static int AD_ENGINE_TAB = 8;
        private TableBasedXXDBTableModel tableModel;
        private XXDBJTable table;
        private TableRowSorter<TableBasedXXDBTableModel> sorter;
        private int tabType;


        public StreamMonitorTab(int tabType) {
            this.tabType = tabType;
            createUI();
        }

        private void createUI() {

            JButton refreshBtn = new JButton("Refresh");

            JPanel findBtnPane = new JPanel();

            JTextField findInputTxt = new JTextField(30);
            JButton findBtn = new JButton("Find");
            JButton clearBtn = new JButton("Clear");

            findBtnPane.add(new JLabel("Filter: "));
            findBtnPane.add(findInputTxt);
            findBtnPane.add(findBtn);
            findBtnPane.add(clearBtn);

            JPanel btnPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
            btnPane.add(refreshBtn);
            btnPane.add(findBtnPane);
            btnPane.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

            updateData();

            if (tableModel == null) {
                table = null;
            } else {
                table = new XXDBJTable(tableModel, null);
                table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                sorter = new TableRowSorter<>(tableModel);
                table.setRowSorter(sorter);

                if (tabType == PUB_TABLE_TAB) {
                    table.getColumn("actions").setPreferredWidth(250);
                }
                if (tabType == PUB_CONN_TAB) {
                    table.getColumn("tables").setPreferredWidth(250);
                }
                if (tabType == SUB_WORKER_TAB) {
                    table.getColumn("topic").setPreferredWidth(350);
                }

            }
            JScrollPane tableJsp = new JScrollPane(table);

            this.setLayout(new BorderLayout(5, 5));
            this.add(tableJsp, BorderLayout.CENTER);
            this.add(btnPane, BorderLayout.NORTH);

            refreshBtn.addActionListener(this);
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
        }

        private void updateData() {
            BasicTable data = null;
            try {
                if (tabType == SUB_CONN_TAB)
                    data = (BasicTable) conn.run("getStreamingStat().subConns;");
                else if (tabType == SUB_WORKER_TAB)
                    data = (BasicTable) conn.run("getStreamingStat().subWorkers;");
                else if (tabType == PUB_CONN_TAB)
                    data = (BasicTable) conn.run("getStreamingStat().pubConns;");
                else if (tabType == PUB_TABLE_TAB)
                    data = (BasicTable) conn.run("getStreamingStat().pubTables;");
                else if (tabType == TS_AGGREGATOR_TAB)
                    data = (BasicTable) conn.run("getAggregatorStat().TimeSeriesAggregator");
                else if (tabType == RS_ENGINE_TAB)
                    data = (BasicTable) conn.run("getAggregatorStat().ReactiveStreamEngine");
                else if (tabType == AD_ENGINE_TAB)
                    data = (BasicTable) conn.run("getAggregatorStat().AnomalyDetectionEngine");
                else if (tabType == CS_AGGREGATOR_TAB)
                    data = (BasicTable) conn.run("getAggregatorStat().CrossSectionalAggregator");
                if (data == null) throw new Exception("Unknown tab");
                tableModel = new TableBasedXXDBTableModel(data);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            updateData();
            if (table == null) return;
            table.setModel(tableModel);
            table.updateUI();
            table.adjustWidthAndHeight();
            if (tabType == PUB_TABLE_TAB) {
                table.getColumn("actions").setPreferredWidth(250);
            }
            if (tabType == PUB_CONN_TAB) {
                table.getColumn("tables").setPreferredWidth(250);
            }
            if (tabType == SUB_WORKER_TAB) {
                table.getColumn("topic").setPreferredWidth(350);
            }
        }
    }

}
