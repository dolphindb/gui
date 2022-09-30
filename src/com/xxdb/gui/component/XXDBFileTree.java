package com.xxdb.gui.component;

import com.xxdb.gui.common.Utility;
import com.xxdb.gui.data.ProjectNode;
import com.xxdb.gui.data.ProjectNode.NodeType;
import com.xxdb.gui.data.Server;
import com.xxdb.gui.data.Workspace;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class XXDBFileTree extends JTree implements ActionListener{
	public final int DEFAULT_PORT = 8848;
	private static final long serialVersionUID = 1L;
	private static final ArrayList<String> ignoreFiles = new ArrayList<String>(Arrays.asList(".project", ".workspace"));
	
	private final JMenuItem mnuRun = new JMenuItem("Run");
	private final JMenuItem mnuUnitTest = new JMenuItem("Unit Test");
	private final JMenuItem mnuNewProject = new JMenuItem("New Project");
	private final JMenuItem mnuNewPackage = new JMenuItem("New Package");
	private final JMenuItem mnuNewFolder = new JMenuItem("New Folder");
	private final JMenuItem mnuImportFolder = new JMenuItem("Import Folder");
	private final JMenuItem mnuImportProject = new JMenuItem("Import Project");
	private final JMenuItem mnuNewModule = new JMenuItem("New Module");
	private final JMenuItem mnuNewFile = new JMenuItem("New File");
	private final JMenuItem mnuDelete = new JMenuItem("Delete");
	private final JMenuItem mnuRename = new JMenuItem("Rename");
	private final JMenuItem mnuRefresh = new JMenuItem("Refresh");
	private final JMenuItem mnuCopy;
	private final JMenuItem mnuPaste;
	private final JMenuItem mnuSync = new JMenuItem("Synchronize to server");

	private final JMenuItem mnuPullModule = new JMenuItem("Pull");
	private final JMenuItem mnuPushModule = new JMenuItem("Synchronize module to server");

	private final JPopupMenu menu = new JPopupMenu();
	private ProjectNode selectedNode;
	private XXDBJTabbedPane tabbedWorkspace;
	private XXDBEditor parent;
	private Workspace ws;
	private File wsDir;
	private HashSet<String> importedFolders = new HashSet<String>();
	
	private int syncSuccessCount;
	private int syncFailCount;

	private int syncModuleSuccessCount;
	private int syncModuleFailCount;

	private DefaultTreeModel treeModel;
	private Clipboard clipboard = new Clipboard("File tree clipboard");
	
	public XXDBFileTree(File dir, XXDBEditor parent, XXDBJTabbedPane tabbedWorkspace) {
		setShowsRootHandles(true);
		this.parent = parent;
		this.tabbedWorkspace = tabbedWorkspace;
		this.wsDir = dir;
		ws = new Workspace();
		loadWorkspace(dir);
		if(ws.getServers().size()==0){
			Server s = new Server("local" + DEFAULT_PORT, "localhost", DEFAULT_PORT, "", "", "");
			ws.addServer(s, true);
		}
		if(Utility.isDPIScaled())
			setRowHeight(Utility.getScaledSize(16));
		
		menu.add(mnuRun);
		menu.add(mnuUnitTest);
		menu.addSeparator();
		menu.add(mnuNewProject);
		menu.add(mnuImportFolder);
		menu.add(mnuImportProject);
		menu.addSeparator();
		menu.add(mnuNewPackage);
		menu.add(mnuNewFolder);
		menu.addSeparator();
		menu.add(mnuNewModule);
		menu.add(mnuNewFile);
		menu.addSeparator();
		menu.add(mnuRename);
		menu.add(mnuDelete);
		menu.add(mnuRefresh);
		menu.addSeparator();
		
		mnuCopy = XXDBMenuBar.createMenuItem(
				this.menu, XXDBMenuBar.ITEM_PLAIN, "Copy",  null, 
				KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()), 
				null);
		mnuPaste = XXDBMenuBar.createMenuItem(
				this.menu, XXDBMenuBar.ITEM_PLAIN, "Paste",  null, 
				KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()), 
				null);
		menu.addSeparator();
		//menu.add(mnuPullModule);
		menu.add(mnuPushModule);
		menu.add(mnuSync);
