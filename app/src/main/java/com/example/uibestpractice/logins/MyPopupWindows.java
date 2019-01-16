package com.example.uibestpractice.logins;

import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.PopupWindow;

import com.example.uibestpractice.R;

public class MyPopupWindows extends PopupWindow {

    private Button changeButton;
    private OnClickListener onClickListener;

    public MyPopupWindows(RecyclerView recyclerView, int width, int height) {
        super(recyclerView, width, height);
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.setBackgroundDrawable(new BitmapDrawable());
    }

    public void setChangeButton(Button btn){
        changeButton = btn;
    }

    public void setOnClickListener(OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }
    @Override
    public void dismiss() {
        super.dismiss();
        if(changeButton!=null){
            changeButton.setBackgroundResource(R.drawable.push);
            onClickListener.onClick();
        }
    }

    public interface OnClickListener{
        void onClick();
    }
}
