package com.example.departmentmap;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * 加载设置选项卡的内容
 * @author anyang
 *
 */
public class Settings extends PreferenceActivity {
  
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.preferences);
    }
}
