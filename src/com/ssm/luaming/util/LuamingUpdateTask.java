package com.ssm.luaming.util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.json.JSONArray;

import android.widget.Toast;

import com.ssm.luaming.LuamingActivity;
import com.ssm.luaming.LuamingConstant;

public class LuamingUpdateTask extends LuamingAsyncTask {
	
	private LuamingActivity activity;
	private String currentEntryName = "";
	
	public LuamingUpdateTask(LuamingActivity act) {
		activity = act;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		if (activity.accessToken.length() > 0) {
			switch(LuamingActivity.downloadFor) {
			case LuamingConstant.DOWNLOAD_FOR_INSTALL:
			case LuamingConstant.DOWNLOAD_FOR_REPLACE: {
				if (updateToReplace(LuamingActivity.mainPath + "/" + activity.accessToken + "/" + activity.packageName, activity.apkName, activity.updateName))
					return LuamingConstant.UPDATE_COMPLETE;
				else
					return LuamingConstant.UPDATE_FAILED;
			}
			case LuamingConstant.DOWNLOAD_FOR_UPDATE: {
				if (update(LuamingActivity.mainPath + "/" + activity.accessToken + "/" + activity.packageName, activity.apkName, activity.updateName))
					return LuamingConstant.UPDATE_COMPLETE;
				else
					return LuamingConstant.UPDATE_FAILED;
			}
			default:
				break;
			}
		}
		
		return LuamingConstant.UPDATE_FAILED;
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		int progress = values[0];
		if (activity.pd != null && activity.pd.isShowing()) {
			activity.pd.updateProgress(progress);
			activity.pd.updateText(currentEntryName);
		}
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		if (activity.pd != null) {
			activity.pd.dismiss();
			activity.pd = null;
		}

		if (result == LuamingConstant.UPDATE_COMPLETE) {
			activity.isUpdating = false;
			activity.startGame();
		}
		else if (result == LuamingConstant.UPDATE_FAILED) {
			activity.isUpdating = false;
			Toast.makeText(activity, "Update Failed", Toast.LENGTH_SHORT).show();
		}
	}
	
	public boolean update(String dirPath, String apkName, String updateName) {
		String oldZipPath = dirPath + "/" + apkName;
		String updateZipPath = dirPath + "/" + updateName;
		String tempZipPath = dirPath + "/temp.apk";
		
		File oldFile = new File(oldZipPath);
		if (!oldFile.exists())
			return false;
		
		File updateFile = new File(updateZipPath);
		if (!updateFile.exists() || updateFile.length() == 0) {
			updateFile = LuamingUpdateUtil.getUpdateFile(dirPath);
			if (!updateFile.exists() || updateFile.length() == 0)
				return false;
		}
		
		File tempFile = new File(tempZipPath);
		if (tempFile.exists())
			tempFile.delete();
		tempFile = null;
		
		try {
			ZipFile oldZip = new ZipFile(oldZipPath);
			ZipFile updateZip = new ZipFile(updateZipPath);
			ZipOutputStream zo = new ZipOutputStream(new FileOutputStream(tempZipPath));

			// Make delete list
			JSONArray deleteArray = LuamingUpdateUtil.makeDeleteList(updateZip);

			// Copy not changed file from oldzip and changed file from update zip
			int entry_total = Collections.list(oldZip.entries()).size() + Collections.list(updateZip.entries()).size();
			int entry_checked = 0;
			for (Enumeration<? extends ZipEntry> e = oldZip.entries(); e.hasMoreElements();) {
				ZipEntry ze = e.nextElement();
				currentEntryName = ze.getName();
				ZipEntry uze = updateZip.getEntry(currentEntryName);
				ZipEntry tempze;
				ZipFile tempZip;
				// If new resource is updated
				if (uze != null) {
					String uName = uze.getName();
					if (!LuamingUpdateUtil.updateZipFilter(uName)) {
						entry_checked++;
						publishProgress((int)(((double)entry_checked / (double)entry_total) * 100));
						continue;
					}
					tempZip = updateZip;
					tempze = uze;
				}
				else {
					String name = ze.getName();
					if (!LuamingUpdateUtil.oldZipFilter(name, deleteArray)) {
						entry_checked++;
						publishProgress((int)(((double)entry_checked / (double)entry_total) * 100));
						continue;
					}

					tempZip = oldZip;
					tempze = ze;
				}

				zo.putNextEntry(tempze);
				if (!tempze.isDirectory()) {
					LuamingUpdateUtil.copy(tempZip.getInputStream(tempze), zo);
				}
				zo.closeEntry();
				
				entry_checked++;
				publishProgress((int)(((double)entry_checked / (double)entry_total) * 100));
			}
			
			// Copy appended file from update zip
			for (Enumeration<? extends ZipEntry> e = updateZip.entries(); e.hasMoreElements();) {
				ZipEntry uze = e.nextElement();
				String uName = uze.getName();
				currentEntryName = uName;
				
				if (!LuamingUpdateUtil.updateZipFilter(uName)) {
					entry_checked++;
					publishProgress((int)(((double)entry_checked / (double)entry_total) * 100));
					continue;
				}
				
				// Is new appened file?
				if (oldZip.getEntry(uze.getName()) == null) {
					zo.putNextEntry(uze);
					if (!uze.isDirectory()) {
						LuamingUpdateUtil.copy(updateZip.getInputStream(uze), zo);
					}
					zo.closeEntry();
				}
				
				entry_checked++;
				publishProgress((int)(((double)entry_checked / (double)entry_total) * 100));
			}

			oldZip.close();
			updateZip.close();
			zo.close();

			// Remove old zip file and rename temp zip file
			tempFile = new File(tempZipPath);
			tempFile.renameTo(oldFile);
			updateFile.delete();
		} catch (Exception e) {
			e.printStackTrace();

			return false;
		}

		return true;
	}
	
	public boolean updateToReplace(String dirPath, String apkName, String updateName) {
		String oldZipPath = dirPath + "/" + apkName;
		String updateZipPath = dirPath + "/" + updateName;
		
		File oldFile = new File(oldZipPath);
		File updateFile = new File(updateZipPath);
		
		publishProgress(100);
		
		if (!updateFile.exists()) 
			return false;
		if (!updateFile.renameTo(oldFile))
			return false;
		
		return true;
	}

}
