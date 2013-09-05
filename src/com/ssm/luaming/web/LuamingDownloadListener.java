package com.ssm.luaming.web;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.net.Uri;
import android.webkit.DownloadListener;
import android.webkit.MimeTypeMap;

import com.ssm.luaming.LuamingActivity;
import com.ssm.luaming.dialog.LuamingProgressDialog;
import com.ssm.luaming.util.LuamingDownloadTask;

@SuppressLint("DefaultLocale")
public class LuamingDownloadListener implements DownloadListener{
	
	private LuamingActivity activity;
	
	public LuamingDownloadListener(LuamingActivity act) {
		activity = act;	
	}

	@Override
	public void onDownloadStart(String url, String userAgent,
			String contentDisposition, String mimetype, long contentLength) {

		MimeTypeMap mtm = MimeTypeMap.getSingleton();
		DownloadManager downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);

		Uri downloadUri = Uri.parse(url);
		// 파일 이름을 추출한다. contentDisposition에 filename이 있으면 그걸 쓰고 없으면 URL의 마지막 파일명을 사용한다.

		String fileName = downloadUri.getLastPathSegment();
		int pos = 0;
		if (contentDisposition != null && (pos = contentDisposition.toLowerCase().lastIndexOf("filename=")) >= 0) {
			fileName = contentDisposition.substring(pos + 9);
			pos = fileName.lastIndexOf(";");

			if (pos > 0) {
				fileName = fileName.substring(0, pos - 1);
			}
		}

		// MIME Type을 확장자를 통해 예측한다.
		String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();
		String mimeType = mtm.getMimeTypeFromExtension(fileExtension);

		// Download 디렉토리에 저장하도록 요청을 작성
		Request request = new DownloadManager.Request(downloadUri);
		request.setTitle(fileName);
		request.setDescription(url);
		request.setMimeType(mimeType);
		request.setDestinationInExternalPublicDir( "/Android/data/com.ssm.luaming/files/.Luaming/" + activity.accessToken + "/" + activity.packageName, activity.updateName);

		// 다운로드 매니저에 요청 등록
		activity.downloadId = downloadManager.enqueue(request);
		
		// Progress Dialog
		activity.pd = new LuamingProgressDialog(activity);
		
		LuamingDownloadTask downTask = new LuamingDownloadTask(activity);
		
		activity.pd.setAsyncTask(downTask);
		activity.pd.updateText(fileName);
		activity.pd.show("Downloading...", fileName);		
	}

}
