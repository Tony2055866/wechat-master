package db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

/**
 * SQLite数据库的帮助类
 * 
 * 该类属于扩展类,主要承担数据库初始化和版本升级使用,其他核心全由核心父类完成
 * 
 */
public class DataBaseHelper extends SDCardSQLiteOpenHelper {

	public DataBaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
        Log.i("tong test"," DataBaseHelper onCreate ");

        try {
//            db.execSQL("drop table  if EXISTS im_msg_his;");
//            db.execSQL("drop table  if EXISTS im_notice;");
//            db.execSQL("drop table  if EXISTS im_friend;");
            
            db.execSQL("CREATE TABLE im_msg_his (_id INTEGER NOT NULL  PRIMARY KEY AUTOINCREMENT, userId text, content text, msg_from text, msg_to text,msg_time TEXT, msg_type INTEGER);");
            db.execSQL("CREATE TABLE im_notice  (_id INTEGER NOT NULL  PRIMARY KEY AUTOINCREMENT, userId text, type text, title text, content text, notice_from text, notice_to text, notice_time TEXT, status INTEGER);");
            db.execSQL("create table im_friend  (_id INTEGER NOT NULL  PRIMARY KEY AUTOINCREMENT, userId text, nickName text, description text, avatar text);");
        }catch (Exception e){
            Log.e("tong test","DataBaseHelper onCreate error!", e);
        }
       // db.execSQL("CREATE TABLE [im_msg_his] ([_id] INTEGER NOT NULL  PRIMARY KEY AUTOINCREMENT, [content] NVARCHAR, [msg_from] NVARCHAR, [msg_to] NVARCHAR, [msg_time] TEXT, [msg_type] INTEGER);");
		//db.execSQL("CREATE TABLE [im_notice]  ([_id] INTEGER NOT NULL  PRIMARY KEY AUTOINCREMENT, [type] INTEGER, [title] NVARCHAR, [content] NVARCHAR, [notice_from] NVARCHAR, [notice_to] NVARCHAR, [notice_time] TEXT, [status] INTEGER);");
		//db.execSQL("create table [im_friend]  ([_id] INTEGER NOT NULL  PRIMARY KEY AUTOINCREMENT, [userId] nvarchar, [nickName] nvarchar, [description] nvarchar, [avatar] nvarchar);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}
}
