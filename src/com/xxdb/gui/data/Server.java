package com.xxdb.gui.data;

public class Server implements Comparable<Server>{
	private String name;
	private String host;
	private int port;
	private String remoteDir;
	
	private String username;
	private String password;
	
	public Server(String name, String host, int port, String remoteDir) {
		this.name = name;
		this.host = host;
		this.port = port;
		this.remoteDir = remoteDir;
	}
	
	public Server(String name, String host, int port, String remoteDir,String username,String password) {
		this.name = name;
		this.host = host;
		this.port = port;
		this.remoteDir = remoteDir;
		this.username = username;
		this.password = password;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public boolean hasRemoteDir() {
		return !remoteDir.trim().isEmpty();
	}
	
	public String getRemoteDir() {
		return remoteDir;
	}
	
	public void setRemoteDir(String remoteDir) {
		this.remoteDir = remoteDir;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String userName) {
		this.username = userName;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String passWord) {
		this.password = passWord;
	}
	
	public String toString() {
		return this.name + "[" + this.host + ":" + this.port+"]";
	}
	
	public String getFormatedRemoteDir() {
		String formatedRemoteDir = this.remoteDir.replace('\\', '/');    // For multiple OS compatibility
		return formatedRemoteDir + (formatedRemoteDir.endsWith("/") ? "" : "/");
	}

	@Override
	public int compareTo(Server obj) {
		return name.compareTo(obj.getName());
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof Server)
			return name.equals(((Server)obj).getName());
		else
			return false;
	}
	
	@Override
	public int hashCode(){
		return name.hashCode();
	}
}
