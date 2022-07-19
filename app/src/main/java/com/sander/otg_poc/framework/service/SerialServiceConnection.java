package com.sander.otg_poc.framework.service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import com.sander.otg_poc.framework.controller.SerialDispatcher;
import com.sander.otg_poc.utils.EventHandler;

import java.util.ArrayList;
import java.util.List;

public class SerialServiceConnection implements ServiceConnection {

    private List<EventHandler> pendingEventHandlers = new ArrayList<>();

    public SerialService getService() {
        return service;
    }

    public Boolean getConnected() {
        return isConnected;
    }

    private SerialService service = null;
    private Boolean isConnected = false;

    public void subscribeToSerial(EventHandler eventHandler){
        if (service==null)
            pendingEventHandlers.add(eventHandler);
        else
            service.serialListener.subscribe(eventHandler);
    }

    public void unSubscribeFromSerial(EventHandler eventHandler){
        if (service==null)
            pendingEventHandlers.remove(eventHandler);
        else
            service.serialListener.unSubscribe(eventHandler);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        SerialService.LocalBinder binder = (SerialService.LocalBinder) iBinder;
        service = binder.getService();
        for (EventHandler handler : pendingEventHandlers)
            service.serialListener.subscribe(handler);
        pendingEventHandlers.clear();
        service.serialListener.subscribe(new SerialDispatcher(service.getApplicationContext()));
        isConnected = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        isConnected = false;
    }
}
