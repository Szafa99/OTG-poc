package com.sander.otg_poc.presenter;

import com.sander.otg_poc.databinding.ActivityProductionBinding;
import com.sander.otg_poc.dto.TemperatureDto;
import com.sander.otg_poc.dto.TimerDto;
import com.sander.otg_poc.model.MachineState;
import com.sander.otg_poc.model.MinuteCountDownTimer;
import com.sander.otg_poc.model.TimerState;
import com.sander.otg_poc.service.ProductionProcessService;
import com.sander.otg_poc.framework.service.SerialServiceConnection;
import com.sander.otg_poc.utils.EventHandler;

import java.util.TimerTask;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ProcessPresenter{

    private static ProcessPresenter INSTANCE=null;
    public static ProcessPresenter create(ActivityProductionBinding activityProductionBinding,
                                          SerialServiceConnection serialServiceConnection){
        if (INSTANCE != null) {
            INSTANCE.serialServiceConnection = serialServiceConnection;
            INSTANCE.activityProductionBinding = activityProductionBinding;
            return INSTANCE;
        }
        INSTANCE = new ProcessPresenter(activityProductionBinding,
                serialServiceConnection);
        return INSTANCE;
    }

    public static ProcessPresenter getInstance(){
        return INSTANCE;
    }

    private ProcessPresenter(ActivityProductionBinding activityProductionBinding
            ,SerialServiceConnection serialServiceConnection) {
        this.activityProductionBinding = activityProductionBinding;

        productionProcess = ProductionProcessService.create(cycleOffHandler,cycleOnHandler,machineTimeHandler,updateTemperatureTask);
        this.serialServiceConnection = serialServiceConnection;
    }


    private ActivityProductionBinding activityProductionBinding;
    private ProductionProcessService productionProcess;
    private SerialServiceConnection serialServiceConnection;


    private EventHandler cycleOnHandler = o -> {
        if (o instanceof Long)
            onCycleOnTick((long) o);
    };
    private EventHandler cycleOffHandler = o -> {
        if (o instanceof Long)
            onCycleOffTick((long) o);
    };
    private EventHandler machineTimeHandler = o -> {
        if (o instanceof Long)
            onMachineTimeTick((long) o);
    };


    TimerTask updateTemperatureTask = new TimerTask() {
        private final String body = " ";
        @Override
        public void run() {
            if (serialServiceConnection!=null && serialServiceConnection.getService()!=null) {
                serialServiceConnection.getService().sendMessage("RX/DS18B20_TEMP/"+body);
            }
        }
    };

    private EventHandler currentTempHandler = o -> {
        if (o instanceof Double)
            onTemperatureChanged((double) o);
    };



    public void onCycleOnTick(long millis){
        TimerDto cycleOnLeft = TimerDto.millisToTimerDto(millis);
        activityProductionBinding.setCycleOn(cycleOnLeft);
    }

    public void onCycleOffTick(long millis){
        TimerDto cycleOffLeft = TimerDto.millisToTimerDto(millis);
        activityProductionBinding.setCycleOff(cycleOffLeft);
    }

    public void onMachineTimeTick(long millis) {
        TimerDto machineTimeLeft = TimerDto.millisToTimerDto(millis);
        activityProductionBinding.setMachineTime(machineTimeLeft);
            if (MILLISECONDS.toSeconds(millis)==0 && productionProcess!=null)
                activityProductionBinding.setMachineState(false);
    }

    public void onTemperatureChanged(Double temp){
        TemperatureDto temperatureDto = new TemperatureDto(temp);
        activityProductionBinding.setMachineTemp(temperatureDto);
    }

    public void setCurrentTemp(String body) {
        Double currentTemp = Double.valueOf(body);
        productionProcess.setTemperature(currentTemp);
        activityProductionBinding.setMachineTemp(new TemperatureDto(currentTemp));
    }


    public void updateMachineTimeSet(TimerDto machineTime) {
        sendMessage("TX/MACHINE_TIME/"+machineTime);
    }

    public void updateCycleOnSet(TimerDto machineTime) {
        sendMessage("TX/ENGINE_ON_PERIOD/"+machineTime);
    }

    public void updateCycleOffSet(TimerDto machineTime) {
        sendMessage("TX/ENGINE_OFF_PERIOD/"+machineTime);
    }

    public void initProductionActivity() {

        MinuteCountDownTimer machineTime = productionProcess.getMachineTime();
        activityProductionBinding.setMachineTimeSet(TimerDto.millisToTimerDto(machineTime.getTimeSet()));
        activityProductionBinding.setMachineTime(TimerDto.millisToTimerDto(0));

        activityProductionBinding.setMachineTemp(new TemperatureDto(productionProcess.getTemperature()));
        activityProductionBinding.setMachineTempAimed(new TemperatureDto(productionProcess.getAimedTemperature()));

        MinuteCountDownTimer cycleOff = productionProcess.getCycleOff();
        activityProductionBinding.setCycleOffSet(TimerDto.millisToTimerDto(cycleOff.getTimeSet()));
        activityProductionBinding.setCycleOff(TimerDto.millisToTimerDto(0));

        MinuteCountDownTimer cycleOn = productionProcess.getCycleOn();
        activityProductionBinding.setCycleOnSet(TimerDto.millisToTimerDto(cycleOn.getTimeSet()));
        activityProductionBinding.setCycleOn(TimerDto.millisToTimerDto(0));

        activityProductionBinding.setMachineState(productionProcess.isProcessRunning());
    }


    public void updateAimedTemperature(TemperatureDto temperatureDto) {
        sendMessage("TX/AIMED_TEMP/"+temperatureDto);
    }

    public void toggleMachine() {
        if (!productionProcess.isProcessRunning()){
            sendMessage("TX/MACHINE_STATE/ON");
        }else {
            sendMessage("TX/MACHINE_STATE/OFF");
        }
    }

    public void setMachineState(String stateStr) {

        MachineState state= MachineState.valueOf(stateStr);
        Boolean running = false;
        if (state == MachineState.ON) {
            productionProcess.startProcess();
            running = true;
        }
        if (state == MachineState.OFF) {
            productionProcess.stopProcess();
            running=false;
        }
        if (state == MachineState.FINISHED) {
            productionProcess.finishProcess();
            running=false;
        }
        activityProductionBinding.setMachineState(running);
    }


    public void sendMessage(String m) {
        if (serialServiceConnection.getService()!=null &&
                serialServiceConnection.getService().sendMessage(m) ==false)
            return;
    }

    public void setAimedTemp(String temp){
        TemperatureDto temperatureDto = new TemperatureDto(Double.valueOf(temp));
        productionProcess.setAimedTemperature(temperatureDto.getTemperature());
        activityProductionBinding.setMachineTempAimed(temperatureDto);
    }


    public void setCycleOnAimed(String s) {
        TimerDto timerDto = new TimerDto(s);
        TimerDto timerDto1 = productionProcess.updateCycleOn(timerDto.getMinutes(), timerDto.getSeconds());

        activityProductionBinding.setCycleOnSet(timerDto1);
    }

    public void setCycleOffAimed(String cycleOff) {
        TimerDto timerDto = new TimerDto(cycleOff);
        TimerDto timerDto1 = productionProcess.updateCycleOff(timerDto.getMinutes(), timerDto.getSeconds());
        activityProductionBinding.setCycleOffSet(timerDto1);
    }

    public void setMachineTimeAimed(String machineTime) {
        TimerDto timerDto = new TimerDto(machineTime);
        TimerDto timerDto1 = productionProcess.updateMachineTime(timerDto.getMinutes(), timerDto.getSeconds());

        activityProductionBinding.setMachineTimeSet(timerDto1);
    }
}
