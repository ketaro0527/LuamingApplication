package com.ssm.luaming.dialog;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.util.Log;

import com.ssm.luaming.LuamingConstant;
import com.ssm.luaming.LuamingActivity;

public class LuamingOnCancelListener implements OnCancelListener{
	
	public static final int LUAMING_CANCEL_TYPE_FINISH = 0;
	public static final int LUAMING_CANCEL_TYPE_UPDATE_START = 1;
	
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
		Log.d("Luaming", "OnCancel");
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
				activity.handler.sendEmptyMessage(LuamingConstant.UPDATE_START);
		}
			break;
		default:
			break;
		}
	}

}
