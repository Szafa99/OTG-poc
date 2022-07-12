package com.sander.otg_poc.dto;

import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public abstract class DecimalInput extends BaseObservable {
    @Bindable
    public abstract String getValue1();
    @Bindable
    public abstract String getValue2();
    @Bindable
    public abstract String getUnit();
    @Bindable
    public abstract String getDelimiter();

    public String render(){
        return getValue1() + getDelimiter() + getValue2() + getUnit();
    }
}
