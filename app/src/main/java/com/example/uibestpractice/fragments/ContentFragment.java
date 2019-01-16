package com.example.uibestpractice.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.uibestpractice.R;

public class ContentFragment extends Fragment {

    private TextView textView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_fragment, container, false);
        textView = (TextView) view.findViewById(R.id.content_text);
        Bundle bundle = getArguments();
        if (bundle != null) {
            String textValue = bundle.getString("textValue");
            textView.setText(textValue);
        }
        return view;
    }
}
