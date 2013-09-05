package com.ssm.luaming;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LuamingOfflineListAdapter extends BaseAdapter {
	
	public Context context;
	public ArrayList<File> gameAPKs;
	public LayoutInflater inflater;
	private static final byte[] BUFFER = new byte[4096 * 1024];
	
	public LuamingOfflineListAdapter(Context ctx, ArrayList<File> list){
		context = ctx;
		gameAPKs = list;
	}

	@Override
	public int getCount() {
		if (gameAPKs == null)
			return 0;
		else
			return gameAPKs.size();
	}

	@Override
	public Object getItem(int position) {
		if (gameAPKs == null || position < 0 || position >= gameAPKs.size())
			return null;
		else
			return gameAPKs.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		if (view == null) {
			inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.luaming_offline_list_row, parent, false);
		}
		
		ImageView iconView = (ImageView)view.findViewById(R.id.offline_list_row_icon);
		TextView nameView = (TextView)view.findViewById(R.id.offline_list_row_name);
		TextView versionView = (TextView)view.findViewById(R.id.offline_list_row_version);
		
		setInformation(position, iconView, nameView, versionView);
		
		return view;
	}

	
	private void setInformation(int position, ImageView icon, TextView name, TextView version) {
		if (gameAPKs == null || position < 0 || position >= gameAPKs.size())
			return;
		
		try {
			ZipFile apkZip = new ZipFile(gameAPKs.get(position));
			for (Enumeration<? extends ZipEntry> e = apkZip.entries(); e.hasMoreElements();) {
				ZipEntry ze = e.nextElement();
				if (ze.getName().contains("LuamingProject.json")) {
					String jsonString = "";
					
					InputStream is = apkZip.getInputStream(ze);
					int bytesRead;
					while ((bytesRead = is.read(BUFFER)) > 0) {
						jsonString += new String(BUFFER, 0, bytesRead);
					}
					is.close();
					
					JSONObject projectInfoJson = new JSONObject(jsonString);
					String name_value = projectInfoJson.getString("PROJECT_NAME");
					String version_value = projectInfoJson.getString("VERSION_NAME");
					
					name.setText(name_value);
					name.setSelected(true);
					version.setText(version_value);
					
					String icon_value = projectInfoJson.getString("OFFLINE_ICON");
					setIconInformation(position, icon, icon_value);
					break;
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	private void setIconInformation(int position, ImageView icon, String entryName) {
		try {
			ZipFile apkZip = new ZipFile(gameAPKs.get(position));
			for (Enumeration<? extends ZipEntry> e = apkZip.entries(); e.hasMoreElements();) {
				ZipEntry ze = e.nextElement();
				if (ze.getName().contains(entryName)) {
					
					InputStream is = apkZip.getInputStream(ze);
					Bitmap bmp = BitmapFactory.decodeStream(is);
					is.close();
					
					Drawable drawable = new BitmapDrawable(bmp);
					icon.setBackgroundDrawable(drawable);
					
					break;
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
