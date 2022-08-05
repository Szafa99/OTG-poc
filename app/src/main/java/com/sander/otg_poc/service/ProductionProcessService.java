package com.sander.otg_poc.service;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.sander.otg_poc.dto.TimerDto;
import com.sander.otg_poc.model.MinuteCountDownTimer;
import com.sander.otg_poc.utils.EventHandler;


import java.util.Timer;
import java.util.TimerTask;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ProductionProcessService {

    private static ProductionProcessService INSTANCE=null;
    private Timer temperatureTimer;
    public static ProductionProcessService create(EventHandler cycleOffTickHandler,
                                                  EventHandler cycleOnTickHandler,
                                                  EventHandler machineTimeTickHandler,
                                                  TimerTask task){
        INSTANCE = new ProductionProcessService(cycleOffTickHandler,cycleOnTickHandler,machineTimeTickHandler);
        INSTANCE.temperatureTimer =  new Timer();
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
        this.machineTimeAimed = new TimerDto(machineTime.getMinutes(),machineTime.getSeconds());
        this.cycleOffAimed = new TimerDto(cycleOff.getMinutes(),cycleOff.getSeconds());
        this.cycleOnAimed = new TimerDto(cycleOn.getMinutes(),cycleOn.getSeconds());
        initOnFinishHandlers();
        handler = new Handler(Looper.getMainLooper());
    }

   void initOnFinishHandlers(){
       cycleOff.setOnFinishHandler( o->{
           if(machineTime.getMillisLeft()>0)
               cycleOn.start();
       });
       cycleOn.setOnFinishHandler( o->{
           if(machineTime.getMillisLeft()>0)
               cycleOff.start();
       });
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

    public void setCycleOff(MinuteCountDownTimer cycleOff) {
        this.cycleOff = cycleOff;
    }

    public MinuteCountDownTimer getCycleOn() {
        return cycleOn;
    }

    public void setCycleOn(MinuteCountDownTimer cycleOn) {
        this.cycleOn = cycleOn;
    }

    public MinuteCountDownTimer getMachineTime() {
        return machineTime;
    }

    public TimerDto getCycleOffAimed() {
        return cycleOffAimed;
    }

    public void setCycleOffAimed(TimerDto cycleOffAimed) {
        this.cycleOffAimed = cycleOffAimed;
    }

    public TimerDto getCycleOnAimed() {
        return cycleOnAimed;
    }

    public void setCycleOnAimed(TimerDto cycleOnAimed) {
        this.cycleOnAimed = cycleOnAimed;
    }

    public TimerDto getMachineTimeAimed() {
        return machineTimeAimed;
    }

    public void setMachineTimeAimed(TimerDto machineTimeAimed) {
        this.machineTimeAimed = machineTimeAimed;
    }

    public void setMachineTime(MinuteCountDownTimer machineTime) {
        this.machineTime = machineTime;
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

    public void setTemperature(double temperature) {
        this.temperature = temperature;

    }

    public TimerDto updateMachineTime(int minutes, int seconds) {

        handler.post(()->{
            machineTime.setMinutes(minutes);
            machineTime.setSeconds(seconds);
        });

        return new TimerDto(minutes,seconds);
    }

    public TimerDto updateCycleOff(int minutes, int seconds) {
        handler.post(()->{
            cycleOff.setMinutes(minutes);
            cycleOff.setSeconds(seconds);
        });

        return new TimerDto(minutes,seconds);
    }

    public TimerDto updateCycleOn(int minutes, int seconds) {
        handler.post(()->{
            cycleOn.setMinutes(minutes);
            cycleOn.setSeconds(seconds);

        });
        return new TimerDto(minutes,seconds);
    }

    public void stopProcess(){
        handler.post(()->{
        cycleOff.stop();
        cycleOn.stop();
        machineTime.stop();
        });
    }

    public void startProcess() {
        handler.post(()->{
            machineTime.start();
            if (  cycleOff.getState().equals(MinuteCountDownTimer.MinuteCountDownTimerState.STOPED) )
                cycleOff.start();
            else
                cycleOn.start();
        });
    }
}
