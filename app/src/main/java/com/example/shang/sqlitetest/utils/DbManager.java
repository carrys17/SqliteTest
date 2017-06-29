package com.example.shang.sqlitetest.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.shang.sqlitetest.Bean.Person;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shang on 2017/6/29.
 */

public class DbManager {
    private static MyDatabaseHelper helper;
    public static MyDatabaseHelper getInstance(Context context){
        if (helper==null){
            helper = new MyDatabaseHelper(context);
        }
        return helper;
    }

    public static Cursor queryBySQL(SQLiteDatabase db,String sql,String []selectionArgs){
        Cursor cursor = null;
        if (db!=null){
            cursor = db.rawQuery(sql,selectionArgs);
        }
        return cursor;
    }

    public static List<Person> cursorToPerson(Cursor cursor){
        List<Person> list = new ArrayList<>();
        while (cursor.moveToNext()){
            // 根据参数指定的字段来读取字段下标
            int index = cursor.getColumnIndex(Constant.ID);
            // 根据参数中指定的字段下标来获取指定的数据
            int id = cursor.getInt(index);

            String name = cursor.getString(cursor.getColumnIndex(Constant.NAME));
            int age = cursor.getInt(cursor.getColumnIndex(Constant.AGE));
            Person person = new Person(id,name,age);
            list.add(person);
        }
        return list;
    }

    public static int getTotalNum(SQLiteDatabase db,String table_name){
        int count = 0;
        if (db!=null){
            Cursor cursor = db.rawQuery("select * from "+table_name,null);
            count = cursor.getCount();
        }
        return count;
    }

    //当前页码数据的集合
    public static List<Person>getListByCurrentPage(SQLiteDatabase db,String table_name,int currentPage,int pageSize){
        int index = (currentPage-1)*pageSize; // 获取当前页码第一条数据的下标
        Cursor cursor = null;
        if (db!=null){
            String sql = "select * from "+table_name+" limit ?,?";  // 两个参数，一个是当前页的第一个数据下标，第二个是当前页的数量
            cursor = db.rawQuery(sql,new String[]{index+"",pageSize+""});
        }
        return cursorToPerson(cursor);
    }

}
