package com.sander.otg_poc.service;

import android.os.Handler;
import android.os.Looper;
import com.sander.otg_poc.dto.TimerDto;
import com.sander.otg_poc.model.MinuteCountDownTimer;
import com.sander.otg_poc.model.TimerState;
import com.sander.otg_poc.utils.EventHandler;


import java.util.Timer;
import java.util.TimerTask;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ProductionProcessService {

    private static ProductionProcessService INSTANCE=null;
    private Timer temperatureTimer = new Timer();
    private Timer engineTaskTimer = new Timer();
    public static ProductionProcessService create(EventHandler cycleOffTickHandler,
                                                  EventHandler cycleOnTickHandler,
                                                  EventHandler machineTimeTickHandler,
                                                  TimerTask task){
        INSTANCE = new ProductionProcessService(cycleOffTickHandler,cycleOnTickHandler,machineTimeTickHandler);
        INSTANCE.temperatureTimer.scheduleAtFixedRate(task,0,SECONDS.toMillis(20));
        return INSTANCE;
    }

    public static ProductionProcessService getInstance(){
        return INSTANCE==null ? new ProductionProcessService(null,null,null) : INSTANCE ;
    }

    private MinuteCountDownTimer cycleOff;
    private TimerDto cycleOffAimed;
    private MinuteCountDownTimer cycleOn;
    private TimerDto cycleOnAimed;
    private MinuteCountDownTimer machineTime;
    private TimerDto machineTimeAimed;
    private double aimedTemperature;
    private double temperature;
    private Handler handler;

    public boolean isProcessRunning() {
        return machineTime.isRunning();
    }


    private ProductionProcessService(MinuteCountDownTimer cycleOff,
                                     MinuteCountDownTimer cycleOn,
                                     MinuteCountDownTimer machineTime,
                                     double aimedTemperature, double temperature) {
        this.cycleOff = cycleOff;
        this.cycleOn = cycleOn;
        this.machineTime = machineTime;
        this.aimedTemperature = aimedTemperature;
        this.temperature = temperature;
        this.machineTimeAimed = TimerDto.millisToTimerDto(machineTime.getTimeSet());
        this.cycleOffAimed = TimerDto.millisToTimerDto(machineTime.getTimeSet());
        this.cycleOnAimed = TimerDto.millisToTimerDto(machineTime.getTimeSet());
        initOnFinishHandlers();
        handler = new Handler(Looper.getMainLooper());
    }

   void initOnFinishHandlers(){
       machineTime.setOnFinishHandler( o->{
           cycleOff.resetTimer();
           cycleOn.resetTimer();
       });
   }

    private ProductionProcessService(EventHandler cycleOffTickHandler,
                                     EventHandler cycleOnTickHandler,
                                     EventHandler machineTimeTickHandler){
        this(
                new MinuteCountDownTimer(0,20,cycleOffTickHandler),
                new MinuteCountDownTimer(0,30,cycleOnTickHandler),
                new MinuteCountDownTimer(1,0,machineTimeTickHandler),
                0,0
        );
    }



    public MinuteCountDownTimer getCycleOff() {
        return cycleOff;
    }

    public MinuteCountDownTimer getCycleOn() {
        return cycleOn;
    }

    public MinuteCountDownTimer getMachineTime() {
        return machineTime;
    }

    public double getAimedTemperature() {
        return aimedTemperature;
    }

    public void setAimedTemperature(double aimedTemperature) {
        this.aimedTemperature = aimedTemperature;
    }

    public double getTemperature() {
        return temperature;
    }



    public TimerDto updateMachineTime(int minutes, int seconds) {

        handler.post(()->{
            machineTime.updateTimer(minutes,seconds);
        });

        return new TimerDto(minutes,seconds);
    }

    public TimerDto updateCycleOff(int minutes, int seconds) {
        handler.post(()->{
            cycleOff.updateTimer(minutes,seconds);
        });

        return new TimerDto(minutes,seconds);
    }

    public TimerDto updateCycleOn(int minutes, int seconds) {
        handler.post(()->{
            cycleOn.updateTimer(minutes,seconds);
        });
        return new TimerDto(minutes,seconds);
    }



    public void stopProcess(){
        handler.post(()->{
            machineTime.stop();
        });
    }

    public void startProcess() {
        handler.post(()->{
            machineTime.start();
            if (  cycleOff.getState().equals(TimerState.STOPPED))
                cycleOff.start();
            else if ( cycleOn.getState().equals(TimerState.STOPPED))
                cycleOn.start();
        });
    }

    public void finishProcess() {
        handler.post(()->{
            machineTime.resetTimer();
            cycleOff.resetTimer();
            cycleOn.resetTimer();
        });
    }
}
