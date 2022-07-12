package com.sander.otg_poc.controller;

import com.sander.otg_poc.model.MachineState;
import com.sander.otg_poc.presenter.ProcessPresenter;
import com.sander.otg_poc.service.SerialServiceConnection;

public class SerialController {
    private SerialServiceConnection serialServiceConnection;
    private ProcessPresenter presenter;

    public SerialController(SerialServiceConnection serialServiceConnection,
                            ProcessPresenter presenter) {
        this.serialServiceConnection = serialServiceConnection;
        this.presenter = presenter;
    }

    public void postMachineState(String body) {
        presenter.setMachineState(MachineState.valueOf(body));
    }

    public void postCurrentTemp(String body) {
        presenter.setCurrentTemp(body);

    }
}
