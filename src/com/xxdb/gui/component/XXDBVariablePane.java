package com.xxdb.gui.component;

import com.xxdb.DBConnection;
import com.xxdb.data.*;
import com.xxdb.data.Entity.DATA_FORM;
import com.xxdb.data.Entity.DATA_TYPE;
import com.xxdb.gui.common.Utility;
import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.ConnectException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class XXDBVariablePane extends JScrollPane implements ActionListener, TreeExpansionListener {
	private static final long serialVersionUID = 1L;

	private VariableNode topNode;
	private VariableNode tableNode;
	private VariableNode matrixNode;
	private VariableNode vectorNode;
	private VariableNode scalarNode;
	private VariableNode dictNode;
	private VariableNode setNode;
	private VariableNode objNode;

	private DefaultMutableTreeNode selectedNode;
	
	
	private VariableNode localNode;
	private VariableNode sharedNode;
	
	private JTree tree;
	private XXDBEditor parent;
	private DBConnection conn;

	private final JMenuItem mnuRefresh = new JMenuItem("Refresh");
	private final JMenuItem mnuUndefine = new JMenuItem("undefine");
	private final JMenuItem mnuSchema = new JMenuItem("schema");
	private final JMenuItem mnuBrowse = new JMenuItem("browse");

	public XXDBVariablePane(DBConnection conn, XXDBEditor parent, final VariableBrowseListener listener){
		this.parent = parent;
		this.conn = parent.getConnection();
		final JPopupMenu menu = new JPopupMenu();
		final JPopupMenu refreshMenu = new JPopupMenu();
		
		mnuRefresh.addActionListener(this);
		mnuRefresh.setActionCommand("refresh");
		refreshMenu.add(mnuRefresh);
	    
	    mnuUndefine.addActionListener(this);
	    mnuUndefine.setActionCommand("undefine");
	    menu.add(mnuUndefine);
	    menu.addSeparator();
	    
	    mnuBrowse.addActionListener(this);
	    mnuBrowse.setActionCommand("browse");
	    menu.add(mnuBrowse);
	    menu.addSeparator();
	    
	    mnuSchema.addActionListener(this);
	    mnuSchema.setActionCommand("schema");
	    menu.add(mnuSchema);
	    
		topNode = new VariableNode("Variables");
		
		localNode = new VariableNode("Local Variables");
		sharedNode = new VariableNode("Shared Tables");
		
		tree = new JTree(topNode);
		topNode.add(localNode);
		topNode.add(sharedNode);
		
		if(Utility.isDPIScaled())
			tree.setRowHeight(Utility.getScaledSize(16));
		if(listener != null){
			tree.addMouseListener(new MouseAdapter(){
				 public void mousePressed(MouseEvent e) {
	                int selRow = tree.getRowForLocation(e.getX(), e.getY());
	                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
	                if(selRow != -1 && selPath != null) {
	                	if(e.getClickCount() == 2){
	                		selectedNode = (DefaultMutableTreeNode)selPath.getLastPathComponent();
		                    handleDataBrowse();
	                	}
	                	else if(e.isPopupTrigger()){
	                		 selectedNode = (DefaultMutableTreeNode)selPath.getLastPathComponent();
	                		 setupPopupMenu(e);
	                	}
	                }
	            }
				 
				public void mouseReleased(MouseEvent e) {
					if(!e.isPopupTrigger())
						return;
	                int selRow = tree.getRowForLocation(e.getX(), e.getY());
	                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
	                if(selRow != -1 && selPath != null) {
                   		 selectedNode = (DefaultMutableTreeNode)selPath.getLastPathComponent();
                		 setupPopupMenu(e);
	                }
	            }
				
				private void setupPopupMenu(MouseEvent e){
					if (selectedNode.equals(topNode)) {
						refreshMenu.show(e.getComponent(), e.getX(), e.getY());
					}
	           		if(selectedNode.getUserObject() instanceof ObjectNode){
	           			ObjectNode node = (ObjectNode)selectedNode.getUserObject();
	           			mnuUndefine.setEnabled(true);
	           			mnuBrowse.setEnabled(node.getForm() != DATA_FORM.DF_SCALAR && node.getForm() != DATA_FORM.DF_PAIR && node.getForm() != DATA_FORM.DF_SYSOBJ);
	           			mnuSchema.setEnabled(node.getForm() == DATA_FORM.DF_TABLE);
	           		}
	           		else if(selectedNode.getUserObject() instanceof DfsObjectNode){
	           			mnuUndefine.setEnabled(false);
	           			mnuBrowse.setEnabled(true);
	           			mnuSchema.setEnabled(false);
	           		}
	           		else {
	           			mnuBrowse.setEnabled(false);
	           			mnuSchema.setEnabled(false);
	           			String nodeText = selectedNode.getUserObject().toString();
	           			mnuUndefine.setEnabled(selectedNode.getChildCount()>0 && !nodeText.startsWith("Shared") && !nodeText.startsWith("Variables"));
	           		}
	           		if(mnuBrowse.isEnabled() || mnuUndefine.isEnabled() || mnuSchema.isEnabled())
	           			menu.show(e.getComponent(), e.getX(), e.getY());
				}
			});
		}
		tree.setShowsRootHandles(true);
		tree.addTreeExpansionListener(this);
		tableNode = new VariableNode("Table");
		matrixNode = new VariableNode("Matrix");
		vectorNode = new VariableNode("Vector");
		scalarNode = new VariableNode("Scalar/Pair");
		dictNode = new VariableNode("Dictionary");
		setNode = new VariableNode("Set");
		objNode = new VariableNode("Object");

		
		tree.setCellRenderer(new XXDBTreeCellRenderer());
		this.setViewportView(tree);
	}
	
	public void initUI(boolean setTitle) {
		if(!topNode.isNodeChild(localNode)) topNode.add(localNode);
		if(!topNode.isNodeChild(sharedNode)) topNode.add(sharedNode);

		tableNode.removeAllChildren();
		matrixNode.removeAllChildren();
		vectorNode.removeAllChildren();
		scalarNode.removeAllChildren();
		dictNode.removeAllChildren();
		setNode.removeAllChildren();
		objNode.removeAllChildren();
		localNode.removeAllChildren();
		sharedNode.removeAllChildren();

		if (setTitle)
			topNode.setUserObject("Variables");
		tree.updateUI();
	}
	
	public void update() throws IOException, Exception{
		if(conn == null){
			if(this.parent.getConnection()==null)
				return;
			else
				conn = this.parent.getConnection();
		}

		initUI(false);

		BasicTable vars = null;
		try {
			List<Entity> args = new ArrayList<Entity>(1);
			args.add(new BasicBoolean(true));
			vars = (BasicTable)conn.run("objs", args	);
		}
		catch (ConnectException ex) {
			topNode.setUserObject("Variables");
			tree.updateUI();
			JOptionPane.showMessageDialog(parent, "Connection refused");
			return;
		}

		updateTree(vars);
	}
	
	private void update(String script) throws IOException, Exception{
		if(conn == null){
			if(this.parent.getConnection()==null)
				return;
			else
				conn = this.parent.getConnection();
		}

		
		initUI(false);
		
		BasicTable vars = null;
		try {
			vars = (BasicTable)conn.run(script);
		}
		catch (ConnectException ex) {
			topNode.setUserObject("Variables");
			tree.updateUI();
			JOptionPane.showMessageDialog(parent, "Connection refused");
			return;
		}

		updateTree(vars);
	}


	private void updateTree(BasicTable vars)throws Exception{

		int rows = vars.rows();

		ArrayList<String> scalars = new ArrayList<>();

		Vector types = (Vector)vars.getColumn("type");
		BasicStringVector names = (BasicStringVector)vars.getColumn("name");
		Vector forms = (Vector)vars.getColumn("form");
		Vector rowCounts = (Vector)vars.getColumn("rows");
		Vector colCounts = (Vector)vars.getColumn("columns");
		BasicLongVector bytes = (BasicLongVector)vars.getColumn("bytes");
		BasicBooleanVector shared = (BasicBooleanVector)vars.getColumn("shared");
		BasicStringVector extras = (BasicStringVector)vars.getColumn("extra");

		long totalBytes = 0;
		for(int i=0; i<bytes.rows(); ++i)
			totalBytes += bytes.getLong(i);
		topNode.setUserObject("Variables [" + (totalBytes>0 ? getSizeString(totalBytes) : "") + "]");

		DecimalFormat df = new DecimalFormat("#,##0");
		for(int i=0; i<rows; ++i){
			final Entity.DATA_FORM  form = DATA_FORM.valueOf("DF_" + forms.get(i).getString());
			final String name = names.getString(i);
			final int rowCount = ((Scalar)rowCounts.get(i)).getNumber().intValue();
			final int colCount = ((Scalar)colCounts.get(i)).getNumber().intValue();
			final long memSize = bytes.getLong(i);
			final boolean isShared = shared.getBoolean(i);
			final String extra = extras.getString(i);
			final String typeStr = types.get(i).getString();
			final String type = "<"+typeStr.toLowerCase()+">";
			final DATA_TYPE dataType = form==DATA_FORM.DF_TABLE ? DATA_TYPE.DT_DICTIONARY : DATA_TYPE.valueOfTypeName(typeStr);
			String desc;

			switch(form){
				case DF_TABLE :
					desc = name + " " + df.format(rowCount) + " x " + df.format(colCount) + " [" + getSizeString(memSize) + "]";
					if(!isShared){
						if(!localNode.isNodeChild(tableNode))
							localNode.add(tableNode);
						if (extra.indexOf("dfs:/")>=0) {
							String path = extra.substring("dfs:/".length(), extra.lastIndexOf("/"));
							String partitionPath = extra.substring("dfs:/".length());
							String dbPath = extra.substring(0, extra.lastIndexOf("/"));
							String tableName = extra.substring(extra.lastIndexOf("/") + 1);
							if(tableName.startsWith("__")){ // dimension table
								String script = "def(){if(exists('" + extra + "')) return select * from rpc(getControllerAlias(), getDFSDirectoryContent, '" + path + "') where filename = '" + tableName + "'}()";
								Entity obj = conn.run(script);
								if(obj.getDataForm() == DATA_FORM.DF_TABLE){
									BasicTable dbContent = (BasicTable)obj;
									if(dbContent.rows()>0) {
                                        if (dbContent.getColumn(4).getString(0).isEmpty()) {
                                            obj = conn.run("def(){if(exists('" + extra + "')) return select * from rpc(getControllerAlias(), getDFSDirectoryContent, '" + path + "/" + tableName + "')}()");
                                            dbContent = (BasicTable)obj;
                                        }
                                        
										BasicStringVector chunks = (BasicStringVector) dbContent.getColumn(3);
										BasicStringVector sites = (BasicStringVector) dbContent.getColumn(4);
										String site = sites.getString(0);
										String version = site.split(";")[0].split(":")[1];
										site = site.substring(0, site.indexOf(':'));
										tableNode.add(new DefaultMutableTreeNode(new DimensionObjectNode(name, site, chunks.getString(0), dbPath, partitionPath, tableName, version)));
									}else{
										tableNode.add(new DefaultMutableTreeNode(new DimensionObjectNode(name, "", "", dbPath, partitionPath, tableName, "")));
									}
								}
							}else{
								tableNode.add(new DfsTableNode(name, path, dbPath, path, tableName));
							}
						}else
							tableNode.add(new DefaultMutableTreeNode(new ObjectNode(name, form, dataType, rowCount, colCount, memSize, desc)));
					}else {
						sharedNode.add(new DefaultMutableTreeNode(new ObjectNode(name, form, dataType, rowCount, colCount, memSize, desc, true)));
					}
					break;
				case DF_MATRIX :
					if(!localNode.isNodeChild(matrixNode)) localNode.add(matrixNode);
					desc =name + type +" "+ df.format(rowCount) + " x " + df.format(colCount) + " [" + getSizeString(memSize) + "]";
					matrixNode.add(new DefaultMutableTreeNode(new ObjectNode(name, form, dataType, rowCount, colCount, memSize, desc)));
					break;
				case DF_VECTOR :
					if(!localNode.isNodeChild(vectorNode)) localNode.add(vectorNode);
					desc =name + type +" "+ df.format(rowCount) + " rows [" + getSizeString(memSize) + "]";
					vectorNode.add(new DefaultMutableTreeNode(new ObjectNode(name, form, dataType, rowCount, colCount, memSize, desc)));
					break;
				case DF_PAIR:
				case DF_SCALAR :
					if(isShared)
						break;
					if(!localNode.isNodeChild(scalarNode)) localNode.add(scalarNode);
					scalars.add(name);
					desc =name + type;
					scalarNode.add(new DefaultMutableTreeNode(new ObjectNode(name, form, dataType, rowCount, colCount, memSize, desc)));
					break;
				case DF_DICTIONARY :
					if(!localNode.isNodeChild(dictNode)) localNode.add(dictNode);
					desc =name + type +" "+ df.format(rowCount) + " keys [" + getSizeString(memSize) + "]";
					dictNode.add(new DefaultMutableTreeNode(new ObjectNode(name, form, dataType, rowCount, colCount, memSize, desc)));
					break;
				case DF_SET :
					if(!localNode.isNodeChild(setNode)) localNode.add(setNode);
					desc =name + type +" "+ df.format(rowCount) + " keys [" + getSizeString(memSize) + "]";
					setNode.add(new DefaultMutableTreeNode(new ObjectNode(name, form, dataType, rowCount, colCount, memSize,desc)));
					break;
				case DF_SYSOBJ :
					if(isShared)
						break;
					if(!localNode.isNodeChild(objNode)) localNode.add(objNode);
					desc =name + type;
					objNode.add(new DefaultMutableTreeNode(new ObjectNode(name, form, dataType, rowCount, colCount, memSize, desc)));
					break;
				default:
			}
		}

		if(!scalars.isEmpty()){
			StringBuilder scriptBuilder = new StringBuilder("[");
			for(String name : scalars){
				scriptBuilder.append(name);
				scriptBuilder.append(",");
			}
			scriptBuilder.append("[0, 0]]"); //append one constant vector in the end to force server to generate any vector.
			if (this.parent.cboLanguage.getSelectedItem().equals("Python"))
				scriptBuilder.append(".toddb()");
			BasicAnyVector vec = (BasicAnyVector)conn.run(scriptBuilder.toString());
			for(int i=0; i<scalars.size(); ++i){
				String desc = scalars.get(i) + "<" + vec.getEntity(i).getDataType().name().toLowerCase().substring(3) + ">: " + vec.getEntity(i).getString();
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)scalarNode.getChildAt(i);
				((ObjectNode)node.getUserObject()).setDescription(desc);
			}
		}

		tree.updateUI();
	}

	public void updateConnection(DBConnection conn){
		this.conn = conn;
	}
	
	private String getSizeString(long size){
		if(size<512)
			return size + "B";
		size = (size + 512) / 1024;
		if(size<512)
			return size +"K";
		size = (size + 512) / 1024;
		if(size<512)
			return size +"M";
		return new DecimalFormat("0.#").format(size/1024.0) + "G";
	}
	
	private class DfsTableNode extends DefaultMutableTreeNode {
		private static final long serialVersionUID = 1L;

		private String path;
		private String dbPath;
		private String parititionPath;
		private String tableName;
		private boolean loaded;

		public DfsTableNode(String name, String path, String dbPath, String partitionPath, String tableName) {
			super(name);
			this.path = path;
			this.dbPath = dbPath;
			this.parititionPath = partitionPath;
			this.tableName = tableName;
			this.loaded = false;
		}
		
		@Override
		public boolean isLeaf() {
			return false;
		}
		
		public void loadChildNodes() {
			if (loaded)
				return;
			if (conn != null) {
				try {
					String script = "select * from rpc(getControllerAlias(), getDFSDirectoryContent, '" + path + "') where !filename.startsWith('__') and filetype in (0,1,4)";
					BasicTable dbContent = (BasicTable) conn.run(script);
					BasicStringVector filenames = (BasicStringVector) dbContent.getColumn(0);
					BasicIntVector filetypes = (BasicIntVector) dbContent.getColumn(1);
					BasicStringVector chunks = (BasicStringVector) dbContent.getColumn(3);
					BasicStringVector sites = (BasicStringVector) dbContent.getColumn(4);
					
					int filetype = filetypes.getInt(0);
					for (int i = 0; i < dbContent.rows(); i++) {
						String filename = filenames.getString(i);

						if (filetype == 1 || filetype==4) {    // leaf
							String chunk = chunks.getString(i);
							String site = sites.getString(i);
							String version = site.split(";")[0].split(":")[1];
							site = site.substring(0, site.indexOf(':'));
							add(new DefaultMutableTreeNode(new DfsObjectNode(filename, site, chunk, dbPath, parititionPath, tableName,version)));
						}
						else
							add(new DfsTableNode(filename, path + "/" + filename, dbPath, parititionPath + "/" + filename, tableName));
					}
					loaded = true;
					tree.updateUI();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private class ObjectNode{
		private DATA_FORM form;
		private DATA_TYPE type;
		private String name;
		private int rows;
		private int columns;
		private long bytes;
		private boolean isShared;
		private String desc;
		
		public ObjectNode(String name, DATA_FORM form, DATA_TYPE type, int rows, int columns, long bytes, String desc){
			this(name, form, type, rows, columns, bytes, desc, false);
		}
		
		public ObjectNode(String name, DATA_FORM form, DATA_TYPE type, int rows, int columns, long bytes, String desc, boolean isShared){
			this.desc = desc;
			this.name = name;
			this.form = form;
			this.type = type;
			this.rows = rows;
			this.bytes = bytes;
			this.columns = columns;
			this.isShared = isShared;
		}
		
		public String getName(){
			return name;
		}
		
		public DATA_FORM getForm(){
			return form;
		}
		
		public DATA_TYPE getType(){
			return type;
		}
		
		public int getRows(){
			return rows;
		}
		
		@SuppressWarnings("unused")
		public int getColumns(){
			return columns;
		}
		
		@SuppressWarnings("unused")
		public long getMemSize(){
			return bytes;
		}
		
		public String toString(){
			return desc;
		}
		
		public void setDescription(String desc){
			this.desc = desc;
		}
		
		public boolean isShared(){
			return this.isShared;
		}
	}

	@Override
	public void actionPerformed(ActionEvent action) {
		String command = action.getActionCommand();	
		if(command.equals("browse")){
			handleDataBrowse();
		}
		else if(command.equals("undefine")){
			handleUndefine();
		}
		else if (command.equals("schema")) {
			handleSchema();
		}
		else if (command.equals("refresh")) {
			try {
				update();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	@Override
	public void treeCollapsed(TreeExpansionEvent event) {
		
	}

	@Override
	public void treeExpanded(TreeExpansionEvent event) {
		Object object = event.getPath().getLastPathComponent();
		if (object instanceof DfsTableNode) {
			DfsTableNode node = (DfsTableNode) object;
			node.loadChildNodes();
		}
	}
	
	private void handleDataBrowse(){
		if(conn == null) return;
		XXDBJTable displayTable = null;
		int rows = 0;
		String name = "";
		Object userObject = selectedNode.getUserObject();
		
		if(conn.isBusy()){
			JOptionPane.showMessageDialog(parent, "The connection to dolphindb is in use. Please try again later.");
			return;
		}
		if (userObject instanceof ObjectNode) {
			ObjectNode node = (ObjectNode)selectedNode.getUserObject();
			if(node ==null) return;
			
			DATA_FORM form = node.getForm();
			rows = node.getRows();
			name = node.getName();
			
			try {
				if(form == DATA_FORM.DF_TABLE){
					displayTable = new XXDBJTable(
						new TableBasedDynamicXXDBTableModel(conn, name, rows),
						null,
						null
					);
				}
				else if(form == DATA_FORM.DF_MATRIX){
					displayTable = new XXDBJTable(new MatrixBasedDynamicXXDBTableModel(conn, name, rows), null, null);
				}
				else if(form == DATA_FORM.DF_DICTIONARY){
					displayTable = new XXDBJTable(new DictionaryBasedDynamicXXDBTableModel(conn, name, rows), null, null);
				}
				else if(form == DATA_FORM.DF_VECTOR || form == DATA_FORM.DF_SET){
					displayTable = new XXDBJTable(new VectorBasedDynamicXXDBTableModel(conn, name, rows, 10, node.getType(), form==DATA_FORM.DF_SET), null, null);
				}
				else
					return;
			}
			catch (RuntimeException ex) {
				try {
					java.util.Date dtShowError = new java.util.Date();
//					parent.displayMessage(Utility.formatDate(dtShowError) + ":");
//					parent.displayMessage(ex.getMessage());
					parent.displayError(new Exception(Utility.formatDate(dtShowError) + ":" + ex.getMessage()));
					update();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (userObject instanceof DimensionObjectNode) {
			DimensionObjectNode node = (DimensionObjectNode)userObject;
			String script = "rpc('" + node.getSite() + "',getTablePartitionSizeAndPath{chunkMeta('" + node.getPartitionPath() + "','" + node.getChunk() +
					"',true,"+node.getVersion() + ",-1,,-1),'" + node.getDbPath().substring("dfs:/".length()) + "','" + node.getLogicalTableName() + "'})";
			try {
				System.out.println(script);
				int totalRows = (int) ((BasicLong)((BasicAnyVector)conn.run(script)).get(0)).getLong();
				if(totalRows <= 0) {
					JOptionPane.showMessageDialog(parent, "There is no data in selected chunk.");
					return;
				}
				displayTable = new XXDBJTable(new DimensionTableBasedDynamicXXDBTableModel(conn, node, totalRows), null, null);
			}
			catch(IndexOutOfBoundsException e1){
				JOptionPane.showMessageDialog(parent, e1.getMessage());
				return;
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
			catch (RuntimeException ex) {
				try {
					parent.displayError(ex);
					update();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}

		} else if (userObject instanceof DfsObjectNode) {
			DfsObjectNode node = (DfsObjectNode)userObject;

			String script = "rpc('" + node.getSite() + "',getTablePartitionSizeAndPath{chunkMeta(\"" + node.getPartitionPath() + "\",\"" + node.getChunk() +
					"\",true,"+node.getVersion() + ",-1,,-1),\"" + node.getDbPath().substring("dfs:/".length()) + "\",\"" + node.getTableName() + "\"})";
			try {
				
				int totalRows = (int) ((BasicLong)((BasicAnyVector)conn.run(script)).get(0)).getLong();
				if(totalRows == 0) {
					JOptionPane.showMessageDialog(parent, "There is no data in selected chunk.");
					return;
				}
				displayTable = new XXDBJTable(new DfsTableBasedDynamicXXDBTableModel(conn, node, totalRows), null, null);
			}
			catch(IndexOutOfBoundsException e1){
				JOptionPane.showMessageDialog(parent, e1.getMessage());
				return;
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
			catch (RuntimeException ex) {
				try {
					parent.displayError(ex);
					update();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		XXDBTablePane pane = new XXDBTablePane(displayTable);
		
		new XXDBDataBrowser(parent, pane, rows, "Data Browser [" + name + "]");
	}
	
	private void handleUndefine(){
		if(conn.isBusy()){
			JOptionPane.showMessageDialog(parent, "The connection to dolphindb is in use. Please try again later.");
			return;
		}
		
		String script = "";
		String msg =  "Are you sure to undefine all variables in this category?";
		if(selectedNode==topNode||selectedNode==localNode){
			script ="undef(all);objs(true)";
		}
		else if(selectedNode.getUserObject() instanceof ObjectNode){
			ObjectNode node = (ObjectNode)selectedNode.getUserObject();
			String name = node.getName();
			msg =  "Are you sure to undefine selected variable [" + name + "]?";
			if(name.startsWith("@"))
				script = "undef(\"" + name + "\",GLOBAL);objs(true)";
			else
				script = "undef(\"" + name + "\",VAR);objs(true)";
			
			if(node.isShared()){
				script = "undef(\"" + name + "\",SHARED);objs(true)";
			}
		}else if(selectedNode instanceof DfsTableNode){
			String name = ((DfsTableNode)selectedNode).getUserObject().toString();
			msg =  "Are you sure to undefine selected variable [" + name + "]?";
			if(name.startsWith("@"))
				script = "undef(\"" + name + "\",GLOBAL);objs(true)";
			else
				script = "undef(\"" + name + "\",VAR);objs(true)";
		}
		else{
			if(selectedNode.getChildCount()==0)
				return;
			StringBuilder vars = new StringBuilder("");
			StringBuilder globals = new StringBuilder("");
			int childCount = selectedNode.getChildCount();
			for(int i=0; i<childCount; ++i){
				String name = ((ObjectNode)((DefaultMutableTreeNode)selectedNode.getChildAt(i)).getUserObject()).getName();
				if(name.startsWith("@")){
					if(globals.length()>0)
						globals.append(",");
					globals.append("\"" + name + "\"");
				}
				else{
					if(vars.length()>0)
						vars.append(",");
					vars.append("\"" + name + "\"");
				}
			}
			if(vars.length()>0)
				script = "undef([" + vars +"],VAR);";
			if(globals.length()>0)
				script = script + "undef([" + globals +"],GLOBAL);";
			script = script + "objs(true)";
		}
		if(script.isEmpty())
			return;

		if (JOptionPane.showConfirmDialog(parent, msg, "Undefine Variables", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION)
			return;
		
		try{
			update(script);
		}
		catch(Exception ex){
			JOptionPane.showMessageDialog(parent, "Error is raised when releasing variables: " + ex.getMessage());
		}
	}
	
	private void handleSchema() {
		if(conn == null) return;
		if(conn.isBusy()){
			JOptionPane.showMessageDialog(parent, "The connection to dolphindb is in use. Please try again later.");
			return;
		}
		
		ObjectNode node;
		try {
			 node = (ObjectNode)selectedNode.getUserObject();
		} catch (Exception e) {
			//e.printStackTrace();
			 return;
		}
		
		if(node ==null) return;
		
		try {
			Table schema = (Table) conn.run("schema(" + node.getName() + ").colDefs");
			XXDBJTable displayTable = new XXDBJTable(new TableBasedXXDBTableModel(schema), null, null);
			XXDBTablePane pane = new XXDBTablePane(displayTable);
			new XXDBDataBrowser(parent, pane, schema.rows(), "Schema of " + node.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
