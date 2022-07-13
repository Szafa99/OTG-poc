package com.sander.otg_poc.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class SerialServiceConnection implements ServiceConnection {

    public SerialService getService() {
        return service;
    }

    public Boolean getConnected() {
        return isConnected;
    }

    private SerialService service = null;
    private Boolean isConnected = false;

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        SerialService.LocalBinder binder = (SerialService.LocalBinder) iBinder;
        service = binder.getService();
        isConnected = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        isConnected = false;
    }
}
