package com.example.departmentmap;

import java.util.ArrayList;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
/**
 * 步数检测类，实现了传感器数据的获取与处理
 * @author anyang
 *
 */
public class StepDetector implements SensorEventListener
{
    private final static String TAG = "StepDetector";
    
    private float   mLimit = 10;
    private float   mLastValues[] = new float[3*2];
    private float   mScale[] = new float[2];
    private float   mYOffset;

    private float   mLastDirections[] = new float[3*2];
    private float   mLastExtremes[][] = { new float[3*2], new float[3*2] };
    private float   mLastDiff[] = new float[3*2];
    private int     mLastMatch = -1;
    // 记录起始时间和结束时间
    private long start = 0, end = 0;
    
    private ArrayList<StepListener> mStepListeners = new ArrayList<StepListener>();
    
    // 设置基本参数
    public StepDetector() {
        int h = 480; 
        mYOffset = h * 0.5f;
        // SensorManager.STANDARD_GRAVITY:Standard gravity (g) on Earth
        mScale[0] = - (h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
        // SensorManager.MAGNETIC_FIELD_EARTH_MAX:Maximum magnetic field on Earth's surface 
        mScale[1] = - (h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
    }
    // 设置灵敏度
    public void setSensitivity(float sensitivity) {
    	// 1.97  2.96  4.44  6.66  10.00  15.00  22.50  33.75  50.62
        mLimit = sensitivity; 
    }
    // 添加步数监听器
    public void addStepListener(StepListener sl) {
        mStepListeners.add(sl);
    }
    
    // 从传感器获取数据信息并进行处理，计算是否产生步伐
    public void onSensorChanged(SensorEvent event) {
    	// 获取sensor对象
        Sensor sensor = event.sensor; 
        
        synchronized (this) {
        	// 如果获取到的是方向传感器
            if (sensor.getType() == Sensor.TYPE_ORIENTATION) {
            }else {
            	// 如果获取到的是加速度传感器
                int j = (sensor.getType() == Sensor.TYPE_ACCELEROMETER) ? 1 : 0;
                if (j == 1) {
                	// 计算各轴加速度之和
                    float vSum = 0;
                    // 获取x，y，z轴加速度
                    for (int i=0 ; i<3 ; i++) {
                    	// 计算各轴速度
                        final float v = mYOffset + event.values[i] * mScale[j];
                        // 计算速度总和
                        vSum += v;
                    }
                    
                    int k = 0;
                    // 计算平均速度
                    float v = vSum / 3;
                    // 
                    float direction = (v > mLastValues[k] ? 1 : (v < mLastValues[k] ? -1 : 0));
                    if (direction == - mLastDirections[k]) {
                        // 改变方向，判断大于还是小于
                        int extType = (direction > 0 ? 0 : 1); 
                        mLastExtremes[extType][k] = mLastValues[k];
                        // 返回变化的绝对值，也就是计算波动大小
                        float diff = Math.abs(mLastExtremes[extType][k] - mLastExtremes[1 - extType][k]);
                        // 如果波动大于设定的阈值（灵敏度）
                        if (diff > mLimit) {
                            
                            boolean isAlmostAsLargeAsPrevious = diff > (mLastDiff[k]*2/3);
                            boolean isPreviousLargeEnough = mLastDiff[k] > (diff/3);
                            boolean isNotContra = (mLastMatch != 1 - extType);
                            // 当以上三个条件均满足的时候，步数加1
                            if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough && isNotContra) {
                            	// 获取系统当前时间（毫秒）
                            	 end= System.currentTimeMillis(); 
                            	// 因为人们的反应速度最快为0.2s，因此当发生动作的时间间隔小与0.2s时，则认为是外界干扰
                            	 if(end - start > 500) {
                            		 Log.i(TAG, "step");
                            		 // 发送通知，通过回调方法将步数显示在ui界面上
                                     for (StepListener stepListener : mStepListeners) {
                                         stepListener.onStep();
                                     }
                                     mLastMatch = extType;
                                     start= end; 
                            	 }  
                            }
                            else {
                                mLastMatch = -1;
                            }
                        }
                        // 记录上一次波动的值，也就是波动大小
                        mLastDiff[k] = diff;
                    }
                    mLastDirections[k] = direction;
                    mLastValues[k] = v;
                }
            }
        }
    }
    
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
       
    }

}