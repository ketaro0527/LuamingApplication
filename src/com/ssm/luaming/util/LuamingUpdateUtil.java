package com.ssm.luaming.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LuamingUpdateUtil {

	private static final byte[] BUFFER = new byte[4096 * 1024];
	private static final String DELETELIST_NAME = "DeleteList.txt";

	public static void copy(InputStream input, OutputStream output) throws IOException {
		int bytesRead;
		while ((bytesRead = input.read(BUFFER))!= -1) {
			output.write(BUFFER, 0, bytesRead);
		}
		input.close();
	}    

	private static boolean isDeleteFile(JSONArray deleteArray, String entryName) {
		if (deleteArray == null || deleteArray.length() == 0 || entryName == null || entryName.length() == 0)
			return false;

		int length = deleteArray.length();
		for (int i = 0; i < length; i++) {
			try {
				if (entryName.equals(deleteArray.getString(i)))
					return true;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return false;
	}

	public static JSONArray makeDeleteList(ZipFile updateZip) {
		if (updateZip == null)
			return null;
		
		try {
			ZipEntry delEntry = updateZip.getEntry(DELETELIST_NAME);
			if (delEntry != null) {
				InputStream input = updateZip.getInputStream(delEntry);
				BufferedReader br = new BufferedReader(new InputStreamReader(input));
				String data = "";
				String s;
				while ((s = br.readLine()) != null)
					data += s;
				br.close();
				input.close();

				return new JSONArray(data);
			}
		} catch (Exception e) {
			return null;
		}

		return null;
	}
	
	public static boolean oldZipFilter(String name, JSONArray deleteArray) {
		if (name.startsWith("/") || isDeleteFile(deleteArray, name))
			return false;
		return true;
	}
	
	public static boolean updateZipFilter(String name) {
		if (name.startsWith("/") || name.contains(DELETELIST_NAME))
			return false;
		return true;
	}
	
	public static int checkVersion(String dirPath, String apkName) {
		int version = 0;
		
		File dir = new File(dirPath);
		if (!dir.exists())
			return version;
		
		try {
			ZipFile apkZip = new ZipFile(dirPath + "/" + apkName);
			for (Enumeration<? extends ZipEntry> e = apkZip.entries(); e.hasMoreElements();) {
				ZipEntry ze = e.nextElement();
				if (ze.getName().contains("LuamingProject.json")) {
					String jsonString = "";
					
					InputStream is = apkZip.getInputStream(ze);
					int bytesRead;
					while ((bytesRead = is.read(BUFFER)) > 0) {
						jsonString += new String(BUFFER, 0, bytesRead);
					}
					is.close();
					
					JSONObject projectInfoJson = new JSONObject(jsonString);
					version = projectInfoJson.getInt("VERSION_CODE");
					break;
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return version;
	}
	
	public static String checkVersionName(String dirPath, String apkName) {
		String versionName = "0.0.0";
		
		File dir = new File(dirPath);
		if (!dir.exists())
			return versionName;
		
		try {
			ZipFile apkZip = new ZipFile(dirPath + "/" + apkName);
			for (Enumeration<? extends ZipEntry> e = apkZip.entries(); e.hasMoreElements();) {
				ZipEntry ze = e.nextElement();
				if (ze.getName().contains("LuamingProject.json")) {
					String jsonString = "";
					
					InputStream is = apkZip.getInputStream(ze);
					int bytesRead;
					while ((bytesRead = is.read(BUFFER)) > 0) {
						jsonString += new String(BUFFER, 0, bytesRead);
					}
					is.close();
					
					JSONObject projectInfoJson = new JSONObject(jsonString);
					versionName = projectInfoJson.getString("VERSION_NAME");
					break;
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return versionName;
	}
	
	public static String checkOrientation(String dirPath, String apkName) {
		String orientation = "landscape";
		
		File dir = new File(dirPath);
		if (!dir.exists())
			return orientation;
		
		try {
			ZipFile apkZip = new ZipFile(dirPath + "/" + apkName);
			for (Enumeration<? extends ZipEntry> e = apkZip.entries(); e.hasMoreElements();) {
				ZipEntry ze = e.nextElement();
				if (ze.getName().contains("LuamingProject.json")) {
					String jsonString = "";
					
					InputStream is = apkZip.getInputStream(ze);
					int bytesRead;
					while ((bytesRead = is.read(BUFFER)) > 0) {
						jsonString += new String(BUFFER, 0, bytesRead);
					}
					is.close();
					
					JSONObject projectInfoJson = new JSONObject(jsonString); 
					orientation = projectInfoJson.getString("ORIENTATION");
					break;
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return orientation;
	}
	
	public static long getFileSize(String dirPath, String updateName, boolean zeroToDelete) {
		String updateZipPath = dirPath + "/" + updateName;
		File updateFile = new File(updateZipPath);
		long size = updateFile.length();
		if (size == 0 && zeroToDelete)
			updateFile.delete();
		return size;
	}
	
	public static File getUpdateFile(String dirPath) {
		File dir = new File(dirPath);
		FilenameFilter filter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				if (name != null && name.contains("_Update") && name.endsWith("apk"))
					return true;
				return false;
			}
		};
		
		File[] updateFiles = dir.listFiles(filter);
		File realUpdateFile = null;
		if (updateFiles != null) {
			int length = updateFiles.length;
			for (int i = 0; i < length; i++) {
				if (updateFiles[i].length() > 0)
					realUpdateFile = updateFiles[i];
				else
					updateFiles[i].delete();
			}
		}
		
		return realUpdateFile;
	}
	
	public static File getUpdateFile(String dirPath, int latestVersion) {
		File dir = new File(dirPath);
		FilenameFilter filter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				if (name != null && name.contains("_Update") && name.endsWith("apk"))
					return true;
				return false;
			}
		};
		
		File[] updateFiles = dir.listFiles(filter);
		File realUpdateFile = null;
		if (updateFiles != null) {
			int length = updateFiles.length;
			for (int i = 0; i < length; i++) {
				if (updateFiles[i].length() > 0 && latestVersion == LuamingUpdateUtil.checkVersion(updateFiles[i].getParent(), updateFiles[i].getName()))
					realUpdateFile = updateFiles[i];
				else
					updateFiles[i].delete();
			}
		}
		
		return realUpdateFile;
	}
}
