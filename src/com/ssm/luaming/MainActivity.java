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
import android.view.View.OnClickListener;
import android.webkit.DownloadListener;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

@SuppressLint({ "HandlerLeak", "DefaultLocale", "SetJavaScriptEnabled" })
public class MainActivity extends Activity implements OnClickListener {
	public static final String LUAMING_PREF = "LUAMING_PREF";
	public static final String LUAMING_GAME_PATH = "LUAMING_GAME_PATH";
	public static final String LUAMING_UPDATE_ID = "LUAMING_UPDATE_ID";
	private static final int UPDATE_START = 0;
	private static final int UPDATE_COMPLETE = 1;
	private static final int UPDATE_FAILED = 2;
	private Button btn_update;
	private Button btn_execute;
	private String mainPath;
	private String apkName = "HelloDownload.apk";
	private String updateName = "Update.apk";
	private LinearLayout mainLayout;
	private boolean isUpdating = false;

	private ProgressDialog pd;

	private BroadcastReceiver completeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("Luaming", intent.getAction());
			Log.d("Luaming", "Download Complete!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
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
		mainLayout = (LinearLayout)getLayoutInflater().inflate(R.layout.main_layout, null);
		setContentView(mainLayout);		

		mainPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		mainPath += "/Luaming";
		File LuamingDir = new File(mainPath);
		if (!LuamingDir.exists())
			LuamingDir.mkdir();

		btn_update = (Button)findViewById(R.id.btn_update);
		btn_update.setOnClickListener(this);

		btn_execute = (Button)findViewById(R.id.btn_execute);
		btn_execute.setOnClickListener(this);
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
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == btn_update) {
			WebView webview = new WebView(this);
			webview.loadUrl("https://www.dropbox.com/s/wflvvh5gnw0teib/Update.apk");
			webview.getSettings().setJavaScriptEnabled(true);
			webview.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					// TODO Auto-generated method stub
					view.loadUrl(url);
					return true;
				}
			});
			webview.setDownloadListener(new DownloadListener() {

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

					SharedPreferences sp = MainActivity.this.getSharedPreferences(LUAMING_PREF, MODE_PRIVATE);
					SharedPreferences.Editor editor = sp.edit();
					editor.putLong(LUAMING_UPDATE_ID, id);
					editor.commit();
				}
			});
			setContentView(webview);

			/*
			pd = ProgressDialog.show(MainActivity.this, "Update", "Please wait...", true);
			pd.setCancelable(false);

			Thread thread = new Thread() {
				@Override
				public void run() {
					if (UpdateUtil.update(MainActivity.this.mainPath, MainActivity.this.apkName, MainActivity.this.updateName))
						MainActivity.this.handler.sendEmptyMessage(UPDATE_COMPLETE);
					else
						MainActivity.this.handler.sendEmptyMessage(UPDATE_FAILED);
				}
			};
			thread.start();
			 */
		}
		else if (v == btn_execute) {
			startGame();
		}
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

}
