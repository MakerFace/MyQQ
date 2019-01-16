package com.example.uibestpractice;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.example.uibestpractice.fragments.OtherFragment;
import com.example.uibestpractice.messages.Message;
import com.example.uibestpractice.services.DownloadServer;
import com.example.uibestpractice.services.UploadTask;
import com.example.uibestpractice.usersBean.User;
import com.example.uibestpractice.utils.Strings;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MessageActivity extends BaseActivity implements View.OnClickListener {

    private final int DOWNLOAD = 0x010;
    private static final String TAG = "MessageActivity";

    private List<Message> messageList = new ArrayList<>();
    private DownloadServer.DownloadBinder downloadBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            downloadBinder = (DownloadServer.DownloadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private EditText inputText;
    private RecyclerView msgRecyclerView;

    private MessageAdapter adapter;
    private Bitmap bitmap;
    private boolean otherShow = false;
    private OtherFragment otherFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initMsgs(); // 初始化消息数据
        inputText = (EditText) findViewById(R.id.input_text);
        Button send = (Button) findViewById(R.id.send);
        Button otherBtn = (Button) findViewById(R.id.msg_add_btn);

        msgRecyclerView = (RecyclerView) findViewById(R.id.msg_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);
        adapter = new MessageAdapter(messageList);
        msgRecyclerView.setAdapter(adapter);
        msgRecyclerView.scrollToPosition(messageList.size() - 1); // 将ListView定位到最后一行
        send.setOnClickListener(this);
        otherBtn.setOnClickListener(this);
        msgRecyclerView.setOnClickListener(this);
        adapter.setCallback(new Handler.Callback() {
            @Override
            public boolean handleMessage(android.os.Message message) {
                switch (message.what) {
                    case DOWNLOAD:
                        startDownloadService((String) message.obj);
                        break;
                }
                return true;
            }
        });
        inputText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b && otherShow){
                    showOtherFunction();
                }
                Log.i(TAG, "onFocusChange: " + b);
            }
        });

        SharedPreferences preferences = getSharedPreferences("data", MODE_PRIVATE);
        String account = preferences.getString("account", "");
        User loginedUser = DataSupport.where("uid = " + account).findFirst(User.class);
        bitmap = BitmapFactory.decodeFile(FragmentTabActivity.APP_PERSONAL_DIRECTORY + "/headIcon/" + loginedUser.getHeadIcon());

        Intent intent = new Intent(this, DownloadServer.class);
        startService(intent);
        bindService(intent, connection, BIND_AUTO_CREATE);
        if (ContextCompat.checkSelfPermission(MessageActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MessageActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private void startDownloadService(String url) {
        try {
            Log.i(TAG, "startDownloadService: url=" + url);
            downloadBinder.startDownload(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initMsgs() {
        messageList = DataSupport.findAll(Message.class);
        String url = Strings.HTTP_URL + "download?fileName=" + "tmp.mp3";
        Message msg = new Message();
        msg.setContent(url);
        msg.setType(Message.TYPE_RECEIVE | Message.TYPE_FILE);
        msg.setNewTask(false);
        messageList.add(msg);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send:
                sendSaveMessage(Message.TYPE_SEND);
                break;
            case R.id.msg_add_btn:
                showOtherFunction();
                msgRecyclerView.scrollToPosition(messageList.size() - 1); // 将RecyclerView定位到最后一行
                break;
            case R.id.msg_recycler_view:
                Toast.makeText(this, "on click recycler view", Toast.LENGTH_SHORT).show();
                if (otherShow) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.hide(otherFragment);
                    transaction.commit();
                    otherShow = false;
                }
                break;
        }
    }

    private void showOtherFunction() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (otherShow) {
            otherShow = false;
            if (otherFragment != null)
                transaction.hide(otherFragment);
        } else {
            otherShow = true;
            if (otherFragment == null) {
                otherFragment = new OtherFragment();
                otherFragment.setListener(new OtherFragment.OnResultListener() {
                    @Override
                    public void onResult(String filePath) {
//                        Toast.makeText(MessageActivity.this, "filePath" + filePath, Toast.LENGTH_SHORT).show();
                        sendFile(filePath);
                    }
                });
                transaction.add(R.id.msg_add_fragment, otherFragment);
            }
            transaction.show(otherFragment);
        }
        transaction.commit();
    }

    private void sendFile(String filePath) {
        if(otherShow)
            showOtherFunction();
        inputText.setText(filePath);
        sendSaveMessage(Message.TYPE_FILE | Message.TYPE_SEND);
    }

    private Message makeMessage(String content, int type) {
        Message message = new Message();
        message.setContent(content);
        message.setType(type);
        message.setNewTask(true);
        messageList.add(message);
        adapter.notifyItemInserted(messageList.size() - 1); // 当有新消息时，刷新ListView中的显示
        msgRecyclerView.scrollToPosition(messageList.size() - 1); // 将ListView定位到最后一行
        inputText.setText(""); // 清空输入框中的内容
        return message;
    }

    private void sendSaveMessage(int type) {
        String content = inputText.getText().toString();
        if (!"".equals(content)) {
            Message message = makeMessage(content, type);
            message.save();
            Intent intent = new Intent(this, MessageActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setContentTitle("发送消息")
                    .setContentText(message.getContent())
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(bitmap)
                    .setContentIntent(pendingIntent)
                    .setLights(Color.GREEN, 1000, 1000)
                    .setAutoCancel(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setVisibility(Notification.VISIBILITY_PUBLIC);
                // 关联PendingIntent
                builder.setFullScreenIntent(pendingIntent, true);// 横幅
            }
            Notification notification = builder.build();
            assert notificationManager != null;
            notificationManager.notify(1, notification);
        }
    }

    public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

        private List<Message> mMessageList;
        private Handler.Callback callback;

        void setCallback(Handler.Callback callback) {
            this.callback = callback;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            LinearLayout leftLayout;

            LinearLayout rightLayout;

            TextView leftMsg;
            ImageView leftImg;
            NumberProgressBar leftPro;

            TextView rightMsgTxt;
            ImageView rightMsgImg;
            ImageView rightImg;
            NumberProgressBar rightPro;

            ViewHolder(View view) {
                super(view);
                leftLayout = (LinearLayout) view.findViewById(R.id.left_layout);
                rightLayout = (LinearLayout) view.findViewById(R.id.right_layout);
                leftMsg = (TextView) view.findViewById(R.id.left_msg);
                leftImg = (ImageView) view.findViewById(R.id.left_img);
                rightMsgTxt = (TextView) view.findViewById(R.id.right_msg_txt);
                rightMsgImg = (ImageView) view.findViewById(R.id.right_msg_img);
                rightImg = (ImageView) view.findViewById(R.id.right_img);
                leftPro = (NumberProgressBar) view.findViewById(R.id.left_pro);
                rightPro = (NumberProgressBar) view.findViewById(R.id.right_pro);
                leftPro.setMax(100);
                rightPro.setMax(100);
            }

            public void setDownloadProgress(final int progress) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        freshDownloadProgress(progress);
                    }
                });
            }

            void freshDownloadProgress(int progress) {
                leftPro.setProgress(progress);
            }

            public void setUploadProgress(final int progress) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        freshUploadProgress(progress);
                    }
                });
            }

            void freshUploadProgress(int progress) {
                rightPro.setProgress(progress);
            }
        }

        MessageAdapter(List<Message> messageList) {
            mMessageList = messageList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            Message message = mMessageList.get(position);
            int type = message.getType();
            if ((type & Message.TYPE_RECEIVE) != 0) {
                setReceiveMessage(holder, message);
            } else if ((type & Message.TYPE_SEND) != 0) {
                setSendMessage(holder, message);
            }
        }

        private void setReceiveMessage(ViewHolder holder, final Message message) {
            // 如果是收到的消息，则显示左边的消息布局，将右边的消息布局隐藏
            holder.leftLayout.setVisibility(View.VISIBLE);
            holder.rightLayout.setVisibility(View.GONE);
            holder.leftMsg.setText(message.getContent());
            if ((message.getType() & Message.TYPE_FILE) != 0) {
                holder.leftMsg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        android.os.Message msg = new android.os.Message();
                        msg.what = DOWNLOAD;
                        msg.obj = message.getContent();
                        callback.handleMessage(msg);
                    }
                });
            }
        }

        private void setSendMessage(ViewHolder holder, Message message) {
            // 如果是发出的消息，则显示右边的消息布局，将左边的消息布局隐藏
            holder.rightLayout.setVisibility(View.VISIBLE);
            holder.leftLayout.setVisibility(View.GONE);
            holder.rightMsgTxt.setText(message.getContent());
            holder.rightImg.setImageBitmap(bitmap);
            Matcher matcher = isUrl(message.getContent());
            if (matcher.find()) {
                final String url = matcher.group();
                holder.rightMsgTxt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MessageActivity.this, WebViewActivity.class);
                        intent.putExtra(WebViewActivity.WEB_URL, url);
                        Log.i(TAG, "onClick: " + url);
                        startActivity(intent);
                    }
                });
            }

            if ((message.getType() & Message.TYPE_FILE) != 0 && message.isNewTask()) {
                Log.i(TAG, "onBindViewHolder: new task");
                message.setNewTask(false);
                message.save();
                holder.rightMsgTxt.setVisibility(View.GONE);
                holder.rightMsgImg.setVisibility(View.VISIBLE);
                holder.rightMsgImg.setImageResource(R.drawable.file);
                holder.rightPro.setVisibility(View.VISIBLE);
                UploadTask uploadTask = new UploadTask(holder, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response != null) {
                            String result = response.body().string();
                            Log.i(TAG, "result===" + result);
                        }
                    }
                });
                uploadTask.executeOnExecutor(Executors.newCachedThreadPool(), Strings.HTTP_URL + "upload", message.getContent());
                Toast.makeText(MessageActivity.this, "send file", Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        public int getItemCount() {
            return mMessageList.size();
        }

        void freshProgress() {
            this.notifyDataSetChanged();
        }

    }

    //很有价值！https://blog.csdn.net/qq_19986309/article/details/73885197
    private Matcher isUrl(String url) {
        Pattern pattern = Patterns.WEB_URL;
        return pattern.matcher(url);
    }
}
