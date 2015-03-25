package com.example.departmentmap;

import java.lang.reflect.Field;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Group ID
		int groupId = 0;
		// The order position of the item
		int menuItemOrder = Menu.NONE;

		menu.add(groupId, 0, menuItemOrder, "起点").setIcon(R.drawable.icon);
		menu.add(groupId, 1, menuItemOrder, "终点").setIcon(R.drawable.icon);
		menu.add(groupId, 2, menuItemOrder, "导航").setIcon(R.drawable.icon);
		menu.add(groupId, 3, menuItemOrder, "重置").setIcon(R.drawable.icon);
		menu.add(groupId, 4, menuItemOrder, "退出").setIcon(R.drawable.icon);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0: {
			// 扫描二维码（真机）
//			qrCode(0);
			// 模拟器上进行测试的代码
			addPOI("2", 0, R.drawable.map_object_start);
			break;
		}
		case 1: {
			// 扫描二维码（真机）
//			qrCode(1);
			// 模拟器上进行测试的代码
			addPOI("55", 1, R.drawable.map_object_end);
			break;
		}
		case 2: {		
			// 路径导航 // 通过id获取图层对象
//			Log.d("layer.getMapObjectCount()", "layer.getMapObjectCount():" + layer.getMapObjectCount());
			if(layer.getMapObjectCount() > 1){
				List<Vertex> path = new Navigation().pathNavigation(layer.getMapObjectByIndex(0), layer.getMapObjectByIndex(1));
				int tmp = 3;
				for(int i = 1; i < path.size()-1; i++){
					addPOI(path.get(i).name, tmp, R.drawable.poi);
					tmp ++;
				}
			}else{
				Toast.makeText(MainActivity.this, "请您选择起点和终点！", Toast.LENGTH_LONG).show();
			}
			break;
		}
		case 3: {
			// 重置
			layer.clearAll();
			map.refreshDrawableState();
//			Log.d(TAG, "count:" + layer.getMapObjectCount());
			break;
		}
		case 4: {
			// 退出程序
			exit();
			break;
		}
		default:
			break;
		}
		return true;
	}

}
