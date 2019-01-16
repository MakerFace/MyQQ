package com.example.uibestpractice.services;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RecoverySystem;
import android.util.Log;

import com.example.uibestpractice.MessageActivity;

import java.io.File;
import java.io.IOException;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;


public class UploadTask extends AsyncTask<String, Integer, TaskStatus> {

    private static final String TAG = "UploadTask";
    private static final int UPDATE = 0x01;
    private MessageActivity.MessageAdapter.ViewHolder viewHolder;
    private Callback callback;

    public UploadTask(MessageActivity.MessageAdapter.ViewHolder viewHolder, Callback callback) {
        this.viewHolder = viewHolder;
        this.callback = callback;
    }

    @Override
    protected TaskStatus doInBackground(String... strings) {
        String url = strings[0];
        String filePath = strings[1];
        File file = new File(filePath);
        Log.i(TAG, "doInBackground: url = " + url + "?fileName=" + file.getName());
        OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        builder.addFormDataPart(
                "file", file.getName(),
                RequestBody.create(MediaType.parse("application/octet-stream"), file));

        MultipartBody multipartBody = builder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(new ProgressRequestBody(multipartBody))
                .build();
        client.newCall(request).enqueue(callback);
        return TaskStatus.TYPE_SUCCESS;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        try {
            Log.i(TAG, "onProgressUpdate: progress" + values[0]);
            viewHolder.setUploadProgress(values[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ProgressRequestBody extends RequestBody {

        private RequestBody requestBody;
        private BufferedSink bufferedSink;
        private MyHandler myHandler;

        ProgressRequestBody(RequestBody body) {
            requestBody = body;
            if (myHandler == null) {
                myHandler = new MyHandler();
            }
        }

        @Override
        public MediaType contentType() {
            return requestBody.contentType();
        }

        @Override
        public long contentLength() throws IOException {
            return requestBody.contentLength();
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            if (bufferedSink == null) {
                bufferedSink = Okio.buffer(sink(sink));
            }
            requestBody.writeTo(bufferedSink);
            bufferedSink.flush();
        }

        private Sink sink(BufferedSink sink) {
            return new ForwardingSink(sink) {
                long bytesWritten = 0L;
                long contentLength = 0L;

                @Override
                public void write(Buffer source, long byteCount) throws IOException {
                    super.write(source, byteCount);
                    if (contentLength == 0) {
                        contentLength = contentLength();
                    }
                    bytesWritten += byteCount;
                    Message msg = Message.obtain();
                    msg.what = UPDATE;
                    msg.obj = (int) (bytesWritten * 100 / contentLength);
                    myHandler.handleMessage(msg);
                }
            };
        }

        @SuppressLint("HandlerLeak")
        class MyHandler extends Handler {
            MyHandler() {
                super(Looper.getMainLooper());
            }

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UPDATE:
                        publishProgress((Integer) msg.obj);
                        break;
                }
            }
        }
    }
}
