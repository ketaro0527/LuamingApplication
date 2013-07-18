package com.ssm.luaming.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.ssm.luaming.LuamingActivity;
import com.ssm.luaming.LuamingConstant;

public class LuamingUpdateThread extends Thread {
	
	private LuamingActivity activity;
	
	public LuamingUpdateThread(LuamingActivity act) {
		activity = act;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		if (activity.accessToken.length() > 0) {
			switch(LuamingActivity.downloadFor) {
			case LuamingConstant.DOWNLOAD_FOR_INSTALL:
			case LuamingConstant.DOWNLOAD_FOR_REPLACE: {
				if (LuamingUpdateUtil.updateToReplace(LuamingActivity.mainPath + "/" + activity.accessToken + "/" + activity.packageName, activity.apkName, activity.updateName))
					activity.handler.sendEmptyMessageDelayed(LuamingConstant.UPDATE_COMPLETE, 500);
				else
					activity.handler.sendEmptyMessage(LuamingConstant.UPDATE_FAILED);
			}
			break;
			case LuamingConstant.DOWNLOAD_FOR_UPDATE: {
				if (LuamingUpdateUtil.update(LuamingActivity.mainPath + "/" + activity.accessToken + "/" + activity.packageName, activity.apkName, activity.updateName))
					activity.handler.sendEmptyMessageDelayed(LuamingConstant.UPDATE_COMPLETE, 500);
				else
					activity.handler.sendEmptyMessage(LuamingConstant.UPDATE_FAILED);
			}
			break;
			default:
				break;
			}
		}
		else
			activity.handler.sendEmptyMessage(LuamingConstant.UPDATE_FAILED);

		SharedPreferences sp = activity.getSharedPreferences(LuamingConstant.LUAMING_PREF, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.remove(LuamingConstant.LUAMING_UPDATE_ID);
		editor.commit();
	}
	
}
