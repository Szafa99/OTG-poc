package com.sander.otg_poc.model;

import android.os.CountDownTimer;
import com.sander.otg_poc.presenter.EventHandler;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

public class MinuteCountDownTimer extends CountDownTimer {
    private long millisLeft;

    public boolean isRunning() {
        return running;
    }

    private boolean running = false;

    private final static long countDownInterval = SECONDS.toMillis(1 );

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    private int minutes;
    private int seconds;

    public EventHandler getOnTickHandler() {
        return onTickHandler;
    }

    public void setOnTickHandler(EventHandler onTickHandler) {
        this.onTickHandler = onTickHandler;
    }

    public EventHandler getOnFinishHandler() {
        return onFinishHandler;
    }

    public void setOnFinishHandler(EventHandler onFinishHandler) {
        this.onFinishHandler = onFinishHandler;
    }

    private EventHandler onTickHandler;
    private EventHandler onFinishHandler;

    public long getMillisLeft() {
        return millisLeft;
    }

    public MinuteCountDownTimer(int minutes, int seconds, EventHandler eventHandler) {
        super(MINUTES.toMillis(minutes)+ SECONDS.toMillis(seconds), countDownInterval);
        this.minutes=minutes;
        this.seconds=seconds;
        this.onTickHandler = eventHandler;
        this.onTickHandler.emitEvent( MINUTES.toMillis(minutes)+ SECONDS.toMillis(seconds) );
    }


    @Override
    public void onTick(long l) {
        running=true;
        millisLeft = l;
        onTickHandler.emitEvent(l);
    }

    @Override
    public void onFinish() {
        running=false;
        onFinishHandler.emitEvent(null);
    }



}
