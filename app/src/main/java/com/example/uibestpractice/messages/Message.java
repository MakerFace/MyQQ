package com.example.uibestpractice.messages;

import org.litepal.crud.DataSupport;

public class Message extends DataSupport {

    public static final int TYPE_RECEIVE = 1;
    public static final int TYPE_SEND = 2;
    public static final int TYPE_FILE = 4;

    private String content;
    private int type;
    private String sendId;
    private String receiveId;
    //仅file模式下使用
    private long progress;
    private boolean newTask = false;

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public String getSendId() {
        return sendId;
    }

    public void setSendId(String sendId) {
        this.sendId = sendId;
    }

    public String getReceiveId() {
        return receiveId;
    }

    public void setReceiveId(String receiveId) {
        this.receiveId = receiveId;
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public boolean isNewTask() {
        return newTask;
    }

    public void setNewTask(boolean newTask) {
        this.newTask = newTask;
    }
}
