package com.sander.otg_poc.model;

public enum MachineState {
    ON("ON"),OFF("OFF");

    private String state;
    MachineState(String state){
        this.state = state;
    }
}
