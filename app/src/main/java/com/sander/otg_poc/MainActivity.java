package com.sander.otg_poc;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.sander.otg_poc.service.SerialServiceConnection;
import com.sander.otg_poc.service.UsbConnectionReceiver;

import java.io.IOException;


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


//    private class UsbConnectionReceiver extends BroadcastReceiver {
//        private Intent usbIntent;
//
//          public SerialServiceConnection getSerialServiceConnection() {
//              return serialServiceConnection;
//          }
//
//          private SerialServiceConnection serialServiceConnection = new SerialServiceConnection();
//
//
//        public void startSerialService(Context context){
//            usbIntent = new Intent(context, SerialService.class);
//            context.bindService(usbIntent,serialServiceConnection,Context.BIND_AUTO_CREATE);
//            context.startService(usbIntent);
//        }
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            switch (intent.getAction()){
//                case ACTION_USB_DEVICE_ATTACHED:
//                    startSerialService(MainActivity.this);
//                    try {
//                        startSerialService(context);
//                    } catch (Exception e) {
//                        runOnUiThread(()->Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_LONG).show());
//                    }
//                    break;
//                    case ACTION_USB_DEVICE_DETACHED:
//                        MainActivity.this.unbindService(serialServiceConnection);
//                        break;
//                case ACTION_USB_PERMISSION:
//                    synchronized (this){
//                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
//                            try {
//                                    serialServiceConnection.getService().startConnection();
//                            } catch (IOException e) {
//                                runOnUiThread(()->Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_LONG).show());
//                            }
//                        }else
//                            runOnUiThread(()->Toast.makeText(MainActivity.this,"USB prem denied",Toast.LENGTH_LONG).show());
//                    }break;
//                default:break;
//            }
//
//        }
//
//    }




}