package com.example.uibestpractice;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ActivityCollector {
    private static final String TAG = "ActivityCollector";
    private static List<Activity> activityList = new ArrayList<>();

    public static void addActivity(Activity activity){
        activityList.add(activity);
    }

    public static void removeActivity(Activity activity){
        activityList.remove(activity);
    }

    public static int getCount(){
        return activityList.size();
    }

    public static void finishActivity(Class name) {
        Log.i(TAG, "finishActivity: " + name);
        for (Activity item :
                activityList) {
            Log.i(TAG, "item: " + item.getClass().toString());
            if (item.getClass().equals(name)) {
                item.finish();
                break;
            }
        }
    }

    public static void finishAll(){
        for (Activity item :
                activityList) {
            item.finish();
        }
    }
}
