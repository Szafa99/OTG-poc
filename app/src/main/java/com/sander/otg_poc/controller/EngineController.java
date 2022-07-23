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

    @SerialRequestMapping(mapping = "cycleOn")
    public void updateEngineCycleOn(String cycleOn){
        if (cycleOn != null) {
            processPresenter.setCycleOn(cycleOn);
        }
    }

    @SerialRequestMapping(mapping = "cycleOff")
    public void updateEngineCycleOff(String cycleOff){

        if (cycleOff != null) {
            processPresenter.setCycleOff(cycleOff);
        }


    }
}
