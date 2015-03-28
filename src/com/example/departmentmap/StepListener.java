package com.example.departmentmap;

/**
 * 步数统计监听器接口定义
 * @author anyang
 *
 */
public interface StepListener {
	// 计算步数
    public void onStep();
    // 传递步数信息
    public void passValue();
}

