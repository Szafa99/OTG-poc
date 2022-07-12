package com.sander.otg_poc;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.appcompat.app.AppCompatActivity;
import com.sander.otg_poc.databinding.ActivityProductionBinding;
import com.sander.otg_poc.databinding.InputValueLayoutBinding;
import com.sander.otg_poc.dto.DecimalInput;
import com.sander.otg_poc.dto.TemperatureDto;
import com.sander.otg_poc.dto.TimerDto;
import com.sander.otg_poc.presenter.ProcessPresenter;

public class ProductionActivity extends AppCompatActivity {

    ActivityProductionBinding activityProductionBinding;
    InputValueLayoutBinding inputValueLayoutBinding;
    AlertDialog.Builder builder;
    ProcessPresenter presenter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityProductionBinding = DataBindingUtil.setContentView(this, R.layout.activity_production);
        activityProductionBinding.setLifecycleOwner(this);
        presenter = new ProcessPresenter(activityProductionBinding);
        builder= new AlertDialog.Builder(this);

        presenter.initProductionActivity();
    }

    android.app.AlertDialog.Builder createDecimalInputAlert(DecimalInput decimalInput, View view){
        inputValueLayoutBinding = InputValueLayoutBinding.inflate(getLayoutInflater(),(ViewGroup) view,false);
        View card =  inputValueLayoutBinding.getRoot();
        inputValueLayoutBinding.setDecimalValue(decimalInput);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this)
                .setMessage("CHANGE MACHINE TIME")
                .setNegativeButton("CANCEL", null)
                .setView(card);
        return builder;
    }

    public void onMachineTimeCardClick(View view) {
        TimerDto timerDto = new TimerDto(1, 1);

        createDecimalInputAlert(timerDto,view)
                .setPositiveButton("CHANGE",(e,l)->{
                    DecimalInput input = inputValueLayoutBinding.getDecimalValue();
                    if ( ! (input instanceof TimerDto)) return;
                    TimerDto machineTime = (TimerDto) input;
                    presenter.updateMachineTimeSet(machineTime);
                })
                .create().show();
    }


    public void onMachineTempCardClick(View view) {
        TemperatureDto dto = new TemperatureDto(36.6f);

        createDecimalInputAlert(dto,view)
                .setPositiveButton("CHANGE",(e,l)->{
                    DecimalInput input = inputValueLayoutBinding.getDecimalValue();
                    if ( ! (input instanceof TemperatureDto)) return;
                    TemperatureDto temperatureDto = (TemperatureDto) input;
                    presenter.updateAimedTemperature(temperatureDto);
                })
                .create().show();

    }

    public void onCycleOnCardClick(View view) {
        TimerDto timerDto = new TimerDto(1, 1);

        createDecimalInputAlert(timerDto,view)
                .setPositiveButton("CHANGE",(e,l)->{
                    DecimalInput input = inputValueLayoutBinding.getDecimalValue();
                    if ( ! (input instanceof TimerDto)) return;
                    TimerDto timerDto1 = (TimerDto) input;
                    presenter.updateCycleOnSet(timerDto1);
                })
                .create().show();

    }

    public void onCycleOffCardClick(View view) {
        TimerDto timerDto = new TimerDto(1, 1);

        createDecimalInputAlert(timerDto,view)
                .setPositiveButton("CHANGE",(e,l)->{
                    DecimalInput input = inputValueLayoutBinding.getDecimalValue();
                    if ( ! (input instanceof TimerDto)) return;
                    TimerDto timerDto1 = (TimerDto) input;
                    presenter.updateCycleOffSet(timerDto1);
                })
                .create().show();

    }

    public void onMachineToggleClick(View view) {
        presenter.toggleMachine();
    }
}