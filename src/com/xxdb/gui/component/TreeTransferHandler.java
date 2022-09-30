package com.xxdb.gui.component;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.nio.file.Files;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.xxdb.gui.data.ProjectNode;
import com.xxdb.gui.data.ProjectNode.NodeType;

public class TreeTransferHandler extends TransferHandler {
	private static final long serialVersionUID = 1L;

	public static final DataFlavor nodesFlavor = new DataFlavor(ProjectNode.class, ""); // TODO: complete the mime type;
	private final DataFlavor[] flavors = new DataFlavor[1];

	private XXDBFileTree parent;
	private DefaultTreeModel treeModel;
	private ProjectNode nodeToInsert;
	private NodesTransferable nodesTransferable;
	private Clipboard clipboard;

	private XXDBConflictPane conflictPane;

	public TreeTransferHandler(XXDBFileTree parent, DefaultTreeModel treeModel, Clipboard clipboard) {
		flavors[0] = nodesFlavor;
		this.parent = parent;
		this.treeModel = treeModel;
		this.clipboard = clipboard;
	}

	public int getSourceActions(JComponent c) {
		return COPY_OR_MOVE;
	}
	
	protected NodesTransferable createTransferable(JComponent c) {
		TreePath[] treePaths = ((XXDBFileTree) c).getSelectionPaths();
		if (treePaths == null) {
			nodesTransferable = new NodesTransferable(null);
			return nodesTransferable;
		}
		ProjectNode[] nodes = new ProjectNode[treePaths.length];

		if (treePaths.length > 0) {
			/* Check if all selected nodes are in the same directory */
			nodes[0] = (ProjectNode) treePaths[0].getLastPathComponent();
			ProjectNode firstParent = (ProjectNode) nodes[0].getParent();
			notAllowed: {
				for (int i = 1; i < treePaths.length; i++) {
					nodes[i] = (ProjectNode) treePaths[i].getLastPathComponent();
					ProjectNode curParent = (ProjectNode) nodes[i].getParent();
					if (!firstParent.equals(curParent)) {
						nodesTransferable = new NodesTransferable(null);
						break notAllowed;
					}
				}
				nodesTransferable = new NodesTransferable(nodes);
			}
		} else {
			nodesTransferable = new NodesTransferable(null);
		}
		clipboard.setContents(nodesTransferable, null);
		return nodesTransferable;
	}

	protected void exportDone(JComponent source, Transferable data, int action) {
		if (action != MOVE) {
			return;
		}

		if (data != null) {
			try {
				ProjectNode[] nodes = (ProjectNode[]) ((NodesTransferable) data).getTransferData(nodesFlavor);
				boolean[] renameConflicts = nodesTransferable.getRenameConflicts();
				for (int i = 0; i < nodes.length; i++) {
					if (renameConflicts[i])
						treeModel.removeNodeFromParent(nodes[i]);
				}
			} catch (Exception ex) {

			}
		}
	}
	
