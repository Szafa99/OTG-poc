package com.sander.otg_poc.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import com.sander.otg_poc.MainActivity;

import java.io.IOException;

import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_ATTACHED;
import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED;
import static com.sander.otg_poc.service.SerialService.ACTION_USB_PERMISSION;

public class UsbConnectionReceiver extends BroadcastReceiver {
    private Intent usbIntent;

    public SerialServiceConnection getSerialServiceConnection() {
        return serialServiceConnection;
    }

    private SerialServiceConnection serialServiceConnection = new SerialServiceConnection();


    public void startSerialService(Context context) {
        usbIntent = new Intent(context, SerialService.class);
        context.bindService(usbIntent, serialServiceConnection, Context.BIND_AUTO_CREATE);
        context.startService(usbIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case ACTION_USB_DEVICE_ATTACHED:
                startSerialService(context);
                break;
            case ACTION_USB_DEVICE_DETACHED:
                context.stopService(usbIntent);
                break;
            case ACTION_USB_PERMISSION:
                synchronized (this) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                serialServiceConnection.getService().startConnection();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else
                        Toast.makeText(context, "USB permission denied", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

}
