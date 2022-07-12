package com.sander.otg_poc.dto;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import com.sander.otg_poc.BR;

import static java.util.concurrent.TimeUnit.*;

public class TimerDto extends DecimalInput{

    private int minutes;
    private int seconds;
    private final String delimiter = ":";
    private final String unit = " min";

    public TimerDto(int minutes, int seconds) {
        this.minutes = minutes;
        this.seconds = seconds;
    }


    public static TimerDto millisToTimerDto(long millis){
        int newMinutes = (int)MILLISECONDS.toMinutes(millis);
        long millisInWholeMinutes = MINUTES.toMillis(newMinutes);
        long leftMillis = millis - millisInWholeMinutes;
        int newSeconds = (int)MILLISECONDS.toSeconds(leftMillis);

        return new TimerDto(newMinutes,newSeconds);
    }


    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
        notifyPropertyChanged(BR.value1);
    }
    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
        notifyPropertyChanged(BR.value2);
    }

    @Override
    public String toString() {
        return  minutes + delimiter + seconds +unit;
    }

    @Override
    @Bindable
    public String getValue1() {
        return String.valueOf(minutes);
    }

    @Override
    @Bindable
    public String getValue2() {
        return String.valueOf(seconds);
    }

    @Override
    @Bindable
    public String getUnit() {
        return this.unit;
    }

    @Override
    @Bindable
    public String getDelimiter() {
        return delimiter;
    }

    public long toMillis(){
        return MINUTES.toMillis(minutes) + SECONDS.toMillis(seconds);
    }
}
