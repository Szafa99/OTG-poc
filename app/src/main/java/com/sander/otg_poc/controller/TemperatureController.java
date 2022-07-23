package com.sander.otg_poc.controller;

import android.util.Log;
import com.sander.otg_poc.dto.TimerDto;
import com.sander.otg_poc.framework.controller.SerialController;
import com.sander.otg_poc.framework.controller.SerialRequestMapping;
import com.sander.otg_poc.presenter.ProcessPresenter;

@SerialController
public class TemperatureController {

    private final ProcessPresenter processPresenter;
    public TemperatureController(){
        this.processPresenter = ProcessPresenter.getInstance();
    }

    @SerialRequestMapping(mapping = "currentTemp")
    public void updateCurrentTemp(String str){
        if (str!=null){
            processPresenter.setCurrentTemp(str);
        }
    }
}
