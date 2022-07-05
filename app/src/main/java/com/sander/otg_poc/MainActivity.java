package com.sander.otg_poc;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final String ACTION_USB_DEVICE_ATTACHED= "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    private static final String ACTION_USB_DEVICE_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";

    private UsbDeviceConnection connection;
    UsbDevice device;
    UsbManager manager;
    UsbSerialPort port;
    UsbSerialDriver driver;


    String currentMsg = "";
    TextView rcvMsg;
    PendingIntent usbPermissionIntent;
    private SerialInputOutputManager ioManager;

    /////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rcvMsg = findViewById(R.id.recieveMessage);

        UsbConnectionReceiver usbConnectionReceiver = new UsbConnectionReceiver();
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        IntentFilter filter2 = new IntentFilter(ACTION_USB_DEVICE_ATTACHED);
        IntentFilter filter3 = new IntentFilter(ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbConnectionReceiver,filter);
        registerReceiver(usbConnectionReceiver,filter2);
        registerReceiver(usbConnectionReceiver,filter3);

        usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);


    }


    public void initConnection(){
        // Find all available drivers from attached devices.
        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) return;

        // Open a connection to the first available driver.
        driver = availableDrivers.get(0);
        device = driver.getDevice();
        connection = manager.openDevice(device );
        if (connection == null) {
            manager.requestPermission(device,usbPermissionIntent );
            return;
        }
    }

    public void startConnection() throws IOException{
        if (driver==null || !manager.hasPermission(device)){
            initConnection();
            return;
        }
        port = driver.getPorts().get(0); // Most devices have just one port (port 0)

        if (port.isOpen()) {
            Toast.makeText(MainActivity.this, "Already connected", Toast.LENGTH_SHORT).show();
            return;
        }
        if (connection==null)
            connection = manager.openDevice(device );

        port.open(connection);
        port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

        Toast.makeText(MainActivity.this, " connected",Toast.LENGTH_LONG).show();
        port.setDTR(true); // for arduino, ...
        port.setRTS(true);
//        port.read(null,0);//flush
        ioManager = new SerialInputOutputManager(port,new SerialListener());
        ioManager.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void connectUart(View view) {
//        if (connection==null || manager.hasPermission(device)==false)
            initConnection();
//        else if(port!=null && !port.isOpen()) {
//            try {
//                startConnection();
//            }catch (Exception e){}
//        }
    }


    public void sendMsg(View view) throws IOException {
        if ( port==null || !port.isOpen()  || !manager.hasPermission(device)) {
            runOnUiThread(()->Toast.makeText(MainActivity.this,"No usb device connection",Toast.LENGTH_LONG).show() );
            return;
        }
        EditText text = findViewById(R.id.message);
        String s = text.getText().toString();

        port.write(s.getBytes(StandardCharsets.UTF_8), 100);
    }

    private class SerialListener implements SerialInputOutputManager.Listener{

        @Override
        public void onNewData(byte[] data) {
            String incoming = new String(data, StandardCharsets.US_ASCII);
            currentMsg += incoming;
            if(currentMsg.length() > 0 && currentMsg.contains("\n") == true){
                String thisLine = currentMsg.substring(0, currentMsg.indexOf("\n")).trim();
                runOnUiThread( () -> rcvMsg.setText(thisLine) );
                currentMsg = currentMsg.substring(currentMsg.indexOf("\n") + 1);
            }
        }

        @Override
        public void onRunError(Exception e) {
            Log.e("SerialListener","Error receiving data");
        }
    }


      class UsbConnectionReceiver extends BroadcastReceiver {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case ACTION_USB_DEVICE_ATTACHED:
                    try {
                        startConnection();
                        rcvMsg.setText(ACTION_USB_DEVICE_ATTACHED);
                          runOnUiThread(()->Toast.makeText(MainActivity.this,"USB connected",Toast.LENGTH_LONG).show());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                    case ACTION_USB_DEVICE_DETACHED:
                        if (port==null) return;
                        try {
                            port.close();
                            runOnUiThread(()->Toast.makeText(MainActivity.this,"USB Device disconnected",Toast.LENGTH_LONG).show());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;

                case ACTION_USB_PERMISSION:
                    synchronized (this){
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            try {
                                startConnection();
                            }catch (Exception e){
                                rcvMsg.setText("er"+e.getMessage());
                            }
                        }else
                            runOnUiThread(()->Toast.makeText(MainActivity.this,"USB prem denied",Toast.LENGTH_LONG).show());
                    }
                    break;
                default:break;
            }

        }

    }




}