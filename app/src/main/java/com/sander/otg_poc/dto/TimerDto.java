package com.sander.otg_poc.dto;

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

        public TimerDto(String time){
        this.seconds =0;
        this.minutes =0;
        String[] split = time.split(this.delimiter);
        if (split[0]!=null)
            this.minutes = Integer.valueOf(split[0]);
        if (split[1]!=null)
            this.seconds = Integer.valueOf(split[1]);
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
        setValue1(String.valueOf(minutes));
    }
    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
        setValue2(String.valueOf(seconds));
    }

    @Override
    public void setValue1(String value1) {
        try {
            this.minutes = Integer.valueOf(value1);
        }catch (Exception e){
            minutes=0;
        }
        notifyPropertyChanged(BR.value1);
    }

    @Override
    public void setValue2(String value2) {
        try {
            this.seconds = Integer.valueOf(value2);
        }catch (Exception e){
            seconds=0;
        }
        notifyPropertyChanged(BR.value2);
    }

    @Bindable
    public String getValue1(){
        return String.valueOf(this.minutes);
    };
    @Bindable
    public String getValue2(){
        return String.valueOf(this.seconds);
    }
    @Bindable
    public String getUnit() {
        return this.unit;
    }
    @Bindable
    public String getDelimiter() {
        return delimiter;
    }

    public long toMillis(){
        return MINUTES.toMillis(minutes) + SECONDS.toMillis(seconds);
    }

    @Override
    public String toString(){
        return minutes+delimiter+seconds;
    }
}
