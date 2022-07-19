package com.sander.otg_poc.controller;

import com.sander.otg_poc.framework.controller.SerialController;
import com.sander.otg_poc.model.MachineState;
import com.sander.otg_poc.presenter.ProcessPresenter;
import com.sander.otg_poc.framework.service.SerialServiceConnection;
import com.sander.otg_poc.framework.service.UsbConnectionReceiver;


@SerialController
public class MachineController {

    private SerialServiceConnection serialServiceConnection;
    private ProcessPresenter presenter;
    private UsbConnectionReceiver usbConnectionReceiver;

    public MachineController(ProcessPresenter presenter) {
        this.presenter = presenter;
    }

    public MachineController(){}
    public void postMachineState(String body) {
        presenter.setMachineState(MachineState.valueOf(body));
    }

    public void postCurrentTemp(String body) {
        presenter.setCurrentTemp(body);

    }
}
