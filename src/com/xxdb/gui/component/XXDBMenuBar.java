package com.xxdb.gui.component;

import java.io.*;
import javax.swing.*;

import com.xxdb.gui.common.Utility;

public class XXDBMenuBar {
	public static final int	ITEM_PLAIN	=	0;
	public static final int	ITEM_RADIO	=	1;
	public static final int	ITEM_CHECK	=	2;
	
	public static JMenuItem createMenuItem(JComponent menu, int iType, String sText,
			ImageIcon image, KeyStroke acceleratorKey, String sToolTip){
		return createMenuItem(menu, iType, sText, sText, image,  acceleratorKey, sToolTip);
	}
	
	public static JMenuItem createMenuItem(JComponent menu, int iType, String sText, String command,
			ImageIcon image, KeyStroke acceleratorKey, String sToolTip)	{
		// Create the item
		JMenuItem menuItem;
		
		switch( iType ){
		case ITEM_RADIO:
			menuItem = new JRadioButtonMenuItem();
			break;
		
		case ITEM_CHECK:
			menuItem = new JCheckBoxMenuItem();
			break;
		
		default:
			menuItem = new JMenuItem();
			break;
		}
		
		// Add the item test
		menuItem.setText( sText );
		menuItem.setActionCommand(command);
		
		// Add the optional icon
		if( image != null )
			menuItem.setIcon( image );
		
		// Add the accelerator key
		if(acceleratorKey != null)
			menuItem.setAccelerator(acceleratorKey);
		
		// Add the optional tool tip text
		if( sToolTip != null )
			menuItem.setToolTipText( sToolTip );
				
		menu.add( menuItem );
		
		return menuItem;
	}
	
	public static boolean openFile(JFrame f, XXDBRSyntaxTextArea t) throws XXDBException {
		JFileChooser open = new JFileChooser(); 
		open.setCurrentDirectory(new File(Utility.getLastUsedWorkspace()));
		
		int option = open.showOpenDialog(f); 
		if (option == JFileChooser.APPROVE_OPTION) {
			String path = open.getSelectedFile().getPath();
			try {
				t.setFileContent(path);
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			}
		} else {
			return false;
		}
		
		t.setSaved(true);
		
		return true;
	}
	
	public static void saveFile(JFrame frame, XXDBRSyntaxTextArea t) throws XXDBException {
		String openFilePath = t.getOpenFilePath();
		if(openFilePath != null && !openFilePath.isEmpty()) {
			try {
                OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(openFilePath),"UTF-8");
				//BufferedWriter out = new BufferedWriter(new FileWriter(openFilePath));
				out.write(t.getText());
                out.flush();
				out.close();
			} catch (Exception ex) {
				throw new XXDBException(ex.getMessage());
			}
		} else {
			saveAsFile(frame, t);
		}
		t.setSaved(true);
	}
	
	public static void saveAsFile(JFrame frame, XXDBRSyntaxTextArea t) throws XXDBException {
		JFileChooser save = new JFileChooser();
		save.setCurrentDirectory(new File(t.getOpenFilePath()));
		
		int option = save.showSaveDialog(frame);
		
		if (option == JFileChooser.APPROVE_OPTION) {
			try {
				String saveFilePath = save.getSelectedFile().getPath();
                OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(saveFilePath),"UTF-8");
				//BufferedWriter out = new BufferedWriter(new FileWriter(saveFilePath));
				out.write(t.getText());
                out.flush();
				out.close();
				t.setOpenFilePath(saveFilePath);
			} catch (Exception ex) {
				throw new XXDBException(ex.getMessage());
			}
		}
		t.setSaved(true);
	}
}
