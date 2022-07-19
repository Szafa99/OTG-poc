package com.sander.otg_poc.controller;

import android.util.Log;
import com.sander.otg_poc.framework.controller.SerialController;
import com.sander.otg_poc.framework.controller.SerialRequestMapping;
import com.sander.otg_poc.presenter.ProcessPresenter;

@SerialController
public class TemperatureController {


    private String s;
    public TemperatureController() {
        this.s = "s";
    }
    @SerialRequestMapping(mapping = "aimedTemp")
    public void getAimedTemp(){

    }

    @SerialRequestMapping(mapping = "")
    public void getTemp(){
        Log.i(this.getClass().getName(),"getTemp");
        ProcessPresenter.getInstance().setCurrentTemp("22.22");
    }

}
