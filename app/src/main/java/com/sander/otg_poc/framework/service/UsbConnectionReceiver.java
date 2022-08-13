package com.sander.otg_poc.framework.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.widget.Toast;
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

    public void startSerialServiceManually(){
        try {
            serialServiceConnection.getService().startConnection();
        } catch (IOException e) {}
    }

    public void startSerialService(Context context){
        usbIntent = new Intent(context, SerialService.class);
        context.bindService(usbIntent,serialServiceConnection,Context.BIND_AUTO_CREATE);
        context.startService(usbIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()){
            case ACTION_USB_DEVICE_ATTACHED:
                startSerialService(context.getApplicationContext());
                break;
            case ACTION_USB_DEVICE_DETACHED:
                if (usbIntent!=null) {
                    context.getApplicationContext().stopService(usbIntent);
                    Toast.makeText(context, "Device disconnected", Toast.LENGTH_LONG).show();
                }
                break;
            case SerialService.ACTION_USB_PERMISSION:
                synchronized (this){
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        try {
                              serialServiceConnection.getService().startConnection();
                        } catch (IOException e) {}
                    }else
                        Toast.makeText(context,"USB prem denied",Toast.LENGTH_LONG).show();
                }
                break;
        }

    }

}