package com.ssm.luaming.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ssm.luaming.R;
import com.ssm.luaming.util.LuamingAsyncTask;

public class LuamingProgressDialog extends Dialog implements OnDismissListener {
	
	private TextView dialogTitle;
	private ProgressBar dialogProgress;
	private TextView dialogPercent;
	private TextView dialogText;
	
	private LuamingAsyncTask asyncTask;
	
	private boolean isCancelable = false;
	
	public static boolean isDialogShowing = false;

	public LuamingProgressDialog(Context context) {
		super(context);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.luaming_progress_dialog);
		dialogTitle = (TextView)findViewById(R.id.progress_dialog_title);
		dialogProgress = (ProgressBar)findViewById(R.id.progress_dialog_progress);
		dialogPercent = (TextView)findViewById(R.id.progress_dialog_percent);
		dialogText = (TextView)findViewById(R.id.progress_dialog_text);
		
		setCancelable(false);
		
		setOnDismissListener(this);
	}
	
	@Override
	public void onBackPressed() {
		if (isCancelable) {
			dismiss();
			isDialogShowing = false;
		}
	}
	
	public void setAsyncTask(LuamingAsyncTask aTask) {
		asyncTask = aTask;
	}
	
	public boolean cancelAsyncTask() {
		if (asyncTask != null && asyncTask.getStatus() != AsyncTask.Status.FINISHED)
			return asyncTask.cancel(true);
		
		return false;
	}
	
	public void show(String title, String text) {		
		if (!isDialogShowing) {
			isDialogShowing = true;
			dialogTitle.setText(title);
			dialogText.setText(text);
			show();
			if (asyncTask != null)
				asyncTask.execute();
		}
	}
	
	public void updateText(String msg) {
		if (isShowing()) {
			dialogText.setText(msg);
		}
	}
	
	public void hideText(boolean hide) {
		if (hide) {
			dialogText.setVisibility(View.GONE);
		}
		else {
			dialogText.setVisibility(View.VISIBLE);
		}
	}
	
	public void updateProgress(int progress) {
		if (progress < 0 || progress > dialogProgress.getMax())
			return;
		
		if (isShowing()) {
			dialogProgress.setProgress(progress);
			dialogPercent.setText("" + progress + "%");
		}
	}
	
	public void setBackCancelable(boolean flag) {
		isCancelable = flag;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		isDialogShowing = false;
		cancelAsyncTask();
	}

}
