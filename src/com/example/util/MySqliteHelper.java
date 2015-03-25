package com.example.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MySqliteHelper {
	// 调试使用
	private static final String TAG = "MySqliteHelper";
	// 创建数据库对象
	private MySQLite mySqlite = null;
	// 获得数据库可写操作的对象
	private SQLiteDatabase dbWriter;
	// 获得数据库可读操作的对象
    private SQLiteDatabase dbReader;
	
	// 构造方法---实例化数据库对象
	public MySqliteHelper(Context context) {
		// 实例化MySqklite对象
		mySqlite = new MySQLite(context);
	}

	// 删除表
	public void drop(SQLiteDatabase db) {
		// 删除表的SQL语句
		String sql = "DROP TABLE mapinfo";
		// 执行SQL
		db.execSQL(sql);
	}

	// 添加数据
	public void addDB(String qrcode, int x_pix, int y_pix) {
		// 获得数据库可写操作权限
		dbWriter = mySqlite.getWritableDatabase();
		// 删除数据库
		// deleteDatabase("mapinfo");
		// 获得ContentValues对象，该对象存储要添加的数据信息
		ContentValues cv = new ContentValues();
		// 调用ContentValues对象的方法插入数据
		// qrcode：二维码标识；x_pix：对应的X轴像素信息；y_pix：对应的Y轴像素信息
		cv.put("qrcode", qrcode);
		cv.put("x_pix", x_pix);	
		cv.put("y_pix", y_pix);
		// dbWriter的dbWriter方法实现数据的同步
		dbWriter.insert("mapinfo", null, cv);		
		// 关闭数据库
		dbWriter.close();
	}
		
	  // 查询数据
	  public int[] selectDB(String[] qrcode) {
		  // 声明pix变量
		  int[] pix = new int[2];
		  // 获得数据库可读操作权限
		  dbReader = mySqlite.getReadableDatabase();
	      // 根据条件进行查询，并使用游标进行结果数据存储
//		  table:表名，不能为null
//		  columns:要查询的列名，可以是多个，可以为null，表示查询所有列
//		  selection:查询条件，比如id=? and name=? 可以为null
//		  selectionArgs:对查询条件赋值，一个问号对应一个值，按顺序 可以为null
//		  groupby:分组
//		  having:语法have，可以为null
//		  orderBy：语法，按xx排序，可以为null
	      Cursor cursor = dbReader.query("mapinfo", null, "qrcode=?", qrcode, null, null, null);
	      // 移动游标，获取查询结果
	       while (cursor.moveToNext()) {
	      // 得到游标中列名pix对应的值   
	    	   String tmp = cursor.getString(cursor.getColumnIndex("x_pix"));
	    	   pix[0] = Integer.parseInt(tmp);
	    	   tmp = cursor.getString(cursor.getColumnIndex("y_pix"));
	    	   pix[1] = Integer.parseInt(tmp);
	       // 调试输出pix值 
	       Log.d(TAG, "" + pix[0] + "," + pix[1]);
	       }
	       // 关闭数据库
	       dbReader.close();
	       // 返回pix值
	       return pix;
	  }

	// // 删除数据
	// public void deleteDB() {
	// dbWriter.delete("user", "_id=1", null);
	// }
	
	// // 修改数据
	// public void updateDB() {
	// ContentValues cv = new ContentValues();
	// cv.put("name", "Hello");// 修改后的数据的存储
	// dbWriter.update("user", cv, "_id=2", null);
	// }

}
