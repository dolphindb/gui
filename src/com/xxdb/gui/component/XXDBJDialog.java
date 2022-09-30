package com.xxdb.gui.component;

import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

public class XXDBJDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private boolean openState;

	public XXDBJDialog(XXDBEditor parent, String title, Container c) {
		super(parent, title);
//		System.out.println("creating the dialog window.");
		Point p = new Point(400, 400);
		setLocation(p.x, p.y);
		this.openState = false;

		getContentPane().add(c);
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(java.awt.event.WindowEvent e) {
				parent.setDialogOpenState(XXDBJDialog.this, false);
			}

			@Override
			public void windowOpened(java.awt.event.WindowEvent e) {
				parent.setDialogOpenState(XXDBJDialog.this, true);
			}
		});

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setVisible(true);
	}
	
	public void setApplicationModal() {
		setVisible(false);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setModal(true);
		setVisible(true);
	}

	public JRootPane createRootPane() {
		JRootPane rootPane = new JRootPane();
		KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");
		Action action = new AbstractAction() {
			
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
//				System.out.println("escaping the dialog window.");
				setVisible(false);
				dispose();
			}
		};
		InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(stroke, "ESCAPE");
		rootPane.getActionMap().put("ESCAPE", action);
		return rootPane;
	}
	
	public void setOpenState(boolean opened) {
		this.openState = opened;
	}
	
	public boolean getOpenState() {
		return this.openState;
	}

	class MyActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
//			System.out.println("disposing the dialog window.");
			setVisible(false);
			dispose();
		}
	}
}
