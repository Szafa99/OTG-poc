package com.sander.otg_poc.dto;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import com.sander.otg_poc.BR;

public class TemperatureDto extends  DecimalInput {
    public double getTemperature() {
        return temperature;
    }

    private double temperature;
    private String unit = "C";
    private String delimiter = ".";

    TemperatureDto(){}

    public TemperatureDto(double temperature){
        this.temperature=temperature;
    }

    @Override
    @Bindable
    public String getValue1() {
        String format = String.format("%02.02f", temperature);
        String[] split = format.split("\\"+delimiter);
        return split[0];
    }

    @Override
    @Bindable
    public String getValue2() {
        String format = String.format("%02.02f", temperature);
        String[] split = format.split("\\"+delimiter);
        return split[1];
    }

    @Override
    @Bindable
    public String getUnit() {
        return unit;
    }

    @Override
    @Bindable
    public String getDelimiter() {
        return String.valueOf(delimiter);
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;

        notifyPropertyChanged(BR.value1);
        notifyPropertyChanged(BR.value2);
    }

    public void setUnit(String unit) {
        this.unit = unit;
        notifyPropertyChanged(BR.unit);
    }

    public void setDelimeter(String delimiter) {
        this.delimiter = delimiter;
        notifyPropertyChanged(BR.delimiter);
    }
}
