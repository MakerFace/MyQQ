package com.example.uibestpractice;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uibestpractice.fragments.ActFragment;
import com.example.uibestpractice.fragments.ContactFragment;
import com.example.uibestpractice.fragments.MapFragment;
import com.example.uibestpractice.fragments.MsgFragment;
import com.example.uibestpractice.usersBean.User;

import org.litepal.crud.DataSupport;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FragmentTabActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "FragmentTabActivity";

    public static String APP_PERSONAL_DIRECTORY = null;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView navTitleText;
    private String[] btnTitles = new String[]{"msg", "contact", "activity", "location"};
    private List<Fragment> fragments = new ArrayList<>();
    private List<MyButton> buttons = new ArrayList<>();
    private ImageView headIconImg;
    private User loginedUser;
    private String account;
    private ImageView navigationHeadView;
    private boolean isLoaded = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_tab);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headView = navigationView.inflateHeaderView(R.layout.head);
        ImageView headIcon = (ImageView) headView.findViewById(R.id.person);
        navTitleText = (TextView) findViewById(R.id.nav_title);
        headIconImg = (ImageView) findViewById(R.id.head_icon_btn);
        navTitleText.setText(btnTitles[0]);
        headIconImg.setOnClickListener(this);
        headIcon.setOnClickListener(this);
        checkLoginState();
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: loadIcon");
        SharedPreferences preferences = getSharedPreferences("data", MODE_PRIVATE);
        account = preferences.getString("account", "");
        APP_PERSONAL_DIRECTORY = new File(getExternalFilesDir(null), account).getPath();
        Log.i(TAG, "onResume: app personal path = " + APP_PERSONAL_DIRECTORY);
        loginedUser = DataSupport.where("uid is '" + account + "'").findFirst(User.class);
        Log.i(TAG, "onResume: account=" + account);
        String modify = preferences.getString("modify", "");
        if ("".equals(modify) || "true".equals(modify) || isLoaded) {
            loadNewHeadIcon();
            isLoaded = false;
        }
    }

    private void loadNewHeadIcon() {
        if (loginedUser == null)
            return;
        String headIcon = loginedUser.getHeadIcon();
        File headFile = new File(getExternalFilesDir(null), account + "/headIcon/" + headIcon);
        if ("".equals(headIcon) || headIcon == null || !headFile.exists()) {
            headIcon = "headIcon.png";
            headFile = new File(getExternalFilesDir(null), account + "/headIcon/" + headIcon);
            Toast.makeText(this, headFile.getPath(), Toast.LENGTH_SHORT).show();
            if (!headFile.exists()) {
                try {
                    headIconImg.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                    headIconImg.layout(0, 0, headIconImg.getMeasuredWidth(), headIconImg.getMeasuredHeight());
                    headIconImg.buildDrawingCache();
                    Bitmap head = headIconImg.getDrawingCache();
                    createNewHeadFile(headFile, head);
                } catch (IOException e) {
                    e.printStackTrace();
                    headFile.delete();
                }
            }
        } else {
            Bitmap bitmap = BitmapFactory.decodeFile(headFile.getPath());
            headIconImg.setImageBitmap(bitmap);
            navigationHeadView.setImageBitmap(bitmap);
        }
        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        editor.putString("modify", "false");
        editor.apply();
    }

    private void createNewHeadFile(File file, Bitmap bitmap) throws IOException {
        file.createNewFile();
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        bos.flush();
        bos.close();
    }

    public void checkLoginState() {
        SharedPreferences preferences = getSharedPreferences("data", MODE_PRIVATE);
        account = preferences.getString("account", "");
        if ("".equals(account)) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.head_icon_btn:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.person:
                Toast.makeText(this, "Set head icon", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, HeadIconSetActivity.class);
                startActivity(intent);
                break;
            default:
                showFragment(view.getTag().toString());
        }
    }

    private void init() {
        initButton();
        initFragment();
        initNavigation();
    }

    private void initNavigation() {
        navigationHeadView = (ImageView) navigationView.getHeaderView(
                navigationView.getHeaderCount() - 1)
                .findViewById(R.id.person);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Log.i(TAG, "onNavigationItemSelected: " + item.getItemId() + "?" + R.id.exit);
                switch (item.getItemId()) {
                    case R.id.exit:
                        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                        editor.putString("account", "");
                        editor.apply();
                        Intent intent = new Intent("com.example.uibestpractice.FORCE_OFFLINE");
                        sendBroadcast(intent);
                        break;
                    case R.id.album:
                        startAlbumActivity();
                        break;
                }
                return true;
            }
        });
    }

    private void startAlbumActivity() {
        Intent intent = new Intent(this, AlbumActivity.class);
        startActivity(intent);
    }

    private void initButton() {
        BottomNavigationView navigationView = findViewById(R.id.bottom_navigation);
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int res = 0;
                switch (menuItem.getItemId()) {
                    case R.id.navigation_msg_btn:
                        res = 0;
                        break;
                    case R.id.navigation_contact_btn:
                        res = 1;
                        break;
                    case R.id.navigation_activity_btn:
                        res = 2;
                        break;
                    case R.id.navigation_map_btn:
                        res = 3;
                        break;
                }
                showFragment(btnTitles[res]);
                return true;
            }
        });
