package com.lucky.scanlib.scanner;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.InputDevice;
import android.view.KeyEvent;

import com.lucky.scanlib.scanner.service.ScanService;

import java.lang.ref.SoftReference;

/**
 * Created by mylo on 2017/7/6.
 * 扫码器调用类
 */

public class ScannerHelper implements InputManager.InputDeviceListener ,ScanService.OnKeyEvent{

    private static final int DEVICE_ADDED = 15;
    private static final int DEVICE_REMOVED = 16;

    private static final int SOURCE = 8451;


    private final InputManager manager;
    private ScannerKeyEventHelper keyEventHelper;
    private InputDeviceHandler handler;
    private ScannerConnectionListener scannerConnectionListener;
    private Context context;
    public ScannerHelper(Context context) {
        this.context=context;
        manager = (InputManager) context.getSystemService(Context.INPUT_SERVICE);
        handler = new InputDeviceHandler(this);
    }

    /**
     * 初始化时调用
     *
     * @param onScanSuccessListener     当扫到码时的成功回调
     * @param scannerConnectionListener 监听输入设备是否连接上了
     */
    public void register(OnScanSuccessListener onScanSuccessListener, ScannerConnectionListener scannerConnectionListener) {
        keyEventHelper = new ScannerKeyEventHelper(onScanSuccessListener);
        this.scannerConnectionListener = scannerConnectionListener;
        manager.registerInputDeviceListener(this, handler);
        ScanService.setOnKeyEvent(this);
    }


    /**
     * 反注册方法
     */
    public void unRegister() {
        manager.unregisterInputDeviceListener(this);
        scannerConnectionListener = null;
        ScanService.setOnKeyEvent(null);
        keyEventHelper.onDestroy();
    }


    public int getSources() {
        return SOURCE;
    }

    /**
     * 检测输入设备是否是扫码器
     *
     * @param context
     * @return 是的话返回true，否则返回false
     */
    public boolean isInputFromScanner(Context context, KeyEvent event) {
        if (event.getDevice() == null) {
            return false;
        }
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
            //实体按键，若按键为返回、音量加减、返回false
            return false;
        }
        if (event.getDevice().getSources() == (InputDevice.SOURCE_KEYBOARD | InputDevice.SOURCE_DPAD | InputDevice.SOURCE_CLASS_BUTTON)) {
            //虚拟按键返回false
            return false;
        }
        Configuration cfg = context.getResources().getConfiguration();
        return cfg.keyboard != Configuration.KEYBOARD_UNDEFINED;
    }

    /**
     * 打开设置-辅助功能页
     * @param context
     */
    public void openAccessibilitySetting(Context context){
        context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    }
    /**
     *
     * @param context
     * @return true辅助功能开 false辅助功能关
     */
    public boolean isAccessibilitySettingsOn(Context context) {
        int accessibilityEnabled = 0;
        final String service = context.getPackageName() + "/" + ScanService.class.getCanonicalName();
        try {
            //获取setting里辅助功能的开启状态
            accessibilityEnabled = Settings.Secure.getInt(
                    context.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibilityEnabled == 1) {
            //获取辅助功能里所有开启的服务 包名列表
            String settingValue = Settings.Secure.getString(
                    context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                //转换程集合
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    //判断当前包名是否在服务集合里
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
    /**
     * 在keyEvent中调用
     *
     * @param event
     */
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (keyEventHelper != null) {
            keyEventHelper.analysisKeyEvent(event);
            return true;
        }
        return false;
    }
    @Override
    public boolean onKeyEvent(KeyEvent event) {
        if(keyEventHelper!=null&&isInputFromScanner(context,event)){
            keyEventHelper.analysisKeyEvent(event);
            return true;
        }
        return false;
    }

    @Override
    public void onInputDeviceAdded(int i) {
        Message msg = Message.obtain();
        msg.obj = i;
        msg.what = DEVICE_ADDED;
        handler.sendMessage(msg);
    }

    @Override
    public void onInputDeviceRemoved(int i) {
        Message msg = Message.obtain();
        msg.obj = i;
        msg.what = DEVICE_REMOVED;
        handler.sendMessage(msg);
    }

    @Override
    public void onInputDeviceChanged(int i) {

    }

    private static class InputDeviceHandler extends Handler {

        private SoftReference<ScannerHelper> ref;

        private InputDeviceHandler(ScannerHelper helper) {
            ref = new SoftReference<>(helper);
        }

        @Override
        public void handleMessage(Message msg) {
            Integer deviceId = (Integer) msg.obj;
            switch (msg.what) {
                case DEVICE_ADDED:
                    if (ref.get() != null && ref.get().scannerConnectionListener != null) {
                        ref.get().scannerConnectionListener.onDeviceAdded(deviceId);
                    }
                    break;
                case DEVICE_REMOVED:
                    if (ref.get() != null && ref.get().scannerConnectionListener != null) {
                        ref.get().scannerConnectionListener.onDeviceRemoved(deviceId);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
