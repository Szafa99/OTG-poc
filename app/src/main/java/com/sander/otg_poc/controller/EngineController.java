package com.sander.otg_poc.controller;

import com.sander.otg_poc.dto.TimerDto;
import com.sander.otg_poc.framework.controller.SerialController;
import com.sander.otg_poc.framework.controller.SerialRequestMapping;
import com.sander.otg_poc.presenter.ProcessPresenter;

@SerialController
public class EngineController{

    private ProcessPresenter processPresenter;

    public EngineController(){
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
}
