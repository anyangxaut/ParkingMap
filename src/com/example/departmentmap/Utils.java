package com.example.departmentmap;

import android.app.Service;
import android.text.format.Time;

public class Utils {
    private static final String TAG = "Utils";
    private Service mService;

    private static Utils instance = null;

    private Utils() {
    }
     
    public static Utils getInstance() {
        if (instance == null) {
            instance = new Utils();
        }
        return instance;
    }
    
    public void setService(Service service) {
        mService = service;
    }
    
    // 返回当前系统时间
    public static long currentTimeInMillis() {
        Time time = new Time();
        // 设置到当前时间
        time.setToNow();
        // 返回毫秒数
        return time.toMillis(false);
    }
}
