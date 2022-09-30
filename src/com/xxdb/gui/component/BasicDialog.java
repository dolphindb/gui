package com.xxdb.gui.component;

import javax.swing.ImageIcon;
import javax.swing.JDialog;

public class BasicDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	public BasicDialog(){
		this.setIconImage(new ImageIcon(XXDBDataBrowser.class.getResource("/logo.jpg")).getImage());
	}
}
