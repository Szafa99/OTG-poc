package com.sander.otg_poc.controller;

import android.os.health.TimerStat;
import com.sander.otg_poc.framework.controller.SerialController;
import com.sander.otg_poc.framework.controller.SerialRequestMapping;
import com.sander.otg_poc.model.TimerState;
import com.sander.otg_poc.presenter.ProcessPresenter;
import com.sander.otg_poc.service.ProductionProcessService;

@SerialController
public class EngineController{

    private ProcessPresenter processPresenter;
    private ProductionProcessService processService;

    public EngineController(){
        this.processService = ProductionProcessService.getInstance();
        this.processPresenter = ProcessPresenter.getInstance();
    }

    @SerialRequestMapping(mapping = "ENGINE_ON_PERIOD")
    public void updateEngineCycleOn(String cycleOn){
        if (cycleOn != null && processPresenter != null) {
            processPresenter.setCycleOnAimed(cycleOn);
        }
    }

    @SerialRequestMapping(mapping = "ENGINE_OFF_PERIOD")
    public void updateEngineCycleOff(String cycleOff){

        if (cycleOff != null && processPresenter!=null) {
            processPresenter.setCycleOffAimed(cycleOff);
        }
    }

    @SerialRequestMapping(mapping = "ENGINE_ON_PERIOD_STATE")
    public void startEngineCycleOnPeriod(String strState){
        if ( processService==null) return;
        TimerState state = TimerState.valueOf(strState);

        processService.getCycleOff().resetTimer();
        if (state.equals(TimerState.RUNNING))
            processService.getCycleOn().start();
        else if (state.equals(TimerState.STOPPED))
            processService.getCycleOn().stop();
    }

    @SerialRequestMapping(mapping = "ENGINE_OFF_PERIOD_STATE")
    public void startEngineCycleOffPeriod(String strState){
        if ( processService==null) return;
        TimerState state = TimerState.valueOf(strState);
        processService.getCycleOn().resetTimer();
        if (state.equals(TimerState.RUNNING))
            processService.getCycleOff().start();
        else if (state.equals(TimerState.STOPPED))
            processService.getCycleOff().stop();

    }


}
