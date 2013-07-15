package com.ssm.luaming;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.webkit.DownloadListener;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

@SuppressLint({ "HandlerLeak", "DefaultLocale", "SetJavaScriptEnabled" })
public class MainActivity extends Activity implements DownloadListener {
	public static final String LUAMING_PREF = "LUAMING_PREF";
	public static final String LUAMING_GAME_PATH = "LUAMING_GAME_PATH";
	public static final String LUAMING_UPDATE_ID = "LUAMING_UPDATE_ID";
	public static final String LUAMING_DOWNLOAD_FOR = "LUAMING_DOWNLOAD_FOR";
	public static final String LUAMING_ACCOUNT_ID = "LUAMING_ACCOUNT_ID";
	public static final String LUAMING_ACCESS_TOKEN = "LUAMING_ACCESS_TOKEN";
	public static final String LUAMING_MOBILE_URL = "http://210.118.74.81/LuamingMobile/";
	private static final int UPDATE_START = 0;
	private static final int UPDATE_COMPLETE = 1;
	private static final int UPDATE_FAILED = 2;
	public static final int DOWNLOAD_FOR_INSTALL = 0;
	public static final int DOWNLOAD_FOR_REPLACE = 1;
	public static final int DOWNLOAD_FOR_UPDATE = 2;
	public static int downloadFor = DOWNLOAD_FOR_INSTALL;

	public static final String mainPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Luaming";
	private String apkName = "HelloLuaming.apk";
	private String updateName = "HelloLuaming_Update.apk";
	private LuamingWebView webview = null;
	private ImageView splash = null;
	private boolean isUpdating = false;

	public boolean isFirstTime = true;
	public boolean hasAccountInfo = false;
	public int accountId = -1;
	public String accessToken = "";
	public String packageName = "";
	private String startURL = LUAMING_MOBILE_URL;

	public boolean canGoBack = false;

	private ProgressDialog pd;

