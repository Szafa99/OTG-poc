package com.sander.otg_poc.dto;

import androidx.databinding.BaseObservable;

public abstract class DecimalInput extends BaseObservable {

    public abstract void setValue1(String value1);

    public abstract void setValue2(String value2);

    public abstract String getValue1();

    public abstract String getValue2();

    public abstract String getUnit();

    public abstract String getDelimiter();

    public String render(){
        return getValue1() + getDelimiter() + getValue2() + getUnit();
    }

}
