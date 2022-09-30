package com.xxdb.gui.component;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.text.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import com.xxdb.gui.component.XXDBJTable.PopupListener;

public class XXDBTextArea extends JTextPane implements ActionListener{
	private static final long serialVersionUID = 1L;
	private String openFilePath = "";
	HTMLEditorKit editorKit = new HTMLEditorKit();
	private HTMLDocument document;

	public XXDBTextArea() {
		StyleSheet styleSheet = new StyleSheet();
		styleSheet.addRule(".time {color: black; font-weight:bold;}");
		styleSheet.addRule(".plain {color: black; word-wrap:break-word; word-break:break-all;}");
		styleSheet.addRule(".path {color: blue;}");
		styleSheet.addRule(".link { color: #0000ee; text-decoration: underline; }");
		styleSheet.addRule(".error {color: red;}");
		editorKit.setStyleSheet(styleSheet);

		setEditable(false);
		setContentType("text/html");
		setEditorKit(editorKit);
		document = (HTMLDocument) getStyledDocument();
		setText("");

		final JPopupMenu menu=new JPopupMenu();
	    final JMenuItem mnuCopy = new JMenuItem("Copy");    
	    mnuCopy.addActionListener(this);
	    mnuCopy.setActionCommand("Copy");
	    menu.add(mnuCopy);
	    menu.addSeparator();
	    final JMenuItem mnuClear = new JMenuItem("Clear");    
	    mnuClear.addActionListener(this);
	    mnuClear.setActionCommand("Clear");
	    menu.add(mnuClear);
		addHyperlinkListener(
			new XXDBHyperLinkListener()
		);
		
	    addMouseListener(
			new PopupListener(menu)
		);
	}

	public String getOpenFilePath() {
		return openFilePath;
	}

	public void setOpenFilePath(String openFilePath) {
		this.openFilePath = openFilePath;
	}

	public void setFileContent(String path) throws XXDBException {
		setText("");
		try {
			setOpenFilePath(path);
			Scanner scan = new Scanner(new FileReader(path));
			while (scan.hasNext())
				append(scan.nextLine() + "\n");
			scan.close();
		} catch (Exception ex) {
			throw new XXDBException(ex.getMessage());
		}
	}
	
	public void actionPerformed(ActionEvent e){
		String action;
		
		action=e.getActionCommand();
		if(action.equalsIgnoreCase("copy")){
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			TransferHandler transferHandler = getTransferHandler();
			transferHandler.exportToClipboard(this, clipboard, TransferHandler.COPY);
		}
		else if(action.equalsIgnoreCase("clear")){
			setText("");
		}
	}

	public void append(String message) {
		try {
			document.insertAfterEnd(document.getCharacterElement(document.getLength()), message + "<br/>");
		} catch (BadLocationException | IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public class XXDBHyperLinkListener implements HyperlinkListener {
		@Override
		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (!e.getEventType().equals(EventType.ACTIVATED))
				return;
			try {
				Desktop.getDesktop().browse(
					e.getURL().toURI()
				);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
