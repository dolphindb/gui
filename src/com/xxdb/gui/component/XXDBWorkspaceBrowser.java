package com.xxdb.gui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.xxdb.gui.common.Utility;

public class XXDBWorkspaceBrowser extends JDialog{
	private static final long serialVersionUID = 1L;
	
	private JLabel lName = new JLabel("Workspace");
	private JLabel eEmptyText = new JLabel("");
	private JTextField tName = new JTextField();
	private JButton btnBrowse = new JButton("Browse");
	private JButton btnOk = new JButton("Ok");
	private JButton btnCancel = new JButton("Cancel");
	

	double adjrate = Utility.getAdjustRate();
	
	public XXDBWorkspaceBrowser() {
		super((JDialog)null, true);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		setSize((int)(adjrate*500),(int)(adjrate*175));
		this.setResizable(false);
		setLocation((screenSize.width  - getSize().width) / 2, 
				(screenSize.height - getSize().height) / 2);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setModal(true);
		
		this.setIconImage(new ImageIcon(XXDBEditor.class.getResource("/logo.jpg")).getImage());		
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setTitle("select a folder as workspace");
		this.setLayout(new FlowLayout(FlowLayout.CENTER));

		int adjWidth  = (int)(84*adjrate);
		int adjHeight  = (int)(20*adjrate);
		
		final JPanel top = new JPanel();

		top.setLayout(new FlowLayout(FlowLayout.LEFT,5,20));
				
		this.add(top,BorderLayout.NORTH);
	
		top.add(eEmptyText,BorderLayout.NORTH);
		lName.setBounds(20, 50, adjWidth, adjHeight);
	    top.add(lName,BorderLayout.WEST);
	    tName.setBackground(Color.WHITE);
	    tName.setEditable(true);
	    tName.setPreferredSize(new Dimension((int)(280*adjrate),adjHeight));
	    tName.setText(Utility.getLastUsedWorkspace());
	    
	    top.add(tName,BorderLayout.CENTER);
	    
	    btnBrowse.setBounds(400, 50, adjWidth, adjHeight);
	    top.add(btnBrowse,BorderLayout.EAST);
	    top.add(eEmptyText,BorderLayout.SOUTH);
	    btnBrowse.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser open = new JFileChooser();
		        open.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				open.setCurrentDirectory(new File(Utility.getLastUsedWorkspace()));
				
				int option = open.showOpenDialog(btnBrowse); 
				if (option == JFileChooser.APPROVE_OPTION) {
					String path = open.getSelectedFile().getPath();
					tName.setText(path);
					eEmptyText.setText("");
				}
			}
		});
	    
	    
		final JPanel bottom = new JPanel();
		bottom.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		this.add(bottom,BorderLayout.SOUTH);
	    btnOk.addActionListener(new ActionListener(){
	    	public void actionPerformed(ActionEvent e){
	    		String selectedWorkspace = tName.getText()==null ? "" : tName.getText().trim();
	    		if(!selectedWorkspace.isEmpty()) {	    			
		    		dispose();
		    		Utility.setLastUsedWorkspace(selectedWorkspace);
	    		} else {
	    			eEmptyText.setText("Please select a workspace");
	    		}
	    	}
	    });
	    
	    btnCancel.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();				
			}
		});
	    
	    eEmptyText.setForeground(Color.RED);
	    eEmptyText.setBounds(20, 100, (int)(200 * adjrate), adjHeight);
	    bottom.add(eEmptyText, BorderLayout.EAST);
	    	    
	    btnOk.setBounds(300, 100, adjWidth, adjHeight);
	    bottom.add(btnOk, BorderLayout.EAST);
	    
	    btnCancel.setBounds(400, 100, adjWidth, adjHeight);
	    bottom.add(btnCancel, BorderLayout.EAST);
	}	
}
