package com.xxdb.gui.component;

import com.xxdb.DBConnection;
import com.xxdb.data.Entity;
import com.xxdb.gui.common.Utility;

import javax.swing.*;
import java.awt.*;

public class XXDBEditorPane extends JPanel {
    XXDBRSyntaxTextArea textArea;

    public XXDBEditorPane(XXDBEditor editor) {
        DBConnection conn = editor.getConnection();
        JButton button = new JButton("Execute");
        textArea = new XXDBRSyntaxTextArea(editor);
        XXDBRTextScrollPane textScrollPane = new XXDBRTextScrollPane(textArea);
        textScrollPane.setPreferredSize(new Dimension(300, 200));
        textScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        JTextField textField = new JTextField();
        textField.setEditable(false);
        this.setLayout(new BorderLayout());
        this.add(button, BorderLayout.NORTH);
        this.add(textScrollPane, BorderLayout.CENTER);
        this.add(textField, BorderLayout.SOUTH);
        button.addActionListener(actionEvent -> {
            System.out.println(textArea.getText());
            try {
                conn.run(textArea.getText());
                textField.setText("Run succeeds");
            } catch (Exception ex) {
                textField.setText(ex.getMessage());
            }
        });
    }

    public String getText() {
        return textArea.getText();
    }
    public void setText(String txt) {
        textArea.setText(txt);
    }

    public void clearText() {
        textArea.setText("");
    }
}
