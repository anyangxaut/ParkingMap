package com.example.departmentmap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.util.MySqliteHelper;
import com.example.util.Vertex;
import com.ls.widgets.map.MapWidget;
import com.ls.widgets.map.config.OfflineMapConfig;
import com.ls.widgets.map.interfaces.Layer;
import com.ls.widgets.map.model.MapObject;

/*
 * mAppWidget是一个代码库，使用它可以很方便的为android手机开发自定义地图的应用。
 * 这个类库提供了很多服务，方便android开发者集成地图到自己的项目，同时支持放大，缩小，气泡，图层等功能。
 * http://blog.csdn.net/wangyuetingtao/article/details/10174455
 * 1.数据库对应关系
 * 2.路径导航
 * 3.计步器
 */
public class MainActivity extends BaseActivity {
	// 调试TAG标识
	private static final String TAG = "MainActivity";
	// 图层id标识
	private static final int PARKING_LAYER = 1;
	// 地图对象实例
	private MapWidget map = null;
	// 初始化地图缩放级别
	private static final int initialZoomLevel = 10;
	// 创建或获取布局实例
	private LinearLayout layout = null;
	// 图层实例
	private Layer layer = null;
	// 设置地图对象id
//	private static int objectId = 0;
	// 获取qrcode的识别结果
	private String result = "";
	// 声明MySqliteHelper类对象
	private MySqliteHelper mySqliteHelper = null;
	
	// 应用设置信息
    private SharedPreferences mSettings;
    private PedometerSettings mPedometerSettings;
    // 获取ui控件，并显示步数统计结果
//    private TextView mStepValueView;
    private int mStepValue;
    
    // 退出标志
    private boolean mQuitting = false;

    // 当service正在运行，该tag为true
    private boolean mIsRunning;
    // 当前行进距离
    private float mDistanceValue;
    // 记录路径导航后的路线点
    private List<Vertex> path = null;
    // 判断当前是否进行了路径导航
    private boolean isNavigation = false;
    // poi点id，每一个poi点均有一个独一无二的id标志
	private int tmp = 3;
	// 记录起点和终点的像素坐标
	private ArrayList<Integer> pixdotx = new ArrayList<Integer>();
	private ArrayList<Integer> pixdoty = new ArrayList<Integer>();
	// 计步器导航坐标id
	private int pedometerNavigation = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 加载布局文件
		setContentView(R.layout.activity_main);

		// 初始化地图实例
		initMap();

		// 创建地图图层
		layer = map.createLayer(PARKING_LAYER);
		
		// 实例化MySqliteHelper类对象
		mySqliteHelper = new MySqliteHelper(MainActivity.this);
		
