package com.ssm.luaming.web;

import com.ssm.luaming.LuamingActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

@SuppressLint("SetJavaScriptEnabled")
public class LuamingWebView extends WebView {
	
	public LuamingActivity activity;

	public LuamingWebView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public LuamingWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}	
	
	public void init(LuamingActivity act) {
		activity = act;
		
		setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
		setScrollbarFadingEnabled(true);
		setScrollContainer(true);
		setDownloadListener(new LuamingDownloadListener(activity));
		getSettings().setJavaScriptEnabled(true);
		getSettings().setSupportZoom(false);
		getSettings().setDomStorageEnabled(true);
		
		setWebViewClient(new LuamingWebViewClient(activity));
		setWebChromeClient(new LuamingWebChromeClient(activity));
	}

}