	public boolean canImport(TransferHandler.TransferSupport support) {
		if (support.isDataFlavorSupported(nodesFlavor)) {
			if (support.isDrop()) {
				JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
				TreePath path = dl.getPath();
				if (path == null)
					return false;
				nodeToInsert = (ProjectNode)path.getLastPathComponent();
			}
			else {
				nodeToInsert = (ProjectNode)parent.getSelectionPath().getLastPathComponent();
			}
			
			ProjectNode[] nodes;
			try {
				nodes = (ProjectNode[]) support.getTransferable().getTransferData(nodesFlavor);
			} catch (UnsupportedFlavorException e) {
				return false;
			} catch (java.io.IOException e) {
				return false;
			}
			
			if (nodes == null)
				return false;
			if (!isMoveOrCopyTypeCorrect(nodes[0], nodeToInsert))
				return false;
			// Check if the node to move is the ancestor
			if (nodeToInsert.getDirPath().startsWith(nodes[0].getDirPath()))
				return false;

			return true;
		}
		else if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			return true;
		}
		else
			return false;
	}

	public static boolean isMoveOrCopyTypeCorrect(ProjectNode node, ProjectNode nodeToInsert) {
		/* Test node type:
		 * FILE can only be moved to FOLDER
		 * FOLDER can only be moved to FOLDER
		 * MODULE can only be moved to PACKAGE
		 * PACKAGE can only be moved to PACKAGE
		 */
		if (nodeToInsert.getNodeType() == NodeType.FOLDER) {
			NodeType nodeType = node.getNodeType();
			if (nodeType != NodeType.FILE && nodeType != NodeType.FOLDER) {
				return false;
			}
		} else if (nodeToInsert.getNodeType() == NodeType.PACKAGE){
			NodeType nodeType = node.getNodeType();
			if (nodeType != NodeType.MODULE && nodeType != NodeType.PACKAGE) {
				return false;
			}
		} else {
			return false;
		}
		return true;
	}
	
	public boolean importData(TransferHandler.TransferSupport support) {
		if (!canImport(support))
			return false;
		else if (support.isDataFlavorSupported(nodesFlavor)) {
			return importNodesData(support);
		}
		else if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
			return importFileData(support);
		else
			return false;
	}

	private boolean importFileData(TransferHandler.TransferSupport support) {
		TreePath path = null;
		try {
			@SuppressWarnings("unchecked")
			List<File> files = (List<File>)support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

			path = parent.getSelectionPath();
			nodeToInsert = (ProjectNode)path.getLastPathComponent();
			
			for (File file : files) {
				copyFileFromTo(file, nodeToInsert);
			}
		} catch (IOException ex) {
			return false;
		} catch (UnsupportedFlavorException e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	private void copyFileFromTo(File file, ProjectNode nodeToInsert) {
		try {
			File copied = new File(nodeToInsert.getDirPath() + File.separator + file.getName());
			Files.copy(file.toPath(), copied.toPath());
			ProjectNode curNode = null;
			if (file.isDirectory()) {
				curNode = new ProjectNode(copied.getAbsolutePath(), file.getName(), true, NodeType.FOLDER);
				parent.insertNodeInto(curNode, nodeToInsert);
				for (String childPath : file.list()) {
					File childFile = new File(file.getAbsolutePath() + File.separator + childPath);
					copyFileFromTo(childFile, curNode);
				}
			}
			else {
				curNode = new ProjectNode(copied.getAbsolutePath(), file.getName(), false, NodeType.FILE);
				parent.insertNodeInto(curNode, nodeToInsert);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private boolean importNodesData(TreeTransferHandler.TransferSupport support) {
		TreePath path = null;
		if (support.isDrop()) {
			JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
			path = dl.getPath();
			nodeToInsert = (ProjectNode)path.getLastPathComponent();
		}
		else {
			path = parent.getSelectionPath();
			nodeToInsert = (ProjectNode)path.getLastPathComponent();
		}

		ProjectNode[] nodes;
		ProjectNode copied = null;
		try {
			nodes = (ProjectNode[])support.getTransferable().getTransferData(nodesFlavor);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		ProjectNode oldParent = (ProjectNode)nodes[0].getParent();

		for (int i = 0; i < nodes.length; i++) {
			ProjectNode curNode = nodes[i];
			
			String oldFolderPath = curNode.getDirPath();
			int oldIndex = oldParent.getIndex(curNode);

			treeModel.insertNodeInto(curNode, nodeToInsert, nodeToInsert.getChildCount());

			String newFolderPath = nodeToInsert.getDirPath() + File.separator + curNode.getName();
			File newDir = new File(newFolderPath);
			ProjectNode newParent = (ProjectNode) curNode.getParent();
			int newIndex = newParent.getIndex(curNode);

			/* Check conflict */
			try {
				if (support.isDrop()) {
					if (newDir.exists()) {
						treeModel.removeNodeFromParent(curNode);
						conflictPane = new XXDBConflictPane(curNode, nodeToInsert, treeModel, parent,
															oldFolderPath, newFolderPath, oldParent, newParent,
															oldIndex, newIndex);
						nodesTransferable.setRenameConflict(i, conflictPane.getImportDone());
					}
					else {
						parent.renameNodeFromTo(curNode, oldFolderPath, newFolderPath);
					}
				} // end if (support.isDrop())
				else { // copy
					while (newDir.exists()) {
						if (newFolderPath.contains("."))
							newFolderPath = newFolderPath.replaceFirst("\\.", "_copy.");
						else
							newFolderPath = newFolderPath + "_copy";
						newDir = new File(newFolderPath);
					}
					copied = parent.copyNodeFromTo(curNode, oldFolderPath, newFolderPath);
					treeModel.removeNodeFromParent(curNode);
					treeModel.insertNodeInto(copied, nodeToInsert, nodeToInsert.getChildCount());
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(parent, ex.getMessage(), "DolphinDB Error", JOptionPane.ERROR_MESSAGE);
			}
			/* Finish conflict checking */
		}
		
		TreePath newPath = support.isDrop() ? path.pathByAddingChild(nodes[0]) : path.pathByAddingChild(copied);
		
		parent.makeVisible(newPath);
		parent.scrollPathToVisible(newPath);
		parent.setSelectionPath(newPath);

		return true;
	}

	private class NodesTransferable implements Transferable {
		private ProjectNode[] nodes = null;
		private boolean[] renameConflicts = null;
		
		public NodesTransferable(ProjectNode[] nodes) {
			this.nodes = nodes;
			renameConflicts = new boolean[nodes.length];
			Arrays.fill(renameConflicts, true);
		}
		
		public boolean[] getRenameConflicts() {
			return renameConflicts;
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
			if (!isDataFlavorSupported(flavor))
				throw new UnsupportedFlavorException(flavor);
			
			return nodes;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return flavors;
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return nodesFlavor.equals(flavor);
		}
		
		public void setRenameConflict(int index, boolean renamed) {
			renameConflicts[index] = renamed;
		}
	}
}