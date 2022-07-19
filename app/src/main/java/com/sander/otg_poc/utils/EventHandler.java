package com.sander.otg_poc.utils;

public interface EventHandler {

    default void emitEvent( Object o){onNotify(o);}

    void onNotify(Object o);

}
