package com.sander.otg_poc.model;

import com.sander.otg_poc.dto.TimerDto;
import com.sander.otg_poc.presenter.EventHandler;
import io.reactivex.rxjava3.core.Maybe;

public class ProductionProcess {

    private static ProductionProcess INSTANCE=null;
    private EventHandler currentTempHandler;
    public static ProductionProcess create(EventHandler cycleOffTickHandler,
                                    EventHandler cycleOnTickHandler,
                                    EventHandler machineTimeTickHandler,
                                    EventHandler currentTempHandler){
        INSTANCE = new ProductionProcess(cycleOffTickHandler,cycleOnTickHandler,machineTimeTickHandler);
        INSTANCE.currentTempHandler = currentTempHandler;
        return INSTANCE;
    }

    public static ProductionProcess getInstance(){
        return INSTANCE==null ? new ProductionProcess(null,null,null) : INSTANCE ;
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

    private ProductionProcess(MinuteCountDownTimer cycleOff,
                             MinuteCountDownTimer cycleOn,
                             MinuteCountDownTimer machineTime,
                             double aimedTemperature, double temperature) {
        this.cycleOff = cycleOff;
        this.cycleOn = cycleOn;
        this.machineTime = machineTime;
        this.aimedTemperature = aimedTemperature;
        this.temperature = temperature;
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

    private ProductionProcess(EventHandler cycleOffTickHandler,
                             EventHandler cycleOnTickHandler,
                             EventHandler machineTimeTickHandler){
        this(
                new MinuteCountDownTimer(0,0,cycleOffTickHandler),
                new MinuteCountDownTimer(0,0,cycleOnTickHandler),
                new MinuteCountDownTimer(0,0,machineTimeTickHandler),
                0,0
        );
    }

    public void stopProcess(){
        cycleOff.cancel();
        cycleOn.cancel();
        machineTime.cancel();
        processRunning=false;
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
//        machineTime.setMinutes(minutes);
//        machineTime.setSeconds(minutes);
        machineTime = new MinuteCountDownTimer(minutes,seconds,onTickHandler);
        if (processRunning)
            machineTime.start();

        return new TimerDto(minutes,seconds);
    }

    public TimerDto updateCycleOff(int minutes, int seconds) {
        EventHandler onTickHandler = cycleOff.getOnTickHandler();
        cycleOff = new MinuteCountDownTimer(minutes,seconds,onTickHandler);
        if (machineTime.isRunning() && !cycleOn.isRunning())
            cycleOff.start();
        return new TimerDto(minutes,seconds);
    }

    public TimerDto updateCycleOn(int minutes, int seconds) {
        EventHandler onTickHandler = cycleOn.getOnTickHandler();
        cycleOn = new MinuteCountDownTimer(minutes,seconds,onTickHandler);
        if (machineTime.isRunning() && !cycleOff.isRunning())
            cycleOn.start();
        return new TimerDto(minutes,seconds);
    }


    public void startProcess() {
        if (processRunning==true) return;
        machineTime.start();
        cycleOn.start();
        processRunning=true;
    }
}
