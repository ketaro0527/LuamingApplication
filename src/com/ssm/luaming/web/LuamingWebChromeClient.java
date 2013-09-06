package com.ssm.luaming.web;

import android.annotation.SuppressLint;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.ssm.luaming.LuamingActivity;
import com.ssm.luaming.dialog.LuamingDialog;
import com.ssm.luaming.dialog.LuamingOnDismissListener;

@SuppressLint("DefaultLocale")
public class LuamingWebChromeClient extends WebChromeClient {
	public LuamingActivity activity;
	
	public LuamingWebChromeClient(LuamingActivity act) {
		activity = act;
	}
	
	@Override
	public void onReceivedTitle(WebView view, String title) {
		super.onReceivedTitle(view, title);
		if (title.toLowerCase().contains("found") || title.toLowerCase().contains("forbidden")) {
			view.setVisibility(View.GONE);
			LuamingDialog dialog = new LuamingDialog(activity, LuamingDialog.LUAMING_DIALOG_STYLE_SINGLE);
			dialog.setBackCancelable(false);
			dialog.setOnDismissListener(new LuamingOnDismissListener(LuamingOnDismissListener.LUAMING_DISMISS_TYPE_FINISH));
			dialog.show(title + "\nLuaming을 종료합니다.");
		}
	}
	
	@Override
	public boolean onJsAlert(WebView view, String url,
			String message, JsResult result) {
		final JsResult finalRes = result;

		//AlertDialog 생성
		LuamingDialog dialog = new LuamingDialog(view.getContext(), LuamingDialog.LUAMING_DIALOG_STYLE_SINGLE);
		dialog.setBackCancelable(false);
		dialog.setOnDismissListener(new LuamingOnDismissListener(LuamingOnDismissListener.LUAMING_DISMISS_TYPE_JSRESULT, finalRes));
		dialog.show(message);
		
		return true;
	}
}
