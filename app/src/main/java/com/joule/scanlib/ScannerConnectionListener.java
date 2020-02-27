package com.joule.scanlib;

/**
 * Created by mylo on 2017/7/7.
 * 扫描连接的listener
 */

public interface ScannerConnectionListener {
    /**
     * 新设备
     * @param deviceId
     */
    void onDeviceAdded(Integer deviceId);

    /**
     * 设备被remove掉之后，有可能deviceid为null，deviceid此处需要判断是否为null
     * @param deviceId
     */
    void onDeviceRemoved(Integer deviceId);
}
