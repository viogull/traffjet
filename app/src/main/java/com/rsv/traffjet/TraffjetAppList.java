package com.rsv.traffjet;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TraffjetAppList {

    private Context mContext;
    private Timer mTimer;
    private TimerTask mTask;

    private int APP_UPDATE_INTERVAL = 50000;

    private boolean isWifiEnabled = false;
    private boolean isMobileEnabled = false;

    private List<TraffjetAppItem> mApplicationItemList = new ArrayList<>();

    public TraffjetAppList(Context _context) {
        mContext = _context;
        updateNetworkState();
    }

    public void Start() {
        mTask = new TimerTask() {
            @Override
            public void run() {
                update();
            }
        };

        mTimer = new Timer();
        mTimer.schedule(mTask, 0, APP_UPDATE_INTERVAL);
    }

    public void Stop() {
        if (mTimer != null) {
            mTimer.purge();
            mTimer.cancel();
        }
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }
    }

    public void update() {
        updateNetworkState();
        if(mApplicationItemList != null) {
            for (int i = 0, l = mApplicationItemList.size(); i < l; i++) {
                mApplicationItemList.get(i).setMobileTraffic(isMobileEnabled);
                mApplicationItemList.get(i).updateStats();
            }
        } else {
            for (ApplicationInfo app : mContext.getPackageManager().getInstalledApplications(0)) {
                TraffjetAppItem item = new TraffjetAppItem(app);
                item.setMobileTraffic(isMobileEnabled);
                mApplicationItemList.add(item);
            }
        }
    }

    private void updateNetworkState() {
        isWifiEnabled = isConnectedWifi();
        isMobileEnabled = isConnectedMobile();
    }

    public List<TraffjetAppItem> getList() {
        return mApplicationItemList;
    }


    public boolean isConnectedWifi(){
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    public boolean isConnectedMobile(){
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

}
