package com.example.departmentmap;

/**
 * 计算并显示行进的距离：distance = steps * step_length
 * @author anyang
 *
 */
public class DistanceNotifier implements StepListener{

    public interface Listener {
        public void valueChanged(float value);
        public void passValue();
    }
    private Listener mListener;
    
    float mDistance = 0;
    
    PedometerSettings mSettings;
    Utils mUtils;

    float mStepLength;

    public DistanceNotifier(Listener listener, PedometerSettings settings, Utils utils) {
        mListener = listener;
        mUtils = utils;
        mSettings = settings;
        reloadSettings();
    }
    public void setDistance(float distance) {
        mDistance = distance;
        notifyListener();
    }
    
    public void reloadSettings() {
        mStepLength = mSettings.getStepLength();
        notifyListener();
    }
    
    public void onStep() {
//        
//        if (mIsMetric) {
//        	// 厘米到千米的转换：100000倍
//            mDistance += (float)(mStepLength / 100000.0); 
//        }
//        else {
//        	// 英寸到英里的转换：63360倍
//            mDistance += (float)(mStepLength / 63360.0); 
//        }
    	// 距离单位：cm
    	mDistance += mStepLength;
        
        notifyListener();
    }
    
    private void notifyListener() {
        mListener.valueChanged(mDistance);
    }
    
    public void passValue() {
        // StepListener的回调 - Not implemented
    }

}

