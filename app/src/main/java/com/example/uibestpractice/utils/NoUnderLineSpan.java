package com.example.uibestpractice.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.view.View;

import com.example.uibestpractice.WebViewActivity;

public class NoUnderLineSpan extends URLSpan {
    private Context mContext;
    private String url;
    public NoUnderLineSpan(Context context, String src) {
        super(src);
        mContext = context;
        url = src;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
        ds.setColor(Color.parseColor("#00B2EE"));
    }

    @Override
    public void onClick(View widget) {
        Intent intent = new Intent(mContext, WebViewActivity.class);
        intent.putExtra(WebViewActivity.WEB_URL, url);
        mContext.startActivity(intent);
    }
}