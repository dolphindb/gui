package com.xxdb.gui.component;

import com.xxdb.DBConnection;
import com.xxdb.data.BasicInt;
import com.xxdb.data.BasicTable;
import com.xxdb.data.EntityBlockReader;
import com.xxdb.io.ProgressListener;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A dialog window for exporting table into CSV.
 */
public class ExportTableDlg extends JFrame implements Runnable {

    private static final long serialVersionUID = 1L;
    private final DBConnection conn = new DBConnection();
    final private int fetchSize = 10000;
    XXDBEditor editor;
    private String script;
    private String fileSavePath;
    private String fileName;
    private EntityBlockReader reader;
    private FileOutputStream stream;
    private OutputStreamWriter writer;
    private ExportProgressFrame pbFrm;


    public ExportTableDlg(XXDBEditor main, String preScript) {
        editor = main;
        String host = main.getServer().getHost();
        int port = main.getServer().getPort();

        //connect
        try {
            conn.connect(host, port, main.getServer().getUsername(), main.getServer().getPassword());
            if (preScript != null) {
                conn.run(preScript, (ProgressListener) null);
//                System.out.println(preScript);
            }
        } catch (Exception e) {
            editor.displayError(e);
            dispose();
            return;
        }

        //gui
        JLabel lblFilePath = new JLabel("Save Path: ");
        JTextField txtFilePath = new JTextField(25);
        txtFilePath.setBackground(Color.WHITE);
        JButton btnFilePath = new JButton("Browse");
        JPanel filePathPane = new JPanel();
        filePathPane.add(lblFilePath);
        filePathPane.add(txtFilePath);
        filePathPane.add(btnFilePath);

        JTextArea txtScript = new JTextArea("Please enter sql to export");
        txtScript.setForeground(Color.GRAY);
        JScrollPane jsp = new JScrollPane(txtScript);
        jsp.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));

        JButton btnExport = new JButton("Export");
        JButton btnCancel = new JButton("Cancel");

        JPanel btnPane = new JPanel();
        btnPane.add(btnExport);
        btnPane.add(btnCancel);

        setLayout(new BorderLayout());
        add(filePathPane, BorderLayout.NORTH);
        add(jsp, BorderLayout.CENTER);
        add(btnPane, BorderLayout.SOUTH);

        setResizable(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setTitle("Export Table");
        setBounds(10, 10, 10, 10);
        setSize(700, 500);
        setLocationRelativeTo(editor);
        setAlwaysOnTop(true);

        btnFilePath.addActionListener(actionEvent -> {
            JFileChooser save = new JFileChooser();
            save.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isDirectory() || file.getName().endsWith(".csv") || file.getName().endsWith(".CSV");
                }

                @Override
                public String getDescription() {
                    return null;
                }
            });
            int option = save.showSaveDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                fileSavePath = save.getSelectedFile().getPath();
                fileName = save.getSelectedFile().getName();
                if (!fileSavePath.endsWith(".csv") && !fileSavePath.endsWith(".CSV")) {
                    fileSavePath += ".csv";
                    fileName += ".csv";
                }
                txtFilePath.setText(fileSavePath);
            }
        });

        txtFilePath.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent focusEvent) {
            }

            @Override
            public void focusLost(FocusEvent focusEvent) {
                String temp = txtFilePath.getText().trim();
                if (!temp.equals("") && !temp.endsWith(".csv") && !temp.endsWith(".CSV")) {
                    temp += ".csv";
                }
                txtFilePath.setText(temp);
                fileSavePath = temp;
                fileName = fileSavePath.substring(fileSavePath.lastIndexOf("/") + 1);
            }
        });

        txtScript.addFocusListener(new FocusListener() {
            boolean isFirst = true;

            @Override
            public void focusGained(FocusEvent focusEvent) {
                if (isFirst) {
                    txtScript.setText("");
                    txtScript.setForeground(Color.BLACK);
                    isFirst = false;
                }
            }

            @Override
            public void focusLost(FocusEvent focusEvent) {
                if (txtScript.getText().equals("")) {
                    txtScript.setText("Please enter sql to export");
                    txtScript.setForeground(Color.GRAY);
                    isFirst = true;
                } else {
                    txtScript.setForeground(Color.BLACK);
                }
            }
        });

        btnExport.addActionListener(actionEvent -> {
            script = txtScript.getText().trim();
            if (fileSavePath == null || script == null || fileSavePath.trim().equals("") || script.trim().equals("")) {
                editor.displayError(new Exception("Export table failed: file path or script cannot be null"));
                dispose();
                return;
            }
            dispose();
            new Thread(this).start();
        });
        btnCancel.addActionListener(actionEvent -> dispose());

    }

    public ExportTableDlg(XXDBEditor main) {
        this(main, null);
    }

    @Override
    public void run() {
        try {
            pbFrm = new ExportProgressFrame(fileName, fileSavePath);
            int indexCsv = 0;
            if (fileSavePath.contains("\\")){
                indexCsv = fileSavePath.lastIndexOf("\\");
            }else if (fileSavePath.contains("/")){
                indexCsv = fileSavePath.lastIndexOf("/");
            }
            String dirPath = fileSavePath.substring(0, indexCsv);
            File checkFile = new File(dirPath);
            if(!checkFile.exists()){
                throw new Exception("The path does not exists");
            }
            readTable();
            editor.displayMessage("The file in " + fileSavePath + " Export successfully!");
        } catch (Exception e) {
            pbFrm.dispose();
            editor.displayError(new Exception("Export table to "  + fileSavePath + " failed: " + e.getMessage()));
        }

    }

    /**
     * Read table from server
     *
     * @return the basic table sent back from server
     * @throws IOException
     */
    public void readTable() throws Exception {
        int lastLineIndex = script.lastIndexOf("\n") + 1;
        int total = 0;
        if (lastLineIndex < script.length()) {
            conn.run(script.substring(0, lastLineIndex));
            String lastLine = script.substring(script.trim().lastIndexOf("\n") + 1);
            total = getTotal(lastLine);
            reader = (EntityBlockReader) conn.run(lastLine, (ProgressListener) null,
                    4, 4, fetchSize);
        } else {
            reader = (EntityBlockReader) conn.run(script, (ProgressListener) null,
                    4, 4, fetchSize);
        }
        File csvFile = new File(fileSavePath);
        csvFile.createNewFile(); // if file already exists will do nothing

        stream = new FileOutputStream(fileSavePath, false);
        writer = new OutputStreamWriter(stream, "gb2312");
        BasicTable table = (BasicTable) reader.read();
        if (table == null) throw new Exception("Cannot export empty table");
        for (int i = 0; i < table.columns(); i++) {
            if (i == 0)
                writer.write(table.getColumnName(i));
            else {
                writer.write(",");
                writer.write(table.getColumnName(i));
            }
        }
        exportCSV(table);
        int cnt = 0;
        while (reader.hasNext()) {
            table = (BasicTable) reader.read();
            exportCSV(table);
            pbFrm.setCurrentProgress(cnt++ * fetchSize * 100 / total);
        }
        writer.close();
        pbFrm.setCurrentProgress(100);
    }

    /**
     * Get total number of rows of the table exported
     *
     * @param lastLine last line of the script entered by user
     * @return total number of rows
     */
    private int getTotal(String lastLine) throws Exception {
        try {
            BasicInt obj = (BasicInt) conn.run("exec count(*) from " + lastLine + "\n");
            return obj.getInt();
        } catch (Exception ex) {
            throw new Exception("Invalid script" + ex.getMessage());
        }
    }

    /**
     * Export a basic table into CSV
     *
     * @param table the basic table to export
     */
    public void exportCSV(BasicTable table) throws Exception {
        try {
            for (int i = 0; i < table.rows(); i++) {
                writer.write("\n");
                String line = table.getRowJson(i);
                Matcher m = Pattern.compile(",.*?:").matcher(line);
                line = m.replaceAll(",");
                line = line.substring(line.indexOf(":") + 1, line.length() - 1);
                writer.write(line);
            }

        } catch (Exception e) {
            throw new Exception("Export to csv failed");
        }
    }


}

