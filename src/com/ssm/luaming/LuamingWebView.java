package com.ssm.luaming;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

@SuppressLint("SetJavaScriptEnabled")
public class LuamingWebView extends WebView {
	
	public MainActivity activity;

	public LuamingWebView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public LuamingWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}	
	
	public void init(MainActivity act) {
		activity = act;
		
		setVerticalScrollBarEnabled(false);
		setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
		setScrollbarFadingEnabled(true);
		getSettings().setJavaScriptEnabled(true);
		getSettings().setSupportZoom(false);
		getSettings().setDomStorageEnabled(true);
		
		setWebViewClient(new LuamingWebViewClient(activity));
		setWebChromeClient(new LuamingWebChromeClient(activity));
	}

}
