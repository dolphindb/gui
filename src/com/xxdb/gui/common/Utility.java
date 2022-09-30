package com.xxdb.gui.common;

import com.xxdb.gui.component.XXDBException;

import java.io.*;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

public class Utility {
	private static String LAST_USED_WORKSPACE = null;
	private static double DPI_SCALE = 1.0;
	public static String cryptProtocol = "SHA-256";

	public static boolean checkNCreateFile(String filename) throws XXDBException {
		File file = new File(filename);
		if (!file.exists()) {
			try {
				file.createNewFile();
				return false;
			} catch (IOException ex) {
				throw new XXDBException("Not able to create file: " + filename + "...");
			}
		}
		return true;
	}

	public static int getScaledSize(int originalSize) {
		return (int) Math.round(originalSize * DPI_SCALE);
	}

	public static void setDPIScaleFactor(double scaleFactor) {
		DPI_SCALE = scaleFactor;
	}

	public static double getDPIScaleFactor() {
		return DPI_SCALE;
	}

	public static double getAdjustRate() {
		double adjrate = (DPI_SCALE - 1) / 2 + 1;
		return adjrate;
	}

	public static boolean isDPIScaled() {
		return DPI_SCALE != 1.0;
	}

	public static String getLastUsedWorkspace() {
		if (LAST_USED_WORKSPACE == null) {
			LAST_USED_WORKSPACE = getUserSetting("lastUsedWorkspace", "");
		}
		return LAST_USED_WORKSPACE;
	}

	public static void setLastUsedWorkspace(String path) {
		LAST_USED_WORKSPACE = path;
		setUserSetting("lastUsedWorkspace", path);
	}

	public static void setUserSetting(String key, String value) {
		Preferences prefs = Preferences.userRoot().node("Dolphin");
		prefs.put(key, value);
	}

	public static String getUserSetting(String key, String defaultValue) {
		Preferences prefs = Preferences.userRoot().node("Dolphin");
		return prefs.get(key, defaultValue);
	}

	public static boolean deleteFolder(File directory) {
		if (!directory.isDirectory())
			return false;
		for (File file : directory.listFiles()) {
			if (file.isFile()) {
				if (!file.delete())
					return false;
			} else
				deleteFolder(file);
		}
		return directory.delete();
	}

	public static String formatDate(java.util.Date dt) {
		SimpleDateFormat bf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
		return bf.format(dt);
	}

	public static String formatTime(Long ms) {
		Integer ss = 1000;
		Integer mi = ss * 60;
		Integer hh = mi * 60;
		Integer dd = hh * 24;

		Long day = ms / dd;
		Long hour = (ms - day * dd) / hh;
		Long minute = (ms - day * dd - hour * hh) / mi;
		Long second = (ms - day * dd - hour * hh - minute * mi) / ss;
		Long milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss;

		StringBuffer sb = new StringBuffer();
		if (day > 0) {
			sb.append(day + "day ");
		}
		if (hour > 0) {
			sb.append(hour + "h ");
		}
		if (minute > 0) {
			sb.append(minute + "m ");
		}
		if (second > 0) {
			sb.append(second + "s ");
		}
		if (milliSecond > 0) {
			sb.append(milliSecond + "ms");
		}
		return sb.toString();
	}

	public static String shaHash(String content) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (Exception ex) {
			return null;
		}

		md.update(content.getBytes());
		byte[] mdbytes = md.digest();

		// convert the byte to hex format method 1
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < mdbytes.length; i++) {
			sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}
	
	public static void changeLocale() {
		Locale.setDefault(Locale.ENGLISH);
	}
	
	static final String _255 = "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
	static final Pattern pattern = Pattern.compile("^(?:" + _255 + "\\.){3}" + _255 + "$");

		
	static String longToIpV4(long longIp) {
		int octet3 = (int) ((longIp >> 24) % 256);
		int octet2 = (int) ((longIp >> 16) % 256);
		int octet1 = (int) ((longIp >> 8) % 256);
		int octet0 = (int) ((longIp) % 256);
		return octet3 + "." + octet2 + "." + octet1 + "." + octet0;
	}

	static long ipV4ToLong(String ip) {
		String[] octets = ip.split("\\.");
		return (Long.parseLong(octets[0]) << 24) + (Integer.parseInt(octets[1]) << 16) + (Integer.parseInt(octets[2]) << 8) + Integer.parseInt(octets[3]);
	}

	static boolean isIPv4Private(String ip) {
	    long longIp = ipV4ToLong(ip);
	    return (longIp >= ipV4ToLong("10.0.0.0") && longIp <= ipV4ToLong("10.255.255.255"))
	            || (longIp >= ipV4ToLong("172.16.0.0") && longIp <= ipV4ToLong("172.31.255.255"))
	            || longIp >= ipV4ToLong("192.168.0.0") && longIp <= ipV4ToLong("192.168.255.255");
	}
	
	static boolean isIPv4Valid(String ip) {
		return pattern.matcher(ip).matches();
	}
	
	public static String readToString(String fileName) {  
        String encoding = "UTF-8";  
        File file = new File(fileName);  
        if(!file.exists())
        	return "";
        Long filelength = file.length();  
        byte[] filecontent = new byte[filelength.intValue()];  
        try {  
            FileInputStream in = new FileInputStream(file);  
            in.read(filecontent);  
            in.close();  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        try {  
            return new String(filecontent, encoding);  
        } catch (UnsupportedEncodingException e) {  
            System.err.println("The OS does not support " + encoding);  
            e.printStackTrace();  
            return null;  
        }  
    }

    public  static boolean isUTF8(String path){
		java.io.File f=new java.io.File(path);
		try{
			java.io.InputStream ios=new java.io.FileInputStream(f);
			byte[] b=new byte[3];
			ios.read(b);
			ios.close();
			if(b[0]==-17&&b[1]==-69&&b[2]==-65)
				return true;
			else
				return false;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	public static void ANSI2UTF8(String filepath) throws IOException{
		BufferedReader buf = null;
		OutputStreamWriter pw=null;
		String str = null;
		String allstr="";
		byte[] c=new byte[2];
		c[0]=0x0d;
		c[1]=0x0a;
		String t=new String(c);
		buf=new BufferedReader(new InputStreamReader(new FileInputStream(filepath), "GBK"));
		while((str = buf.readLine()) != null){
			allstr=allstr+str+t;
		}
		buf.close();
		pw =new OutputStreamWriter(new FileOutputStream(filepath),"UTF-8");
		pw.write(allstr);
		pw.close();
	}

	public static boolean isNumeric(String str){
		for (int i = str.length();--i>=0;){
		    if (!Character.isDigit(str.charAt(i))){
		        return false;
		    }
		}
		return true;
	}

	public static String getPathSeperator(String os){
		if(os.toLowerCase().equals("linux")){
			return "/";
		}else if(os.toLowerCase().equals("windows")){
			return "\\";
		}else {
			return "/";
		}
	}
}
