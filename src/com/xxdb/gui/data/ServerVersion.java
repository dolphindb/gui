package com.xxdb.gui.data;

public class ServerVersion {
    private int major;
    private int minor;
    private int revision;
    
    public ServerVersion(String versionString) throws Exception{
        String[] v = versionString.split("\\.");
        if(v.length<3){
            throw new Exception("Invalid version string: " + versionString);
        }
        major = Integer.parseInt(v[0]);
        minor = Integer.parseInt(v[1]);
        revision = Integer.parseInt(v[2]);
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getRevision() {
        return revision;
    }
    
    public int getValue(){
        return major * 10000 + minor * 100 + revision;
    }
}
