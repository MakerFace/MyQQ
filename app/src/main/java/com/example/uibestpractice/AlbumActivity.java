package com.example.uibestpractice;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class AlbumActivity extends BaseActivity {

    private static final String TAG = "AlbumActivity";
    private List<AlbumInfo> albumInfoList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        RecyclerView albumRecyclerView = (RecyclerView) findViewById(R.id.album_recycler_view);
        albumRecyclerView.setLayoutManager(new GridLayoutManager(this,3));
        initAlbum();
        albumRecyclerView.setAdapter(new AlbumAdapter(albumInfoList));
        Log.i(TAG, "onCreate: Album Activity");
    }

    private void initAlbum() {
        for (int i = 0; i < 30; i++) {
            albumInfoList.add(new AlbumInfo("" + i,R.drawable.gift));
        }
    }

    private class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {

        private List<AlbumInfo> mAlbumInfoList;
        private Context mContext;

        AlbumAdapter(List<AlbumInfo> list) {
            mAlbumInfoList = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if(mContext == null){
                mContext = parent.getContext();
            }
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            AlbumInfo albumInfo = mAlbumInfoList.get(position);
            holder.txt.setText(albumInfo.getImgName());
            Glide.with(mContext).load(albumInfo.getImgID()).into(holder.img);
            Log.i(TAG, "onBindViewHolder: " + albumInfo.getImgName());
        }

        @Override
        public int getItemCount() {
            return mAlbumInfoList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView img;
            private TextView txt;

            ViewHolder(View itemView) {
                super(itemView);
                img = (ImageView) itemView.findViewById(R.id.album_img);
                txt = (TextView) itemView.findViewById(R.id.album_txt);
            }
        }
    }

    private class AlbumInfo {
        private String imgName;
        private int imgID;

        AlbumInfo(String name, int imgID) {
            this.imgName = name;
            this.imgID = imgID;
        }

        String getImgName() {
            return imgName;
        }

        int getImgID() {
            return imgID;
        }
    }

}
