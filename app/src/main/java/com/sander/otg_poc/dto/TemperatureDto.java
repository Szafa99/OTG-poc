package com.sander.otg_poc.dto;

import androidx.databinding.Bindable;
import com.sander.otg_poc.BR;

public class TemperatureDto extends  DecimalInput {


    private double temperature;
    private String unit = "C";
    private String delimiter = ".";


    public TemperatureDto(double temperature){
        this.temperature=temperature;
    }

    public double getTemperature() {
        return temperature;
    }


    public void setTemperature(double temperature) {
        this.temperature = temperature;
        String[] split = String.format("%2.f",temperature).split("\\.");

        setValue1(split[0]);
        setValue1(split[1]);
    }

    @Override
    public void setValue1(String value1) {
        if (value1.isEmpty())
            value1="0";
        this.temperature = Double.valueOf(value1+getDelimiter()+getValue2());
        notifyPropertyChanged(BR.value1);
    }

    @Override
    public void setValue2(String value2) {
        if (value2.isEmpty())
            value2="0";
        this.temperature = Double.valueOf(getValue1()+getDelimiter()+value2);
        notifyPropertyChanged(BR.value2);
    }

    @Bindable
    public String getValue1(){
        String[] split = String.format("%.2f",temperature).split("\\.");
        return split[0];
    };

    @Bindable
    public String getValue2(){
        String[] split = String.format("%.2f",temperature).split("\\.");
        return split[1];
    }

    @Bindable
    public String getUnit() {
        return this.unit;
    }

    @Bindable
    public String getDelimiter() {
        return delimiter;
    }


    public void setUnit(String unit) {
        this.unit = unit;
        notifyPropertyChanged(BR.unit);
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
        notifyPropertyChanged(BR.delimiter);
    }

    @Override
    public String toString(){
        return String.format("%2.2f",temperature);
    }
}
