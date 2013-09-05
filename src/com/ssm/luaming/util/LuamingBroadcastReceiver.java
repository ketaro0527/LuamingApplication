package com.ssm.luaming.util;

import java.io.File;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.ssm.luaming.LuamingActivity;
import com.ssm.luaming.dialog.LuamingDialog;
import com.ssm.luaming.dialog.LuamingOnCancelListener;
import com.ssm.luaming.dialog.LuamingOnDismissListener;

public class LuamingBroadcastReceiver extends BroadcastReceiver {
	private LuamingActivity activity;
	private LuamingDialog luamingDialog = null;
	
	public LuamingBroadcastReceiver(LuamingActivity act) {
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
						
						File update = LuamingUpdateUtil.getUpdateFile(LuamingActivity.mainPath + "/" + activity.accessToken + "/" + activity.packageName, activity.latestVersion);
						if (update != null) {
							activity.updateName = update.getName();
							activity.isUpdating = true;
							activity.updatePackage();//activity.handler.sendEmptyMessageDelayed(LuamingConstant.UPDATE_START, 500);
							downloadManager.remove(activity.downloadId);
							activity.downloadId = -1;
							return;
						}
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
		LuamingDialog dialog = new LuamingDialog(context, LuamingDialog.LUAMING_DIALOG_STYLE_OK_CANCEL);
		dialog.setBackCancelable(false);
		dialog.setOnCancelListener(new LuamingOnCancelListener(LuamingOnCancelListener.LUAMING_CANCEL_TYPE_OFFLINE_MODE, activity));
		dialog.setOnDismissListener(new LuamingOnDismissListener(LuamingOnDismissListener.LUAMING_DISMISS_TYPE_FINISH));
		dialog.show("인터넷에 연결되어 있지 않습니다.\n오프라인 모드로 전환할까요?");
		
		return dialog;
	}

}
