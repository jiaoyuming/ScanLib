package com.joule.scanlib;

/**
 * Created by mylo on 2017/7/7.
 */


public interface OnScanSuccessListener {
    /**
     * 扫码成功
     * @param code
     */
    void onScanSuccess(String code);
}
