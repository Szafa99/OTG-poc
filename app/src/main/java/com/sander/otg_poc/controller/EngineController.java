package com.sander.otg_poc.controller;

import com.sander.otg_poc.framework.controller.SerialController;
import com.sander.otg_poc.framework.controller.SerialRequestMapping;
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

    @SerialRequestMapping(mapping = "ENGINE_ON_PERIOD_START")
    public void startEngineCycleOnPeriod(String cycleOn){
        if ( processService!=null) {
            processService.getCycleOff().stop();
            if (processService.isProcessRunning()) {
                processService.getCycleOn().start();
                processService.getCycleOff().resetTimer();
            }
        }
    }

    @SerialRequestMapping(mapping = "ENGINE_OFF_PERIOD_START")
    public void startEngineCycleOffPeriod(String cycleOff){
        if ( processService!=null) {
            processService.getCycleOn().stop();
            if (processService.isProcessRunning()) {
                processService.getCycleOff().start();
                processService.getCycleOn().resetTimer();
            }
        }
    }


}
