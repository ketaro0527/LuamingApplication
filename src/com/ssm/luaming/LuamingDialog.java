package com.ssm.luaming;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class LuamingDialog extends Dialog implements android.view.View.OnClickListener {
	public static final int LUAMING_DIALOG_STYLE_SINGLE = 0;
	public static final int LUAMING_DIALOG_STYLE_OK_CANCEL = 1;

	private ImageButton btnOK;
	private ImageButton btnCancel;
	private TextView dialogText;

	private int dialogType;

	public LuamingDialog(Context context, int type) {
		super(context);
		// TODO Auto-generated constructor stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		dialogType = type;

		if (type == LUAMING_DIALOG_STYLE_SINGLE)
			setSingleDialog();
		else
			seOKCancelDialog();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (dialogType == LUAMING_DIALOG_STYLE_SINGLE) {
			if (v == btnOK) {
				dismiss();
			}
		}
		else {
			if (v == btnOK) {
				cancel();
			}
			else if (v == btnCancel) {
				dismiss();
			}
		}
	}
	
	@Override
	public void onBackPressed() {
		dismiss();
	}
	
	public void show(String msg) {
		dialogText.setText(msg);
		show();
	}

	private void setSingleDialog() {
		setContentView(R.layout.luaming_single_dialog);

		dialogText = (TextView) findViewById(R.id.single_dialog_text);

		btnOK = (ImageButton) findViewById(R.id.single_dialog_ok);
		btnOK.setImageResource(R.drawable.yes);
		btnOK.setOnClickListener(this);
	}

	private void seOKCancelDialog() {
		setContentView(R.layout.luaming_okcancel_dialog);

		dialogText = (TextView) findViewById(R.id.oc_dialog_text);

		btnOK = (ImageButton) findViewById(R.id.oc_dialog_ok);
		btnOK.setImageResource(R.drawable.yes);
		btnOK.setOnClickListener(this);
		
		btnCancel = (ImageButton) findViewById(R.id.oc_dialog_cancel);
		btnCancel.setImageResource(R.drawable.no);
		btnCancel.setOnClickListener(this);
	}
}
