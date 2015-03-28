package com.example.departmentmap;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * 步数统计服务
 * 前台运行service
 * @author anyang
 *
 */
public class StepService extends Service {
	// 测试标签
	private static final String TAG = "com.example.departmentmap.StepService";
	// 使用sharedpreferences存储配置信息
    private SharedPreferences mSettings;
    private PedometerSettings mPedometerSettings;
    private SharedPreferences mState;
    private SharedPreferences.Editor mStateEditor;
    // 工具类，返回系统时间
    private Utils mUtils;
    // 传感器管理器
    private SensorManager mSensorManager;
    // 传感器类
    private Sensor mSensor;
    // 步数检测程序
    private StepDetector mStepDetector;
    // 步数统计结果显示
    private StepDisplayer mStepDisplayer;
    // 行进距离显示类
    private DistanceNotifier mDistanceNotifier;
    // 保持屏幕常亮
    private PowerManager.WakeLock wakeLock;
    // 状态栏通知管理器
    private NotificationManager mNM;
    // 步数变量
    private int mSteps;
    
    private float mDistance;
    

    public class StepBinder extends Binder {
        StepService getService() {
            return StepService.this;
        }
    }
    
    @Override
    public void onCreate() {
        Log.i(TAG, "[SERVICE] onCreate");
        super.onCreate();
        
    	// 获取系统通知service
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        // 设置通知参数
        showNotification();
        
        // 加载设置
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        mPedometerSettings = new PedometerSettings(mSettings);
        mState = getSharedPreferences("state", 0);

        mUtils = Utils.getInstance();
        mUtils.setService(this);
        
        new Thread(new Runnable() {  
            @Override  
            public void run() {  
                // 开始执行后台任务  
                acquireWakeLock();
                
                // 开始检测步数
                mStepDetector = new StepDetector();
                // 获取系统传感器管理器对象
                mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
                // 注册检测器
                registerDetector();

                // 注册接收器（Intent.ACTION_SCREEN_OFF：按power键的时候，关闭和打开屏幕都会发送广播，
                // 一个是Intent.ACTION_SCREEN_OFF，还有一个是Intent.ACTION_SCREEN_ON）
                IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
                registerReceiver(mReceiver, filter);

                mStepDisplayer = new StepDisplayer(mPedometerSettings, mUtils);
                mStepDisplayer.setSteps(mSteps = mState.getInt("steps", 0));
                mStepDisplayer.addListener(mStepListener);
                mStepDetector.addStepListener(mStepDisplayer);
                
                mDistanceNotifier = new DistanceNotifier(mDistanceListener, mPedometerSettings, mUtils);
                mDistanceNotifier.setDistance(mDistance = mState.getFloat("distance", 0));
                mStepDetector.addStepListener(mDistanceNotifier);
               
            }  
        }).start();  
        
        // 显示service已启动
        Toast.makeText(this, getText(R.string.started), Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onStart(Intent intent, int startId) {
        Log.i(TAG, "[SERVICE] onStart");
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "[SERVICE] onDestroy");

        // 注销接收器
        unregisterReceiver(mReceiver);
        unregisterDetector();
        
        mStateEditor = mState.edit();
        mStateEditor.putInt("steps", mSteps);
        mStateEditor.putFloat("distance", mDistance);
        mStateEditor.commit();
        
        mNM.cancel(R.string.app_name);

        wakeLock.release();
        
        super.onDestroy();
        
        // 停止检测步数
        mSensorManager.unregisterListener(mStepDetector);

        // 显示service已停止信息
        Toast.makeText(this, getText(R.string.stopped), Toast.LENGTH_SHORT).show();
    }

    private void registerDetector() {
    	// 获取指定类型的传感器对象（加速度传感器）
        mSensor = mSensorManager.getDefaultSensor(
            Sensor.TYPE_ACCELEROMETER /*|  
            Sensor.TYPE_MAGNETIC_FIELD | 
            Sensor.TYPE_ORIENTATION*/);
       // 将传感器对象和传感器操作类绑定
        mSensorManager.registerListener(mStepDetector,
            mSensor,
            // 尽可能快的获取传感器数据
            SensorManager.SENSOR_DELAY_FASTEST);
    }

    // 注销步数检测服务
    private void unregisterDetector() {
        mSensorManager.unregisterListener(mStepDetector);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "[SERVICE] onBind");
        return mBinder;
    }

    // 从activity中接收消息
    private final IBinder mBinder = new StepBinder();

    public interface ICallback {
        public void stepsChanged(int value);
        public void distanceChanged(float value);
    }
    
    // 回调方法
    private ICallback mCallback;
    public void registerCallback(ICallback cb) {
        mCallback = cb;
    }
    
    // 重新加载设置信息
    public void reloadSettings() {
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        
        if (mStepDetector != null) { 
            mStepDetector.setSensitivity(
                    Float.valueOf(mSettings.getString("sensitivity", "10"))
            );
        }
        
        if (mStepDisplayer != null) mStepDisplayer.reloadSettings();
        if (mDistanceNotifier != null) mDistanceNotifier.reloadSettings();
    }
    
    public void resetValues() {
        mStepDisplayer.setSteps(0);
        mDistanceNotifier.setDistance(0);
    }
    
    // 从PaceNotifier传递步数到activity（回调方法）
    private StepDisplayer.Listener mStepListener = new StepDisplayer.Listener() {
        public void stepsChanged(int value) {
            mSteps = value;
            passValue();
        }
        public void passValue() {
            if (mCallback != null) {
                mCallback.stepsChanged(mSteps);
            }
        }
    };
   
    private DistanceNotifier.Listener mDistanceListener = new DistanceNotifier.Listener() {
        public void valueChanged(float value) {
            mDistance = value;
            passValue();
        }
        public void passValue() {
            if (mCallback != null) {
                mCallback.distanceChanged(mDistance);
            }
        }
    };
    
    // 当service运行时，在状态栏显示通知（实现前台service运行）
    private void showNotification() {
        CharSequence text = getText(R.string.app_name);
        Notification notification = new Notification(R.drawable.ic_notification, null,
                System.currentTimeMillis());
        notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        Intent pedometerIntent = new Intent();
        pedometerIntent.setComponent(new ComponentName(this, MainActivity.class));
        pedometerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                pedometerIntent, 0);
        notification.setLatestEventInfo(this, text,
                getText(R.string.notification_subtitle), contentIntent);

        mNM.notify(R.string.app_name, notification);
    }


    // 通过BroadcastReceiver来处理ACTION_SCREEN_OFF事件，也就是当我们按power键关闭屏幕时的处理事件
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
               
                StepService.this.unregisterDetector();
                StepService.this.registerDetector();
                
                if (mPedometerSettings.wakeAggressively()) {
                    wakeLock.release();
                    acquireWakeLock();
                }
            }
        }
    };

    // 设置屏幕状态
    private void acquireWakeLock() {
    	// 获取系统service
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        int wakeFlags;
        if (mPedometerSettings.wakeAggressively()) {
        	// 屏幕暗淡，键盘关闭（由用户活动触发该配置）
            wakeFlags = PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP;
        }
        else if (mPedometerSettings.keepScreenOn()) {
        	// 屏幕暗淡，键盘关闭
            wakeFlags = PowerManager.SCREEN_DIM_WAKE_LOCK;
        }
        else {
        	// 屏幕关闭，键盘关闭
            wakeFlags = PowerManager.PARTIAL_WAKE_LOCK;
        }
        wakeLock = pm.newWakeLock(wakeFlags, TAG);
        // 根据wakeFlags设置， 申请屏幕常亮
        wakeLock.acquire();
    }

}

