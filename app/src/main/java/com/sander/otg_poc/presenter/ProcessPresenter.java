package com.sander.otg_poc.presenter;

import com.sander.otg_poc.databinding.ActivityProductionBinding;
import com.sander.otg_poc.dto.TemperatureDto;
import com.sander.otg_poc.dto.TimerDto;
import com.sander.otg_poc.model.MachineState;
import com.sander.otg_poc.model.MinuteCountDownTimer;
import com.sander.otg_poc.service.ProductionProcessService;
import com.sander.otg_poc.framework.service.SerialServiceConnection;
import com.sander.otg_poc.utils.EventHandler;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ProcessPresenter{

    private static ProcessPresenter INSTANCE=null;

    public static ProcessPresenter create(ActivityProductionBinding activityProductionBinding,
                                          SerialServiceConnection serialServiceConnection){
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
        productionProcess = ProductionProcessService.create(cycleOffHandler,cycleOnHandler,machineTimeHandler,currentTempHandler);
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
//        view.renderCycleOff(cycleOffLeft);
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

        activityProductionBinding.setMachineTemp(new TemperatureDto(currentTemp));
    }

    public void setMachineState(MachineState state) {
        if (state == MachineState.ON)
            productionProcess.startProcess();
        if (state == MachineState.OFF)
            productionProcess.stopProcess();

        activityProductionBinding.setMachineState(productionProcess.isProcessRunning());
    }

    public void updateMachineTimeSet(TimerDto machineTime) {
        TimerDto timerDto = productionProcess.updateMachineTime(machineTime.getMinutes(), machineTime.getSeconds());
        activityProductionBinding.setMachineTimeSet(timerDto);
        if (serialServiceConnection.getService()!=null) {
            serialServiceConnection.getService().sendMessage("TX/machineTime/"+machineTime);
        }
    }

    public void updateCycleOnSet(TimerDto machineTime) {
        TimerDto timerDto = productionProcess.updateCycleOn(machineTime.getMinutes(), machineTime.getSeconds());
        activityProductionBinding.setCycleOnSet(timerDto);
        if (serialServiceConnection.getService()!=null) {
            serialServiceConnection.getService().sendMessage("TX/cycleOn/"+machineTime);

        }
    }

    //
    public void updateCycleOffSet(TimerDto machineTime) {
        TimerDto timerDto = productionProcess.updateCycleOff(machineTime.getMinutes(), machineTime.getSeconds());
        activityProductionBinding.setCycleOffSet(timerDto);
        if (serialServiceConnection.getService()!=null) {
            serialServiceConnection.getService().sendMessage("TX/cycleOff/"+machineTime);
        }
    }

    public void initProductionActivity() {

        MinuteCountDownTimer machineTime = productionProcess.getMachineTime();
        activityProductionBinding.setMachineTime(TimerDto.millisToTimerDto(machineTime.getMillisLeft()));
        activityProductionBinding.setMachineTimeSet(new TimerDto(machineTime.getMinutes(), machineTime.getSeconds()));

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
        if (!productionProcess.isProcessRunning()){
            productionProcess.startProcess();
        }else
            productionProcess.stopProcess();

        activityProductionBinding.setMachineState(productionProcess.isProcessRunning());

    }

    public void sendMessage(String m) {
        if (serialServiceConnection.getService()!=null)
            serialServiceConnection.getService().sendMessage(m);
    }

    public void setCycleOn(String s) {
        TimerDto timerDto = new TimerDto(s);
        activityProductionBinding.setCycleOn(timerDto);
        productionProcess.updateCycleOn(timerDto.getMinutes(),timerDto.getSeconds());
    }

    public void setCycleOff(String cycleOff) {
        TimerDto timerDto = new TimerDto(cycleOff);
        activityProductionBinding.setCycleOff(timerDto);
        productionProcess.updateCycleOff(timerDto.getMinutes(),timerDto.getSeconds());
    }

    public void setMachineState(String state) {
        setMachineState(MachineState.valueOf(state));
    }

    public void setMachineTime(String machineTime) {
        TimerDto timerDto = new TimerDto(machineTime);
        activityProductionBinding.setMachineTime(timerDto);
        productionProcess.updateMachineTime(timerDto.getMinutes(),timerDto.getSeconds());
    }
}
