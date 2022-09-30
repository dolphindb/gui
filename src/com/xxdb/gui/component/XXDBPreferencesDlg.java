package com.xxdb.gui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import com.xxdb.gui.common.Utility;

public class XXDBPreferencesDlg extends JDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	double adjrate = Utility.getAdjustRate();

	final JPanel pnlLeft = new JPanel();
	final JPanel pnlRight = new JPanel();
	final JPanel pnlBottom = new JPanel();
	
	private JButton mnuDisplay = new JButton("display");
	
	private DisplayPreferenceSettingPanel pnlDisplay;
	
	private JButton btnOK = new JButton("OK");
	private JButton btnCancel = new JButton("Cancel");
	
	private PreferenceSettingPanel currentPanel;
	
	private XXDBEditor parent;
	
	private void initializeComponents()	{
		setSize((int)(adjrate*550),(int)(adjrate*430));
		setLocationRelativeTo(null);
		
		this.setIconImage(new ImageIcon(XXDBEditor.class.getResource("/logo.jpg")).getImage());
		this.setTitle("Preferences");
		this.setLayout(new BorderLayout());
		this.setBackground(new Color(255,255,255));

		this.add(pnlLeft,BorderLayout.WEST);

		pnlRight.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		pnlRight.setBorder(BorderFactory.createEtchedBorder());
		this.add(pnlRight,BorderLayout.CENTER);
		
		pnlBottom.setLayout(new FlowLayout(FlowLayout.RIGHT));
		this.add(pnlBottom, BorderLayout.SOUTH);
		
		pnlBottom.add(btnOK);
		
		
		
		pnlBottom.add(btnCancel);
		
		
		pnlBottom.setBorder(BorderFactory.createEtchedBorder());
		
		pnlLeft.setLayout(new FlowLayout(FlowLayout.CENTER));
		pnlLeft.setBorder(BorderFactory.createEtchedBorder());
		pnlLeft.add(mnuDisplay);
		pnlLeft.setVisible(false);
	
		pnlDisplay = new DisplayPreferenceSettingPanel(parent);
		pnlRight.add(pnlDisplay);
		this.currentPanel = pnlDisplay;
	}
	
	
	public XXDBPreferencesDlg(XXDBEditor parent) {
		super((JDialog)null, true);

		this.parent = parent;
		initializeComponents();
		
		mnuDisplay.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				pnlDisplay.setVisible(true);
			}
		
		});
		
		btnOK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				saveCurrentSetting();	
				dispose();
			}
		});
		
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
	}
	
	private void saveCurrentSetting( ){
		this.currentPanel.Save();
		
	}
	
}
