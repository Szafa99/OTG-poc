package com.sander.otg_poc.framework.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.sander.otg_poc.MainActivity;
import com.sander.otg_poc.ProductionActivity;
import com.sander.otg_poc.R;
import com.sander.otg_poc.utils.EventHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.sander.otg_poc.MainActivity.NOTIFICATION_CHANEL;

public class SerialService extends Service {
    public static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    public static final String INIT_CONNECTION = "INIT_CONNECTION";

    private UsbDeviceConnection connection;
    private UsbDevice device;
    private UsbManager manager;
    private UsbSerialPort port;
    private UsbSerialDriver driver;
    private Boolean serviceStarted = false;
    private SerialInputOutputManager ioManager;
    private PendingIntent usbPermissionIntent;
    SerialListener serialListener = new SerialListener();

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
        usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getAction()) {
            case UsbManager.EXTRA_PERMISSION_GRANTED:
                try {
                    startConnection();
                    serviceStarted = true;
                } catch (IOException e) {
                    Toast.makeText(this,"Failed to start",Toast.LENGTH_SHORT).show();
                }
                break;
            case INIT_CONNECTION:
                initConnection();
                break;
        }

        return START_NOT_STICKY;
    }

    private void startForeGroundService(){
        Notification notification =null;

        Intent resultIntent = new Intent(this,ProductionActivity.class);
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, NOTIFICATION_CHANEL)
                    .setContentTitle("Prod procces")
                    .setContentIntent(resultPendingIntent)
                    .setContentText("Production process is running")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setOngoing(true)
                    .build();
        }

        startForeground(1, notification);

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

        manager = (UsbManager) getApplicationContext().getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()){
            Toast.makeText(this, "No divers found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Open a connection to the first available driver.
        if (driver==null || device==null) {
            driver = availableDrivers.get(0);
            device = driver.getDevice();
        }
        connection = manager.openDevice(device);

//        if (connection == null || !manager.hasPermission(device)) {
        manager.requestPermission(device,usbPermissionIntent );
//        }
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
        initConnection();

        if (connection==null) {
            Toast.makeText(this, "Usb connection failed", Toast.LENGTH_SHORT).show();
            return;
        }
        port.open(connection);
        port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

        Toast.makeText(this, " connected",Toast.LENGTH_LONG).show();
        port.setDTR(true); // for arduino, ...
        port.setRTS(true);
        ioManager = new SerialInputOutputManager(port,serialListener);
        ioManager.start();
        startForeGroundService();
    }
    public boolean sendMessage(String message){
        try {
            if (!serviceStarted || port == null || !port.isOpen() || manager == null || device == null || !manager.hasPermission(device) || connection==null) {
                Toast.makeText(this, "No usb device connection", Toast.LENGTH_LONG).show();
                return false;
            }
            try {
                port.write(message.getBytes(StandardCharsets.UTF_8), 100);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }catch (Exception e){
            return false;
        }
        return true;
    }




}