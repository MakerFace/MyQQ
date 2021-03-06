package com.example.uibestpractice.services;

import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.example.uibestpractice.FragmentTabActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DownloadTask extends AsyncTask<String, Integer, TaskStatus> {

    private static final String TAG = "DownloadTask";

    private TaskStatus status;
    private DownloadListener listener;
    private boolean isCanceled = false;
    private boolean isPaused = false;
    private int lastProgress;

    public DownloadTask(DownloadListener listener) {
        this.listener = listener;
    }

    @Override
    protected TaskStatus doInBackground(String... params) {
        InputStream is = null;
        RandomAccessFile savedFile = null;
        File file = null;
        try {
            long downloadedLength = 0;
            String downloadUrl = params[0];
            //serverUrl:8080/download?filename=*
            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("=") + 1);
            ///storage/emulated/0/Android/data/com.example.uibestpractice/files/account/fileRecv/*
            String directory = FragmentTabActivity.APP_PERSONAL_DIRECTORY + "/fileRecv/";
            File folder = new File(directory);
            if (!folder.exists()) {
                folder.mkdir();
            }
            file = new File(directory + fileName);
            if (file.exists()) {
                downloadedLength = file.length();
            }
            Log.i(TAG, "doInBackground: download url = " + downloadUrl
                    + "===file name = " + fileName + "===local file path = " + file.getAbsolutePath());
            long contentLength = getContentLength(downloadUrl);
            if (contentLength == 0) {
                return TaskStatus.TYPE_FAILED;
            } else if (contentLength == downloadedLength) {
                return TaskStatus.TYPE_SUCCESS;
            }
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("fileName", fileName)
                    .build();
            Request request = new Request.Builder()
                    .addHeader("RANGE", "bytes=" + downloadedLength)
                    .post(body)
                    .url(downloadUrl)
                    .build();
            Log.i(TAG, "doInBackground: request" + request.toString());
            Response response = client.newCall(request).execute();
            if (response != null) {
                is = response.body().byteStream();
                savedFile = new RandomAccessFile(file, "rw");
                savedFile.seek(downloadedLength);
                byte[] b = new byte[1024];
                int total = 0;
                int len;
                while ((len = is.read(b)) != -1) {
                    if (isCanceled) {
                        return TaskStatus.TYPE_CANCELED;
                    } else if (isPaused) {
                        return TaskStatus.TYPE_PAUSE;
                    } else {
                        total += len;
                        savedFile.write(b, 0, len);
                        int progress = (int) ((total + downloadedLength) * 100 / contentLength);
                        publishProgress(progress);
                    }
                }
                response.body().close();
                return TaskStatus.TYPE_SUCCESS;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (savedFile != null) {
                    savedFile.close();
                }
                if (isCanceled && file != null) {
                    file.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return TaskStatus.TYPE_FAILED;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if (progress > lastProgress) {
            listener.onProgress(progress);
            lastProgress = progress;
        }
    }

    @Override
    protected void onPostExecute(TaskStatus taskStatus) {
        switch (taskStatus) {
            case TYPE_SUCCESS:
                listener.onSuccess();
                break;
            case TYPE_FAILED:
                listener.onFailed();
                break;
            case TYPE_PAUSE:
                listener.onPaused();
                break;
            case TYPE_CANCELED:
                listener.onCanceled();
                break;
            default:
                break;
        }
    }

    public void pauseDownload() {
        isPaused = true;
    }

    public void cancelDownload() {
        isCanceled = true;
    }

    private long getContentLength(String downloadUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        Response response = client.newCall(request).execute();
        Log.i(TAG, "getContentLength: download url" + downloadUrl
                + "...response = " + response);
        if (response != null && response.isSuccessful()) {
            long contentLength = response.body().contentLength();
            Log.i(TAG, "getContentLength: contentLength" + contentLength);
            response.close();
            return contentLength;
        }
        return 0;
    }
}
