package com.sander.otg_poc.framework.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.sander.otg_poc.controller.EngineController;
import com.sander.otg_poc.controller.MachineController;
import com.sander.otg_poc.framework.service.SerialService;
import com.sander.otg_poc.utils.EventHandler;
import dalvik.system.DexFile;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class SerialDispatcher implements EventHandler {

    Set<Class> serialControllers;
    Context context;
    SerialService serialService;

    public SerialDispatcher(Context context)  {
        this.context = context;
        this.serialControllers = getAnnotatedClasses(SerialController.class);
    }


    @SuppressLint("NewApi")
    Set<Class> getAnnotatedClasses(Class annotation){
        Set<Class> annotatedClasses = new HashSet<>();
        try {
            DexFile df = new DexFile(context.getApplicationContext().getPackageCodePath());
            for (Enumeration<String> iter = df.entries(); iter.hasMoreElements();) {
                String s = iter.nextElement();
                Class<?> cl = null;
                try {
                    cl = Class.forName(s);
                    Annotation[] declaredAnnotations = cl.getAnnotations();
                    for (Annotation a : declaredAnnotations )
                        if ( a instanceof SerialController || s.equals(EngineController.class.getName()) )
                            annotatedClasses.add(cl);
                }catch (Error | Exception e) {}
            }
        } catch (IOException e) {}


        return annotatedClasses;
    }

    @SuppressLint("NewApi")
    void dispatchMessage(String mapping,Object ... objects) {

        serialControllers.forEach( sc-> {
            List<Method> methods = new ArrayList<>(1);
            SerialController serialController = (SerialController) sc.getDeclaredAnnotation(SerialController.class);
            Method[] declaredMethods = sc.getDeclaredMethods();
            Arrays.stream(declaredMethods).forEach(m -> {
                SerialRequestMapping sm = m.getDeclaredAnnotation(SerialRequestMapping.class);
                if (sm!=null && serialController.serialMapping().concat(sm.mapping()).equals(mapping))
                    methods.add(m);
            });
            if (methods.size()>1)
                throw new RuntimeException(mapping+" MUST BE UNIQUE");
            else if (methods.size()==1) {
                try {
                    methods.get(0).invoke(sc.newInstance(), objects);
                }catch (Exception e){e.printStackTrace();}
            }
        });

    }


    // ONLY INCOMING SERIAL MESSAGES
    public void onNext(String s) {
        String[] split = s.split(SerialRequest.delimiter);
        if (split.length<2) return;
        String method = split[0];
        String mapping = split[1];

        if (SerialRequest.TX.toString().equals(method)) {
                dispatchMessage(mapping,Arrays.copyOfRange(split,2,split.length));
        }
    }

    @Override
    public void onNotify(Object o) {
        if (o instanceof String)
            onNext(o.toString());
    }


    private enum SerialRequest {
        RX("RX"),TX("TX");//receive transmit
        private String serialMethod;
        public final static String delimiter = "/";

        SerialRequest(String serialMethod) {
            this.serialMethod = serialMethod;
        }
    }



}

