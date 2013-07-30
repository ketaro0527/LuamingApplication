package com.ssm.luaming;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ssm.luaming.dialog.LuamingDialog;
import com.ssm.luaming.dialog.LuamingOnCancelListener;
import com.ssm.luaming.dialog.LuamingOnDismissListener;
import com.ssm.luaming.game.Luaming;
import com.ssm.luaming.game.LuamingPortrait;
import com.ssm.luaming.util.LuamingUpdateUtil;

@SuppressLint({ "HandlerLeak", "DefaultLocale" })
public class LuamingOfflineActivity extends Activity implements OnItemClickListener {

	private static final int FIND_GAME_LIST = 0;
	private static final int SHOW_GAME_LIST = 1;

	private ProgressDialog pd = null;
	private ArrayList<File> gameAPKs;
	private ListView gameListView;
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case FIND_GAME_LIST: {
				pd = ProgressDialog.show(LuamingOfflineActivity.this, "Searching", "Please wait...", true);
				thread = new Thread() {

					@Override
					public void run() {
						gameAPKs = getGameList();
						handler.sendEmptyMessage(SHOW_GAME_LIST);
					}
				};
				thread.start();
			}
			break;
			case SHOW_GAME_LIST: {
				if (pd != null) {
					pd.dismiss();
					pd = null;
				}
				if (gameAPKs != null && gameAPKs.size() != 0) {
					ArrayList<String> list = new ArrayList<String>();
					for (int i = 0; i < gameAPKs.size(); i++) {
						list.add(gameAPKs.get(i).getName());
					}
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(LuamingOfflineActivity.this, android.R.layout.simple_list_item_1, list);
					gameListView.setAdapter(adapter);
					gameListView.setOnItemClickListener(LuamingOfflineActivity.this);
				}
				else {
					ArrayList<String> list = new ArrayList<String>();
					list.add("다운 받은 게임이 없습니다");
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(LuamingOfflineActivity.this, android.R.layout.simple_list_item_1, list);
					gameListView.setAdapter(adapter);
				}
			}
			break;
			default:
				break;
			}
		}
	};

	private Thread thread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		// 외장 메모리 확인
		if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			LuamingDialog dialog = new LuamingDialog(this, LuamingDialog.LUAMING_DIALOG_STYLE_SINGLE);
			dialog.setBackCancelable(false);
			dialog.setOnDismissListener(new LuamingOnDismissListener(LuamingOnDismissListener.LUAMING_DISMISS_TYPE_FINISH));
			dialog.show("SD카드가 없습니다.\nLuaming을 종료합니다.");
		}

		LuamingActivity.mainPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.Luaming";

		setContentView(R.layout.offline_layout);
		gameListView = (ListView)findViewById(R.id.offline_game_list);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		handler.sendEmptyMessage(FIND_GAME_LIST);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(0, 0, 0, "온라인 모드");
		menu.add(0, 1, 0, "종료하기");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()) {
		case 0: {
			Intent intent = new Intent(this, LuamingActivity.class);
			startActivity(intent);
			finish();
		}
		break;
		case 1: {
			android.os.Process.killProcess(android.os.Process.myPid());
		}
		break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		LuamingDialog dialog = new LuamingDialog(this, LuamingDialog.LUAMING_DIALOG_STYLE_OK_CANCEL);
		dialog.setOnCancelListener(new LuamingOnCancelListener(LuamingOnCancelListener.LUAMING_CANCEL_TYPE_FINISH));
		dialog.show("종료하시겠습니까?");
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		startGame(gameAPKs.get(arg2).getParent(), gameAPKs.get(arg2).getName());
	}

	private ArrayList<File> getGameList() {
		ArrayList<File> apkList = new ArrayList<File>(); 

		File dir = new File(LuamingActivity.mainPath);
		String[] tokenDirList = dir.list();
		for (int i = 0; i < tokenDirList.length; i++) {
			File tokenDir = new File(LuamingActivity.mainPath + "/" + tokenDirList[i]);
			if (tokenDir.isDirectory()) {
				String[] gameDirList = tokenDir.list();
				for (int j = 0; j < gameDirList.length; j++) {
					File gameDir = new File(tokenDir.getAbsolutePath() + "/" + gameDirList[j]);
					if (gameDir.isDirectory()) {
						FilenameFilter filter = new FilenameFilter() {

							@Override
							public boolean accept(File dir, String name) {
								// TODO Auto-generated method stub
								if (name != null && !name.contains("_Update") && !name.contains("temp") && name.endsWith("apk"))
									return true;
								return false;
							}
						};

						File[] apkFiles = gameDir.listFiles(filter);
						for (int k = 0; k < apkFiles.length; k++)
							apkList.add(apkFiles[k]);
					}
				}
			}
		}
		return apkList;
	}

	public void startGame(String dir, String apkName) {
		String orientation = LuamingUpdateUtil.checkOrientation(dir, apkName);

		// 이동
		Class<?> cls = null;
		if ("landscape".equals(orientation.toLowerCase()))
			cls = Luaming.class;
		else
			cls = LuamingPortrait.class;

		Intent intent = new Intent(this, cls);
		intent.putExtra(LuamingConstant.LUAMING_GAME_PATH, dir + "/" + apkName);
		startActivity(intent);
	}
}
