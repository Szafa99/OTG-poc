package com.sander.otg_poc;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.sander.otg_poc.presenter.EventHandler;
import com.sander.otg_poc.service.SerialService;
import com.sander.otg_poc.service.SerialServiceConnection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Flow;


public class MainActivity extends AppCompatActivity {
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final String ACTION_USB_DEVICE_ATTACHED= "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    private static final String ACTION_USB_DEVICE_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";

    TextView rcvMsg;
    UsbConnectionReceiver usbConnectionReceiver;
    SerialServiceConnection serialServiceConnection;
    /////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        rcvMsg = findViewById(R.id.recieveMessage);

        usbConnectionReceiver = new UsbConnectionReceiver();
        serialServiceConnection = usbConnectionReceiver.getSerialServiceConnection();

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        IntentFilter filter2 = new IntentFilter(ACTION_USB_DEVICE_ATTACHED);
        IntentFilter filter3 = new IntentFilter(ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbConnectionReceiver,filter);
        registerReceiver(usbConnectionReceiver,filter2);
        registerReceiver(usbConnectionReceiver,filter3);
    }





    public void sendMsg(View view) throws IOException {
        if (serialServiceConnection.getService().eventHandler==null){
            serialServiceConnection.getService().eventHandler= (o)-> {
                    if (o instanceof String)
                        rcvMsg.setText((String)o);
                };
        }
        EditText text = findViewById(R.id.message);
        String s = text.getText().toString();
        if (serialServiceConnection.getConnected()) {
            serialServiceConnection.getService().sendMessage(s);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public void connectUart(View view) {
        usbConnectionReceiver.startSerialService(this);

//        Flow.Publisher<String> serialListener = serialServiceConnection.getService().getSerialListener();
//        serialListener.subscribe(new Flow.Subscriber<String>() {
//            @Override
//            public void onSubscribe(Flow.Subscription subscription) {
//
//            }
//
//            @Override
//            public void onNext(String s) {
//                rcvMsg.setText(s);
//            }
//
//            @Override
//            public void onError(Throwable throwable) {
//
//            }
//
//            @Override
//            public void onComplete() {
//
//            }
//        });
    }

    public void switchView(View view) {
        Intent intent = new Intent(this, ProductionActivity.class);
        startActivity(intent);
    }


    private class UsbConnectionReceiver extends BroadcastReceiver {
        private Intent usbIntent;

          public SerialServiceConnection getSerialServiceConnection() {
              return serialServiceConnection;
          }

          private SerialServiceConnection serialServiceConnection = new SerialServiceConnection();


        public void startSerialService(Context context){
            usbIntent = new Intent(context, SerialService.class);
            context.bindService(usbIntent,serialServiceConnection,Context.BIND_AUTO_CREATE);
            context.startService(usbIntent);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case ACTION_USB_DEVICE_ATTACHED:
                    startSerialService(MainActivity.this);
                    try {
                        startSerialService(context);
                    } catch (Exception e) {
                        runOnUiThread(()->Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_LONG).show());
                    }
                    break;
                    case ACTION_USB_DEVICE_DETACHED:
                        MainActivity.this.unbindService(serialServiceConnection);
                        break;
                case ACTION_USB_PERMISSION:
                    synchronized (this){
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            try {
                                    serialServiceConnection.getService().startConnection();
                            } catch (IOException e) {
                                runOnUiThread(()->Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_LONG).show());
                            }
                        }else
                            runOnUiThread(()->Toast.makeText(MainActivity.this,"USB prem denied",Toast.LENGTH_LONG).show());
                    }break;
                default:break;
            }

        }

    }




}