package com.sander.otg_poc.service;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.sander.otg_poc.presenter.EventHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Flow;

public class SerialService extends Service {
    public static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private UsbDeviceConnection connection;
    private UsbDevice device;
    private UsbManager manager;
    private UsbSerialPort port;
    private UsbSerialDriver driver;
    private String currentMsg = "";
    private Boolean serviceStarted = false;
    private SerialInputOutputManager ioManager;
    private PendingIntent usbPermissionIntent;
//    private SerialListener serialListener;

    private final IBinder binder = new LocalBinder();


    public class LocalBinder extends Binder {
        SerialService getService() {
            return SerialService.this;
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        serialListener = new SerialListener();
        usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            startConnection();
            serviceStarted = true;
        }catch (Exception e){}
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        try {
            port.close();
            driver=null;
            serviceStarted = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent){
        return true;
    }
    private void initConnection(){
        // Find all available drivers from attached devices.
        Toast.makeText(this, "Init connection", Toast.LENGTH_SHORT).show();

        manager = (UsbManager) getApplicationContext().getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()){
            Toast.makeText(this, "No divers found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Open a connection to the first available driver.
        driver = availableDrivers.get(0);
        device = driver.getDevice();
        connection = manager.openDevice(device);

        if (connection == null) {
            manager.requestPermission(device,usbPermissionIntent );
        }
    }

    public void startConnection() throws IOException{
        if (driver==null || !manager.hasPermission(device) || device==null){
            initConnection();
            return;
        }
        port = driver.getPorts().get(0); // Most devices have just one port (port 0)

        if (port.isOpen()) {
            Toast.makeText(this, "Already connected", Toast.LENGTH_SHORT).show();
            return;
        }
        int maxAttempts =2;

        while (connection==null && maxAttempts>0) {
            try {
                Thread.sleep(2000);
                initConnection();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            maxAttempts--;
        }


        if (connection==null) {
            Toast.makeText(this, "Usb connection failed", Toast.LENGTH_SHORT).show();
            return;
        }
        port.open(connection);
        port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

        Toast.makeText(this, " connected",Toast.LENGTH_LONG).show();
        port.setDTR(true); // for arduino, ...
        port.setRTS(true);
        ioManager = new SerialInputOutputManager(port,new SerialListener());
        ioManager.start();
    }

//    public SerialListener  getSerialListener() {
//        return serialListener;
//    }
    public EventHandler eventHandler=null;

    public boolean sendMessage(String message){

        if ( !serviceStarted || port==null || !port.isOpen()  || !manager.hasPermission(device)) {
            Toast.makeText(this,"No usb device connection",Toast.LENGTH_LONG).show();
            return false;
        }
        try {
            port.write(message.getBytes(StandardCharsets.UTF_8), 1000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

 private class SerialListener implements SerialInputOutputManager.Listener {


     @Override
     public void onNewData(byte[] data) {
         String incoming = new String(data, StandardCharsets.US_ASCII);
         currentMsg += incoming;
         if (currentMsg.length() > 0 && currentMsg.contains("\n") == true) {
             String thisLine = currentMsg.substring(0, currentMsg.indexOf("\n")).trim();
             eventHandler.emitEvent(thisLine);
         }
     }

     @Override
     public void onRunError(Exception e) {
         Log.e("SerialListener", "Error receiving data");
     }
 }


//
//    @SuppressLint("NewApi")
//    private class SerialListener implements SerialInputOutputManager.Listener, Flow.Publisher<String>{
//
//        List <Flow.Subscriber> subscribers;
//
//        @Override
//        public void onNewData(byte[] data) {
//            String incoming = new String(data, StandardCharsets.US_ASCII);
//            currentMsg += incoming;
//            if(currentMsg.length() > 0 && currentMsg.contains("\n") == true){
//                String thisLine = currentMsg.substring(0, currentMsg.indexOf("\n")).trim();
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                    subscribers.forEach(s->s.onNext(thisLine));
//                }
//            }
//        }
//
//        @Override
//        public void onRunError(Exception e) {
//            Log.e("SerialListener","Error receiving data");
//        }
//
//        @Override
//        public void subscribe(Flow.Subscriber subscriber) {
//            subscribers.add(subscriber);
//        }


//    }

}