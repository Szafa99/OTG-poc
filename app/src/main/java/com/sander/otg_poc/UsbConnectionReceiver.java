package com.sander.otg_poc;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class UsbConnectionReceiver extends BroadcastReceiver {

    public UsbConnectionReceiver(){}

    public  UsbConnectionReceiver(Activity activity){
        this.activity = new WeakReference<>(activity);
    }

    WeakReference<Activity> activity;
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(activity.get(), intent.getType(),Toast.LENGTH_SHORT);

    }
}