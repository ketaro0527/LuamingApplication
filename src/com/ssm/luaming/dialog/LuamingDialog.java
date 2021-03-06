package com.ssm.luaming.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.ssm.luaming.R;
import com.ssm.luaming.util.LuamingImageButton;

public class LuamingDialog extends Dialog implements android.view.View.OnClickListener {
	
	public static final int LUAMING_DIALOG_STYLE_SINGLE = 0;
	public static final int LUAMING_DIALOG_STYLE_OK_CANCEL = 1;

	private LuamingImageButton btnOK;
	private LuamingImageButton btnCancel;
	private TextView dialogText;
	private boolean isCancelable = false;
	private boolean isClicked = false;

	public static boolean isDialogShowing = false;
	
	private int dialogType;

	public LuamingDialog(Context context, int type) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		dialogType = type;

		if (type == LUAMING_DIALOG_STYLE_SINGLE)
			setSingleDialog();
		else
			seOKCancelDialog();
		
		setCancelable(false);
	}

	@Override
	public void onClick(View v) {
		if (dialogType == LUAMING_DIALOG_STYLE_SINGLE) {
			if (v == btnOK) {
				isClicked = true;
				dismiss();
			}
		}
		else {
			if (v == btnOK) {
				isClicked = true;
				cancel();
			}
			else if (v == btnCancel) {
				isClicked = true;
				dismiss();
			}
		}
		
		isDialogShowing = false;
	}
	
	@Override
	public void onBackPressed() {
		if (isCancelable) {
			dismiss();
			isDialogShowing = false;
		}
	}
	
	public void show(String msg) {
		if (!isDialogShowing) {
			isDialogShowing = true;
			dialogText.setText(msg);
			show();
		}
	}

	private void setSingleDialog() {
		setContentView(R.layout.luaming_single_dialog);

		dialogText = (TextView) findViewById(R.id.single_dialog_text);

		btnOK = (LuamingImageButton) findViewById(R.id.single_dialog_ok);
		btnOK.setImageResource(R.drawable.yes);
		btnOK.setOnClickListener(this);
	}

	private void seOKCancelDialog() {
		setContentView(R.layout.luaming_okcancel_dialog);

		dialogText = (TextView) findViewById(R.id.oc_dialog_text);

		btnOK = (LuamingImageButton) findViewById(R.id.oc_dialog_ok);
		btnOK.setImageResource(R.drawable.yes);
		btnOK.setOnClickListener(this);
		
		btnCancel = (LuamingImageButton) findViewById(R.id.oc_dialog_cancel);
		btnCancel.setImageResource(R.drawable.no);
		btnCancel.setOnClickListener(this);
	}
	
	public void setBackCancelable(boolean flag) {
		isCancelable = flag;
	}
	
	public boolean canClose() {
		return isClicked;
	}
}
