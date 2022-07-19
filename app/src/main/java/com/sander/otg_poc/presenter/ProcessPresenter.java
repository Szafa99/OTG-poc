package com.sander.otg_poc.presenter;

import com.sander.otg_poc.controller.MachineController;
import com.sander.otg_poc.databinding.ActivityProductionBinding;
import com.sander.otg_poc.dto.TemperatureDto;
import com.sander.otg_poc.dto.TimerDto;
import com.sander.otg_poc.model.MachineState;
import com.sander.otg_poc.model.MinuteCountDownTimer;
import com.sander.otg_poc.service.ProductionProcessService;
import com.sander.otg_poc.framework.service.SerialServiceConnection;
import com.sander.otg_poc.framework.service.UsbConnectionReceiver;
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
        //        machineController = new MachineController(this);
    }


    private ActivityProductionBinding activityProductionBinding;
    private ProductionProcessService productionProcess;
    private MachineController machineController;
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
//        view.renderCycleOn(cycleOnLeft);
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
            toggleMachine();
    }

    public void onTemperatureChanged(Double temp){
        TemperatureDto temperatureDto = new TemperatureDto(temp);
        activityProductionBinding.setMachineTemp(temperatureDto);
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

    public void sendMessage(String m) {
        serialServiceConnection.getService().sendMessage(m);
    }
}
