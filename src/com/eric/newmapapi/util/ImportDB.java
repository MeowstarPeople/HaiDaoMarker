package com.eric.newmapapi.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.eric.newmapapi.R;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
public class ImportDB {
	public static final String DB_NAME = "HD_Island.db"; //保存的数据库文件名
	public static final String PACKAGE_NAME = "com.eric.newmapapi";
	public static final String DB_PATH = File.separator+"data"
			+ Environment.getDataDirectory().getAbsolutePath() + File.separator
			+ PACKAGE_NAME;  //在手机里存放数据库的位置
	private SQLiteDatabase database;
	private Context context;
	public ImportDB(Context context) {
		this.context = context;
	}
	public SQLiteDatabase openDatabase() {
		database = this.openDatabase(DB_PATH +File.separator + DB_NAME);
		return database;
	}
	private SQLiteDatabase openDatabase(String dbfile) {
		SQLiteDatabase db = null;
		try {
			if (!(new File(dbfile).exists())) {//判断数据库文件是否存在，若不存在则执行导入，否则直接打开数据库
				InputStream is = this.context.getResources().openRawResource(R.raw.island); //欲导入的数据库
				int len=is.available();
				FileOutputStream fos = new FileOutputStream(dbfile);
				byte[] buffer = new byte[len];
				int count = 0;
				while ((count = is.read(buffer)) > 0) {
					fos.write(buffer, 0, count);
				}
				fos.close();
				is.close();
			}
			db = SQLiteDatabase.openOrCreateDatabase(dbfile,null);
		} catch (FileNotFoundException e) {
			Log.e("Database", "File not found");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("Database", "IO exception");
			e.printStackTrace();
		}
		return db;
	}
	public void closeDatabase() {
		this.database.close();
	}
}
