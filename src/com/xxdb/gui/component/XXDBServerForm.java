package com.xxdb.gui.component;

import com.xxdb.gui.data.Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class XXDBServerForm extends JPanel{
	private static final long serialVersionUID = 1L;
	
	private JLabel lName = new JLabel("Name");
	private JTextField tName = new JTextField();
	private JLabel eName = new JLabel("*");
	private JLabel lHost = new JLabel("Host");
	private JTextField tHost = new JTextField();
	private JLabel eHost = new JLabel("*");
	private JLabel lPort = new JLabel("Port");
	private JTextField tPort = new JTextField();
	private JLabel ePort = new JLabel("*");
	private JLabel lRemoteDir = new JLabel("Remote Directory");
	private JTextField tRemoteDir = new JTextField();
	
	private JLabel lUsername = new JLabel("Username");
	private JTextField tUsername = new JTextField();
	
	private JLabel lPassword = new JLabel("Password");
	private JPasswordField tPassword = new JPasswordField();
	
	public XXDBServerForm() {
		
		GridBagLayout panelGridBagLayout = new GridBagLayout();
	    panelGridBagLayout.columnWidths = new int[] { 86, 86, 86, 86, 0 };
	    panelGridBagLayout.rowHeights = new int[] { 20, 20, 20, 20, 20, 20, 20, 20, 0 };
	    panelGridBagLayout.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
	    panelGridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
	    this.setLayout(panelGridBagLayout);

	    tName.setColumns(15);
	    eName.setMaximumSize(new Dimension(1000, 100));
	    addComponentsToGrid(lName, tName, eName, 1, this);
	    tHost.setColumns(15);
	    addComponentsToGrid(lHost, tHost, eHost, 2, this);
	    tPort.setSize(10, 10);
	    tPort.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char vChar = e.getKeyChar();
                if (!(Character.isDigit(vChar)
                        || (vChar == KeyEvent.VK_BACK_SPACE)
                        || (vChar == KeyEvent.VK_DELETE))) {
                    e.consume();
                }
            }
        });
	    addComponentsToGrid(lPort, tPort, ePort, 3, this);
	    tRemoteDir.setColumns(15);
	    addComponentsToGrid(lRemoteDir, tRemoteDir, null, 4, this);
	    
	    addComponentsToGrid(lUsername, tUsername, null, 5, this);
	    addComponentsToGrid(lPassword, tPassword, null, 6, this);
	}
	
	public Server getServer() {
		String name = this.tName.getText();
		String host = this.tHost.getText();
		String port = this.tPort.getText();
		String remoteDir = this.tRemoteDir.getText();
		
		String username = this.tUsername.getText();
		String password = new String(this.tPassword.getPassword());
		password = password.isEmpty() ? "" : new String(this.tPassword.getPassword());
		
		if(name != null && host != null && port != null) {
			Server svr = new Server(name, host, Integer.parseInt(port), remoteDir,username,password);
			return svr;
		}
		return null;
	}
	
	private void addComponentsToGrid(JComponent comp1, JComponent comp2, 
			JComponent comp3, int yPos, Container serverPanel) {
	    
	    GridBagConstraints gridBagConstraintForLabel = new GridBagConstraints();
	    gridBagConstraintForLabel.fill = GridBagConstraints.BOTH;
	    gridBagConstraintForLabel.insets = new Insets(0, 0, 5, 5);
	    gridBagConstraintForLabel.gridx = 1;
	    gridBagConstraintForLabel.gridy = yPos;
	    serverPanel.add(comp1, gridBagConstraintForLabel);

	    if(comp2 != null) {
		    GridBagConstraints gridBagConstraintForTextField = new GridBagConstraints();
		    gridBagConstraintForTextField.fill = GridBagConstraints.BOTH;
		    gridBagConstraintForTextField.insets = new Insets(0, 0, 5, 0);
		    gridBagConstraintForTextField.gridx = 2;
		    gridBagConstraintForTextField.gridy = yPos;
		    serverPanel.add(comp2, gridBagConstraintForTextField);
	    }
	    
	    if(comp3 != null) {
		    GridBagConstraints gridBagConstraintForTextField = new GridBagConstraints();
		    gridBagConstraintForTextField.fill = GridBagConstraints.BOTH;
		    gridBagConstraintForTextField.insets = new Insets(0, 0, 5, 0);
		    gridBagConstraintForTextField.gridx = 3;
		    gridBagConstraintForTextField.gridy = yPos;
		    serverPanel.add(comp3, gridBagConstraintForTextField);
	    }
	}
	
}
