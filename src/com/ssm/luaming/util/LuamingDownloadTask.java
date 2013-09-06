package com.ssm.luaming.util;

import java.io.File;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;

import com.ssm.luaming.LuamingActivity;

public class LuamingDownloadTask extends LuamingAsyncTask {

	private LuamingActivity activity;

	public LuamingDownloadTask(LuamingActivity act) {
		activity = act;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		boolean downloading = true;
		DownloadManager downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);

		DownloadManager.Query q = new DownloadManager.Query();
		q.setFilterById(activity.downloadId);

		while(downloading && activity.pd != null && activity.downloadId != -1) {
			try{
				Cursor cursor = downloadManager.query(q);
				cursor.moveToFirst();

				double bytes_total = (double)cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

				if (bytes_total < 0) {
					cursor.close();
					continue;
				}

				String path = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));

				double bytes_downloaded = (double)new File(path).length();
				if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
					downloading = false;
				}

				final double dl_progress = (bytes_downloaded / bytes_total) * 100;

				publishProgress((int)dl_progress);
				cursor.close();
			} catch(CursorIndexOutOfBoundsException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		int progress = values[0];
		if (activity.pd != null && activity.pd.isShowing())
			activity.pd.updateProgress(progress);
	}

}
