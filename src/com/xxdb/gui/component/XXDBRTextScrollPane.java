package com.xxdb.gui.component;

import javax.swing.tree.TreePath;

import org.fife.ui.rtextarea.RTextScrollPane;

import com.xxdb.gui.data.ProjectNode;

public class XXDBRTextScrollPane extends RTextScrollPane{
	private static final long serialVersionUID = 1L;
	
	private XXDBRSyntaxTextArea tArea;
	private TreePath nodePath = null;
	
	public XXDBRTextScrollPane(XXDBRSyntaxTextArea textArea) {
		super(textArea);
		textArea.setDolphinDBCodeStyle();
		this.tArea = textArea;
	}
	
	public XXDBRSyntaxTextArea getTextArea() {
		return this.tArea;
	}
	
	public String getFilePath() {
		return tArea.getOpenFilePath();
	}
	
	public void setNode(ProjectNode node) {
		tArea.setNode(node);
	}
	
	public void setNodePath(TreePath nodePath) {
		this.nodePath = nodePath;
	}

	public TreePath getNodePath() {
		return nodePath;
	}
}