		// 1、打开Preferences，名称为mapFlag，如果存在则打开它，否则创建新的Preferences
		// 0代表该文件是私有数据，只能被应用本身访问，在该模式下，写入的内容会覆盖原文件的内容，
		// 如果想把新写入的内容追加到原文件中，可以使用Activity.MODE_APPEND 
		SharedPreferences mapFlag = getSharedPreferences("mapflag", 0);
		// 当SharedPreferences中对应key的值不存在时，该操作返回默认值，在本程序中是true
		if(mapFlag.getBoolean("flag", true) == true){			
		// 初始化数据库信息
		initSqlite();
		// 设置flag为false
		SharedPreferences.Editor editor = mapFlag.edit();
		//3、存放数据
		editor.putBoolean("flag", false);
		//4、完成提交
		editor.commit();
		}
	}

	private void initMap() {

		// 使用反射机制去掉水印logo
		Class<?> c = null;
		try {
			// 反射找到Resources类
			c = Class.forName("com.ls.widgets.map.utils.Resources");
			Object obj = c.newInstance();
			// 找到Logo 属性，是一个数组
			Field field = c.getDeclaredField("LOGO");
			field.setAccessible(true);
			// 将LOGO字段设置为null
			field.set(obj, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 初始化地图类,参数1：上下文;参数2：rootmapfolder地图根路径，也就是assets文件夹下的首个文件夹名称;参数3：地图缩放级别
		map = new MapWidget(MainActivity.this, "mdepartment", initialZoomLevel);

		// 设置map属性参数
		OfflineMapConfig config = map.getConfig();
		config.setZoomBtnsVisible(true); // 设置缩放按钮可见
		config.setPinchZoomEnabled(true); // 设置可以通过pinch方式缩放地图
		config.setFlingEnabled(true); // 设置可以滑动地图
		config.setMapCenteringEnabled(true); // 设置map居中显示，这样就可以不显示背景

		// 获取当前地图缩放级别，默认为10
		Log.d(TAG, "getZoomLevel：" + map.getZoomLevel());

		// 获取layout布局对象
		layout = (LinearLayout) findViewById(R.id.mainLayout);

		// 添加地图实例到layout布局中
		layout.addView(map);
	}
	
	
	private void initSqlite(){
		// qrcode计数
		int qrcode = 0;
		// 控制行
		for(int i = 0; i < 8; i++){
			// 控制列
			for(int j = 0; j < 10; j++){
				// 添加qrcode和像素的一一对应关系
				mySqliteHelper.addDB(qrcode + "" , (j * 46 - 19), (i * 44 - 19));
				qrcode++;
			}
			
		}
		
	}

	private void addPOI(String qrcode, int objectId, int image) {
		
		
		if(layer.getMapObjectCount() > objectId){
			
//			 根据objectId获取当前要创建的mapobject对象，如果已经存在该对象，则remove，如果没有则创建
			Log.d("objectId", "objectId:" + objectId);
			Log.d("MapObject", "MapObject:" + layer.getMapObjectByIndex(objectId));
			
			if(layer.getMapObjectByIndex(objectId) != null){
				
				layer.removeMapObject(layer.getMapObjectByIndex(objectId));
				
				Log.d("hehe", "hehe");
			}
			
		}
		
		// 创建地图对象图标
		Drawable drawable = getResources().getDrawable(
				image);
		// 计算像素点坐标
		// 实例化MySqliteHelper类对象
		String[] qr = new String[1];
		qr[0] = qrcode;
		int[] pix = mySqliteHelper.selectDB(qr);
		// 创建地图对象(在本地图中一个方格的长和宽均为35像素)
		MapObject mapObject = new MapObject(objectId, // 设置对象id

				drawable, // 设置图标

				pix[0], pix[1], // 像素坐标

				11, 33, // 中心点坐标

				true, // 可触摸

				true); // 可扩展
		
		Log.d(TAG, "" + "pix：" + pix[0] + "," + pix[1]);

		// 添加地图对象到图层中(任何位置)
		layer.addMapObject(mapObject);
		// 对象id加1,为下一对象的创建提供新的id
//		 objectId += 1;

	}

	// 二维码扫描
	private void qrCode(int i) {
		// 二维码扫描CaptureActivity
		Intent openCameraIntent = new Intent(MainActivity.this,
				CaptureActivity.class);
		// 返回扫描结果
		startActivityForResult(openCameraIntent, i);
	}

	// 获取二维码识别结果，并在地图上进行显示
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {

			Bundle bundle = data.getExtras();
			result = bundle.getString("result");
//			result = Integer.parseInt(scanResult);
			Log.d(TAG, "" + "result：" + result);
			
			if (requestCode == 0) {
				// 在地图上显示地图对象,起点地图对象id为0
				addPOI(result, 0, R.drawable.map_object_start);
			}else if(requestCode == 1){
				// 在地图上显示地图对象，终点地图对象id为1
				addPOI(result, 1, R.drawable.map_object_end);
			}

		}
	}
	// 初始化计步器参数
	private void initPedometer(){
		
        // 初始化步数
        mStepValue = 0;
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        mPedometerSettings = new PedometerSettings(mSettings);
        
        // 读取preferences中的信息，判断service是否正在运行
        mIsRunning = mPedometerSettings.isServiceRunning();
        
        // 启动service
        if (!mIsRunning && mPedometerSettings.isNewStart()) {
            startStepService();
            bindStepService();
        }
        else if (mIsRunning) {
            bindStepService();
        }
        
        mPedometerSettings.clearServiceRunning();

//        mStepValueView = (TextView) findViewById(R.id.step_value);    
        
        resetValues(true);

	}
	
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        menu.add(0, 0, 0, "起点")
        .setIcon(R.drawable.icon)
        .setShortcut('0', 'q');
        menu.add(0, 1, 0, "终点")
        .setIcon(R.drawable.icon)
        .setShortcut('1', 'z')
        .setIntent(new Intent(this, Settings.class));
        menu.add(0, 2, 0, "导航")
        .setIcon(R.drawable.icon)
        .setShortcut('2', 'd');
        menu.add(0, 3, 0, "重置")
        .setIcon(R.drawable.icon)
        .setShortcut('3', 'c');
        menu.add(0, 4, 0, "计步器")
        .setIcon(R.drawable.icon)
        .setShortcut('4', 'j');
        if (mIsRunning) {
            menu.add(0, 5, 0, "计步器pause")
            .setIcon(android.R.drawable.ic_media_pause)
            .setShortcut('5', 'p');
        }
        else {
            menu.add(0, 6, 0, "计步器resume")
            .setIcon(android.R.drawable.ic_media_play)
            .setShortcut('5', 'p');
        }
        menu.add(0, 7, 0, "计步器重置")
        .setIcon(android.R.drawable.ic_menu_close_clear_cancel)
        .setShortcut('7', 'r');
        menu.add(0, 8, 0, "计步器设置")
        .setIcon(android.R.drawable.ic_menu_preferences)
        .setShortcut('8', 's')
        .setIntent(new Intent(this, Settings.class));
        menu.add(0, 9, 0, "退出")
        .setIcon(android.R.drawable.ic_lock_power_off)
        .setShortcut('9', 'e');
        return true;
    }

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0: {
			// 扫描二维码（真机）
			qrCode(0);
			// 模拟器上进行测试的代码
//			addPOI("2", 0, R.drawable.map_object_start);
			return true;
		}
		case 1: {
			// 扫描二维码（真机）
			qrCode(1);
			// 模拟器上进行测试的代码
//			addPOI("55", 1, R.drawable.map_object_end);
			return true;
		}
		case 2: {		
			// 路径导航 // 通过id获取图层对象
//			Log.d("layer.getMapObjectCount()", "layer.getMapObjectCount():" + layer.getMapObjectCount());
			if(layer.getMapObjectCount() > 1){
				path = new Navigation().pathNavigation(layer.getMapObjectByIndex(0), layer.getMapObjectByIndex(1));
				for(int i = 1; i < path.size()-1; i++){
					addPOI(path.get(i).name, tmp, R.drawable.poi);
					tmp ++;
				}
				isNavigation = true;
			}else{
				Toast.makeText(MainActivity.this, "请您选择起点和终点！", Toast.LENGTH_LONG).show();
			}
			return true;
		}
		case 3: {
			// 重置
			layer.clearAll();
			map.refreshDrawableState();
			isNavigation = false;
//			Log.d(TAG, "count:" + layer.getMapObjectCount());
			return true;
		}
		case 4: {
			// 判断如果已经进行了路径导航则进行计步器操作，实时在地图上显示用户位置
            if(isNavigation){
            	selectPoi();
    			// 计步器
    			initPedometer();
            }else{
            	Toast.makeText(MainActivity.this, "请您先进行路径导航！", Toast.LENGTH_LONG).show();
            }
			return true;
		}
		case 5:{
            if (mIsRunning) {
                unbindStepService();
            }
//            unbindStepService();
            stopStepService();
            return true;
		}
        case 6:{
            startStepService();
            bindStepService();
            return true;
        }
        case 7:{
            resetValues(true);
            return true;
        }
        case 9:{
            resetValues(false);
            if (mIsRunning) {
                unbindStepService();
            }
//            unbindStepService();
            stopStepService();
            mQuitting = true;
			// 退出程序
			exit();
            return true;
		}
		}
		return false;
	}

