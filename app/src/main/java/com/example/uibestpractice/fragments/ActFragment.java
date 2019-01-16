package com.example.uibestpractice.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.uibestpractice.R;

import java.io.IOException;


public class ActFragment extends Fragment implements View.OnClickListener {
    private VideoView videoView;
    private Button audio;
    private Button video;
    private Button play;
    private Button pause;
    private Button stop;
    private MediaPlayer media = new MediaPlayer();
    private boolean isMedia = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_fragment, container, false);
        videoView = (VideoView) view.findViewById(R.id.play_video);
        audio = (Button) view.findViewById(R.id.select_audio);
        video = (Button) view.findViewById(R.id.select_video);
        play = (Button) view.findViewById(R.id.play_media);
        pause = (Button) view.findViewById(R.id.pause_media);
        stop = (Button) view.findViewById(R.id.stop_media);
        audio.setOnClickListener(this);
        video.setOnClickListener(this);
        play.setOnClickListener(this);
        pause.setOnClickListener(this);
        stop.setOnClickListener(this);
        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                selectAudio();
                break;
            case 2:
                selectVideo();
                break;
        }
    }

    private boolean checkPermission(int requestCode) {
        if (ContextCompat.checkSelfPermission(
                getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.select_audio:
                if (checkPermission(1))
                    selectAudio();
                break;
            case R.id.select_video:
                if (checkPermission(2))
                    selectVideo();
                break;
            case R.id.play_media:
                playMedia();
                break;
            case R.id.pause_media:
                pauseMedia();
                break;
            case R.id.stop_media:
                stopMedia();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String path = null;
        if (Build.VERSION.SDK_INT >= 19) {
            path = handleMediaOnKitKat(data);
        } else {
            path = handleMediaBeforeKitKat(data);
        }
        if (path == null || "".equals(path)) {
            Toast.makeText(getContext(), "path is null", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (requestCode) {
            case 1:
                try {
                    media.reset();
                    media.setDataSource(path);
                    media.prepare();
                    isMedia = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                if (media.isPlaying()) {
                    media.stop();
                }
                videoView.setVideoPath(path);
                isMedia = false;
                break;
        }
    }

    @TargetApi(19)
    private String handleMediaOnKitKat(Intent data) {
        String imgPath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(getContext(), uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            assert uri != null;
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imgPath = getPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.media.downloads".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imgPath = getPath(contentUri, null);
            }
        } else {
            assert uri != null;
            if ("content".equalsIgnoreCase(uri.getScheme())) {
                imgPath = getPath(uri, null);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                imgPath = uri.getPath();
            }
        }
        return imgPath;
    }

    private String handleMediaBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        return getPath(uri, null);
    }

    private String getPath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContext().getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void selectAudio() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("audio/*");
        startActivityForResult(intent, 1);
    }

    private void selectVideo() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("video/*");
        startActivityForResult(intent, 2);
    }

    private void playMedia() {
        if (isMedia) {
            media.start();
        } else {
            videoView.start();
        }
    }

    private void pauseMedia() {
        if (isMedia) {
            media.pause();
        } else {
            videoView.pause();
        }
    }

    private void stopMedia() {
        if (isMedia) {
            media.stop();
        } else {
            videoView.stopPlayback();
        }
    }
}
