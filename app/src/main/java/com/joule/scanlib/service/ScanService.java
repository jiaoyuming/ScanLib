package com.joule.scanlib.service;


import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public class ScanService extends AccessibilityService {
    private static OnKeyEvent onKeyEvent;
    public static void setOnKeyEvent(OnKeyEvent onKeyEvent){
        ScanService.onKeyEvent=onKeyEvent;
    }
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }
    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        if(onKeyEvent!=null){
           return onKeyEvent.onKeyEvent(event);
        }
        return super.onKeyEvent(event);
    }
    public interface OnKeyEvent{
        /**
         * 扫码枪事件
         * @param event
         * @return
         */
        boolean onKeyEvent(KeyEvent event);
    }
}
