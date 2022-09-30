package com.xxdb.gui.component;

import javax.swing.*;

public class BasicFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	public BasicFrame(){
		this.setIconImage(new ImageIcon(XXDBDataBrowser.class.getResource("/logo.jpg")).getImage());
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
}
