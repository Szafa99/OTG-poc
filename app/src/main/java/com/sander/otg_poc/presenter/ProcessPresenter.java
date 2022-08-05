package com.sander.otg_poc.presenter;

import android.os.Looper;
import android.widget.Toast;
import com.sander.otg_poc.databinding.ActivityProductionBinding;
import com.sander.otg_poc.dto.TemperatureDto;
import com.sander.otg_poc.dto.TimerDto;
import com.sander.otg_poc.framework.controller.SerialDispatcher;
import com.sander.otg_poc.model.MachineState;
import com.sander.otg_poc.model.MinuteCountDownTimer;
import com.sander.otg_poc.service.ProductionProcessService;
import com.sander.otg_poc.framework.service.SerialServiceConnection;
import com.sander.otg_poc.utils.EventHandler;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.LoggingPermission;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ProcessPresenter{

    private static ProcessPresenter INSTANCE=null;
//    private SerialDispatcher serialDispatcher;
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
        productionProcess = ProductionProcessService.create(cycleOffHandler,cycleOnHandler,machineTimeHandler,updateTemperatureTask);
        this.serialServiceConnection = serialServiceConnection;
//        serialDispatcher = new SerialDispatcher(this.activityProductionBinding.getRoot().getContext());
//        Looper.prepare();
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
            if (serialServiceConnection.getService()!=null)
            serialServiceConnection.getService().sendMessage("RX/DS18B20_TEMP/"+body);
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


    public void updateMachineTimeSet(TimerDto machineTime) {
//        TimerDto timerDto = productionProcess.updateMachineTime(machineTime.getMinutes(), machineTime.getSeconds());
//        activityProductionBinding.setMachineTimeSet(timerDto);
//        productionProcess.setMachineTimeAimed(timerDto);


//        if (serialServiceConnection.getService()!=null) {
//            serialServiceConnection.getService().sendMessage("TX/MACHINE_TIME/"+machineTime);
//        }
        sendMessage("TX/MACHINE_TIME/"+machineTime);

    }

    public void updateCycleOnSet(TimerDto machineTime) {

//        TimerDto timerDto = productionProcess.updateCycleOn(machineTime.getMinutes(), machineTime.getSeconds());
//        productionProcess.setCycleOnAimed(timerDto);

//        activityProductionBinding.setCycleOnSet(timerDto);
//        if (serialServiceConnection.getService()!=null) {
//            serialServiceConnection.getService().sendMessage("TX/ENGINE_ON_PERIOD/"+machineTime);
        sendMessage("TX/ENGINE_ON_PERIOD/"+machineTime);
//        }
    }

    //Should update
    public void updateCycleOffSet(TimerDto machineTime) {
//        if (serialServiceConnection.getService()!=null) {
//            serialServiceConnection.getService().sendMessage("TX/ENGINE_OFF_PERIOD/"+machineTime);
        sendMessage("TX/ENGINE_OFF_PERIOD/"+machineTime);
//        }

//        TimerDto timerDto = productionProcess.updateCycleOff(machineTime.getMinutes(), machineTime.getSeconds());
//        productionProcess.setCycleOnAimed(timerDto);
//        activityProductionBinding.setCycleOffSet(timerDto);
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

    void initMachineStart(){
        if (serialServiceConnection.getService()==null) return;
        serialServiceConnection.getService().sendMessage("TX/MACHINE_STATE/ON");

        if (! productionProcess.getMachineTime().getState().equals(MinuteCountDownTimer.MinuteCountDownTimerState.FINISHED)) {
        TimerDto cycleOff = new TimerDto(productionProcess.getCycleOff().getMinutes(), productionProcess.getCycleOff().getSeconds());
        sendMessage("TX/ENGINE_OFF_PERIOD/"+cycleOff);

        TimerDto cycleOn = new TimerDto(productionProcess.getCycleOn().getMinutes(), productionProcess.getCycleOn().getSeconds());
        sendMessage("TX/ENGINE_ON_PERIOD/"+cycleOn);

        TimerDto machineTime = new TimerDto(productionProcess.getMachineTime().getMinutes(), productionProcess.getMachineTime().getSeconds());
        sendMessage("TX/MACHINE_TIME/"+machineTime);
        }

    }

    public void toggleMachine() {
        if (!productionProcess.isProcessRunning()){
//            initMachineStart();
            sendMessage("TX/MACHINE_STATE/ON");

//            productionProcess.startProcess();
        }else {
//            productionProcess.stopProcess();
//            if (serialServiceConnection.getService()!=null)
//                serialServiceConnection.getService().sendMessage("TX/MACHINE_STATE/OFF");
            sendMessage("TX/MACHINE_STATE/OFF");
        }
//        activityProductionBinding.setMachineState(productionProcess.isProcessRunning());
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
        activityProductionBinding.setMachineState(running);
    }


    public void sendMessage(String m) {
        if (serialServiceConnection.getService()!=null &&
                serialServiceConnection.getService().sendMessage(m) ==false)
            return;
//          serialDispatcher.onNext(m);
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
