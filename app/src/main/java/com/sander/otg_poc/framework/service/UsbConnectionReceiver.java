package com.sander.otg_poc.framework.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.widget.Toast;

import java.io.IOException;

import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_ATTACHED;
import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED;

public class UsbConnectionReceiver extends BroadcastReceiver {
    private Intent usbIntent;

    public SerialServiceConnection getSerialServiceConnection() {
        return serialServiceConnection;
    }

    private SerialServiceConnection serialServiceConnection = new SerialServiceConnection();


    public void startSerialService(Context context){
        usbIntent = new Intent(context, SerialService.class);
        context.bindService(usbIntent,serialServiceConnection,Context.BIND_AUTO_CREATE);
        context.startService(usbIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()){
            case ACTION_USB_DEVICE_ATTACHED:
                try {
                    startSerialService(context);
                } catch (Exception e) {
//                  runOnUiThread(()->Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_LONG).show());
                  Toast.makeText(context,e.getMessage(),Toast.LENGTH_LONG).show();
                }
                break;
            case ACTION_USB_DEVICE_DETACHED:
//                context.unbindService(serialServiceConnection);
                context.stopService(usbIntent);
                break;
            case SerialService.ACTION_USB_PERMISSION:
                synchronized (this){
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        try {
                            SerialService serialService =(SerialService) context.getSystemService(SerialService.class.getName());
                            serialServiceConnection.getService().startConnection();
                        } catch (IOException e) {
                            Toast.makeText(context,e.getMessage(),Toast.LENGTH_LONG).show();
//                            runOnUiThread(()->Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_LONG).show());
                        }
                    }else
//                        runOnUiThread(()->Toast.makeText(MainActivity.this,"USB prem denied",Toast.LENGTH_LONG).show());
                        Toast.makeText(context,"USB prem denied",Toast.LENGTH_LONG).show();
                }break;
            default:break;
        }

    }

}