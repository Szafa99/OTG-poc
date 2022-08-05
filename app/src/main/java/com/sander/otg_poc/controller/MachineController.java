package com.sander.otg_poc.controller;

import com.sander.otg_poc.framework.controller.SerialController;
import com.sander.otg_poc.framework.controller.SerialRequestMapping;
import com.sander.otg_poc.model.MachineState;
import com.sander.otg_poc.presenter.ProcessPresenter;
import com.sander.otg_poc.framework.service.SerialServiceConnection;
import com.sander.otg_poc.framework.service.UsbConnectionReceiver;


@SerialController
public class MachineController {

    private final ProcessPresenter processPresenter;
    public MachineController(){
        this.processPresenter = ProcessPresenter.getInstance();
    }

    @SerialRequestMapping(mapping = "MACHINE_STATE")
    public void updateMachineState(String state){
        if (state!=null)
            processPresenter.setMachineState(state);
    }

    @SerialRequestMapping(mapping = "MACHINE_TIME")
    public void updateMachineTime(String machineTime){
        if (machineTime!=null && processPresenter!=null)
            processPresenter.setMachineTimeAimed(machineTime);
    }
}
