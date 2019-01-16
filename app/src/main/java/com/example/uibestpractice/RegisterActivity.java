package com.example.uibestpractice;

import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.uibestpractice.usersBean.User;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.uibestpractice.utils.ParseJSON.parseJSONWithJSONObject;

public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    private EditText account;
    private EditText pass;
    private EditText rePass;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);
        account = (EditText) findViewById(R.id.account_edit);
        pass = (EditText) findViewById(R.id.pass_edit);
        rePass = (EditText) findViewById(R.id.re_pass_edit);
        Button regBtn = (Button) findViewById(R.id.reg_btn);
        regBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (account.getText().toString().equals("")
                || account.getText().toString().length() < 7
                || account.getText().toString().length() > 10) {
            Toast.makeText(this,
                    "请输入输入大于7位数小于10位数的数字账号",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (pass.getText().toString().equals("")) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            pass.setText("");
            rePass.setText("");
            return;
        }
        if (!pass.getText().toString().equals(rePass.getText().toString())) {
            Toast.makeText(this, "密码不一致", Toast.LENGTH_SHORT).show();
            pass.setText("");
            rePass.setText("");
            return;
        }
        if (pass.getText().toString().length() < 8) {
            Toast.makeText(this, "密码太短了", Toast.LENGTH_SHORT).show();
            pass.setText("");
            rePass.setText("");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://10.102.229.236:8080/register?name="
                                + account.getText().toString()
                                + "&pass=" + pass.getText().toString())
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Log.i("RegisterActivity", "run: " + responseData);
                    String res = parseJSONWithJSONObject(responseData);
                    if ("success".equals(res)) {
                        User user = new User();
                        user.setId(Integer.parseInt(account.getText().toString()));
                        user.setPass(pass.getText().toString());
                        user.setTime(System.currentTimeMillis());
                        user.save();
                        Looper.prepare();
                        Toast.makeText(RegisterActivity.this, "register success", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                        ActivityCollector.finishActivity(RegisterActivity.class);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
