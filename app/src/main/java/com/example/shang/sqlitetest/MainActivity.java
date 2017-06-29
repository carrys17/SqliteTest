package com.example.shang.sqlitetest;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shang.sqlitetest.Bean.Person;
import com.example.shang.sqlitetest.adapter.MyAdapter;
import com.example.shang.sqlitetest.utils.Constant;
import com.example.shang.sqlitetest.utils.DbManager;
import com.example.shang.sqlitetest.utils.MyDatabaseHelper;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MyDatabaseHelper helper;
    private ListView mListView;
    private Button mButton;

    private int sum;//总的数据数目
    private int pageSize = 20; //每页有15条数据
    private int pageNum; // 一共有多少页
    private int currentPage = 1; //当期页码
    private List<Person> totalList; // 表示数据源
    private MyAdapter myAdapter;
    private boolean isDivPage; //判断是否分页
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        helper = DbManager.getInstance(this);
        mButton = (Button) findViewById(R.id.id_add);
        mListView = (ListView) findViewById(R.id.id_listView);
    }

    public void add(View view){
        SQLiteDatabase db = helper.getReadableDatabase();
        long t = System.currentTimeMillis();

        db.beginTransaction();
        for (int i=1;i<=100;i++){
            String sql = "insert into "+ Constant.TABBLE_NAME +" values("+i+",'张三"+i+"',20)";
            Log.i("xyz",sql);
            db.execSQL(sql);
        }
        db.setTransactionSuccessful();
        db.endTransaction();

        t = System.currentTimeMillis() -t;
        Log.i("time",t+"");
        db.close();
        Toast.makeText(MainActivity.this,"创建并添加数据成功！！！",Toast.LENGTH_SHORT).show();
        mButton.setEnabled(false);
    }

    public void onClick(View view){
        switch (view.getId()) {
            case R.id.quert_sql:
                SQLiteDatabase db = helper.getReadableDatabase();
                String sql = "select * from "+Constant.TABBLE_NAME;
                //通过打开本地数据库路径得到数据库
//                Context cont = this.getApplicationContext();
//                String s = cont.getDatabasePath("info.db")+"";
//                SQLiteDatabase db = SQLiteDatabase.openDatabase(s,null,SQLiteDatabase.OPEN_READONLY);
                Cursor cursor = DbManager.queryBySQL(db,sql,null);
               // 传统的做法就是把cursor转换为list，然后在listview中显示，会用到simpleAdapter
                List<Person> persons =  DbManager.cursorToPerson(cursor);
                for (Person p:persons){
                    Log.i("xyz",p.toString());
                }

                //现在，我们用simpleCursorAdapter直接对cursor处理
                //使用SimpleCursorAdapter时匹配的表的主键名必须是_id作为主键名，不然会查找不到
                //最后一个参数是设置为观察者模式，数据一改变，界面的显示也跟着改变
                SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,R.layout.list_item,cursor,
                        new String[]{Constant.ID,Constant.NAME,Constant.AGE},
                        new int[]{R.id.item_id,R.id.item_name,R.id.item_age}
                        ,SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
                mListView.setAdapter(adapter);
                // cursor.close(); cursor注定不能关闭吗？艹，关了之后数据线显示不出来
                db.close();
            break;
            case R.id.quert_api:
                db = helper.getReadableDatabase();
                //String table,  查询的表名
                // String[] columns, 查询的字段名称 null 所有
                // String selection, 查询条件
                //String[] selectionArgs, 查询条件的占位符
                // String groupBy, 表示分组条件
                // String having,  表示筛选条件
                //String orderBy   表示排序条件 desc 降序  asc升序
                cursor = db.query(Constant.TABBLE_NAME,null,Constant.ID+">?",new String[]{"10"},null,null,Constant.ID+" desc");
                persons = DbManager.cursorToPerson(cursor);
                for (Person p:persons){
                    Log.i("xyz",p.toString());
                }

                MyCursorAdapter adapter1 = new MyCursorAdapter(this,cursor,CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
                mListView.setAdapter(adapter1);
                db.close();
                break;
        }
    }

    //CursorAdapter是一个接口，不能直接new一个出来，所以要定义一个类去继承使用
    public class MyCursorAdapter extends CursorAdapter{

        public MyCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        // 每项item的view对象
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(MainActivity.this).inflate(R.layout.list_item,null);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView id = (TextView) view.findViewById(R.id.item_id);
            TextView name = (TextView) view.findViewById(R.id.item_name);
            TextView age = (TextView) view.findViewById(R.id.item_age);
            //记得加""，否则返回的是id不是值
            id.setText(cursor.getInt(cursor.getColumnIndex(Constant.ID))+"");
            name.setText(cursor.getString(cursor.getColumnIndex(Constant.NAME)));
            age.setText(cursor.getInt(cursor.getColumnIndex(Constant.AGE))+"");

        }
    }


    public void select(View view){
        final SQLiteDatabase db = helper.getReadableDatabase();
        sum = DbManager.getTotalNum(db,Constant.TABBLE_NAME);
//        if (sum%pageSize!=0){
//            pageNum = sum/pageSize + 1;
//        }else {
//            pageNum = sum/pageSize;
//        }
        pageNum = (int) Math.ceil(sum/(double)pageSize); //向上取整
        if(currentPage ==1){
            totalList = DbManager.getListByCurrentPage(db,Constant.TABBLE_NAME,currentPage,pageSize);
        }
        myAdapter = new MyAdapter(this,totalList);
        mListView.setAdapter(myAdapter);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //已经分页并且当前滚动状态为停止滚动了
                if (isDivPage && AbsListView.OnScrollListener.SCROLL_STATE_IDLE == scrollState){
                    if (currentPage<pageNum){
                        currentPage++;
                        // 根据最新的页码加载获取集合存储到数据源中
                        totalList.addAll(DbManager.getListByCurrentPage(db,Constant.TABBLE_NAME,currentPage,pageSize));
                        myAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                isDivPage = ((firstVisibleItem+visibleItemCount)==totalItemCount);
            }
        });


    }

}
