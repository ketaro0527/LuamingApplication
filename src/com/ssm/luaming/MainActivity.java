package com.ssm.luaming;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class MainActivity extends Activity implements OnClickListener {
	public static final String LUAMING_GAME_PATH = "LUAMING_GAME_PATH";
	private static final int UPDATE_COMPLETE = 0;
	private static final int UPDATE_FAILED = 1;
	private Button btn_update;
	private Button btn_execute;
	private String mainPath;
	private String apkName = "HelloSSM.apk";
	private String updateName = "Update.apk";
	
	private ProgressDialog pd;
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message m) {
			if (pd != null) {
				pd.dismiss();
				pd = null;
			}
			
			if (m.what == UPDATE_COMPLETE) {
				MainActivity.this.startGame();
			}
			else if (m.what == UPDATE_FAILED) {
				Toast.makeText(MainActivity.this, "Update Failed", Toast.LENGTH_SHORT).show();
			}
		}
	};
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		
		mainPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		mainPath += "/Luaming";
		File LuamingDir = new File(mainPath);
		if (!LuamingDir.exists())
			LuamingDir.mkdir();
		
		btn_update = (Button)findViewById(R.id.btn_update);
		btn_update.setOnClickListener(this);
		
		btn_execute = (Button)findViewById(R.id.btn_execute);
		btn_execute.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == btn_update) {
			pd = ProgressDialog.show(MainActivity.this, "Update", "Please wait...", true);
			pd.setCancelable(false);
			
			Thread thread = new Thread() {
				@Override
				public void run() {
					if (UpdateUtil.update(MainActivity.this.mainPath, MainActivity.this.apkName, MainActivity.this.updateName))
						MainActivity.this.handler.sendEmptyMessage(UPDATE_COMPLETE);
					else
						MainActivity.this.handler.sendEmptyMessage(UPDATE_FAILED);
				}
			};
			thread.start();
		}
		else if (v == btn_execute) {
			startGame();
		}
	}
	
	public void startGame() {
		Intent intent = new Intent(this, Luaming.class);
		intent.putExtra(LUAMING_GAME_PATH, mainPath + "/" + apkName);
		startActivity(intent);
	}

}
