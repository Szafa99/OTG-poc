package com.sander.otg_poc.framework.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.navigation.ui.NavigationUI;

import java.io.IOException;

import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_ATTACHED;
import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED;

public class UsbConnectionReceiver extends BroadcastReceiver {

    private static UsbConnectionReceiver INSTANCE= null;
    private Intent usbIntent;

    public static UsbConnectionReceiver getInstance(){
        if (INSTANCE==null)
            INSTANCE = new UsbConnectionReceiver();
        return INSTANCE;
    }

    private UsbConnectionReceiver(){}

    public SerialServiceConnection getSerialServiceConnection() {
        return serialServiceConnection;
    }

    private SerialServiceConnection serialServiceConnection = new SerialServiceConnection();


    public void startSerialService(Context context){
        usbIntent = new Intent(context, SerialService.class);
        usbIntent.setAction(SerialService.INIT_CONNECTION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(usbIntent);
            context.bindService(usbIntent,serialServiceConnection,Context.BIND_AUTO_CREATE);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()){
            case ACTION_USB_DEVICE_ATTACHED:
                startSerialService(context.getApplicationContext());
                break;
            case ACTION_USB_DEVICE_DETACHED:
                if (usbIntent!=null) {
                    context.getApplicationContext().stopService(usbIntent);
                    context.getApplicationContext().unbindService(serialServiceConnection);
                    Toast.makeText(context, "Device disconnected", Toast.LENGTH_LONG).show();
                }
                break;
            case SerialService.ACTION_USB_PERMISSION:
                synchronized (this){
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        Intent usbPremIntent = new Intent(context, SerialService.class);
                        usbPremIntent.setAction(UsbManager.EXTRA_PERMISSION_GRANTED);
                        context.startForegroundService(usbPremIntent);
                    }else
                        Toast.makeText(context,"USB prem denied",Toast.LENGTH_LONG).show();
                }
                break;
        }

    }

}