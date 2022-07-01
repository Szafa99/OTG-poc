package com.sander.otg_poc;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
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

    private UsbDeviceConnection connection;
    TextView rcvMsg;
    UsbDevice device;
    String currentMsg = "";
    private SerialInputOutputManager ioManager;

    /////////////////
    UsbSerialPort port;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rcvMsg = findViewById(R.id.recieveMessage);
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void connectUart(View view) throws IOException {

        // Find all available drivers from attached devices.
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return;
        }

        // Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            rcvMsg.setText("No connection");
            PendingIntent usbPermissionIntent = PendingIntent
                    .getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
            manager.requestPermission(driver.getDevice(),usbPermissionIntent );
            return;
        }
        String s ="";
        driver.getPorts().forEach(p-> s.concat(p.getPortNumber()+"|") );
        rcvMsg.setText(s);
        port = driver.getPorts().get(0); // Most devices have just one port (port 0)
        port.open(connection);
        port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);


        rcvMsg.setText(driver.getDevice().getManufacturerName());
        port.setDTR(true); // for arduino, ...
        port.setRTS(true);
        ioManager = new SerialInputOutputManager(port,new SerialListner());
        ioManager.start();
    }


    public void sendMsg(View view) throws IOException {
        EditText text = findViewById(R.id.message);
       String s = text.getText().toString();

//        while (true)
//            if (port.getCTS()) {
                port.write(s.getBytes(StandardCharsets.UTF_8), 1000);
//                return;
//            }
    }

    private class SerialListner implements SerialInputOutputManager.Listener{

        @Override
        public void onNewData(byte[] data) {
//                if (data!=null ) {
//                    for (byte c : data){
//                         if (c == '\n' || c == '\0' || currentMsg.length()>=14) {
//                             synchronized (this) {
//                                 runOnUiThread(() -> {
//                                     rcvMsg.setText(currentMsg);
//                                     currentMsg = "";
//                                 });
//                             }
//                         }else  currentMsg += (char)c;
//                    }
//
//                }
            String incoming = new String(data, StandardCharsets.US_ASCII);
            currentMsg += incoming;
            if(currentMsg.length() > 0 && currentMsg.contains("\n") == true){
                String thisLine = currentMsg.substring(0, currentMsg.indexOf("\n")).trim();
                //Handle this line here
                runOnUiThread( () -> rcvMsg.setText(thisLine) );

                //Trim the processed line from the readBuffer
                currentMsg = currentMsg.substring(currentMsg.indexOf("\n") + 1);
            }
        }

        @Override
        public void onRunError(Exception e) {
        }
    }



}