package com.ssm.luaming;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class LuamingBroadcastReceiver extends BroadcastReceiver {
	private MainActivity activity;
	private LuamingDialog luamingDialog = null;
	
	public LuamingBroadcastReceiver(MainActivity act) {
		activity = act;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
			if (!activity.isUpdating && activity.downloadId != -1) {
				DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
				DownloadManager.Query query = new Query();
				query.setFilterById(activity.downloadId);
				Cursor cursor = downloadManager.query(query);
				if (cursor.moveToFirst()) {
					int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
					if (status == DownloadManager.STATUS_SUCCESSFUL) {
						if (UpdateUtil.getFileSize(MainActivity.mainPath + "/" + activity.accessToken + "/" + activity.packageName, activity.updateName, true) == 0) {
							activity.updateName = activity.updateName.split("\\.")[0] + "-1.apk";

							if (UpdateUtil.getFileSize(MainActivity.mainPath + "/" + activity.accessToken + "/" + activity.packageName, activity.updateName, false) != 0) {
								activity.handler.sendEmptyMessage(MainActivity.UPDATE_START);
							}
						}

						else
							activity.handler.sendEmptyMessage(MainActivity.UPDATE_START);
					}
				}
			}
		}
		else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
			ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mobileNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);				
			NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);				
			NetworkInfo wimaxNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);
			
			NetworkInfo.State currentMobileNetworkState = null;
			NetworkInfo.State currentWifiNetworkState = null;
			NetworkInfo.State currentWimaxNetworkState = null;
			
			if (mobileNetworkInfo != null)
				currentMobileNetworkState = mobileNetworkInfo.getState();
			if (wifiNetworkInfo != null)
				currentWifiNetworkState = wifiNetworkInfo.getState();
			if (wimaxNetworkInfo != null)
				currentWimaxNetworkState = wimaxNetworkInfo.getState();
			
			if (!NetworkInfo.State.CONNECTED.equals(currentMobileNetworkState) 
					&& !NetworkInfo.State.CONNECTED.equals(currentWifiNetworkState)
					&& !NetworkInfo.State.CONNECTED.equals(currentWimaxNetworkState)) {
				if (luamingDialog == null)
					luamingDialog = showError(context);
			}
			else {
				if (luamingDialog != null) {
					luamingDialog.setOnDismissListener(null);
					luamingDialog.dismiss();
					luamingDialog = null;
				}
			}
		}
	}
	
	private LuamingDialog showError(Context context) {
		activity.initWithError = true;
		LuamingDialog dialog = new LuamingDialog(context, LuamingDialog.LUAMING_DIALOG_STYLE_SINGLE);
		dialog.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		});
		dialog.show("인터넷 연결을 확인하세요.\nLuaming을 종료합니다.");
		
		return dialog;
	}

}
