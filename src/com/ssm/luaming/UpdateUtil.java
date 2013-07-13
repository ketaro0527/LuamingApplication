package com.ssm.luaming;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UpdateUtil {

	private static final byte[] BUFFER = new byte[4096 * 1024];
	private static final String DELETELIST_NAME = "DeleteList.txt";

	public static boolean update(String dirPath, String apkName, String updateName) {
		String oldZipPath = dirPath + "/" + apkName;
		String updateZipPath = dirPath + "/" + updateName;
		String tempZipPath = dirPath + "/temp.apk";
		
		File oldFile = new File(oldZipPath);
		if (!oldFile.exists())
			return false;
		
		File updateFile = new File(updateZipPath);
		if (!updateFile.exists())
			return false;
		
		try {
			ZipFile oldZip = new ZipFile(oldZipPath);
			ZipFile updateZip = new ZipFile(updateZipPath);
			ZipOutputStream zo = new ZipOutputStream(new FileOutputStream(tempZipPath));

			// Make delete list
			JSONArray deleteArray = makeDeleteList(updateZip);

			// Copy not changed file from oldzip and changed file from update zip
			for (Enumeration<? extends ZipEntry> e = oldZip.entries(); e.hasMoreElements();) {
				ZipEntry ze = e.nextElement();
				ZipEntry uze = updateZip.getEntry(ze.getName());
				ZipEntry tempze;
				ZipFile tempZip;
				// If new resource is updated
				if (uze != null) {
					String uName = uze.getName();
					if (!updateZipFilter(uName))
						continue;
					tempZip = updateZip;
					tempze = uze;
				}
				else {
					String name = ze.getName();
					if (!oldZipFilter(name, deleteArray))
						continue;

					tempZip = oldZip;
					tempze = ze;
				}

				zo.putNextEntry(tempze);
				if (!tempze.isDirectory()) {
					copy(tempZip.getInputStream(tempze), zo);
				}
				zo.closeEntry();
			}
			// Copy appended file from update zip
			for (Enumeration<? extends ZipEntry> e = updateZip.entries(); e.hasMoreElements();) {
				ZipEntry uze = e.nextElement();
				String uName = uze.getName();
				if (!updateZipFilter(uName))
					continue;

				// Is new appened file?
				if (oldZip.getEntry(uze.getName()) == null) {
					zo.putNextEntry(uze);
					if (!uze.isDirectory()) {
						copy(updateZip.getInputStream(uze), zo);
					}
					zo.closeEntry();
				}
			}

			oldZip.close();
			updateZip.close();
			zo.close();

			// Remove old zip file and rename temp zip file
			File tempFile = new File(tempZipPath);
			tempFile.renameTo(oldFile);
			updateFile.delete();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			return false;
		}

		return true;
	}
	
	public static boolean updateToReplace(String dirPath, String apkName, String updateName) {
		String oldZipPath = dirPath + "/" + apkName;
		String updateZipPath = dirPath + "/" + updateName;
		
		File oldFile = new File(oldZipPath);
		File updateFile = new File(updateZipPath);
		if (!updateFile.exists()) 
			return false;
		if (!updateFile.renameTo(oldFile))
			return false;
		
		return true;
	}

	private static void copy(InputStream input, OutputStream output) throws IOException {
		int bytesRead;
		while ((bytesRead = input.read(BUFFER))!= -1) {
			output.write(BUFFER, 0, bytesRead);
		}
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

	private static JSONArray makeDeleteList(ZipFile updateZip) {
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

				return new JSONArray(data);
			}
		} catch (Exception e) {
			return null;
		}

		return null;
	}
	
	private static boolean oldZipFilter(String name, JSONArray deleteArray) {
		if (name.startsWith("/") || isDeleteFile(deleteArray, name))
			return false;
		return true;
	}
	
	private static boolean updateZipFilter(String name) {
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
}
