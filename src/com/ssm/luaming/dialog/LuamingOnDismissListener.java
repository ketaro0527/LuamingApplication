package com.ssm.luaming.dialog;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.util.Log;
import android.webkit.JsResult;

public class LuamingOnDismissListener implements OnDismissListener{
	
	public static final int LUAMING_DISMISS_TYPE_FINISH = 0;
	public static final int LUAMING_DISMISS_TYPE_JSRESULT = 1;
	
	private int type = -1;
	private JsResult finalRes = null;
	
	public LuamingOnDismissListener(int type) {
		this.type = type;
	}
	
	public LuamingOnDismissListener(int type, JsResult finalRes) {
		this.type = type;
		this.finalRes = finalRes;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		Log.d("Luaming", "OnDismiss");
		// TODO Auto-generated method stub
		LuamingDialog luamingDialog = (LuamingDialog)dialog;
		switch(type) {
		case LUAMING_DISMISS_TYPE_FINISH: {
			if (luamingDialog.canClose())
				android.os.Process.killProcess(android.os.Process.myPid());
		}
			break;
		case LUAMING_DISMISS_TYPE_JSRESULT: {
			if (luamingDialog.canClose() && finalRes != null)
				finalRes.confirm();
		}
		break;
		default:
			break;
		}
	}

}
