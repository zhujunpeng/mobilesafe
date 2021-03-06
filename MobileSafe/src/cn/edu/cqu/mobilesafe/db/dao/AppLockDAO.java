package cn.edu.cqu.mobilesafe.db.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import cn.edu.cqu.mobilesafe.db.AppLockDBOpenHelper;

/**
 * 程序锁的增删改查的业务
 * 
 * @author Administrator
 * 
 */
public class AppLockDAO {
	private AppLockDBOpenHelper helper;
	private Context context;

	public AppLockDAO(Context context) {
		helper = new AppLockDBOpenHelper(context);
		this.context = context;
	}

	/**
	 * 查找数据库中
	 * 
	 * @param number
	 * @return
	 */
	public boolean find(String packname) {
		boolean reslut = false;
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from applock where packname = ?",
				new String[] { packname });
		if (cursor.moveToNext()) {
			reslut = true;
		}
		cursor.close();
		db.close();
		return reslut;
	}

	/**
	 * 查找数据库中所有的包名
	 * 
	 * @param number
	 * @return
	 */
	public List<String> findAll() {
		List<String> packagenames = new ArrayList<String>();
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select packname from applock", null);
		if (cursor.moveToNext()) {
			packagenames.add(cursor.getString(0));
		}
		cursor.close();
		db.close();
		System.out.println("packagenames---" + packagenames);
		return packagenames;
	}

	/**
	 * 添加一条软件锁
	 * 
	 * @param number
	 * @param mode
	 */
	public void add(String packname) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("packname", packname);
		db.insert("applock", null, values);
		db.close();
		// 当数据发生改变发出一个广播
		Intent intent = new Intent();
		intent.setAction("cn.edu.cqu.mobilesafe.packagechange");
		context.sendBroadcast(intent);
	}

	/**
	 * 删除一条程序锁
	 * 
	 * @param number
	 * @param newmode
	 */
	public void delete(String packname) {
		SQLiteDatabase db = helper.getWritableDatabase();
		db.delete("applock", "packname=?", new String[] { packname });
		db.close();
		// 当数据发生改变发出一个广播
		Intent intent = new Intent();
		intent.setAction("cn.edu.cqu.mobilesafe.packagechange");
		context.sendBroadcast(intent);
	}

}
