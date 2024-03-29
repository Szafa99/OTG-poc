package com.sander.otg_poc;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.sander.otg_poc.controller.MachineController;
import com.sander.otg_poc.framework.controller.SerialDispatcher;
import com.sander.otg_poc.framework.service.SerialServiceConnection;
import com.sander.otg_poc.framework.service.UsbConnectionReceiver;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final String ACTION_USB_DEVICE_ATTACHED= "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    private static final String ACTION_USB_DEVICE_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    public static final String NOTIFICATION_CHANEL = "PRODUCTION_CHANEL";
    TextView rcvMsg;
    UsbConnectionReceiver usbConnectionReceiver;
    SerialServiceConnection serialServiceConnection;

    MachineController machineController = new MachineController();
    /////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            new SerialDispatcher(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        rcvMsg = findViewById(R.id.recieveMessage);

        usbConnectionReceiver = UsbConnectionReceiver.getInstance();
        serialServiceConnection = usbConnectionReceiver.getSerialServiceConnection();
        serialServiceConnection.subscribeToSerial((o)-> {
            if (o instanceof String)
               runOnUiThread(()->rcvMsg.setText((String)o) );
        });


        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        IntentFilter filter2 = new IntentFilter(ACTION_USB_DEVICE_ATTACHED);
        IntentFilter filter3 = new IntentFilter(ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbConnectionReceiver,filter);
        registerReceiver(usbConnectionReceiver,filter2);
        registerReceiver(usbConnectionReceiver,filter3);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChanel();
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChanel(){
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANEL, "example", NotificationManager.IMPORTANCE_DEFAULT);

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    public void sendMsg(View view) throws IOException {
        EditText text = findViewById(R.id.message);
        String s = text.getText().toString();
        if (serialServiceConnection.getConnected()) {
            serialServiceConnection.getService().sendMessage(s);
        }
    }

    public void connectUart(View view) {
        usbConnectionReceiver.startSerialService(this);

    }

    public void switchView(View view) {
        Intent intent = new Intent(this, ProductionActivity.class);
        startActivity(intent);
    }


}