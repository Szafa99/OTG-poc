package com.sander.otg_poc.model;

import com.sander.otg_poc.utils.EventHandler;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;


public class MinuteCountDownTimer extends PreciseTimer {
    public final static long COUNT_DOWN_INTERVAL = 50;// millis
    private long startTime =0;
    private EventHandler onTickHandler;
    private EventHandler onFinishHandler;

    public MinuteCountDownTimer(int minutes, int seconds, EventHandler eventHandler) {
        super(MINUTES.toMillis(minutes)+SECONDS.toMillis(seconds),COUNT_DOWN_INTERVAL);
        this.onTickHandler = eventHandler;
        this.onTickHandler.emitEvent(MINUTES.toMillis(minutes) + SECONDS.toMillis(seconds));
    }


    public void updateTimer(long minutes, long seconds){
        long millis = MINUTES.toMillis(minutes)+ SECONDS.toMillis(seconds);
        setTimeSet(millis);
    }

    @Override
    public void onTick(long l) {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - startTime >= 1000L) {
            onTickHandler.emitEvent(l);
            startTime=System.currentTimeMillis();;
        }
    }

    @Override
    public void onFinish() {
        if (onFinishHandler!=null)
            onFinishHandler.emitEvent(null);
    }

    public void setOnFinishHandler(EventHandler onFinishHandler) {
        this.onFinishHandler = onFinishHandler;
    }
}


