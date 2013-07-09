package com.ssm.luaming;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class LuamingWebViewClient extends WebViewClient {
	public MainActivity activity;
	
	public LuamingWebViewClient(MainActivity act) {
		activity = act;
	}
	
	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		// TODO Auto-generated method stub
		if (url.startsWith("luaming")) {
			if (url.contains("account")) {
				String[] temp = url.split("@");
				try {
					JSONObject account = new JSONObject(temp[1]);
					
					SharedPreferences sp = activity.getSharedPreferences(MainActivity.LUAMING_PREF, Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = sp.edit();
					editor.putInt(MainActivity.LUAMING_ACCOUNT_ID, Integer.parseInt(account.getString("account_id")));
					editor.putString(MainActivity.LUAMING_ACCESS_TOKEN, account.getString("access_token"));
					editor.commit();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if (url.contains("cangoback")) {
				String[] temp = url.split("@");
				activity.canGoBack =  Boolean.parseBoolean(temp[1]);
			}
			else if (url.endsWith("ok")) {
				activity.startGame();
			}						
			else if (url.endsWith("logout")) {
				SharedPreferences sp = activity.getSharedPreferences(MainActivity.LUAMING_PREF, Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();
				editor.clear();
				editor.commit();
				
				view.loadUrl("javascript: Luaming.initLocalStorage()");
				view.loadUrl("javascript: Luaming.redirectToHome()");
			}
			return true;
		}
							
		view.loadUrl(url);
		
		return false;
	}
	
	@Override
	public void onPageFinished(WebView view, String url) {
		// TODO Auto-generated method stub
		super.onPageFinished(view, url);
		if (activity.isFirstTime) {
			activity.isFirstTime = false;
			if (!activity.hasAccountInfo) {
				view.loadUrl("javascript: Luaming.initLocalStorage()");
				view.loadUrl("javascript: Luaming.redirectToHome()");
			}
			else {
				view.loadUrl("javascript: Luaming.initLocalStorage()");
				view.loadUrl("javascript: Luaming.setAccountInfo(" + activity.accountId + ", \"" + activity.accessToken + "\")");
				view.loadUrl("javascript: Luaming.redirectToMain()");
			}
		}
		else {
			view.loadUrl("javascript: Luaming.setCanGoBack()");
			view.loadUrl("javascript: Luaming.setLuamingBrowser()");
		}
	}
}
