package com.xxdb.gui.component;

import javax.swing.tree.DefaultMutableTreeNode;

public class VariableNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = 1L;
	
	public VariableNode(Object nodeText) {
		super(nodeText);
	}
	
	@Override
	public boolean isLeaf(){
		String nodeText = getUserObject().toString();
		if(nodeText.equalsIgnoreCase("variables")||nodeText.equalsIgnoreCase("Table")||nodeText.equalsIgnoreCase("Matrix")||
				nodeText.equalsIgnoreCase("Vector")||nodeText.equalsIgnoreCase("Scalar/Pair")||nodeText.equalsIgnoreCase("Dictionary")||
				nodeText.equalsIgnoreCase("Set")||nodeText.equalsIgnoreCase("Local Variables")||nodeText.equalsIgnoreCase("Shared Tables") ||
				nodeText.equalsIgnoreCase("Object"))
				return false;
		return super.isLeaf();
	}

}
