package com.ssm.luaming.dialog;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;

import com.ssm.luaming.LuamingActivity;
import com.ssm.luaming.LuamingOfflineActivity;

public class LuamingOnCancelListener implements OnCancelListener{
	
	public static final int LUAMING_CANCEL_TYPE_FINISH = 0;
	public static final int LUAMING_CANCEL_TYPE_UPDATE_START = 1;
	public static final int LUAMING_CANCEL_TYPE_OFFLINE_MODE = 2;
	
	private int type = -1;
	private LuamingActivity activity = null;
	
	public LuamingOnCancelListener(int type) {
		this.type = type;
	}
	
	public LuamingOnCancelListener(int type, LuamingActivity act) {
		this.type = type;
		activity = act;
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		// TODO Auto-generated method stub
		LuamingDialog luamingDialog = (LuamingDialog)dialog;
		switch(type) {
		case LUAMING_CANCEL_TYPE_FINISH: {
			if (luamingDialog.canClose())
				android.os.Process.killProcess(android.os.Process.myPid());
		}
			break;
		case LUAMING_CANCEL_TYPE_UPDATE_START: {
			if (luamingDialog.canClose() && activity != null)
				activity.updatePackage();//activity.handler.sendEmptyMessage(LuamingConstant.UPDATE_START);
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
		default:
			break;
		}
	}

}
