package com.ssm.luaming.dialog;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.webkit.WebView;

import com.ssm.luaming.LuamingActivity;
import com.ssm.luaming.LuamingOfflineActivity;

public class LuamingOnCancelListener implements OnCancelListener{
	
	public static final int LUAMING_CANCEL_TYPE_FINISH = 0;
	public static final int LUAMING_CANCEL_TYPE_UPDATE_START = 1;
	public static final int LUAMING_CANCEL_TYPE_OFFLINE_MODE = 2;
	public static final int LUAMING_CANCEL_TYPE_DOWNLOAD = 3;
	
	private int type = -1;
	private LuamingActivity activity = null;
	private WebView webview = null;
	private String path = "";
	
	public LuamingOnCancelListener(int type) {
		this.type = type;
	}
	
	public LuamingOnCancelListener(int type, LuamingActivity act) {
		this.type = type;
		activity = act;
	}
	
	public LuamingOnCancelListener(int type, WebView webview, String path) {
		this.type = type;
		this.webview = webview;
		this.path = path;
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		LuamingDialog luamingDialog = (LuamingDialog)dialog;
		switch(type) {
		case LUAMING_CANCEL_TYPE_FINISH: {
			if (luamingDialog.canClose())
				android.os.Process.killProcess(android.os.Process.myPid());
		}
			break;
		case LUAMING_CANCEL_TYPE_UPDATE_START: {
			if (luamingDialog.canClose() && activity != null)
				activity.updatePackage();
		}
			break;
		case LUAMING_CANCEL_TYPE_OFFLINE_MODE: {
			if (luamingDialog.canClose() && activity != null){
				Intent intent = new Intent(activity, LuamingOfflineActivity.class);
				activity.startActivity(intent);
				activity.finish();
			}
		}
			break;
		case LUAMING_CANCEL_TYPE_DOWNLOAD: {
			if (luamingDialog.canClose() && webview != null){
				webview.loadUrl(path);
			}
		}
			break;
		default:
			break;
		}
	}

}
