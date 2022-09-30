package com.xxdb.gui.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import com.xxdb.gui.data.ProjectNode.NodeType;

public class Workspace {
	private TreeMap<String, Server> servers = new TreeMap<>();
	private String activeServer;
	private List<String> openFiles = new ArrayList<>();
	private String activeFile;
	private List<ProjectNode> projects = new ArrayList<>();
	
	public Workspace(){
		
	}
	
	public void loadWorkspace(String wsFile) throws IOException{
		clear();
		File file = new File(wsFile);
		if(!file.exists())
			return;
	
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while((line = br.readLine()) != null){
			line = line.trim();
			

			
			if(line.isEmpty())
				continue;
			String[] arrField = line.split(",");
			if(arrField.length < 2 || arrField.length > 7)
				continue;
			if(arrField[0].equals("Project")){
				projects.add(new ProjectNode(arrField[2], arrField[1], NodeType.PROJECT));
			}
			else if(arrField[0].equals("Folder")){
				projects.add(new ProjectNode(arrField[2], arrField[1], NodeType.FOLDER));
			}
			else if(arrField[0].equals("Server")){
				servers.put(arrField[1], new Server(arrField[1], arrField[2], Integer.parseInt(arrField[3]),
							(arrField.length <= 4 ? "" : arrField[4]),(arrField.length <= 5 ? "" : arrField[5]),(arrField.length <= 6 ? "" : arrField[6])));
			}
			else if(arrField[0].equals("Open")){
				File openFile = new File(arrField[1]);
				if(openFile.isFile() && openFile.exists())
					openFiles.add(arrField[1]);
			}
			else if(arrField[0].equals("ActiveFile"))
				activeFile = arrField[1].trim();
			else if(arrField[0].equals("ActiveServer"))
				activeServer = arrField[1].trim();
		}

		br.close();
	}
	
	public void saveWorkspace(String wsFile) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(wsFile)));
		for(ProjectNode node : projects){
			if(node.getNodeType() == NodeType.PROJECT)
				bw.write("Project,"+node.getName()+","+node.getDirPath());
			else if(node.getNodeType() == NodeType.FOLDER)
				bw.write("Folder,"+node.getName()+","+node.getDirPath());
			else
				continue;
			bw.newLine();
		}
		
		for(String file : openFiles){
			bw.write("Open," + file);
			bw.newLine();
		}
		
		for(Server server : servers.values()){
			bw.write("Server," + server.getName() + "," + server.getHost() + "," + server.getPort() + "," + server.getRemoteDir() + "," + server.getUsername() + "," + server.getPassword());
			bw.newLine();
		}
		
		if(activeFile != null && !activeFile.isEmpty()){
			bw.write("ActiveFile," + activeFile);
			bw.newLine();
		}
		
		if(activeServer != null && ! activeServer.isEmpty()){
			bw.write("ActiveServer," + activeServer);
			bw.newLine();
		}
		bw.close();
	}
	
	public void clear(){
		openFiles.clear();
		servers.clear();
		projects.clear();
		activeFile = null;
		activeServer = null;
	}
	
	public void clearProjects(){
		projects.clear();
	}
	
	public void clearOpenFiles(){
		openFiles.clear();
		activeFile = null;
	}
	
	public void clearServers(){
		servers.clear();
		activeServer = null;
	}
	
	public boolean addServer(Server server, boolean active){
		servers.put(server.getName(), server);
		if(active)
			activeServer = server.getName();
		return true;
	}
	
	public Server getServer(String name){
		return servers.get(name);
	}
	
	public void addProject(ProjectNode node){
		projects.add(node);
	}
	
	public void addFile(String file, boolean active){
		openFiles.add(file);
		if(active)
			activeFile = file;
	}
	
	public Collection<Server> getServers(){
		return servers.values();
	}
	
	public List<String> getOpenFiles(){
		return openFiles;
	}
	
	public List<ProjectNode> getProjects(){
		return projects;
	}
	
	public Server getActiveServer(){
		if(activeServer == null)
			return null;
		else
			return servers.get(activeServer);
	}
	
	public String getActiveFile(){
		return activeFile;
	}
}
