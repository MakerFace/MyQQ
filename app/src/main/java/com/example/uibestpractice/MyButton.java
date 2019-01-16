package com.example.uibestpractice;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

public class MyButton {

    private int blueImage;
    private int grayImage;
    private View button;
    private TextView buttonName;

    public MyButton(View btn, TextView btnName, int bi, int gi) {
        blueImage = bi;
        grayImage = gi;
        button = btn;
        buttonName = btnName;
    }

    public void show(String tag) {
        if (button.getTag().toString().equals(tag)) {
            button.setBackgroundResource(blueImage);
            if (buttonName != null)
                buttonName.setTextColor(Color.BLUE);
        } else {
            button.setBackgroundResource(grayImage);
            if (buttonName != null)
                buttonName.setTextColor(Color.GRAY);
        }
    }
}
