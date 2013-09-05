package com.ssm.luaming;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

import com.ssm.luaming.dialog.LuamingDialog;
import com.ssm.luaming.dialog.LuamingOnCancelListener;
import com.ssm.luaming.dialog.LuamingOnDismissListener;
import com.ssm.luaming.dialog.LuamingProgressDialog;
import com.ssm.luaming.game.Luaming;
import com.ssm.luaming.game.LuamingPortrait;
import com.ssm.luaming.util.LuamingBroadcastReceiver;
import com.ssm.luaming.util.LuamingDownloadTask;
import com.ssm.luaming.util.LuamingUpdateTask;
import com.ssm.luaming.util.LuamingUpdateUtil;
import com.ssm.luaming.web.LuamingWebView;

@SuppressLint({ "HandlerLeak", "DefaultLocale", "SetJavaScriptEnabled" })
public class LuamingActivity extends Activity {
	public static int downloadFor = LuamingConstant.DOWNLOAD_FOR_INSTALL;

	public static String mainPath;
	public String apkName = "";
	public String updateName = "";
	
	private LuamingWebView webview = null;
	private ImageView splash = null;
	
	public boolean isUpdating = false;
	public long downloadId = -1;

	public boolean isFirstTime = true;
	public boolean hasAccountInfo = false;
	public int accountId = -1;
	public int gameId = 0;
	public int latestVersion = 0;
	public String accessToken = "";
	public String packageName = "";
	private String startURL = LuamingConstant.LUAMING_MOBILE_URL;

	public boolean canGoBack = false;

	public boolean initWithError = false;

	public LuamingProgressDialog pd = null;

