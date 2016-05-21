package com.rsv.traffjet;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.TrafficStats;

/**
 * Created by rsv on 21.05.2016.
 */
public class TraffjetAppItem {


    private long tx = 0;
    private long rx = 0;

    private long wifi_tx = 0;
    private long wifi_rx = 0;

    private long mobile_tx = 0;
    private long mobile_rx = 0;

    private long current_tx = 0;
    private long current_rx = 0;

    private ApplicationInfo applicationInfo;

    private boolean isMobile = false;

    public TraffjetAppItem(ApplicationInfo app)
    {
        this.applicationInfo = app;
        updateStats();
    }




    public void updateStats()
    {
        long delta_tx = TrafficStats.getUidTxBytes(applicationInfo.uid) - tx;
        long delta_rx = TrafficStats.getUidRxBytes(applicationInfo.uid) - rx;


        tx = TrafficStats.getUidTxBytes(applicationInfo.uid);
        rx = TrafficStats.getUidRxBytes(applicationInfo.uid);


        current_tx = current_tx + delta_tx;
        current_rx = current_rx + delta_rx;

        if(isMobile == true) {
            mobile_tx = mobile_tx + delta_tx;
            mobile_rx = mobile_rx + delta_rx;
        } else {
            wifi_tx = wifi_tx + delta_tx;
            wifi_rx = wifi_rx + delta_rx;
        }
    }

    public static TraffjetAppItem create(ApplicationInfo app){
        long _tx = TrafficStats.getUidTxBytes(app.uid);
        long _rx = TrafficStats.getUidRxBytes(app.uid);

        if((_tx + _rx) > 0)
            return new TraffjetAppItem(app);
        return null;
    }

    public int getTotalUsageKb() {
        return Math.round((tx + rx)/ 1024);
    }

    public int getWifiKb() {
        return Math.round((wifi_tx + wifi_rx)/ 1024);
    }

    public int getMobileKb() {
        return Math.round((mobile_rx + mobile_tx)/ 1024);
    }

    public String getApplicationLabel(PackageManager _packageManager) {
        return _packageManager.getApplicationLabel(applicationInfo).toString();
    }

    public Drawable getIcon(PackageManager _packageManager) {
        return _packageManager.getApplicationIcon(applicationInfo);
    }

    public void setMobileTraffic(boolean isMobile) {
        this.isMobile = isMobile;
    }





}
