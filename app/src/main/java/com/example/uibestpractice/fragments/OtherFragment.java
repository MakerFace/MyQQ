package com.example.uibestpractice.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.uibestpractice.R;
import com.example.uibestpractice.utils.ContentUriUtil;

import static android.app.Activity.RESULT_OK;

public class OtherFragment extends Fragment implements View.OnClickListener {

    private static final int CHOOSE_FILES = 1;
    private Button sendFileBtn;
    private OnResultListener listener;
    private String filePath;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.other_fragment, container, false);
        sendFileBtn = (Button) view.findViewById(R.id.send_file);
        sendFileBtn.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send_file:
                sendFile();
                break;
        }
    }

    public void setListener(OnResultListener listener){
        this.listener = listener;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case CHOOSE_FILES:
                if(resultCode == RESULT_OK){
                    if (Build.VERSION.SDK_INT >= 19) {
                        handleFileOnKitKat(data);
                    } else {
                        handleFileBeforeKitKat(data);
                    }
                    listener.onResult(filePath);
                }
                break;
        }
    }

    private void handleFileBeforeKitKat(Intent data) {
        filePath = null;
        filePath = getFilePath(data.getData());
    }

    private void handleFileOnKitKat(Intent data) {
        Uri uri = data.getData();
        filePath = getFilePath(uri);
    }

    private String getFilePath(Uri uri) {
        return ContentUriUtil.getPath(getContext(),uri);
    }

    private void sendFile() {
        if (ContextCompat.checkSelfPermission(
                getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    CHOOSE_FILES);
        } else {
            openFolder();
        }
//        Toast.makeText(this.getActivity(), "Select File", Toast.LENGTH_SHORT).show();
    }

    private void openFolder(){
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("*/*");
        startActivityForResult(intent, CHOOSE_FILES);
    }

    public interface OnResultListener {
        void onResult(String filePath);
    }
}
