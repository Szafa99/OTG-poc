package com.sander.otg_poc.framework.service;

import android.util.Log;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.sander.otg_poc.utils.EventHandler;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SerialListener implements SerialInputOutputManager.Listener {

    private List<EventHandler> subscribers = new ArrayList();

    void subscribe(EventHandler eventHandler){
        subscribers.add(eventHandler);
    }

    void unSubscribe(EventHandler eventHandler){
        subscribers.remove(eventHandler);
    }

    private String currentMsg = "";


    @Override
    public void onNewData(byte[] data) {
        String incoming = new String(data, StandardCharsets.US_ASCII);
        currentMsg += incoming;
        if(currentMsg.length() > 0 && currentMsg.contains("\n") == true){
            String thisLine = currentMsg.substring(0, currentMsg.indexOf("\n")).trim();
            currentMsg = currentMsg.substring(currentMsg.indexOf("\n") + 1);

            for (EventHandler subscriber : subscribers)
                subscriber.emitEvent(thisLine);
        }
    }

    @Override
    public void onRunError(Exception e) {
        Log.e("SerialListener", "Error receiving data");
    }
}
