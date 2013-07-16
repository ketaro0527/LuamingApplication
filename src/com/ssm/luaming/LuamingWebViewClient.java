package com.ssm.luaming;

import java.io.File;
import java.io.FilenameFilter;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class LuamingWebViewClient extends WebViewClient {
	public MainActivity activity;

	public LuamingWebViewClient(MainActivity act) {
		activity = act;
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		// TODO Auto-generated method stub
		if (url.startsWith("luaming")) {
			try {
				if (url.contains("account")) {
					String[] temp = url.split("@");

					JSONObject account = new JSONObject(temp[temp.length-1]);

					SharedPreferences sp = activity.getSharedPreferences(MainActivity.LUAMING_PREF, Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = sp.edit();
					editor.putInt(MainActivity.LUAMING_ACCOUNT_ID, account.getInt("account_id"));
					editor.putString(MainActivity.LUAMING_ACCESS_TOKEN, account.getString("access_token"));
					editor.commit();

					File dir = new File(MainActivity.mainPath + "/" + account.getString("access_token"));
					if (!dir.exists())
						dir.mkdir();
					
					activity.accountId = account.getInt("account_id");
					activity.accessToken = account.getString("access_token");
				}
				else if (url.contains("cangoback")) {
					String[] temp = url.split("@");
					activity.canGoBack =  Boolean.parseBoolean(temp[temp.length-1]);
				}
				else if (url.endsWith("ok")) {
					activity.startGame();
				}						
				else if (url.endsWith("logout")) {
					SharedPreferences sp = activity.getSharedPreferences(MainActivity.LUAMING_PREF, Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = sp.edit();
					editor.clear();
					editor.commit();

					view.loadUrl("javascript: Luaming.initLocalStorage()");
					view.loadUrl("javascript: Luaming.redirectToHome()");
				}
				else if (url.contains("download")) {
					String[] temp = url.split("@");
					JSONObject projectInfo = new JSONObject(temp[temp.length-1]);
					String packageName = projectInfo.getString("package_name");
					String projectName = projectInfo.getString("project_name");
					int gameId = projectInfo.getInt("game_id");
					int latestVersion = projectInfo.getInt("latest_version_code");
					
					int currentVersion = UpdateUtil.checkVersion(MainActivity.mainPath + "/" + activity.accessToken + "/" + packageName, projectName + ".apk");

					activity.setProjectName(projectName, packageName);
					
					SharedPreferences sp = activity.getSharedPreferences(MainActivity.LUAMING_PREF, Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = sp.edit();
					// 이미 설치되어 있는 경우
					if (currentVersion > 0) {
						// 현재 버전이 최신버전과 동일한 경우
						if (latestVersion == currentVersion) {
							activity.startGame();
						}
						// 현재 버전이 구버전인 경우
						else {
							// 이전에 다운받아놓은 업데이트 있는지 확인
							File update = updateFile(MainActivity.mainPath + "/" + activity.accessToken + "/" + packageName, latestVersion);
							if (update != null) {
								activity.updateName = update.getName();
								activity.handler.sendEmptyMessage(MainActivity.UPDATE_START);
								return true;
							}
							
							String latestVersionName = projectInfo.getString("latest_version_name");
							String currentVersionName = UpdateUtil.checkVersionName(MainActivity.mainPath + "/" + activity.accessToken + "/" + packageName, projectName + ".apk");
							// 메이저 버전이 같은 경우 => 업데이트
							if (currentVersionName.startsWith(latestVersionName.substring(0, 1))) {
								editor.putInt(MainActivity.LUAMING_DOWNLOAD_FOR, MainActivity.DOWNLOAD_FOR_UPDATE);
								editor.commit();
								MainActivity.downloadFor = MainActivity.DOWNLOAD_FOR_UPDATE;
								// 다운로드 시작
								view.loadUrl("javascript: Luaming.updateDownload(" + gameId + ", " + currentVersion + ")");
							}
							// 메이저 버전이 다른 경우 => 새로운 Full 소스 다운로드
							else {
								editor.putInt(MainActivity.LUAMING_DOWNLOAD_FOR, MainActivity.DOWNLOAD_FOR_REPLACE);
								editor.commit();
								MainActivity.downloadFor = MainActivity.DOWNLOAD_FOR_REPLACE;
								// 다운로드 시작
								view.loadUrl("javascript: Luaming.fullDownload(" + gameId + ")");
							}
						}
					}
					// 설치되어 있지 않은 경우 => Full 소스 다운로드
					else {
						// 폴더부터 생성
						File dir = new File(MainActivity.mainPath + "/" + activity.accessToken + "/" + packageName);
						if (!dir.exists())
							dir.mkdir();
						editor.putInt(MainActivity.LUAMING_DOWNLOAD_FOR, MainActivity.DOWNLOAD_FOR_INSTALL);
						editor.commit();
						MainActivity.downloadFor = MainActivity.DOWNLOAD_FOR_INSTALL;
						
						// 다운로드 시작
						view.loadUrl("javascript: Luaming.fullDownload(" + gameId + ")");
					}
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}

		view.loadUrl(url);

		return false;
	}

	@Override
	public void onPageFinished(WebView view, String url) {
		// TODO Auto-generated method stub
		super.onPageFinished(view, url);
		if (activity.isFirstTime) {
			activity.isFirstTime = false;
			if (!activity.hasAccountInfo) {
				view.loadUrl("javascript: Luaming.initLocalStorage()");
				view.loadUrl("javascript: Luaming.redirectToHome()");
			}
			else {
				view.loadUrl("javascript: Luaming.initLocalStorage()");
				view.loadUrl("javascript: Luaming.setAccountInfo(" + activity.accountId + ", \"" + activity.accessToken + "\")");
				//view.loadUrl("javascript: Luaming.redirectToMain()");
			}
		}
		else {
			view.loadUrl("javascript: Luaming.setCanGoBack()");
			view.loadUrl("javascript: Luaming.setLuamingBrowser()");
		}
	}
	
	public File updateFile(String dirPath, int latestVersion) {
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
				if (latestVersion == UpdateUtil.checkVersion(updateFiles[i].getParent(), updateFiles[i].getName()))
					realUpdateFile = updateFiles[i];
				else
					updateFiles[i].delete();
			}
		}
		
		return realUpdateFile;
	}
}