class ExportProgressFrame extends JFrame {

    private final String fileName;
    private final String filePath;
    private final JProgressBar progressBar;
    private final int MIN_PROGRESS = 0;
    private final int MAX_PROGRESS = 100;
    private JLabel status;
    private JLabel openFile;
    private int currentProgress = MIN_PROGRESS;

    public ExportProgressFrame(String fileName, String path) throws HeadlessException {
        super(fileName);
        this.fileName = fileName;
        this.filePath = path;

        status = new JLabel("Start exporting...");

        progressBar = new JProgressBar();
        progressBar.setMinimum(MIN_PROGRESS);
        progressBar.setMaximum(MAX_PROGRESS);
        progressBar.setValue(currentProgress);
        progressBar.setStringPainted(true);

        openFile = new JLabel();
        openFile.setText("<html><u>" + filePath + "</u></html>");
        openFile.setForeground(Color.BLACK);

        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(status);
        panel.add(progressBar);
        panel.add(openFile);

        add(panel);

        setSize(450, 150);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);

        progressBar.addChangeListener(changeEvent -> {
            if (currentProgress == 100) {
                updateStatus();
                enableOpenFile();
                setState(JFrame.NORMAL);
                toFront();
            }
        });
    }

    public void setCurrentProgress(int currentProgress) {
        this.currentProgress = currentProgress;
        progressBar.setValue(currentProgress);
    }

    private void updateStatus() {
        if (currentProgress >= 100) {
            status.setText("Export complete");
            openFile.setForeground(Color.BLUE);
        }
        else status.setText("Start exporting");
    }

    /**
     * This function enables users to open the path they save their files to.
     */
    public void enableOpenFile() {
        openFile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                try {
                    Desktop.getDesktop().open(new File(filePath));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Invalid action");
                    e.printStackTrace();
                }
            }
        });
    }
}
