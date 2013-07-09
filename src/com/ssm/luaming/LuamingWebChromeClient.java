package com.ssm.luaming;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class LuamingWebChromeClient extends WebChromeClient {
	public MainActivity activity;
	
	public LuamingWebChromeClient(MainActivity act) {
		activity = act;
	}
	
	@Override
	public boolean onJsAlert(WebView view, String url,
			String message, JsResult result) {
		// TODO Auto-generated method stub
		final JsResult finalRes = result;

		//AlertDialog 생성
		LuamingDialog dialog = new LuamingDialog(view.getContext(), LuamingDialog.LUAMING_DIALOG_STYLE_SINGLE);
		dialog.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				finalRes.confirm();
			}
		});
		dialog.show(message);
		
		return true;
	}
}