//	   @Override
//	protected void onStop() {
//	        Log.i(TAG, "[ACTIVITY] onPause");
//	        if (mIsRunning) {
//	            unbindStepService();
//	        }
//	        if (mQuitting) {
//	            mPedometerSettings.saveServiceRunningWithNullTimestamp(mIsRunning);
//	        }
//	        else {
//	        	if(mPedometerSettings == null){
//	        		
//	        	}else{
//	            mPedometerSettings.saveServiceRunningWithTimestamp(mIsRunning);
//	        	}
//	        }
//		super.onStop();
//	}

	@Override
	    protected void onPause() {
	        Log.i(TAG, "[ACTIVITY] onPause");
	        if (mIsRunning) {
	            unbindStepService();
	        }
	        if (mQuitting) {
	            mPedometerSettings.saveServiceRunningWithNullTimestamp(mIsRunning);
	        }
	        else {
	        	if(mPedometerSettings == null){
	        		
	        	}else{
	            mPedometerSettings.saveServiceRunningWithTimestamp(mIsRunning);
	        	}
	        }

	        super.onPause();
	    }

	    private StepService mService;
	    
	    // service与activity建立联系
	    private ServiceConnection mConnection = new ServiceConnection() {
	        public void onServiceConnected(ComponentName className, IBinder service) {
	            mService = ((StepService.StepBinder)service).getService();

	            mService.registerCallback(mCallback);
	            mService.reloadSettings();
	            
	        }

	        public void onServiceDisconnected(ComponentName className) {
	            mService = null;
	        }
	    };
	    
	    // 启动服务
	    private void startStepService() {
	        if (! mIsRunning) {
	            Log.i(TAG, "[SERVICE] Start");
	            mIsRunning = true;
	            startService(new Intent(MainActivity.this,
	                    StepService.class));
	        }
	    }
	    // 绑定服务到activity
	    private void bindStepService() {
	        Log.i(TAG, "[SERVICE] Bind");
	        bindService(new Intent(MainActivity.this, 
	                StepService.class), mConnection, Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
	    }
	    // 解除service与activity之间的绑定关系
	    private void unbindStepService() {
	        Log.i(TAG, "[SERVICE] Unbind");
	        unbindService(mConnection);
	    }
	    // 停止服务运行
	    private void stopStepService() {
	        Log.i(TAG, "[SERVICE] Stop");
	        if (mService != null) {
	            Log.i(TAG, "[SERVICE] stopService");
	            stopService(new Intent(MainActivity.this,
	                  StepService.class));
	        }
	        mIsRunning = false;
	    }
	    // 重置步数变量
	    private void resetValues(boolean updateDisplay) {
	        if (mService != null && mIsRunning) {
	            mService.resetValues();                    
	        }
	        else {
//	            mStepValueView.setText("0");
	            SharedPreferences state = getSharedPreferences("state", 0);
	            SharedPreferences.Editor stateEditor = state.edit();
	            if (updateDisplay) {
	                stateEditor.putInt("steps", 0);
	                stateEditor.putFloat("distance", 0);
	                stateEditor.commit();
	            }
	        }
	    }

	    
	    private StepService.ICallback mCallback = new StepService.ICallback() {
	        public void stepsChanged(int value) {
	            mHandler.sendMessage(mHandler.obtainMessage(STEPS_MSG, value, 0));
	        }
	        
	        public void distanceChanged(float value) {
	            mHandler.sendMessage(mHandler.obtainMessage(DISTANCE_MSG, (int)(value*1000), 0));
	        }
	    };
	    
	    private static final int STEPS_MSG = 1;
	    private static final int DISTANCE_MSG = 3;
	    
	    private Handler mHandler = new Handler() {
	        @Override public void handleMessage(Message msg) {
	            switch (msg.what) {
	                case STEPS_MSG:
	                    mStepValue = (int)msg.arg1;
	                    Log.i(TAG, "mStepValueView:" + mStepValue + "步");
	                    break;
	                case DISTANCE_MSG:
	                    mDistanceValue = ((int)msg.arg1)/1000f;
	                    // 当步行距离小于等于0时的处理方案
	                    if (mDistanceValue <= 0) { 
//	                    	// 计算终点二维码数据0-79
//	                    	int endy =  ((layer.getMapObjectByIndex(1).getY() + 19) / 44) * 10;
//	                    	int endtag = ((layer.getMapObjectByIndex(1).getX() + 19) / 46) + tmp;
//	        				addPOI(endtag+"", tmp, R.drawable.pedometerpoi);
//	        				tmp ++;
	                    	Log.i(TAG, "mDistanceValueView:0cm");
	                    }// 当步行距离大于0时的处理方案
	                    else {
	                    	int tag = 0;
	                    	// 当导航标志小于最终标志时，导航进行
	                    	if(pedometerNavigation < (pixdotx.size() - 1)){
	                    		Log.i(TAG, "pixdotx.get(pedometerNavigation):" + pixdotx.get(pedometerNavigation) + ",pixdotx.get(pedometerNavigation+1):" 
	                    	+ pixdotx.get(pedometerNavigation+1));
	                    		// 导航开始
	                    		// 当x轴相同时，y轴递加
		                    	if(pixdotx.get(pedometerNavigation).equals(pixdotx.get(pedometerNavigation+1))){
		                    		double distance = Math.abs(pixdoty.get(pedometerNavigation) - pixdoty.get(pedometerNavigation+1));
		                    		Log.i(TAG, "distance:" + distance + ",mDistanceValue:" + mDistanceValue);
		                    		// Y轴坐标
		                    		int ytmp = 0;
			                    	if(mDistanceValue < distance){
			                    		// 创建地图对象图标
			                    		Drawable drawable = getResources().getDrawable(
			                    				R.drawable.pedometerpoi);
			                    		if(pixdoty.get(pedometerNavigation) > pixdoty.get(pedometerNavigation+1)){
//			                    			tag = pedometerNavigation+1;
			                    			 ytmp = pixdoty.get(pedometerNavigation) - (int)mDistanceValue;
			                    			
			                    		}else{
//			                    			tag = pedometerNavigation;
			                    			 ytmp = pixdoty.get(pedometerNavigation) + (int)mDistanceValue;
			                    		}
			                    		// 创建地图对象(在本地图中一个方格的长和宽均为35像素)
			                    		MapObject mapObject = new MapObject(tmp, // 设置对象id

			                    				drawable, // 设置图标

			                    				pixdotx.get(pedometerNavigation), ytmp, // 像素坐标

			                    				11, 33, // 中心点坐标

			                    				true, // 可触摸

			                    				true); // 可扩展
			                    		tmp++;
			                    		Log.d(TAG, "" + "pixdotx：" + pixdotx.get(pedometerNavigation) + ",pixdoty:" + ytmp);

			                    		// 添加地图对象到图层中(任何位置)
			                    		layer.addMapObject(mapObject);
			                    	}else{
			                    		pedometerNavigation ++;
			                    		resetValues(true);
			                    	}
			                    	// 当y轴相同时，x轴递加
		                    	}else if(pixdoty.get(pedometerNavigation).equals(pixdoty.get(pedometerNavigation+1))){

		                    		double distance = Math.abs(pixdotx.get(pedometerNavigation) - pixdotx.get(pedometerNavigation+1));
		                    		// X轴坐标
		                    		int xtmp = 0;
			                    	if(mDistanceValue < distance){
			                    		// 创建地图对象图标
			                    		Drawable drawable = getResources().getDrawable(
			                    				R.drawable.pedometerpoi);
			                    		if(pixdotx.get(pedometerNavigation) > pixdotx.get(pedometerNavigation+1)){
//			                    			tag = pedometerNavigation+1;
			                    			xtmp = pixdotx.get(pedometerNavigation) - (int)mDistanceValue;
			                    		}else{
			                    			xtmp = pixdotx.get(pedometerNavigation) + (int)mDistanceValue;	
			                    		}
			                    		// 创建地图对象(在本地图中一个方格的长和宽均为35像素)
			                    		MapObject mapObject = new MapObject(tmp, // 设置对象id

			                    				drawable, // 设置图标

			                    				xtmp, pixdoty.get(pedometerNavigation), // 像素坐标

			                    				11, 33, // 中心点坐标

			                    				true, // 可触摸

			                    				true); // 可扩展
			                    		tmp++;
			                    		Log.d(TAG, "" + "pixdotx：" + xtmp + ",pixdoty:" + pixdoty.get(pedometerNavigation));

			                    		// 添加地图对象到图层中(任何位置)
			                    		layer.addMapObject(mapObject);
			                    	}else{
			                    		pedometerNavigation ++;
			                    		resetValues(true);
			                    	}
			                     	}
		                    	
	                    	}else{
	                    		// 导航完毕
	                            resetValues(false);
	                            if (mIsRunning) {
	                                unbindStepService();
	                            }
//	                            unbindStepService();
	                            stopStepService();
	                            mQuitting = true;
	                            Toast.makeText(MainActivity.this, "导航结束！", Toast.LENGTH_LONG).show();
	                    	}

	                    	Log.i(TAG, "mDistanceValueView:" + mDistanceValue + "cm");
	                    }
	                    break;
	                default:
	                    super.handleMessage(msg);
	            }
	        }
	        
	    };
	    
	    private void selectPoi(){
	    	
	    	String[] nametag = new String[1];
	    	boolean flagx = false;
	    	boolean flagy = false;
	    	
	    	for(int i = 0; i < path.size(); i++){
	    		Log.i(TAG, path.size()+"");
	    		nametag[0] = path.get(i).name;
		    	int[] pixPedometer = mySqliteHelper.selectDB(nametag);
		    	// 起点坐标像素
	    		if(i == 0){
	    			pixdotx.add(pixPedometer[0]);
	    			pixdoty.add(pixPedometer[1]);
	    			// 终点坐标像素
	    		}else if(i == (path.size()-1)){
	    			pixdotx.add(pixPedometer[0]);
	    			pixdoty.add(pixPedometer[1]);
	    	    	// 其他坐标像素
	    		}else{
	    			// 如果x轴坐标相同，y不同
	    			if(pixPedometer[0] == pixdotx.get(pixdotx.size()-1)){
	    				flagy = false;
	    				if(flagx){
	    					pixdotx.remove(pixdotx.size()-1);
	    					pixdoty.remove(pixdoty.size()-1);
	    					pixdotx.add(pixPedometer[0]);
	    	    			pixdoty.add(pixPedometer[1]);
	    				}else{
	    					pixdotx.add(pixPedometer[0]);
	    	    			pixdoty.add(pixPedometer[1]);
	    	    			flagx = true;
	    				}
	    				// 如果y轴坐标相同，x不同
	    			}else if(pixPedometer[1] == pixdoty.get(pixdoty.size()-1)){
	    				flagx = false;
	    				if(flagy){
	    					pixdotx.remove(pixdotx.size()-1);
	    					pixdoty.remove(pixdoty.size()-1);
	    					pixdotx.add(pixPedometer[0]);
	    	    			pixdoty.add(pixPedometer[1]);
	    				}else{
	    					pixdotx.add(pixPedometer[0]);
	    	    			pixdoty.add(pixPedometer[1]);
	    	    			flagy = true;
	    				}	    				
	    			}
	    		}
	
	    	}
	    	Log.i(TAG, "pixdotx:" + pixdotx.toString() + ",pixdoty:" + pixdoty.toString());
	    }
	    
}
