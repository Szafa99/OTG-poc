package com.sander.otg_poc.model;

import java.util.Timer;
import java.util.TimerTask;

import static com.sander.otg_poc.model.TimerState.*;

public abstract class PreciseTimer {

    private TimerState state= FINISHED;
    private long timeSet;
    private long timeLeft;
    private long interval;
    private Timer timer;


    PreciseTimer(long duration, long interval) {
        this.timeLeft = duration;
        this.interval = interval;
        this.timeSet = duration;
        this.timer = new Timer();
    }

    abstract void onFinish();

    abstract void onTick(long millisLeft);

    public void start() {
        if(isRunning()) return;

        timer.scheduleAtFixedRate(new PreciseTimerTask(), 0, interval);
        if (state.equals(STOPPED))
            state = RESUMED;
        else
            state = RUNNING;
    }

    public void stop() {
        timer.cancel();
        timer.purge();// remove all canceled tasks
        state = STOPPED;
        timer=new Timer();
    }

    public void resetTimer() {
        this.stop();
        timeLeft = timeSet;
        state=FINISHED;
    }

    public TimerState getState() {
        return state;
    }

    protected void setTimeSet(long timeSet) {
        this.timeSet = timeSet;
        this.timeLeft = timeSet;
    }
    public long getTimeSet(){return this.timeSet;}

    public boolean isRunning(){
        return state.equals(RESUMED) || state.equals(RUNNING);}

    private class PreciseTimerTask extends TimerTask{
        @Override
        public void run() {
            timeLeft -= interval;
            onTick(timeLeft);
            if (timeLeft <= 0) {
                onFinish();
            }
        }
    }

}
