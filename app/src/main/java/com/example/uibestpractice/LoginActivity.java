package com.example.uibestpractice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uibestpractice.logins.MyPopupWindows;
import com.example.uibestpractice.usersBean.User;
import com.example.uibestpractice.utils.ParseJSON;
import com.example.uibestpractice.utils.Strings;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private EditText userIdText;
    private EditText userPassText;
    private CheckBox remPassCheck;
    private Button loginBtn;
    private Button allUserBtn;
    private View passView;
    private View otherView;

    private List<User> users;
    private AllUsersAdapter usersAdapter;
    private MyPopupWindows mPopupWindow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        userIdText = (EditText) findViewById(R.id.user_id);
        userPassText = (EditText) findViewById(R.id.user_pass);
        loginBtn = (Button) findViewById(R.id.login_btn);
        Button regBtn = (Button) findViewById(R.id.go_reg_btn);
        remPassCheck = (CheckBox) findViewById(R.id.rem_pass);
        allUserBtn = (Button) findViewById(R.id.all_user_btn);
        passView = findViewById(R.id.pass_layout);
        otherView = findViewById(R.id.other_layout);
        loginBtn.setOnClickListener(this);
        regBtn.setOnClickListener(this);
        allUserBtn.setOnClickListener(this);
        initUsers();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
        Log.i("LoginActivity", "onWindowFocusChanged: focus=" + hasFocus);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_btn:
                login();
                break;
            case R.id.go_reg_btn:
                Intent intent = new Intent();
                intent.setClass(this, RegisterActivity.class);
                startActivity(intent);
                break;
            case R.id.all_user_btn:
                allUserBtn.setBackgroundResource(R.drawable.pop);
                RecyclerView mRecyclerView = initRecyclerViewData();
                mPopupWindow = new MyPopupWindows(mRecyclerView, userIdText.getWidth(), 300);
                mPopupWindow.setChangeButton(allUserBtn);
                mPopupWindow.setOnClickListener(new MyPopupWindows.OnClickListener() {
                    @Override
                    public void onClick() {
                        showOtherView();
                    }
                });
                mPopupWindow.showAsDropDown(userIdText);
                hideOtherView();
                break;
            case R.id.all_user_del:
                User user = (User) view.getTag();
                Toast.makeText(this, "Delete:" + user.getUid(), Toast.LENGTH_SHORT).show();
                users.remove(user);
                usersAdapter.notifyDataSetChanged();
                user.delete();
                break;
        }
    }

    private void hideOtherView() {
        loginBtn.setVisibility(View.GONE);
        passView.setVisibility(View.GONE);
        otherView.setVisibility(View.GONE);
    }

    private void showOtherView() {
        loginBtn.setVisibility(View.VISIBLE);
        passView.setVisibility(View.VISIBLE);
        otherView.setVisibility(View.VISIBLE);
    }

    private RecyclerView initRecyclerViewData() {
        RecyclerView mRecyclerView = new RecyclerView(this);
        mRecyclerView.setVerticalScrollBarEnabled(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        usersAdapter = new AllUsersAdapter(users);
        usersAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onClick(View view) {
                onItemClick(view);
            }
        });
        usersAdapter.setOnInnerItemClickListener(new OnInnerItemClickListener() {
            @Override
            public void onInnerClick(View view) {
                onClick(view);
            }
        });
        mRecyclerView.setAdapter(usersAdapter);
        return mRecyclerView;
    }

    private void initUsers() {
        users = DataSupport.findAll(User.class);
    }

    private void onItemClick(View view) {
        User user = (User) view.getTag();
        userIdText.setText(user.getUid());
        userPassText.setText(user.getPass());
        mPopupWindow.dismiss();
    }

    private void login() {
        if (verify()) {
            if (remPassCheck.isChecked()) {
                rememberAccount();
            }
        } else {
            Toast.makeText(this, "登陆失败，账号密码错误……", Toast.LENGTH_SHORT).show();
            userPassText.setText("");
        }
    }

    private void pass() {
        String account = userIdText.getText().toString();
//        Toast.makeText(this, "登录中……", Toast.LENGTH_SHORT).show();
        Log.i("LoginActivity", "pass: account=" +  account);
        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        editor.putString("account", account);
        editor.apply();
        File appFolder = this.getExternalFilesDir(null);
        File userFolder = new File(appFolder.getPath() + "/" + account);
        if (!userFolder.exists()) {//新用户，创建个人文件夹
            if (userFolder.mkdir()) {
                new File(userFolder, "headIcon").mkdir();
                new File(userFolder, "message").mkdir();
            } else {
                Toast.makeText(this, "创建个人文件夹失败", Toast.LENGTH_SHORT).show();
            }
        }
        if (ActivityCollector.getCount() == 1) {
            Intent intent = new Intent();
            intent.setClass(this, FragmentTabActivity.class);
            startActivity(intent);
        } else {
            this.finish();
        }
    }

    private boolean verify() {
        String id = userIdText.getText().toString();
        String pass = userPassText.getText().toString();
        sendRequestWithHttpURLConnection(id, pass);
        //debug 无法连接服务器使用
        pass();
        return true;
    }

    private void sendRequestWithHttpURLConnection(final String id, final String pass) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("name", id)
                        .add("pass", pass)
                        .build();
                Request request = new Request.Builder()
                        .url(Strings.HTTP_URL+"login")
                        .post(requestBody)
                        .build();
                Log.i("Login Activity", "run: request=" + request.toString());
                try {
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    String res = ParseJSON.parseJSONWithJSONObject(responseData);
                    if ("success".equals(res)) {
                        pass();
                    }
                    Log.i("LoginActivity", "verify: " + responseData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void rememberAccount() {
        User user = new User();
        user.setUid(userIdText.getText().toString());
        user.setPass(userPassText.getText().toString());
        user.setTime(System.currentTimeMillis());
        for (User item :
                users) {//存在于数据库，但是密码不一致，则按新的存
            if (user.getUid().equals(item.getUid())) {
                if (user.getPass().equals(item.getPass()))
                    return;
                item.setPass(user.getPass());
                item.setTime(user.getTime());
                item.save();
                return;
            }
        }
        user.save();//新用户
    }

    class AllUsersAdapter extends RecyclerView.Adapter<ViewHolder> {

        private List<User> users;
        private OnItemClickListener onItemClickListener;
        private OnInnerItemClickListener onInnerItemClickListener;

        AllUsersAdapter(List<User> list) {
            users = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_user_item, parent, false);
            ViewHolder holder = new ViewHolder(view);
            holder.delBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onInnerItemClickListener.onInnerClick(view);
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onClick(view);
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            User user = users.get(position);
            holder.uidText.setText(user.getUid());
            holder.itemView.setTag(user);
            holder.delBtn.setTag(user);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        void setOnInnerItemClickListener(OnInnerItemClickListener onInnerItemClickListener) {
            this.onInnerItemClickListener = onInnerItemClickListener;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView uidText;
        ImageView headIcon;
        Button delBtn;

        ViewHolder(View itemView) {
            super(itemView);
            uidText = (TextView) itemView.findViewById(R.id.all_user_id);
            headIcon = (ImageView) itemView.findViewById(R.id.all_user_img);
            delBtn = (Button) itemView.findViewById(R.id.all_user_del);
        }

    }

    interface OnItemClickListener {
        void onClick(View view);
    }

    interface OnInnerItemClickListener {
        void onInnerClick(View view);
    }
}
