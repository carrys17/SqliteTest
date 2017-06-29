package com.example.shang.sqlitetest.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by shang on 2017/6/29.
 */

public class MyDatabaseHelper extends SQLiteOpenHelper {

    public MyDatabaseHelper(Context context){
        super(context,Constant.DATABASE_NAME,null,Constant.DATA_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table "+Constant.TABBLE_NAME +"("+Constant.ID+" integer primary key autoincrement,"+Constant.NAME+","+Constant.AGE+")";
        Log.i("xyz",sql);
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