//        BottomNavigationItemView msgBtn = findViewById(R.id.navigation_msg_btn);
////        TextView msgTxt = (TextView) findViewById(R.id.msg_name);
//        BottomNavigationItemView cotBtn = findViewById(R.id.navigation_contact_btn);
////        TextView cotTxt = (TextView) findViewById(R.id.cot_name);
//        BottomNavigationItemView actBtn = findViewById(R.id.navigation_activity_btn);
////        TextView actTxt = (TextView) findViewById(R.id.act_name);
//        BottomNavigationItemView mapBtn = findViewById(R.id.navigation_map_btn);
////        TextView mapTxt = (TextView) findViewById(R.id.map_name);
//        msgBtn.setOnClickListener(this);
//        cotBtn.setOnClickListener(this);
//        actBtn.setOnClickListener(this);
//        mapBtn.setOnClickListener(this);
//        buttons.add(new MyButton(msgBtn, msgTxt, R.drawable.msg_blue, R.drawable.msg_gray));
//        buttons.add(new MyButton(cotBtn, cotTxt, R.drawable.contact_blue, R.drawable.contact_gray));
//        buttons.add(new MyButton(actBtn, actTxt, R.drawable.activity_blue, R.drawable.activity_gray));
//        buttons.add(new MyButton(mapBtn, mapTxt, R.drawable.map_blue, R.drawable.map_gray));

    }

    @SuppressLint("ResourceType")
    private void initFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        MsgFragment msgFragment = new MsgFragment();
        ContactFragment contactFragment = new ContactFragment();
        ActFragment actFragment = new ActFragment();
        MapFragment mapFragment = new MapFragment();

        transaction.add(R.id.contextFrameLayout, msgFragment, btnTitles[0]);
        transaction.add(R.id.contextFrameLayout, contactFragment, btnTitles[1]);
        transaction.add(R.id.contextFrameLayout, actFragment, btnTitles[2]);
        transaction.add(R.id.contextFrameLayout, mapFragment, btnTitles[3]);

        fragments.add(msgFragment);
        fragments.add(contactFragment);
        fragments.add(actFragment);
        fragments.add(mapFragment);

        transaction.commit();

        showFragment(btnTitles[0]);
    }

    private void showFragment(String btnTitle) {
        Log.i(TAG, "showFragment: btnTitle" + btnTitle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        for (Fragment fragment : fragments) {
            if (fragment.getTag().equals(btnTitle)) {
                transaction.show(fragment);
            } else {
                transaction.hide(fragment);
            }
        }
        transaction.commit();

//        for (MyButton button : buttons) {
//            button.show(btnTitle);
//            navTitleText.setText(btnTitle);
//        }
    }
}