//		if (parent.getServer() != null && parent.getServer().hasRemoteDir()) {
//
//		}

		
		mnuRun.addActionListener(this);
		mnuUnitTest.addActionListener(this);
		mnuNewProject.addActionListener(this);
		mnuImportFolder.addActionListener(this);
		mnuImportProject.addActionListener(this);
		mnuNewPackage.addActionListener(this);
		mnuNewFolder.addActionListener(this);
		mnuNewModule.addActionListener(this);
		mnuNewFile.addActionListener(this);
		mnuRename.addActionListener(this);
		mnuDelete.addActionListener(this);
		mnuRefresh.addActionListener(this);
		mnuSync.addActionListener(this);
		mnuPullModule.addActionListener(this);
		mnuPushModule.addActionListener(this);
		mnuCopy.setActionCommand((String)TreeTransferHandler.getCopyAction().getValue(Action.NAME));
		mnuPaste.setActionCommand((String)TreeTransferHandler.getPasteAction().getValue(Action.NAME));
		
		mnuCopy.addActionListener(this);
		mnuPaste.addActionListener(this);

		MouseListener ml = new MouseAdapter() {
		    public void mousePressed(MouseEvent e) {
		    	if((System.getProperty("os.name").equalsIgnoreCase("linux")
						|| System.getProperty("os.name").toLowerCase().contains("mac")) && e.isPopupTrigger()){
					TreePath[] selectionPaths = getSelectionPaths();
					int selRow = getRowForLocation(e.getX(), e.getY());
					TreePath selPath = getPathForLocation(e.getX(), e.getY());
					if (selectionPaths != null && Arrays.asList(selectionPaths).contains(selPath)) {
						selectedNode = (ProjectNode) selPath.getLastPathComponent();
						setupPopupMenu(e, selectedNode);
					}
					else
					if (selRow != -1 && selPath != null) {
						setSelectionPath(selPath);
						selectedNode = (ProjectNode) selPath.getLastPathComponent();
						setupPopupMenu(e, selectedNode);
					}
				}

		    	int selRow = getRowForLocation(e.getX(), e.getY());
		        TreePath selPath = getPathForLocation(e.getX(), e.getY());
		        if(selRow != -1 && selPath != null) {
		            if(e.getClickCount() == 2) {
		            	 ProjectNode node = (ProjectNode) selPath.getLastPathComponent();
		            	 if (!node.isDir()) {
		 					XXDBRTextScrollPane textScrollPane = tabbedWorkspace.isAlreadyOpen(node.getDirPath());
		 					if (textScrollPane == null) {
		 						openFileForEditing(node.getDirPath(), true);
		 						bindTabWithNodePath(node);
		 					}
		 					else
		 						tabbedWorkspace.setSelectedComponent(textScrollPane);
		 				}
		            }
		         }
		     }
		    
			public void mouseReleased(MouseEvent e) {
				if(!e.isPopupTrigger())
					return;
				
				TreePath[] selectionPaths = getSelectionPaths();
                int selRow = getRowForLocation(e.getX(), e.getY());
                TreePath selPath = getPathForLocation(e.getX(), e.getY());
                if (selectionPaths != null && Arrays.asList(selectionPaths).contains(selPath)) {
               		selectedNode = (ProjectNode) selPath.getLastPathComponent();
               		setupPopupMenu(e, selectedNode);
                }
                else 
                	if (selRow != -1 && selPath != null) {
        			setSelectionPath(selPath);
        			selectedNode = (ProjectNode) selPath.getLastPathComponent();
               		setupPopupMenu(e, selectedNode);
                }
            }
		 };
		 addMouseListener(ml);
	}
	
	private void setMappings() {
		ActionMap map = getActionMap();
		map.remove(TransferHandler.getCutAction().getValue(Action.NAME));
		map.getParent().remove(TransferHandler.getCutAction().getValue(Action.NAME));
		map.put(TransferHandler.getCopyAction().getValue(Action.NAME),
				TransferHandler.getCopyAction());
		map.put(TransferHandler.getPasteAction().getValue(Action.NAME),
				TransferHandler.getPasteAction());
	}

	public void loadWorkspace(File wsDir) {
		ProjectNode root = new ProjectNode(wsDir.getAbsolutePath(), wsDir.getName(), true, NodeType.WORKSPACE);
		tabbedWorkspace.removeAll();
		tabbedWorkspace.removeAllWorkBook();
		this.wsDir = wsDir;

		/* Drag and drop, cut, copy and paste */ 
		setDragEnabled(true);
		treeModel =  new DefaultTreeModel(root);
		setModel(treeModel);
		setTransferHandler(new TreeTransferHandler(this, treeModel, clipboard));
		setMappings();

		try{
			ws.loadWorkspace(wsDir.getAbsolutePath() + File.separator + ".workspace");
		}
		catch(Exception ex){
			ex.printStackTrace();
		}

		
		for(String file : ws.getOpenFiles()){
			openFileForEditing(file,false);
		}
		
		if(ws.getActiveFile() != null){
			XXDBRTextScrollPane textScrollPane = tabbedWorkspace.isAlreadyOpen(ws.getActiveFile());
			if(textScrollPane != null)
				tabbedWorkspace.setSelectedComponent(textScrollPane);
		}

		for(ProjectNode node : ws.getProjects()){
			importedFolders.add(node.getDirPath());
			addNodes(root, new File(node.getDirPath()), node.getNodeType() == NodeType.PROJECT, -1);
		}

		treeModel.reload(root);
	}
  
	public void saveWorkspace(){
		ws.clearOpenFiles();
		ws.clearProjects();
		ProjectNode root = (ProjectNode)((DefaultTreeModel)getModel()).getRoot();
		for(int i=0; i<root.getChildCount(); ++i){
			ProjectNode curNode = (ProjectNode)root.getChildAt(i);
			ws.addProject(curNode);
		}
		
		for(int i=0; i<tabbedWorkspace.getTabCount(); ++i){
			XXDBRTextScrollPane editor = (XXDBRTextScrollPane)tabbedWorkspace.getComponentAt(i);
			String filePath = editor.getTextArea().getOpenFilePath();
			if(filePath != null)
				ws.addFile(filePath, tabbedWorkspace.getSelectedIndex() == i);
		}
		
		try{
			ws.saveWorkspace(getWorkspaceFile());
		}
		catch(IOException ex){
			
		}
	}
	
	public Workspace getWorkspace(){
		return ws;
	}
	
	public String getWorkspaceFile(){
		return wsDir.getAbsolutePath() + File.separator + ".workspace";
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();
		if(command.equals("New Project"))
			newProject(selectedNode);
		else if(command.equals("Import Project"))
			importFolder(selectedNode,true);
		else if(command.equals("Import Folder"))
			importFolder(selectedNode,false);
		else if(command.equals("Rename"))
			renameNode(selectedNode);
		else if(command.equals("Delete"))
			deleteNodes();
		else if(command.equals("New Package"))
			newPackage(selectedNode);
		else if(command.equals("New Folder"))
			newFolder(selectedNode);
		else if(command.equals("New Module"))
			newModule(selectedNode);
		else if(command.equals("New File"))
			newFile(selectedNode);
		else if(command.equals("Run"))
			runFile(selectedNode);
		else if(command.equals("Unit Test"))
			testFile(selectedNode);
		else if(command.equals("Refresh"))
			refreshNode(selectedNode);
		else if(command.equals(TransferHandler.getCopyAction().getValue(Action.NAME)) ||
				command.equals(TransferHandler.getPasteAction().getValue(Action.NAME))) {
			Action action = getActionMap().get(command);
			if (action != null) {
				action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
			}
		}
		else if(command.equals("Synchronize to server"))
			prepareSyncNode();
		else if(command.equals("Pull")){

		}else if(command.equals("Synchronize module to server")){
			prepareSyncModule();
		}
	}
  
	private ProjectNode addNodes(ProjectNode parent, File dir, boolean isProject, int index) {
		String curPath = dir.getPath();
		NodeType parentType = parent.getNodeType();
		NodeType nodeType;
		if(parentType == NodeType.WORKSPACE){
			if(isProject)
				nodeType = NodeType.PROJECT;
			else
				nodeType = NodeType.FOLDER;
		}
		else if(parentType == NodeType.PROJECT){
			String name = dir.getName();
			if(name.equalsIgnoreCase("modules"))
				nodeType = NodeType.PACKAGE;
			else 
				nodeType = NodeType.FOLDER;
		}
		else if(parentType == NodeType.PACKAGE)
			nodeType = NodeType.PACKAGE;
		else
			nodeType = NodeType.FOLDER;
			
		ProjectNode curDir = new ProjectNode(dir.getAbsolutePath(), dir.getName(), true, nodeType);
		if(index<0 || index >= parent.getChildCount())
			insertNodeInto(curDir, parent);
		else
			treeModel.insertNodeInto(curDir, parent, index);
		
		Vector<String> ol = new Vector<>();
		String[] tmp = dir.list();
		if(tmp != null){
			for (int i = 0; i < tmp.length; i++)
				ol.addElement(tmp[i]);
		}
		Collections.sort(ol, String.CASE_INSENSITIVE_ORDER);
		
		File f;
		Vector<ProjectNode> files = new Vector<>();
		// Make two passes, one for Dirs and one for Files. This is #1.
		for (int i = 0; i < ol.size(); i++) {
			String thisObject = (String) ol.elementAt(i);
			if(!ignoreFiles.contains(thisObject)) {
				String newPath;
				if (curPath.equals("."))
					newPath = thisObject;
				else
					newPath = curPath + File.separator + thisObject;
				if ((f = new File(newPath)).isDirectory())
					addNodes(curDir, f, false, -1);
				else{
					NodeType childType;
					if(thisObject.endsWith(".dos")){
						if(nodeType == NodeType.PACKAGE)
							childType = NodeType.MODULE;
						else
							childType = NodeType.SCRIPT;
					}
					else
						childType = NodeType.FILE;
					files.addElement(new ProjectNode(newPath, thisObject, false, childType));
				}
			}
		}
		
		// Pass two: for files.
		for (int fnum = 0; fnum < files.size(); fnum++) {
			ProjectNode node = files.elementAt(fnum);
			treeModel.insertNodeInto(node, curDir, curDir.getChildCount());
			bindTabWithNodePath(node);
		}
		
		return curDir;
	}
	
	private boolean bindTabWithNodePath(ProjectNode node) {
		for (XXDBRTextScrollPane workbook : tabbedWorkspace.getAllWorkbooks()) {
			if (workbook.getFilePath().equals(node.getDirPath())) {
				workbook.setNodePath(new TreePath(node.getPath()));
				workbook.setNode(node);
				return true;
			}
		}
		return false;
	}
	
	private void setupPopupMenu(MouseEvent e, ProjectNode node){
		NodeType nodeType = node.getNodeType();

		mnuRun.setEnabled(!node.isDir());
		mnuUnitTest.setEnabled(nodeType != NodeType.WORKSPACE && nodeType != NodeType.PROJECT );
		mnuNewProject.setEnabled(nodeType == NodeType.WORKSPACE);
		mnuImportFolder.setEnabled(nodeType == NodeType.WORKSPACE);
		mnuImportProject.setEnabled(nodeType == NodeType.WORKSPACE);
		mnuNewPackage.setEnabled(nodeType == NodeType.PACKAGE);
		mnuNewFolder.setEnabled(nodeType == NodeType.FOLDER || nodeType == NodeType.PROJECT);
		mnuNewModule.setEnabled(nodeType == NodeType.PACKAGE);
		mnuNewFile.setEnabled(nodeType == NodeType.FOLDER);
		mnuRename.setEnabled(nodeType != NodeType.WORKSPACE && nodeType != NodeType.PROJECT && !isModuleRoot(node));
		mnuDelete.setEnabled(nodeType != NodeType.WORKSPACE && !isModuleRoot(node));
		mnuRefresh.setEnabled(nodeType == NodeType.PROJECT || nodeType == NodeType.PACKAGE || nodeType == NodeType.FOLDER);
		mnuPushModule.setEnabled(nodeType == NodeType.PACKAGE||nodeType==NodeType.MODULE);
		mnuSync.setEnabled(parent.getServer() != null && parent.getServer().hasRemoteDir()&& nodeType != NodeType.WORKSPACE);
		// check paste menu item
		try {
			if (clipboard.getContents(null) == null) 
				mnuPaste.setEnabled(false);
			else {
				ProjectNode[] nodes = (ProjectNode[]) clipboard.getContents(null).getTransferData(TreeTransferHandler.nodesFlavor);
				ProjectNode nodeToInsert = (ProjectNode)getSelectionPath().getLastPathComponent();
				if (nodes == null)
					mnuPaste.setEnabled(false);
				else if (!TreeTransferHandler.isMoveOrCopyTypeCorrect(nodes[0], nodeToInsert))
					mnuPaste.setEnabled(false);
				else
					mnuPaste.setEnabled(true);
			}
		} catch (IOException ex) {
			mnuPaste.setEnabled(false);
		} catch (UnsupportedFlavorException ex) {
			ex.printStackTrace();
		}
		
//		mnuSync.setEnabled(nodeType != NodeType.WORKSPACE);
//		if (parent.getServer() != null && parent.getServer().hasRemoteDir()) {
//			if (menu.getComponentIndex(mnuSync) < 0) {
//				menu.addSeparator();
//				menu.add(mnuSync);
//			}
//		}
//		else {
//			if (menu.getComponentIndex(mnuSync) >= 0) {
//				menu.remove(mnuSync);
//				menu.remove(menu.getComponentCount() - 1);
//			}
//		}

		menu.show(e.getComponent(), e.getX(), e.getY());
	}
	
	private boolean isModuleRoot(ProjectNode node){
		ProjectNode parent = (ProjectNode)node.getParent();
		if(parent.getNodeType() == NodeType.PROJECT && node.getNodeType() == NodeType.PACKAGE)
			return true;
		else
			return false;
	}
	
	private void runFile(ProjectNode node){
		parent.executeCode(
			"run(\"" + parent.getRemoteNodePath(node) + "\")"
		);
	}
	
	private void testFile(ProjectNode node){
		parent.executeCode(
			"test(\"" + parent.getRemoteNodePath(node) + "\")"
		);
	}

	private void syncModule (ProjectNode node){
		if (!node.isDir()) {
			String remotePath = parent.getRemoteModulePath(node);
			try {
				File file = new File(node.getDirPath());
				String fileContent = Utility.readToString(node.getDirPath());

				long lastModified = file.lastModified();

				switch (parent.syncFile(remotePath, fileContent, lastModified)) {
					case XXDBEditor.SYNC_FAIL: syncModuleFailCount++; break;
					case XXDBEditor.SYNC_SUCCESS: syncModuleSuccessCount++; break;
					default: break;
				}
			}
			catch (Exception ex) {
				syncModuleFailCount++;
				java.util.Date dtStart = new java.util.Date();
				parent.displayMessage(Utility.formatDate(dtStart) + ": Could not synchronize " + remotePath);
			}
		}
		else {
			int childNum = node.getChildCount();
			for (int i = 0; i < childNum; i++)
				syncModule((ProjectNode)node.getChildAt(i));
		}
	}

	private void syncNode(ProjectNode node) {
		if (!node.isDir()) {
			String remotePath = parent.getRemoteNodePath(node);
			try {
				File file = new File(node.getDirPath());
				String fileContent = Utility.readToString(node.getDirPath());
//				Scanner scanner = new Scanner(file);
//				String fileContent = scanner.useDelimiter("\\Z").next();
//				scanner.close();

				long lastModified = file.lastModified();
				switch (parent.syncFile(remotePath, fileContent, lastModified)) {
					case XXDBEditor.SYNC_FAIL: syncFailCount++; break;
					case XXDBEditor.SYNC_SUCCESS: syncSuccessCount++; break;
					default: break;
				}
			}
			catch (Exception ex) {
				syncFailCount++;
				java.util.Date dtStart = new java.util.Date();

				parent.displayMessage(Utility.formatDate(dtStart) + ": Could not synchronize " + remotePath);
			}
		}
		else {
			int childNum = node.getChildCount();
			for (int i = 0; i < childNum; i++)
				syncNode((ProjectNode)node.getChildAt(i));
		}
	}
	
	private void prepareSyncNode() {
		syncFailCount = syncSuccessCount = 0;
		TreePath[] selectionPaths = getSelectionPaths();

		for (TreePath path: selectionPaths)
			syncNode((ProjectNode) path.getLastPathComponent());
		
		int shouldSyncNum = syncFailCount + syncSuccessCount;
		String shouldSyncString = Integer.toString(shouldSyncNum);
		shouldSyncString += shouldSyncNum <= 1 ? " file needs to synchronize. " : " files need to synchronize. ";
		
		String syncedString = Integer.toString(syncSuccessCount);
		syncedString += syncSuccessCount <= 1 ? " file synchronized" : " files synchronized";

		parent.displayMessage(shouldSyncString + syncedString);
	}

	private void prepareSyncModule() {
		syncModuleFailCount = syncModuleSuccessCount = 0;
		TreePath[] selectionPaths = getSelectionPaths();

		for (TreePath path: selectionPaths)
			syncModule((ProjectNode) path.getLastPathComponent());

		int shouldSyncNum = syncModuleFailCount + syncModuleSuccessCount;
		String shouldSyncString = Integer.toString(shouldSyncNum);
		shouldSyncString += shouldSyncNum == 1 ? " module needs to synchronize. " : " modules need to synchronize. ";

		String syncedString = Integer.toString(syncModuleSuccessCount);
		syncedString += syncModuleSuccessCount == 1 ? " module synchronized" : " modules synchronized";

		parent.displayMessage(shouldSyncString + syncedString);
	}

	private void renameNode(ProjectNode node){
		Object rename = JOptionPane.showInputDialog(parent, "Enter a new name for the selected node", "DolphinDB Package", JOptionPane.QUESTION_MESSAGE, null, null, node.getName());
		if(rename == null)
			return;
		
		String folder = rename.toString().trim();
		if(folder != null && folder.isEmpty())
			return;
		
		String oldFolderPath = node.getDirPath();
		//String newFolderPath = node.getDirPath() + File.separator + folder;
		File newDir = new File(new File(oldFolderPath).getParentFile().getAbsolutePath() + File.separator + folder);
		String newFolderPath = newDir.getAbsolutePath();
		
		try {
			if (newDir.exists()&&!(oldFolderPath.equalsIgnoreCase(newDir.getAbsolutePath())&&!oldFolderPath.equals(newDir.getAbsolutePath())))
				throw new XXDBException("Package already exists");
			else {
				renameNodeFromTo(node, oldFolderPath, newFolderPath);
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(parent, ex.getMessage(), "DolphinDB Error", JOptionPane.ERROR_MESSAGE);
		}
		
	}
	
	public void renameNodeFromTo(ProjectNode node, String oldFolderPath, String newFolderPath) {
		File oldDir = new File(oldFolderPath);
		File newDir = new File(newFolderPath);
		boolean b = oldDir.renameTo(newDir);
		NodeType type = node.getNodeType();
		if((type == NodeType.FOLDER || type == NodeType.PACKAGE)&&b==false){
			JOptionPane.showMessageDialog(parent, "Folder rename failed .");
			return;
		}
		if (type == NodeType.FILE || type == NodeType.MODULE || type == NodeType.SCRIPT) {
			renameEditor(oldFolderPath, newFolderPath);
		} else if (type == NodeType.FOLDER || type == NodeType.PACKAGE) {
			/* Rename every child node in the folder */
			int childCount = node.getChildCount();
			for (int childIndex = 0; childIndex < childCount; childIndex++) {
				ProjectNode child = (ProjectNode)node.getChildAt(childIndex);
				String oldChildPath = child.getDirPath();
				String newChildPath = oldChildPath.replace(oldFolderPath, newFolderPath);

				// Rename tab
				ArrayList<String> files = new ArrayList<>();
				for(int i=0; i<tabbedWorkspace.getTabCount(); ++i){
					XXDBRTextScrollPane editor =  (XXDBRTextScrollPane)tabbedWorkspace.getComponentAt(i);
					if(editor.getTextArea().getOpenFilePath().startsWith(oldFolderPath))
						files.add(editor.getTextArea().getOpenFilePath());
				}
				for(String file : files) {
					String oldFilePath = file;
					String newFilePath = file.replace(oldFolderPath, newFolderPath);
					renameEditor(oldFilePath, newFilePath);
				}

				// Recursively rename child nodes
				renameNodeFromTo(child, oldChildPath, newChildPath);
			}
		}
		
		node.setPath(newFolderPath);
		treeModel.reload(node);
		bindTabWithNodePath(node);
		
		/* Rename title */
		parent.setTitle();
	}
	
	public ProjectNode copyNodeFromTo(ProjectNode node, String oldFolderPath, String newFolderPath) {
		ProjectNode copied = new ProjectNode(node);

		File oldDir = new File(oldFolderPath);
		File newDir = new File(newFolderPath);
		
		try {
			Files.copy(oldDir.toPath(), newDir.toPath());
		} catch (IOException ex) {
			return null;
		}

		copied.setPath(newFolderPath);
		NodeType type = node.getNodeType();
		if (type == NodeType.FILE || type == NodeType.MODULE || type == NodeType.SCRIPT) {
		} else if (type == NodeType.FOLDER || type == NodeType.PACKAGE) {
			int childCount = node.getChildCount();
			for (int childIndex = 0; childIndex < childCount; childIndex++) {
				ProjectNode child = (ProjectNode)node.getChildAt(0);    // After removal from parent, next child has index 0
				String oldChildPath = child.getDirPath();
				String newChildPath = oldChildPath.replace(oldFolderPath, newFolderPath);
				
				treeModel.removeNodeFromParent(child);
				// Recursively rename child nodes
				ProjectNode copiedChild = copyNodeFromTo(child, oldChildPath, newChildPath);
				treeModel.insertNodeInto(copiedChild, copied, childIndex);
			}
		}
		
		parent.setTitle();
		return copied;
	}
	
	private void renameEditor(String oldFolderPath, String newFolderPath) {
		XXDBRTextScrollPane editor =  tabbedWorkspace.isAlreadyOpen(oldFolderPath);
		if (editor != null) {
			tabbedWorkspace.renameWorkbook(oldFolderPath, newFolderPath);
		}
	}
	
	private void refreshNode(ProjectNode node){
		ProjectNode parent = (ProjectNode)node.getParent();
		int index = parent.getIndex(node);
		treeModel.removeNodeFromParent(node);
		ProjectNode newNode = addNodes(parent, new File(node.getDirPath()), node.getNodeType()==NodeType.PROJECT, index);
		ArrayList<XXDBRTextScrollPane> deletedComps = new ArrayList<>();
		for(XXDBRTextScrollPane comp : tabbedWorkspace.getAllWorkbooks()) {
			XXDBRSyntaxTextArea current = (XXDBRSyntaxTextArea) (comp.getTextArea());
			try {
				current.setFileContent(comp.getFilePath());
				current.setSaved(true);
			} catch (FileNotFoundException ex) {
				deletedComps.add(comp);
				JOptionPane.showMessageDialog(this.parent, "File " + comp.getFilePath() + " is not in its original location.", "DolphinDB Error", JOptionPane.ERROR_MESSAGE);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this.parent, ex.getMessage(), "DolphinDB Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		for (XXDBRTextScrollPane comp : deletedComps) {
			tabbedWorkspace.remove(comp);
			tabbedWorkspace.removeWorkBook(comp.getFilePath());
			tabbedWorkspace.checkAllTabsClosed();
		}
		expandPath(newNode.getTreePath());
	}
	
	private void deleteNodes() {
		TreePath[] selectionPaths = getSelectionPaths();
		for (TreePath path : selectionPaths) {
			deleteNode((ProjectNode)path.getLastPathComponent());
		}
	}
	
	private void deleteNode(ProjectNode node){
		String message;
		NodeType type = node.getNodeType();
		NodeType parentType = ((ProjectNode)node.getParent()).getNodeType();
		if(type == NodeType.PROJECT)
			message ="Are you sure to permanently delete the project [" + node.getName() + "]?";
		else if(type == NodeType.FOLDER){
			if(parentType == NodeType.WORKSPACE)
				message ="Are you sure to remove the link of folder [" + node.getName() + "] from the workspace?";
			else
				message ="Are you sure to permanently delete the folder " + node.getName() + "?";
		}
		else if(type == NodeType.PACKAGE)
			message ="Are you sure to permanently delete the package " + node.getName() + "?";
		else if(type == NodeType.MODULE)
			message ="Are you sure to permanently delete the module file " + node.getName() + "?";
		else if(type == NodeType.SCRIPT)
			message ="Are you sure to permanently delete the script " + node.getName() + "?";
		else if(type == NodeType.FILE)
			message ="Are you sure to permanently delete the file " + node.getName() + "?";
		else
			return;
		
		if (JOptionPane.showConfirmDialog(parent, message, "DolphinDB Workspace", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION)
			return;
		
		try{
			File curFile = new File(node.getDirPath());
			if(parentType != NodeType.WORKSPACE || type != NodeType.FOLDER){
				boolean ret = curFile.isFile() ? curFile.delete() : Utility.deleteFolder(curFile);
				if(!ret){
					JOptionPane.showMessageDialog(parent, "Failed to delete " + node.getDirPath() , "DolphinDB Error", JOptionPane.ERROR_MESSAGE);
					return;
				}	
			} else {
				importedFolders.remove(node.getDirPath());
			}
			treeModel.removeNodeFromParent(node);
			if(node.isDir()){
				ArrayList<String> files = new ArrayList<>();
				for(int i=0; i<tabbedWorkspace.getTabCount(); ++i){
					XXDBRTextScrollPane editor =  (XXDBRTextScrollPane)tabbedWorkspace.getComponentAt(i);
					if(editor.getTextArea().getOpenFilePath().startsWith(node.getDirPath()))
						files.add(editor.getTextArea().getOpenFilePath());
				}
				for(String file : files)
					removeEditor(file);
			}
			else{
				removeEditor(node.getDirPath());
			}
		}
		catch(Exception ex){
			JOptionPane.showMessageDialog(parent, ex.getMessage(), "DolphinDB Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void newPackage(ProjectNode node){
		String folder = JOptionPane.showInputDialog(parent, "Please input a package name (e.g. Demo)", "DolphinDB Package", JOptionPane.QUESTION_MESSAGE);
		if(folder == null || folder.trim().isEmpty())
			return;
		try{
			String newFolderPath = node.getDirPath() + File.separator + folder.trim();
			File directory = new File(newFolderPath);
			if (directory.exists())
				throw new XXDBException("Package already exists");
			else {
				directory.mkdirs();
			}
			ProjectNode curNode = new ProjectNode(newFolderPath, directory.getName(), true, NodeType.PACKAGE);
			insertNodeInto(curNode, node);
		}
		catch (Exception ex) {
			JOptionPane.showMessageDialog(parent, ex.getMessage(), "DolphinDB Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void newFolder(ProjectNode node){
		String folder = JOptionPane.showInputDialog(parent, "Please input a folder name (e.g. Demo)", "DolphinDB Folder", JOptionPane.QUESTION_MESSAGE);
		if(folder == null || folder.trim().isEmpty())
			return;
		try{
			String newFolderPath = node.getDirPath() + File.separator + folder.trim();
			File directory = new File(newFolderPath);
			if (directory.exists())
				throw new XXDBException("Folder already exists");
			else {
				directory.mkdirs();
			}
			ProjectNode curNode = new ProjectNode(newFolderPath, directory.getName(), true, NodeType.FOLDER);
			insertNodeInto(curNode, node);
		}
		catch (Exception ex) {
			JOptionPane.showMessageDialog(null, ex.getMessage(), "DolphinDB Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void newModule(ProjectNode node){
		String filename = JOptionPane.showInputDialog(parent, "Please input a module name (e.g. demo.dos)", "DolphinDB Module", JOptionPane.QUESTION_MESSAGE);
		if(filename == null || filename.trim().isEmpty())
			return;
		try{
			int start = filename.lastIndexOf('.');
			if(start<0)
				filename = filename + ".dos";
			else if(! filename.endsWith(".dos"))
				throw new XXDBException("Module name must use the extension .dos");
			String newFilePath = node.getDirPath() + File.separator + filename.trim();
			File file = new File(newFilePath);
			if (file.exists())
				throw new XXDBException("Module already exists");
			else {
				file.createNewFile();
			}
			ProjectNode curNode = new ProjectNode(file.getAbsolutePath(), file.getName(), false, NodeType.MODULE);
			insertNodeInto(curNode, node);
			openFileForEditing(newFilePath, true);
			bindTabWithNodePath(curNode);
		}
		catch (Exception ex) {
			JOptionPane.showMessageDialog(parent, ex.getMessage(), "DolphinDB Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void newFile(ProjectNode node){
		String filename = JOptionPane.showInputDialog(parent, "Please input a file name (e.g. demo.txt)", "DolphinDB File", JOptionPane.QUESTION_MESSAGE);
		if(filename == null || filename.trim().isEmpty())
			return;
		try{
			String newFilePath = node.getDirPath() + File.separator + filename.trim();
			File file = new File(newFilePath);
			if (file.exists())
				throw new XXDBException("File already exists");
			else {
				file.createNewFile();
			}
			ProjectNode curNode = new ProjectNode(file.getAbsolutePath(), file.getName(), false, NodeType.FILE);
			insertNodeInto(curNode, node);
			openFileForEditing(newFilePath, true);
			bindTabWithNodePath(curNode);
		}
		catch (Exception ex) {
			JOptionPane.showMessageDialog(parent, ex.getMessage(), "DolphinDB Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void newProject(ProjectNode node){
		try {
			String projectName = JOptionPane.showInputDialog(parent, "Please input a project name (e.g. Demo)", "DolphinDB Project", JOptionPane.QUESTION_MESSAGE);
			if(projectName == null || projectName.trim().isEmpty())
				return;
			projectName = projectName.trim();
			if (projectName != null) {
				if (projectName.contains(File.separator) || projectName.contains("/") || projectName.contains("\\"))
					throw new XXDBException("Invalid project name");
				String dirToCreate = node.getDirPath() + File.separator + projectName;
				// .project file
				String defaultFileToCreate = dirToCreate + File.separator + ".project";
				// scripts/modules sub directories
				String defaultScriptsDir = dirToCreate + File.separator + "scripts";
				String defaultModulesDir = dirToCreate + File.separator + "modules";
				File defaultDir = new File(dirToCreate);
				File directory = new File(defaultDir.getAbsolutePath());
				if (directory.exists())
					throw new XXDBException("Project already exists");
				else {
					directory.mkdirs();
					if (!Utility.checkNCreateFile(defaultFileToCreate)) {
						new File(defaultScriptsDir).mkdir();
						new File(defaultModulesDir).mkdir();
						importedFolders.add(defaultDir.getAbsolutePath());
						addNodes(node, defaultDir, true, -1);
					} else
						throw new XXDBException("Project already exists");
				}
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(parent, ex.getMessage(), "DolphinDB Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void importFolder(ProjectNode node, boolean isProject){
		JFileChooser open = new JFileChooser();
		open.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		open.setCurrentDirectory(new File(Utility.getLastUsedWorkspace()));

		int option = open.showOpenDialog(this);
		if (option == JFileChooser.APPROVE_OPTION) {
			String path = open.getSelectedFile().getPath();
			try {
				/* Check existing folder or subfolder of existing folder */
				if (importedFolders.contains(path))
					throw new XXDBException("Folder already exists");
				else {
					for (String importedFolder : importedFolders) {
						if (path.startsWith(importedFolder)) {
							if (path.substring(importedFolder.length(), path.length()).contains(File.separator))
								throw new XXDBException("The folder is a subfolder of an existing folder");
						}
						else if (importedFolder.startsWith(path)) {
							if (importedFolder.substring(path.length(), importedFolder.length()).contains(File.separator))
								throw new XXDBException("The folder is the parent folder of an existing folder");
						}
					}
					importedFolders.add(path);
					addNodes(node, new File(path), isProject, -1);
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(parent, ex.getMessage(), "DolphinDB Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	private boolean openFileForEditing(String filename, boolean selected){
		XXDBRSyntaxTextArea newbook = new XXDBRSyntaxTextArea(parent);
		try {
			newbook.setFileContent(filename);
			newbook.setSaved(true);
			XXDBRTextScrollPane textScrollPane = new XXDBRTextScrollPane(newbook);
			tabbedWorkspace.addWorkBook(newbook.getName(), textScrollPane);
			if(selected)
				tabbedWorkspace.setSelectedComponent(textScrollPane);
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	private boolean removeEditor(String filename){
		XXDBRTextScrollPane editor =  tabbedWorkspace.isAlreadyOpen(filename);
		if(editor != null){
			tabbedWorkspace.remove(editor);
			tabbedWorkspace.removeWorkBook(filename);
			tabbedWorkspace.checkAllTabsClosed();
			return true;
		}
		else
			return false;
	}
	
	public void insertNodeInto(ProjectNode curNode, ProjectNode parent) {
		treeModel.insertNodeInto(curNode, parent, parent.getChildCount());
		TreePath parentPath = getSelectionPath();
		
		if (parentPath != null) {
			TreePath newPath = parentPath.pathByAddingChild(curNode);
			makeVisible(newPath);
			scrollPathToVisible(newPath);
		}
	}

	public XXDBEditor getEditor() {
		return this.parent;
	}
}
