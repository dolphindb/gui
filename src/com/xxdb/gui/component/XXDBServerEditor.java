package com.xxdb.gui.component;

import com.xxdb.data.BasicInt;
import com.xxdb.data.Entity.DATA_CATEGORY;
import com.xxdb.gui.common.Utility;
import com.xxdb.gui.data.Server;
import com.xxdb.gui.data.Workspace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class XXDBServerEditor extends BasicDialog implements ActionListener{
	private static final long serialVersionUID = 1L;
	
	private Workspace ws;
	private String wsFile;
	private JComboBox<String> cboServer;
	private XXDBJTable keyTable;
	private ArrayBasedXXDBTableModel keyModel;

	public XXDBServerEditor(JFrame parent, Workspace ws, String wsFile, JComboBox<String> cboServer) {
		double adjrate = Utility.getAdjustRate();
		
		this.ws = ws;
		this.wsFile = wsFile;
		this.cboServer = cboServer;
		setModalityType(ModalityType.APPLICATION_MODAL);
		setModal(true);
		setResizable(true);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Server Editor");
		setSize((int)(adjrate*540), (int)(adjrate*400));
		setLocationRelativeTo(null);

		final JSplitPane splitPane = new JSplitPane();
		splitPane.setDividerSize(2);
		splitPane.setDividerLocation((int)(40*adjrate));
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		getContentPane().add(splitPane, BorderLayout.CENTER);

		final JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		splitPane.setLeftComponent(panel);

		int adjWidth  = (int)(84*adjrate);
		int adjHeight  = (int)(20*adjrate);
		
		final JButton cmdOK = new JButton();
		cmdOK.setText("OK");
		cmdOK.setBounds(241, 10, adjWidth, adjHeight);
		cmdOK.addActionListener(this);
		panel.add(cmdOK);

		final JButton cmdCancel = new JButton();
		cmdCancel.setText("Cancel");
		cmdCancel.setBounds(331, 10, adjWidth, adjHeight);
		cmdCancel.addActionListener(this);
		panel.add(cmdCancel);
		
		List<TableColumnMeta> cols = new ArrayList<>();
		cols.add(new TableColumnMeta("Delete", Boolean.class, DATA_CATEGORY.LOGICAL, null, true));
		cols.add(new TableColumnMeta("Name", String.class, DATA_CATEGORY.LITERAL, null, false));
		cols.add(new TableColumnMeta("Host", String.class, DATA_CATEGORY.LITERAL, null, true));
		cols.add(new TableColumnMeta("Port", Integer.class, DATA_CATEGORY.INTEGRAL, "0", true));
		cols.add(new TableColumnMeta("Remote Directory", String.class, DATA_CATEGORY.LITERAL, null, true));
		cols.add(new TableColumnMeta("Username", String.class, DATA_CATEGORY.LITERAL, null, true));
		cols.add(new TableColumnMeta("RealPassword", String.class, DATA_CATEGORY.LITERAL, null, true));
		cols.add(new TableColumnMeta("Password", String.class, DATA_CATEGORY.LITERAL, null, true));
		keyModel = new ArrayBasedXXDBTableModel(cols){
			private static final long serialVersionUID = 1L;
		    
		    public boolean isColumnReorderingAllowed(){
		    	return false;
		    }
		    
		    public boolean isRowReorderingAllowed(){
		    	return false;
		    }
		    
		    public boolean showPopupMenu(){
		    	return false;
		    }
		};
		keyModel.updateData(getServers(ws, cboServer));
		keyTable = new XXDBJTable(keyModel, null);
		//hide password column
		keyTable.removeColumn(keyTable.getColumn("RealPassword"));
		
		keyTable.setRowHeight((int)(adjrate*20));
		keyTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		keyTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		
		JTextField txt = new JTextField();
		DefaultCellEditor numEditor = new DefaultCellEditor(txt){
			private static final long serialVersionUID = 1L;
			public Object getCellEditorValue() {
				try{
					return new BasicInt(Integer.parseInt(txt.getText()));
				}
				catch(Exception ex){
					BasicInt x = new BasicInt(0);
					x.setNull();
					return x;
				}
		    }
		};
		
		keyTable.getColumnModel().getColumn(3).setCellEditor(numEditor);
		
		final JScrollPane sPanel = new JScrollPane();
		sPanel.setViewportView(keyTable);
		splitPane.setRightComponent(sPanel);
	}
	
	public void actionPerformed(ActionEvent event){
		String action;
		
		action=event.getActionCommand();
		if(action.equalsIgnoreCase("Cancel")){
			this.dispose();
		}
		else if(action.equalsIgnoreCase("OK")){
			setServers();
			this.dispose();
		}
	}
	
	private void setServers(){
		List<Server> servers = new ArrayList<Server>();
		String activeServer = "";
		if(ws.getActiveServer() != null)
			activeServer = ws.getActiveServer().getName();
		for(int i=0;i<keyModel.getRowCount();i++){
			boolean del =(boolean)keyModel.getValueAt(i,0);
			if(!del){
				String name = (String)keyModel.getValueAt(i, 1);
				Server server = ws.getServer(name);
				String host = (String)keyModel.getValueAt(i, 2);
				BasicInt port = (BasicInt)keyModel.getValueAt(i, 3);
				String remoteDir = (String)keyModel.getValueAt(i, 4);
				String username = (String)keyModel.getValueAt(i, 5);
				String password = (String)keyModel.getValueAt(i, 6);
				String editPassword = (String)keyModel.getValueAt(i, 7);
				if(editPassword != "******")		{
					password = editPassword;
				}
				if (editPassword.isEmpty()) {
					password = "";
				}
				if(!host.isEmpty() && !port.isNull()){
					server.setHost(host);
					server.setPort(port.getInt());
					server.setRemoteDir(remoteDir.trim());
					server.setUsername(username);
					server.setPassword(password);
				}
				servers.add(server);
			}
		}
		ws.clearServers();
		for(Server server : servers){
			ws.addServer(server, activeServer.equals(server.getName()));
		}
		
		if(servers.size() < cboServer.getItemCount()){
			cboServer.removeAllItems();
			for(Server server : servers)
				cboServer.addItem(server.getName());
			cboServer.setSelectedItem(activeServer);
		}
		try{
			ws.saveWorkspace(wsFile);
		}
		catch(Exception ex){
			JOptionPane.showMessageDialog(this, "Failed to save workspace to " + wsFile, "DolphinDB Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private Object[][] getServers(Workspace ws, JComboBox<String> cboServer){
		Object[][] arrData = new Object[cboServer.getItemCount()][8];
		
		for(int i=0;i<cboServer.getItemCount();i++){
			Server server = ws.getServer(cboServer.getItemAt(i));
			arrData[i][0]= false;
			arrData[i][1]= cboServer.getItemAt(i);
			arrData[i][2]= server.getHost();
			arrData[i][3]= new BasicInt(server.getPort());
			arrData[i][4]= server.getRemoteDir();
			arrData[i][5]= server.getUsername();
			arrData[i][6]= server.getPassword();
			arrData[i][7]= server.getPassword().isEmpty() ? "" : "******";//mask password string
		}
		return arrData;
	}
}
