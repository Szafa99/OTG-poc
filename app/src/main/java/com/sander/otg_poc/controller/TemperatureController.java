package com.sander.otg_poc.controller;

import android.util.Log;
import com.sander.otg_poc.dto.TimerDto;
import com.sander.otg_poc.framework.controller.SerialController;
import com.sander.otg_poc.framework.controller.SerialRequestMapping;
import com.sander.otg_poc.presenter.ProcessPresenter;

@SerialController
public class TemperatureController {


    @SerialRequestMapping(mapping = "DS18B20_TEMP")
    public void updateCurrentTemp(String str){
        ProcessPresenter processPresenter = ProcessPresenter.getInstance();
        if (str!=null && processPresenter!=null){
            processPresenter.setCurrentTemp(str);
        }
    }
}
