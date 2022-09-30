package com.xxdb.gui.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPanel extends BasicFrame {
	private static final long serialVersionUID = 1L;
	
	JLabel lblUsername = new JLabel("User Id ");
    JTextField txtUsername = new JTextField();
    JLabel lblPassword = new JLabel("Password ");
    JPasswordField txtPassword = new JPasswordField();
    JButton btnLogin = new JButton("Login");
    XXDBEditor editor = null;

    public LoginPanel(XXDBEditor main) {
        editor = main;
        setLayout(new FlowLayout(FlowLayout.CENTER));
        JPanel topPanel = new JPanel();
        BoxLayout layout=new BoxLayout(topPanel, BoxLayout.Y_AXIS);
        topPanel.setLayout(layout);
        JPanel lpanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lpanel.add(lblUsername);
        topPanel.add(lpanel);
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userPanel.setSize(200, 30);
        txtUsername.setColumns(18);
        txtUsername.setPreferredSize(new Dimension(180, 25));
        userPanel.add(txtUsername);
        topPanel.add(userPanel);

        JPanel pPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pPanel.add(lblPassword);
        topPanel.add(pPanel);
        JPanel passPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtPassword.setColumns(18);
        txtPassword.setPreferredSize(new Dimension(180, 25));
        passPanel.add(txtPassword);
        topPanel.add(passPanel);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (editor.tryLogin(txtUsername.getText(), new String(txtPassword.getPassword()))) {
                    setVisible(false);
                }else{
                    JOptionPane.showMessageDialog(null,"The user name or password is incorrect.");
                }
            }
        });
        btnPanel.add(btnLogin);
        topPanel.add(btnPanel);
        this.add(topPanel);
        this.setSize(300, 200);
        setTitle("Login DolphinDB");
        setLocationRelativeTo(null);
        setResizable(false);
    }
}
