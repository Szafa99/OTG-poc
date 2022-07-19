package com.sander.otg_poc.service;

import com.sander.otg_poc.dto.TimerDto;
import com.sander.otg_poc.model.MinuteCountDownTimer;
import com.sander.otg_poc.utils.EventHandler;

public class ProductionProcessService {

    private static ProductionProcessService INSTANCE=null;
    private EventHandler currentTempHandler;
    public static ProductionProcessService create(EventHandler cycleOffTickHandler,
                                                  EventHandler cycleOnTickHandler,
                                                  EventHandler machineTimeTickHandler,
                                                  EventHandler currentTempHandler){
        INSTANCE = new ProductionProcessService(cycleOffTickHandler,cycleOnTickHandler,machineTimeTickHandler);
        INSTANCE.currentTempHandler = currentTempHandler;
        return INSTANCE;
    }

    public static ProductionProcessService getInstance(){
        return INSTANCE==null ? new ProductionProcessService(null,null,null) : INSTANCE ;
    }

    private MinuteCountDownTimer cycleOff;
    private MinuteCountDownTimer cycleOn;
    private MinuteCountDownTimer machineTime;
    private double aimedTemperature;
    private double temperature;

    public boolean isProcessRunning() {
        return processRunning;
    }

    private boolean processRunning =false;

    private ProductionProcessService(MinuteCountDownTimer cycleOff,
                                     MinuteCountDownTimer cycleOn,
                                     MinuteCountDownTimer machineTime,
                                     double aimedTemperature, double temperature) {
        this.cycleOff = cycleOff;
        this.cycleOn = cycleOn;
        this.machineTime = machineTime;
        this.aimedTemperature = aimedTemperature;
        this.temperature = temperature;

        initOnFinishHandlers();
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
           cycleOff.cancel();
           cycleOn.cancel();
       });
   }

    private ProductionProcessService(EventHandler cycleOffTickHandler,
                                     EventHandler cycleOnTickHandler,
                                     EventHandler machineTimeTickHandler){
        this(
                new MinuteCountDownTimer(0,0,cycleOffTickHandler),
                new MinuteCountDownTimer(0,0,cycleOnTickHandler),
                new MinuteCountDownTimer(0,0,machineTimeTickHandler),
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
        EventHandler onTickHandler = machineTime.getOnTickHandler();
        EventHandler onFinishHandler = machineTime.getOnFinishHandler();
        machineTime = new MinuteCountDownTimer(minutes,seconds,onTickHandler,onFinishHandler);
        if (processRunning)
            machineTime.start();

        return new TimerDto(minutes,seconds);
    }

    public TimerDto updateCycleOff(int minutes, int seconds) {
        EventHandler onTickHandler = cycleOff.getOnTickHandler();
        EventHandler onFinishHandler = cycleOff.getOnFinishHandler();
        cycleOff = new MinuteCountDownTimer(minutes,seconds,onTickHandler,onFinishHandler);
        if (machineTime.isRunning() && !cycleOn.isRunning() && processRunning)
            cycleOff.start();
        return new TimerDto(minutes,seconds);
    }

    public TimerDto updateCycleOn(int minutes, int seconds) {
        EventHandler onTickHandler = cycleOn.getOnTickHandler();
        EventHandler onFinishHandler = cycleOn.getOnFinishHandler();

        cycleOn = new MinuteCountDownTimer(minutes,seconds,onTickHandler,onFinishHandler);
        if (machineTime.isRunning() && !cycleOff.isRunning() && processRunning)
            cycleOn.start();
        return new TimerDto(minutes,seconds);
    }

    public void stopProcess(){
        cycleOff.cancel();
        cycleOn.cancel();
        machineTime.cancel();
        processRunning=false;
    }

    public void startProcess() {
        if (processRunning==true) return;
        machineTime.start();
        cycleOn.start();
        processRunning=true;
    }
}
