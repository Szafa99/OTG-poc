package com.sander.otg_poc.controller;

import android.content.IntentFilter;
import android.os.Build;
import androidx.annotation.RequiresApi;

import java.util.concurrent.Flow;

@RequiresApi(api = Build.VERSION_CODES.R)
public class SerialDispatcher implements Flow.Subscriber<String>{

    private SerialController serialController;

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
    }


    // ONLY INCOMING SERIAL MESSAGES
    @Override
    public void onNext(String s) {
        String[] split = s.split(":");
        if (split.length!=3) return;
        String method = split[0];
        String command = split[1];
        String body = split[2];
        if (!"POST".equals(method))return;
        switch (command) {
            case "CURRENT_TEMP":
                serialController.postCurrentTemp(body);break;
            case "MACHINE_STATE":
                serialController.postMachineState(body);break;
        }
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }
}
