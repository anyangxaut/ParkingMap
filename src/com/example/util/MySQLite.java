package com.example.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLite extends SQLiteOpenHelper{

	// 调试标识
	private static final String TAG = "MySQLite";
	
	// 构造方法--创建数据库mapinfo，版本为1
	public MySQLite(Context context) {
		super(context, "mapinfo", null, 1);
		// TODO Auto-generated constructor stub
	}
	
	// 创建数据库中的表
	@Override
	public void onCreate(SQLiteDatabase arg0) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onCreate");
		arg0.execSQL("create table mapinfo(id integer primary key autoincrement, qrcode Text not null," +
				"x_pix integer not null," + "y_pix integer not null)");
	}

	// 当数据库版本发生变化时，要执行的操作
	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
	}

}