	private BroadcastReceiver completeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!isUpdating)				
				MainActivity.this.handler.sendEmptyMessage(UPDATE_START);
		}
	};

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message m) {
			if (pd != null) {
				pd.dismiss();
				pd = null;
			}

			if (m.what == UPDATE_START) {
				isUpdating = true;
				MainActivity.this.updatePackage();
			}
			else if (m.what == UPDATE_COMPLETE) {
				isUpdating = false;
				MainActivity.this.startGame();
			}
			else if (m.what == UPDATE_FAILED) {
				isUpdating = false;
				Toast.makeText(MainActivity.this, "Update Failed", Toast.LENGTH_SHORT).show();
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		
		File LuamingDir = new File(mainPath);
		if (!LuamingDir.exists())
			LuamingDir.mkdir();

		SharedPreferences sp = getSharedPreferences(LUAMING_PREF, MODE_PRIVATE);
		accountId = sp.getInt(LUAMING_ACCOUNT_ID, -1);
		accessToken = sp.getString(LUAMING_ACCESS_TOKEN, "");
		if (accountId != -1 && accessToken.length() > 0) {
			hasAccountInfo = true;
			File userDir = new File(mainPath + "/" + accessToken);
			if (!userDir.exists())
				userDir.mkdir();
		}
		Log.d("Luaming", "onCreate: " + mainPath);

		setContentView(R.layout.main_layout);

		webview = (LuamingWebView) findViewById(R.id.main_webview);
		webview.init(this);
		webview.loadUrl(startURL);
		webview.setDownloadListener(this);
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
	public void onResume() {
		super.onResume();
		// 앱이 실행되면 리시버 등록
		IntentFilter completeFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		registerReceiver(completeReceiver, completeFilter);

		SharedPreferences sp = getSharedPreferences(LUAMING_PREF, MODE_PRIVATE);
		long id = sp.getLong(LUAMING_UPDATE_ID, -1);
		downloadFor = sp.getInt(LUAMING_DOWNLOAD_FOR, DOWNLOAD_FOR_INSTALL);

		if (id != -1) {
			DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
			DownloadManager.Query query = new Query();
			query.setFilterById(id);
			query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);
			Cursor cursor = downloadManager.query(query);

			String msg = "";
			if (downloadFor == DOWNLOAD_FOR_INSTALL)
				msg = "다운로드가 완료되었습니다.\n실행하시겠습니까?";
			else
				msg = "다운로드가 완료되었습니다.\n업데이트를 진행하시겠습니까?";

			if (cursor.getCount() > 0) {
				LuamingDialog dialog = new LuamingDialog(this, LuamingDialog.LUAMING_DIALOG_STYLE_OK_CANCEL);
				dialog.setOnCancelListener(new OnCancelListener() {				
					@Override
					public void onCancel(DialogInterface dialog) {
						// TODO Auto-generated method stub
						MainActivity.this.handler.sendEmptyMessage(UPDATE_START);
					}
				});
				dialog.show(msg);
			}

			query = new Query();
			query.setFilterById(id);
			query.setFilterByStatus(DownloadManager.STATUS_RUNNING);
			cursor = downloadManager.query(query);
			if (cursor.getCount() > 0) {
				pd = ProgressDialog.show(MainActivity.this, "Downloading", "Please wait...", true);
				pd.setCancelable(false);
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
		unregisterReceiver(completeReceiver);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (webview != null && !canGoBack) {
			LuamingDialog dialog = new LuamingDialog(this, LuamingDialog.LUAMING_DIALOG_STYLE_OK_CANCEL);
			dialog.setOnCancelListener(new OnCancelListener() {				
				@Override
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
					MainActivity.this.finish();
				}
			});
			dialog.show("종료하시겠습니까?");
		}
		else {
			webview.loadUrl("javascript: Luaming.backEvent();");
		}
	}

	public void updatePackage() {
		pd = ProgressDialog.show(MainActivity.this, "Update", "Please wait...", true);
		pd.setCancelable(false);

		Thread thread = new Thread() {
			@Override
			public void run() {
				if (accessToken.length() > 0) {
					switch(downloadFor) {
					case DOWNLOAD_FOR_INSTALL: 
					case DOWNLOAD_FOR_REPLACE: {
						Log.d("Luaming", "UpdatePackage: " + MainActivity.mainPath);
						if (UpdateUtil.updateToReplace(MainActivity.mainPath + "/" + accessToken + "/" + packageName, MainActivity.this.apkName, MainActivity.this.updateName))
							MainActivity.this.handler.sendEmptyMessage(UPDATE_COMPLETE);
						else
							MainActivity.this.handler.sendEmptyMessage(UPDATE_FAILED);
					}
					break;
					case DOWNLOAD_FOR_UPDATE: {
						if (UpdateUtil.update(MainActivity.mainPath + "/" + accessToken + "/" + packageName, MainActivity.this.apkName, MainActivity.this.updateName))
							MainActivity.this.handler.sendEmptyMessage(UPDATE_COMPLETE);
						else
							MainActivity.this.handler.sendEmptyMessage(UPDATE_FAILED);
					}
					default:
						break;
					}
				}
				else
					MainActivity.this.handler.sendEmptyMessage(UPDATE_FAILED);

				SharedPreferences sp = MainActivity.this.getSharedPreferences(LUAMING_PREF, MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();
				editor.remove(LUAMING_UPDATE_ID);
				editor.commit();
			}
		};
		thread.start();
	}

	public void startGame() {
		String orientation = UpdateUtil.checkOrientation(mainPath + "/" + accessToken + "/" + packageName, apkName);

		// 이동
		Class<?> cls = null;
		if ("landscape".equals(orientation.toLowerCase()))
			cls = Luaming.class;
		else
			cls = LuamingPortrait.class;

		Intent intent = new Intent(this, cls);
		intent.putExtra(LUAMING_GAME_PATH, mainPath + "/" + accessToken + "/" + packageName + "/" + apkName);
		startActivity(intent);
	}

	@Override
	public void onDownloadStart(String url, String userAgent,
			String contentDisposition, String mimetype, long contentLength) {
		// TODO Auto-generated method stub
		pd = ProgressDialog.show(MainActivity.this, "Downloading", "Please wait...", true);
		pd.setCancelable(false);

		MimeTypeMap mtm = MimeTypeMap.getSingleton();
		DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

		Uri downloadUri = Uri.parse(url);
		// 파일 이름을 추출한다. contentDisposition에 filename이 있으면 그걸 쓰고 없으면 URL의 마지막 파일명을 사용한다.

		String fileName = downloadUri.getLastPathSegment();
		int pos = 0;
		if ((pos = contentDisposition.toLowerCase().lastIndexOf("filename=")) >= 0) {
			fileName = contentDisposition.substring(pos + 9);
			pos = fileName.lastIndexOf(";");

			if (pos > 0) {
				fileName = fileName.substring(0, pos - 1);
			}
		}

		// MIME Type을 확장자를 통해 예측한다.
		String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();
		String mimeType = mtm.getMimeTypeFromExtension(fileExtension);

		// Download 디렉토리에 저장하도록 요청을 작성
		Request request = new DownloadManager.Request(downloadUri);
		request.setTitle(fileName);
		request.setDescription(url);
		request.setMimeType(mimeType);
		request.setDestinationInExternalPublicDir( "/Luaming/" + accessToken + "/" + packageName, updateName);
		//Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS).mkdirs();

		// 다운로드 매니저에 요청 등록
		long id = downloadManager.enqueue(request);

		SharedPreferences sp = getSharedPreferences(MainActivity.LUAMING_PREF, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putLong(MainActivity.LUAMING_UPDATE_ID, id);
		editor.commit();
	}

	public void setProjectName(String projectName, String packageName) {
		apkName = projectName + ".apk";
		updateName = projectName + "_Update.apk";
		this.packageName = packageName; 
	}
}
