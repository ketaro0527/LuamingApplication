package com.ssm.luaming.web;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ssm.luaming.LuamingActivity;
import com.ssm.luaming.LuamingConstant;
import com.ssm.luaming.dialog.LuamingDialog;
import com.ssm.luaming.dialog.LuamingOnCancelListener;
import com.ssm.luaming.dialog.LuamingOnDismissListener;
import com.ssm.luaming.util.LuamingUpdateUtil;

@SuppressLint("HandlerLeak")
public class LuamingWebViewClient extends WebViewClient {
	public LuamingActivity activity;
	public WebView view;
	public Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case 0:
				view.loadUrl("javascript: Luaming.initAndRedirectToHome()");
				break;
			case 1:
				view.loadUrl("javascript: Luaming.initAndRedirectToMain(" + activity.accountId + ", \"" + activity.accessToken + "\")");
				break;
			case 2:
				view.loadUrl("javascript: Luaming.setCanGoBackAndLuamingBrowser()");
				break;
			default:
				break;
			}
		};
	};

	public LuamingWebViewClient(LuamingActivity act) {
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

					SharedPreferences sp = activity.getSharedPreferences(LuamingConstant.LUAMING_PREF, Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = sp.edit();
					editor.putInt(LuamingConstant.LUAMING_ACCOUNT_ID, account.getInt("account_id"));
					editor.putString(LuamingConstant.LUAMING_ACCESS_TOKEN, account.getString("access_token"));
					editor.commit();

					File mainDir = new File(LuamingActivity.mainPath);
					if (!mainDir.exists())
						mainDir.mkdir();
					File dir = new File(LuamingActivity.mainPath + "/" + account.getString("access_token"));
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
					SharedPreferences sp = activity.getSharedPreferences(LuamingConstant.LUAMING_PREF, Context.MODE_PRIVATE);
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
					activity.gameId = gameId;
					activity.latestVersion = projectInfo.getInt("latest_version_code");
					
					File mainDir = new File(LuamingActivity.mainPath);
					if (!mainDir.exists())
						mainDir.mkdir();
					File dir = new File(LuamingActivity.mainPath + "/" + activity.accessToken);
					if (!dir.exists())
						dir.mkdir();
					
					int currentVersion = LuamingUpdateUtil.checkVersion(LuamingActivity.mainPath + "/" + activity.accessToken + "/" + packageName, projectName + ".apk");

					activity.setProjectName(projectName, packageName);
					
					// 이미 설치되어 있는 경우
					if (currentVersion > 0) {
						// 현재 버전이 최신버전과 동일한 경우
						if (activity.latestVersion == currentVersion) {
							activity.startGame();
						}
						// 현재 버전이 구버전인 경우
						else {
							// 이전에 다운받아놓은 업데이트 있는지 확인
							File update = LuamingUpdateUtil.getUpdateFile(LuamingActivity.mainPath + "/" + activity.accessToken + "/" + packageName, activity.latestVersion);
							if (update != null) {
								activity.updateName = update.getName();
								activity.isUpdating = true;
								activity.handler.sendEmptyMessage(LuamingConstant.UPDATE_START);
								return true;
							}
							
							String latestVersionName = projectInfo.getString("latest_version_name");
							String currentVersionName = LuamingUpdateUtil.checkVersionName(LuamingActivity.mainPath + "/" + activity.accessToken + "/" + packageName, projectName + ".apk");
							// 메이저 버전이 같은 경우 => 업데이트
							if (currentVersionName.startsWith(latestVersionName.substring(0, 1))) {
								LuamingActivity.downloadFor = LuamingConstant.DOWNLOAD_FOR_UPDATE;
								// 다운로드 시작
								view.loadUrl("javascript: Luaming.updateDownload(" + gameId + ", " + currentVersion + ")");
							}
							// 메이저 버전이 다른 경우 => 새로운 Full 소스 다운로드
							else {
								LuamingActivity.downloadFor = LuamingConstant.DOWNLOAD_FOR_REPLACE;
								// 다운로드 시작
								view.loadUrl("javascript: Luaming.fullDownload(" + gameId + ")");
							}
						}
					}
					// 설치되어 있지 않은 경우 => Full 소스 다운로드
					else {
						// 이전에 받은 Full 소스 있는지 먼저 확인
						File update = LuamingUpdateUtil.getUpdateFile(LuamingActivity.mainPath + "/" + activity.accessToken + "/" + packageName, activity.latestVersion);
						if (update != null) {
							activity.updateName = update.getName();
							activity.isUpdating = true;
							LuamingActivity.downloadFor = LuamingConstant.DOWNLOAD_FOR_REPLACE;
							activity.handler.sendEmptyMessage(LuamingConstant.UPDATE_START);
							return true;
						}
						
						// 폴더부터 생성
						File gameDir = new File(LuamingActivity.mainPath + "/" + activity.accessToken + "/" + packageName);
						if (!gameDir.exists())
							gameDir.mkdir();
						LuamingActivity.downloadFor = LuamingConstant.DOWNLOAD_FOR_INSTALL;
						
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
		this.view = view;
		if (activity.isFirstTime) {
			activity.isFirstTime = false;
			if (!activity.hasAccountInfo) {
				handler.sendEmptyMessageDelayed(0, 200);
			}
			else {
				handler.sendEmptyMessageDelayed(1, 200);
			}
		}
		else {
			handler.sendEmptyMessageDelayed(2, 200);
		}
	}
	
	@Override
	public void onReceivedError(WebView view, int errorCode,
			String description, String failingUrl) {
		// TODO Auto-generated method stub
		super.onReceivedError(view, errorCode, description, failingUrl);
		activity.initWithError = true;
		view.setVisibility(View.GONE);
		LuamingDialog dialog = new LuamingDialog(activity, LuamingDialog.LUAMING_DIALOG_STYLE_OK_CANCEL);
		dialog.setBackCancelable(false);
		dialog.setOnCancelListener(new LuamingOnCancelListener(LuamingOnCancelListener.LUAMING_CANCEL_TYPE_OFFLINE_MODE, activity));
		dialog.setOnDismissListener(new LuamingOnDismissListener(LuamingOnDismissListener.LUAMING_DISMISS_TYPE_FINISH));
		dialog.show("인터넷에 연결되어 있지 않습니다.\n오프라인 모드로 전환할까요?");
	}
}
