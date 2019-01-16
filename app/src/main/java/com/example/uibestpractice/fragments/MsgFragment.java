package com.example.uibestpractice.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.uibestpractice.MessageActivity;
import com.example.uibestpractice.R;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.List;

public class MsgFragment extends Fragment {

    private static final String TAG = "MsgFragment";
    private List<Msg> msgs = new ArrayList<>();
    private MsgAdapter msgAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.msg_fragment, container, false);
        Log.i(TAG, "onCreateView: msgInit");
        initMsgs();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.msg_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        msgAdapter = new MsgAdapter(msgs);
        msgAdapter.setmItemClickListener(new OnItemClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getContext(), MessageActivity.class);
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view) {

            }
        });
        recyclerView.setAdapter(msgAdapter);
        return view;
    }

    private void initMsgs() {
        for (int i = 1; i <= 10; i++) {
            Msg msg = new Msg();
            msg.setHeadIcon(R.drawable.head_icon);
            msg.setName("name-" + i);
            msg.setContent("content-" + i);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
            msg.setTime(calendar);
            msgs.add(msg);
        }
    }

    class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.ViewHolder> {

        private List<Msg> mMsgList;
        private OnItemClickListener mItemClickListener;

        MsgAdapter(List<Msg> msgList) {
            mMsgList = msgList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mItemClickListener.onClick(view);
                }
            });
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mItemClickListener.onLongClick(view);
                    return true;
                }
            });
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Msg msg = mMsgList.get(position);
            holder.headIcon.setImageResource(msg.getHeadIcon());
            holder.nameText.setText(msg.getName());
            holder.contentText.setText(msg.getContent());
            holder.timeText.setText(msg.getTime());
        }

        @Override
        public int getItemCount() {
            return mMsgList.size();
        }

        void setmItemClickListener(OnItemClickListener mItemClickListener) {
            this.mItemClickListener = mItemClickListener;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private ImageView headIcon;
            private TextView nameText;
            private TextView contentText;
            private TextView timeText;

            ViewHolder(View itemView) {
                super(itemView);
                headIcon = (ImageView) itemView.findViewById(R.id.head_icon);
                nameText = (TextView) itemView.findViewById(R.id.name_text);
                contentText = (TextView) itemView.findViewById(R.id.content_text);
                timeText = (TextView) itemView.findViewById(R.id.time_text);
            }
        }
    }

    interface OnItemClickListener {
        void onClick(View view);

        void onLongClick(View view);
    }

    class Msg {
        private int headIcon;
        private String name;
        private String content;
        private int year;
        private int month;
        private int date;
        private int hour;
        private int minute;
        private int second;


        int getHeadIcon() {
            return headIcon;
        }

        void setHeadIcon(int headIcon) {
            this.headIcon = headIcon;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getContent() {
            return content;
        }

        void setContent(String content) {
            this.content = content;
        }

        public String getTime() {
            return String.valueOf(hour) + ":" + String.valueOf(minute) + ":" + String.valueOf(second);
        }

        public void setTime(Calendar cal) {
            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH);
            date = cal.get(Calendar.DATE);
            hour = cal.get(Calendar.HOUR);
            minute = cal.get(Calendar.MINUTE);
            second = cal.get(Calendar.SECOND);
        }
    }
}
