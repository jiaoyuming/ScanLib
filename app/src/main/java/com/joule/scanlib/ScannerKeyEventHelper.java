package com.joule.scanlib;

import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;


/**
 * 扫码枪事件解析类
 */
class ScannerKeyEventHelper {

    /**
     * 延迟500ms，判断扫码是否完成。
     */
    private final static long MESSAGE_DELAY = 500;

    /**
     * 两次扫码的时间间隔
     */
    private final static long INTERVAL = 5000;
    /**
     * 扫码内容
     */
    private StringBuffer mStringBufferResult;
    /**
     * 大小写区分
     */
    private boolean mCaps;
    private final Handler mHandler;
    private final Runnable mScanningFishedRunnable;
    private OnScanSuccessListener mOnScanSuccessListener;
    private long timeLastScan;

    private final String TAG = ScannerKeyEventHelper.class.getSimpleName();

    public ScannerKeyEventHelper(OnScanSuccessListener onScanSuccessListener) {
        mOnScanSuccessListener = onScanSuccessListener;
        mStringBufferResult = new StringBuffer();
        mHandler = new Handler();
        mScanningFishedRunnable = new Runnable() {
            @Override
            public void run() {
                performScanSuccess();
            }
        };
    }


    /**
     * 返回扫码成功后的结果
     */
    private void performScanSuccess() {
        String code = mStringBufferResult.toString();
        Log.d(TAG, "扫码成功：" + code);
        long now = System.currentTimeMillis();
        if (now - timeLastScan < INTERVAL) {
            Log.d(TAG, "扫码间隔太短，抛弃");
            return;
        }
        timeLastScan = now;
        if (mOnScanSuccessListener != null) {
            mOnScanSuccessListener.onScanSuccess(code);
        }
        mStringBufferResult.setLength(0);
    }


    /**
     * 扫码枪事件解析
     *
     * @param event
     */
    public void analysisKeyEvent(KeyEvent event) {

        int keyCode = event.getKeyCode();

        //字母大小写判断
        checkLetterStatus(event);

        if (event.getAction() == KeyEvent.ACTION_DOWN) {

            char aChar = getInputCode(event);
            Log.d(TAG, "收到字符：" + aChar);

            if (aChar != 0) {
                mStringBufferResult.append(aChar);
            }

            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                //若为回车键，直接返回
                Log.d(TAG, "收到回车键");
                mHandler.removeCallbacks(mScanningFishedRunnable);
                mHandler.post(mScanningFishedRunnable);
            } else {
                //延迟post，若500ms内，有其他事件
                Log.d(TAG, "不是回车键");
                mHandler.removeCallbacks(mScanningFishedRunnable);
                mHandler.postDelayed(mScanningFishedRunnable, MESSAGE_DELAY);
            }

        }
    }

    /**
     * 检查shift键
     * @param event
     */
    private void checkLetterStatus(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT || keyCode == KeyEvent.KEYCODE_SHIFT_LEFT) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                //按着shift键，表示大写
                mCaps = true;
            } else {
                //松开shift键，表示小写
                mCaps = false;
            }
        }
    }


    /**
     * 获取扫描内容
     * @param event
     * @return
     */
    private char getInputCode(KeyEvent event) {

        int keyCode = event.getKeyCode();

        char aChar;

        if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
            //字母
            aChar = (char) ((mCaps ? 'A' : 'a') + keyCode - KeyEvent.KEYCODE_A);
        } else if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            //数字
            aChar = (char) ('0' + keyCode - KeyEvent.KEYCODE_0);
        } else if (keyCode == KeyEvent.KEYCODE_ENTER) {
            aChar = 0;
        } else {
            //其他符号
            aChar = (char) event.getUnicodeChar();
        }

        return aChar;

    }


    public void onDestroy() {
        mHandler.removeCallbacks(mScanningFishedRunnable);
        mOnScanSuccessListener = null;
    }


}





