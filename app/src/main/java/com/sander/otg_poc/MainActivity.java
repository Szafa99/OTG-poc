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

        port = driver.getPorts().get(0); // Most devices have just one port (port 0)
        port.open(connection);
        port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);


        rcvMsg.setText(driver.getDevice().getManufacturerName());
        ioManager = new SerialInputOutputManager(port,new SerialListner());
        ioManager.start();
    }


    public void sendMsg(View view) throws IOException {
        if (!port.isOpen())
            port.open(connection);

        EditText text = findViewById(R.id.message);
       String s = text.getText().toString();
       port.write(s.getBytes(StandardCharsets.UTF_8), 0);

    }

    private class SerialListner implements SerialInputOutputManager.Listener{

        @Override
        public void onNewData(byte[] data) {
//            rcvMsg.setText(data.toString());
                if (data!=null)
                    rcvMsg.setText( new String(data,StandardCharsets.UTF_8) );
                else
                    Toast.makeText(MainActivity.this, "data is null", Toast.LENGTH_SHORT);

        }

        @Override
        public void onRunError(Exception e) {
            rcvMsg.setText("Serial listner error ");
        }
    }



}