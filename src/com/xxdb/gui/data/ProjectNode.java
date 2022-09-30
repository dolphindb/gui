package com.xxdb.gui.data;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.io.File;

public class ProjectNode extends DefaultMutableTreeNode{
	private static final long serialVersionUID = 1L;
	
	public enum NodeType {WORKSPACE, PROJECT, FOLDER, PACKAGE, MODULE, SCRIPT, FILE}
	
	private String dirpath;	
	private boolean isDir = false;
	private NodeType nodeType;
	
	public ProjectNode(String dirpath, String name, NodeType nodeType) {
		this(dirpath, name, false, nodeType);
	}
	
	public ProjectNode(String dirpath, String name, boolean isDir, NodeType nodeType) {
		super(name);
		this.dirpath = dirpath;
		this.isDir = isDir;
		this.nodeType = nodeType;
	}
	
	public ProjectNode(ProjectNode node) {
		this(node.dirpath, node.getName(), node.isDir, node.nodeType);
	}

	public String getName() {
		return dirpath.substring(dirpath.lastIndexOf(File.separator)+1);
	}
	
	public String getDirPath() {
		return dirpath;
	}
	
	public TreePath getTreePath() {
		TreeNode[] nodes = getPath();
		return new TreePath(nodes);
	}

	public void setPath(String dirpath) {
		this.dirpath = dirpath;
		String newName = this.getName();
		this.setName(newName);
	}
	public void setName(String name) {
		super.setUserObject(name);
	}
	public boolean isDir() {
		return isDir;
	}
	public void setDir(boolean isDir) {
		this.isDir = isDir;
	}
	public NodeType getNodeType(){
		return nodeType;
	}
	
	@Override
	public boolean getAllowsChildren(){
		return isDir;
	}
	
	@Override
	public boolean isLeaf(){
		return !isDir;
	}
	
}
