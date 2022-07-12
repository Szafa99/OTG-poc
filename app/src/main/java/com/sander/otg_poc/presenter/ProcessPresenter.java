package com.sander.otg_poc.presenter;

import androidx.databinding.library.baseAdapters.BR;
import com.sander.otg_poc.databinding.ActivityProductionBinding;
import com.sander.otg_poc.dto.TemperatureDto;
import com.sander.otg_poc.dto.TimerDto;
import com.sander.otg_poc.model.MachineState;
import com.sander.otg_poc.model.MinuteCountDownTimer;
import com.sander.otg_poc.model.ProductionProcess;

public class ProcessPresenter {

//    private ProductionActivity view;
    private ActivityProductionBinding activityProductionBinding;
    private ProductionProcess productionProcess;

    private EventHandler cycleOnHandler = o -> onCycleOnTick((long)o);
    private EventHandler cycleOffHandler = o -> onCycleOffTick((long)o);
    private EventHandler machineTimeHandler = o -> onMachineTimeTick((long)o);
    private EventHandler currentTempHandler = o -> onTemperatureChanged((double)o);


    public ProcessPresenter(ActivityProductionBinding activityProductionBinding) {
        this.activityProductionBinding = activityProductionBinding;
        productionProcess = ProductionProcess.create(cycleOffHandler,cycleOnHandler,machineTimeHandler,currentTempHandler);
    }

    public void onCycleOnTick(long millis){
        TimerDto cycleOnLeft = TimerDto.millisToTimerDto(millis);
        activityProductionBinding.setCycleOn(cycleOnLeft);
//        view.renderCycleOn(cycleOnLeft);
    }

    public void onCycleOffTick(long millis){
        TimerDto cycleOffLeft = TimerDto.millisToTimerDto(millis);
//        view.renderCycleOff(cycleOffLeft);
        activityProductionBinding.setCycleOff(cycleOffLeft);
    }

    public void onMachineTimeTick(long millis) {
        TimerDto machineTimeLeft = TimerDto.millisToTimerDto(millis);
//        view.renderMachineTime(cycleOffLeft);
        activityProductionBinding.setMachineTime(machineTimeLeft);
    }

    public void onTemperatureChanged(Double temp){
        TemperatureDto temperatureDto = new TemperatureDto(temp);
        activityProductionBinding.setMachineTemp(temperatureDto);
    }


    public TimerDto getMachineTime() {
        return TimerDto.millisToTimerDto(productionProcess.getMachineTime().getMillisLeft());
    }

    public TimerDto getAimedMachineTime() {
        return new TimerDto(productionProcess.getMachineTime().getMinutes(),productionProcess.getMachineTime().getSeconds());
    }


    public TemperatureDto getCurrentTemp() {
        return new TemperatureDto( productionProcess.getTemperature());
    }

    public TimerDto getCycleOff() {
        return TimerDto.millisToTimerDto(productionProcess.getCycleOff().getMillisLeft());
    }

    public TimerDto getCycleOffSet() {
        return new TimerDto(productionProcess.getCycleOff().getMinutes(),productionProcess.getCycleOff().getMinutes());
    }

    public TimerDto getCycleOn() {
        return TimerDto.millisToTimerDto(productionProcess.getCycleOn().getMillisLeft());
    }

    public TimerDto getCycleOnSet() {
        return new TimerDto(productionProcess.getCycleOn().getMinutes(),productionProcess.getCycleOn().getMinutes());
    }

    public void setCurrentTemp(String body) {
        Double currentTemp = Double.valueOf(body);

//        view.renderCurrentTemp(body);
        activityProductionBinding.setMachineTemp(new TemperatureDto(currentTemp));
    }

    public void setMachineState(MachineState state) {
        if (state == MachineState.ON)
            productionProcess.startProcess();
        if (state == MachineState.OFF)
            productionProcess.stopProcess();
    }

    public void updateMachineTimeSet(TimerDto machineTime) {
        TimerDto timerDto = productionProcess.updateMachineTime(machineTime.getMinutes(), machineTime.getSeconds());
        activityProductionBinding.setMachineTimeSet(timerDto);
    }

    public void updateCycleOnSet(TimerDto machineTime) {
        TimerDto timerDto = productionProcess.updateCycleOn(machineTime.getMinutes(), machineTime.getSeconds());
        activityProductionBinding.setCycleOnSet(timerDto);
    }

    public void updateCycleOffSet(TimerDto machineTime) {
        TimerDto timerDto = productionProcess.updateCycleOff(machineTime.getMinutes(), machineTime.getSeconds());
        activityProductionBinding.setCycleOffSet(timerDto);
    }

    public void initProductionActivity() {

        MinuteCountDownTimer machineTime = productionProcess.getMachineTime();
        activityProductionBinding.setMachineTime(new TimerDto(machineTime.getMinutes(), machineTime.getSeconds()));
        activityProductionBinding.setMachineTimeSet(TimerDto.millisToTimerDto(machineTime.getMillisLeft()));

        activityProductionBinding.setMachineTemp(new TemperatureDto(productionProcess.getTemperature()));
        activityProductionBinding.setMachineTempAimed(new TemperatureDto(productionProcess.getAimedTemperature()));

        MinuteCountDownTimer cycleOff = productionProcess.getCycleOff();
        activityProductionBinding.setCycleOffSet(new TimerDto(cycleOff.getMinutes(),cycleOff.getSeconds()));
        activityProductionBinding.setCycleOff(TimerDto.millisToTimerDto(cycleOff.getMillisLeft()));

        MinuteCountDownTimer cycleOn = productionProcess.getCycleOn();
        activityProductionBinding.setCycleOnSet(new TimerDto(cycleOn.getMinutes(),cycleOn.getSeconds()));
        activityProductionBinding.setCycleOn(TimerDto.millisToTimerDto(cycleOn.getMillisLeft()));

        activityProductionBinding.setMachineState(false);
    }


    public void updateAimedTemperature(TemperatureDto temperatureDto) {
        productionProcess.setAimedTemperature(temperatureDto.getTemperature());
        activityProductionBinding.setMachineTempAimed(new TemperatureDto(productionProcess.getAimedTemperature()));
    }

    public void toggleMachine() {
        if (!productionProcess.isProcessRunning()) {
            productionProcess.startProcess();
        }else
            productionProcess.stopProcess();

        activityProductionBinding.setMachineState(productionProcess.isProcessRunning());
    }
}