	private LuamingBroadcastReceiver broadcastReceiver = null;
/*
	public Handler handler = new Handler() {
		@Override
		public void handleMessage(Message m) {
			if (pd != null) {
				pd.dismiss();
				pd = null;
			}

			if (m.what == LuamingConstant.UPDATE_START) {
				LuamingActivity.this.updatePackage();
			}
			else if (m.what == LuamingConstant.UPDATE_COMPLETE) {
				isUpdating = false;
				LuamingActivity.this.startGame();
			}
			else if (m.what == LuamingConstant.UPDATE_FAILED) {
				isUpdating = false;
				Toast.makeText(LuamingActivity.this, "Update Failed", Toast.LENGTH_SHORT).show();
			}
		}
	};
*/
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// 외장 메모리 확인
		if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			LuamingDialog dialog = new LuamingDialog(this, LuamingDialog.LUAMING_DIALOG_STYLE_SINGLE);
			dialog.setBackCancelable(false);
			dialog.setOnDismissListener(new LuamingOnDismissListener(LuamingOnDismissListener.LUAMING_DISMISS_TYPE_FINISH));
			dialog.show("SD카드가 없습니다.\nLuaming을 종료합니다.");
		}
		
		mainPath = getExternalFilesDir(null).getAbsolutePath() + "/.Luaming";

		File LuamingDir = new File(mainPath);
		if (!LuamingDir.exists())
			LuamingDir.mkdir();

		SharedPreferences sp = getSharedPreferences(LuamingConstant.LUAMING_PREF, MODE_PRIVATE);
		accountId = sp.getInt(LuamingConstant.LUAMING_ACCOUNT_ID, -1);
		accessToken = sp.getString(LuamingConstant.LUAMING_ACCESS_TOKEN, "");
		if (accountId != -1 && accessToken.length() > 0) {
			hasAccountInfo = true;
			File userDir = new File(mainPath + "/" + accessToken);
			if (!userDir.exists())
				userDir.mkdir();
		}

		setContentView(R.layout.main_layout);

		webview = (LuamingWebView) findViewById(R.id.main_webview);
		webview.init(this);
		webview.loadUrl(startURL);
		webview.setVisibility(View.INVISIBLE);

		splash = (ImageView) findViewById(R.id.splash);
		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setAnimationListener(new AnimationListener() {
			public void onAnimationEnd(Animation arg) {
				Handler handle = new Handler();
				handle.postDelayed(new Runnable() {
					@Override
					public void run() {
						splash.setVisibility(View.GONE);
						if (initWithError == false)
							webview.setVisibility(View.VISIBLE);
					}
				}, 500);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

			}
		});
		animation.setDuration(2000);
		splash.setAnimation(animation);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(0, 0, 0, "오프라인 모드");
		menu.add(0, 1, 0, "종료하기");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()) {
		case 0: {
			Intent intent = new Intent(this, LuamingOfflineActivity.class);
			startActivity(intent);
			finish();
		}
			break;
		case 1: {
			android.os.Process.killProcess(android.os.Process.myPid());
		}
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume() {
		super.onResume();
		// 앱이 실행되면 리시버 등록
		if (broadcastReceiver == null)
			broadcastReceiver = new LuamingBroadcastReceiver(this);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		registerReceiver(broadcastReceiver, filter);

		if (downloadId != -1) {
			DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
			DownloadManager.Query query = new Query();
			query.setFilterById(downloadId);
			query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);
			Cursor cursor = downloadManager.query(query);

			String msg = "";
			if (downloadFor == LuamingConstant.DOWNLOAD_FOR_INSTALL)
				msg = "다운로드가 완료되었습니다.\n실행하시겠습니까?";
			else
				msg = "다운로드가 완료되었습니다.\n업데이트를 진행하시겠습니까?";

			if (cursor.getCount() > 0) {
				LuamingDialog dialog = new LuamingDialog(this, LuamingDialog.LUAMING_DIALOG_STYLE_OK_CANCEL);
				dialog.setBackCancelable(false);
				dialog.setOnCancelListener(new LuamingOnCancelListener(LuamingOnCancelListener.LUAMING_CANCEL_TYPE_UPDATE_START, this));
				dialog.show(msg);
				downloadManager.remove(downloadId);
				downloadId = -1;
				return;
			}

			query = new Query();
			query.setFilterById(downloadId);
			query.setFilterByStatus(DownloadManager.STATUS_RUNNING);
			cursor = downloadManager.query(query);
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				String title = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE));
				cursor.close();
				
				LuamingDownloadTask downTask = new LuamingDownloadTask(this);
				
				pd = new LuamingProgressDialog(this);
				pd.setAsyncTask(downTask);
				pd.show("Downloading...", title);
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		if (pd != null) {
			pd.dismiss();
			pd = null;
		}

		// 앱이 중단 되면 리시버 등록 해제
		unregisterReceiver(broadcastReceiver);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (webview != null && !canGoBack) {
			LuamingDialog dialog = new LuamingDialog(this, LuamingDialog.LUAMING_DIALOG_STYLE_OK_CANCEL);
			dialog.setOnCancelListener(new LuamingOnCancelListener(LuamingOnCancelListener.LUAMING_CANCEL_TYPE_FINISH));
			dialog.show("종료하시겠습니까?");
		}
		else {
			webview.loadUrl("javascript: Luaming.backEvent();");
		}
	}

	public void updatePackage() {
		if (pd != null && pd.isShowing()) {
			pd.dismiss();
			pd = null;
			LuamingProgressDialog.isDialogShowing = false;
		}
		
		LuamingUpdateTask updateTask = new LuamingUpdateTask(this);
		
		if (downloadFor == LuamingConstant.DOWNLOAD_FOR_UPDATE) {
			pd = new LuamingProgressDialog(this);
			pd.setAsyncTask(updateTask);
			pd.show("Updating...", "");
		}
		else
			updateTask.execute();
		//LuamingUpdateThread thread = new LuamingUpdateThread(this);
		//thread.start();
	}

	public void startGame() {
		webview.loadUrl("javascript: Luaming.playGame(" + gameId + ")");

		String orientation = LuamingUpdateUtil.checkOrientation(mainPath + "/" + accessToken + "/" + packageName, apkName);

		// 이동
		Class<?> cls = null;
		if ("landscape".equals(orientation.toLowerCase()))
			cls = Luaming.class;
		else
			cls = LuamingPortrait.class;

		Intent intent = new Intent(this, cls);
		intent.putExtra(LuamingConstant.LUAMING_GAME_PATH, mainPath + "/" + accessToken + "/" + packageName + "/" + apkName);
		startActivity(intent);
		downloadId = -1;
	}

	public void setProjectName(String projectName, String packageName) {
		apkName = projectName + ".apk";
		updateName = projectName + "_Update.apk";
		this.packageName = packageName; 
	}
}
