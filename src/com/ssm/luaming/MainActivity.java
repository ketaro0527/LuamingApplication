package com.ssm.luaming;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.view.Menu;
import android.view.MenuItem;
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
	public static final String LUAMING_ACCOUNT_ID = "LUAMING_ACCOUNT_ID";
	public static final String LUAMING_ACCESS_TOKEN = "LUAMING_ACCESS_TOKEN";
	public static final String LUAMING_MOBILE_URL = "http://210.118.74.81/LuamingMobile/";
	private static final int UPDATE_START = 0;
	private static final int UPDATE_COMPLETE = 1;
	private static final int UPDATE_FAILED = 2;

	private String mainPath;
	private String apkName = "HelloDownload.apk";
	private String updateName = "Update.apk";
	private LuamingWebView webview = null;
	private ImageView splash = null;
	private boolean isUpdating = false;
	
	public boolean isFirstTime = true;
	public boolean hasAccountInfo = false;
	public int accountId = -1;
	public String accessToken = "";
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
				MainActivity.this.updatePackage();
			}
			else if (m.what == UPDATE_COMPLETE) {
				MainActivity.this.startGame();
			}
			else if (m.what == UPDATE_FAILED) {
				Toast.makeText(MainActivity.this, "Update Failed", Toast.LENGTH_SHORT).show();
			}
		}
	};


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		

		mainPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		mainPath += "/Luaming";
		File LuamingDir = new File(mainPath);
		if (!LuamingDir.exists())
			LuamingDir.mkdir();

		SharedPreferences sp = getSharedPreferences(LUAMING_PREF, MODE_PRIVATE);
		accountId = sp.getInt(LUAMING_ACCOUNT_ID, -1);
		accessToken = sp.getString(LUAMING_ACCESS_TOKEN, "");
		if (accountId != -1 && accessToken.length() > 0) {
			hasAccountInfo = true;
		}
		
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

		if (id != -1) {
			DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
			DownloadManager.Query query = new Query();
			query.setFilterById(id);
			query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);
			Cursor cursor = downloadManager.query(query);

			if (cursor.getCount() > 0) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("다운로드가 완료되었습니다.\n업데이트를 진행하시겠습니까?")
				.setCancelable(false)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						MainActivity.this.handler.sendEmptyMessage(UPDATE_START);
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});

				AlertDialog alert = builder.create();
				alert.show();

				return;
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(0, 0, 0, "새로고침");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case 0:
			if (webview != null) {
				webview.reload();
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void updatePackage() {
		isUpdating = true;

		pd = ProgressDialog.show(MainActivity.this, "Update", "Please wait...", true);
		pd.setCancelable(false);

		Thread thread = new Thread() {
			@Override
			public void run() {
				if (UpdateUtil.update(MainActivity.this.mainPath, MainActivity.this.apkName, MainActivity.this.updateName))
					MainActivity.this.handler.sendEmptyMessage(UPDATE_COMPLETE);
				else
					MainActivity.this.handler.sendEmptyMessage(UPDATE_FAILED);

				MainActivity.this.isUpdating = false;

				SharedPreferences sp = MainActivity.this.getSharedPreferences(LUAMING_PREF, MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();
				editor.remove(LUAMING_UPDATE_ID);
				editor.commit();
			}
		};
		thread.start();
	}

	public void startGame() {
		Intent intent = new Intent(this, Luaming.class);
		intent.putExtra(LUAMING_GAME_PATH, mainPath + "/" + apkName);
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
		request.setDestinationInExternalPublicDir( "/Luaming", updateName);
		//Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS).mkdirs();

		// 다운로드 매니저에 요청 등록
		long id = downloadManager.enqueue(request);

		SharedPreferences sp = getSharedPreferences(MainActivity.LUAMING_PREF, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putLong(MainActivity.LUAMING_UPDATE_ID, id);
		editor.commit();
	}
}
