package com.sander.otg_poc.model;

public enum TimerState {
    RUNNING("RUNNING"), STOPPED("STOPPED"),
    RESUMED("RESUMED"), FINISHED("FINISHED");

    TimerState(String state) {
        this.state = state;
    }

    private String state;
}

