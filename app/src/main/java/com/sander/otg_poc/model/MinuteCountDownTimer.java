package com.sander.otg_poc.model;

import android.os.CountDownTimer;
import com.sander.otg_poc.utils.EventHandler;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

public class MinuteCountDownTimer {

    private int minutes;
    private int seconds;

    private long millisLeft;

    public MinuteCountDownTimerState getState() {
        return state;
    }

    private MinuteCountDownTimerState state = MinuteCountDownTimerState.FINISHED;
    public final static long COUNT_DOWN_INTERVAL = SECONDS.toMillis(1);
    private Timer timer;

    public boolean isRunning() {
        return state.equals(MinuteCountDownTimerState.RUNNING) || state.equals(MinuteCountDownTimerState.RESUMED);
    }


    public int getMinutes() {
        return minutes;
    }

    public boolean resetTimer() {
        boolean wasRunning = isRunning();
        if (wasRunning) {
            state = MinuteCountDownTimerState.FINISHED;
            timer.cancel();
        }
        return wasRunning;
    }

    public void setMinutes(int minutes) {
        boolean wasRunning = resetTimer();
        this.minutes = minutes;
        timer = new Timer(minutes, seconds);

        if (wasRunning) {
            timer.start();
            state = MinuteCountDownTimerState.RUNNING;
        }
    }

    public void setSeconds(int seconds) {
        boolean wasRunning = resetTimer();
        this.seconds = seconds;
        timer = new Timer(minutes, seconds);

        if (wasRunning) {
            timer.start();
            state = MinuteCountDownTimerState.RUNNING;
        }
    }


    public int getSeconds() {
        return seconds;
    }


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
        this.timer = new Timer(minutes, seconds);
        this.minutes = minutes;
        this.seconds = seconds;
        this.onTickHandler = eventHandler;
        this.onTickHandler.emitEvent(MINUTES.toMillis(minutes) + SECONDS.toMillis(seconds));
    }

    public MinuteCountDownTimer(int minutes, int seconds, EventHandler onTickHandler, EventHandler onFinishHandler) {
        this(minutes, seconds, onTickHandler);
        this.onFinishHandler = onFinishHandler;
    }

    public void start() {
        if (state.equals(MinuteCountDownTimerState.STOPED)) {
            timer = new Timer(millisLeft);
            timer.start();
            state = MinuteCountDownTimerState.RESUMED;

        } else if (state.equals(MinuteCountDownTimerState.RESUMED)) {
            timer = new Timer(minutes, seconds);
            timer.start();
            state = MinuteCountDownTimerState.RUNNING;
        } else if (state.equals(MinuteCountDownTimerState.FINISHED)) {
            timer.start();
            state = MinuteCountDownTimerState.RUNNING;
        }
    }


    public void stop() {
        //if was running
        if (resetTimer()) {
            state = MinuteCountDownTimerState.STOPED;
        }
    }

    private class Timer extends CountDownTimer {

        Timer(int minutes, int seconds) {
            this(MINUTES.toMillis(minutes) + SECONDS.toMillis(seconds));
        }

        Timer(long millis) {
            super(millis, COUNT_DOWN_INTERVAL);
        }

        @Override
        public void onTick(long l) {
            millisLeft = l;
            onTickHandler.emitEvent(l);
        }

        @Override
        public void onFinish() {
            if (state.equals(MinuteCountDownTimerState.RESUMED))
                timer = new Timer(minutes, seconds);

            state = MinuteCountDownTimerState.FINISHED;
            onFinishHandler.emitEvent(null);

        }


    }

    public enum MinuteCountDownTimerState{
        RUNNING("RUNNING"),STOPED("STOPED"),
        RESUMED("RESUMED"),FINISHED("FINISHED");

        MinuteCountDownTimerState(String state){
            this.state = state;
        }
        private String state;
    }

}


