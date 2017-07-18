
# SqliteTest

一个关于Sqlite的总结项目，对以前学过的知识的巩固。涉及SQliteDatabase、数据库插入、数据库查询的两种方式、最后还复习了adapter中的处理，借助convertview和viewholder的方式，还有关于事务的作用及操作，数据的分页显示。

首先继承SQliteDatabaseHelper创建自己的helper，在这个类中有三个方法，构造方法、oncreate、onupdtae。
一般在构造方法中创建数据库，在oncreate中创建表。注意sql语句的拼写。


在创建一个中间操作类，用于部分逻辑实现。

两种查询操作


1、通过该sql语句
    
      String sql = "select * from "+Constant.TABBLE_NAME;
      cursor = db.rawQuery(sql,selectionArgs);

2、通过api方式

      
      
         //String table,  查询的表名
         // String[] columns, 查询的字段名称 null 所有
         // String selection, 查询条件
         //String[] selectionArgs, 查询条件的占位符
         // String groupBy, 表示分组条件
         // String having,  表示筛选条件
         //String orderBy   表示排序条件 desc 降序  asc升序      
         cursor = db.query(Constant.TABBLE_NAME,null,Constant.ID+">?",new String[]{"10"},null,null,Constant.ID+" desc");


这样就得到cursor对象，传统的做法就是将cursor转换为list对象，然后用simpleAdapter将数据显示在listview中，有点麻烦。

现在，我们用simpleCursorAdapter直接对cursor处理

      //使用SimpleCursorAdapter时匹配的表的主键名必须是_id作为主键名，不然会查找不到
      //最后一个参数是设置为观察者模式，数据一改变，界面的显示也跟着改变
      SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,R.layout.list_item,cursor,
                        new String[]{Constant.ID,Constant.NAME,Constant.AGE},
                        new int[]{R.id.item_id,R.id.item_name,R.id.item_age}
                        ,SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
      mListView.setAdapter(adapter);
      
还有要注意得到的cursor不能close,不然数据没法在listview显示了。

还有一种是用CursorAdapter来实现的。
       


    //CursorAdapter是一个抽象类，不能直接new一个出来，所以要定义一个类去继承使用
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



      MyCursorAdapter adapter1 = new MyCursorAdapter(this,cursor,CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER) 
      mListView.setAdapter(adapter1);
        
      
事务

 一次插入可以理解为一个事务。

当数据库的数据较多时，比如现在有100条数据，每次都执行一个事务，相当于100个事务，这样老是打开关闭事务，会耗用太多资源。
所以可以将这100个插入显式的放在一个事务中，这样就只打开和关闭一个事务，提高了编码的效率。同时，事务还有一个好处，就是一致性。
只有当所有的操作都正确的情况下，才可以进行操作。
事务一般是用于批量操作的。

        db.beginTransaction();
        for (int i=1;i<=100;i++){
              String sql = "insert into "+ Constant.TABBLE_NAME +" values("+i+",'张三"+i+"',20)";
            Log.i("xyz",sql);
            db.execSQL(sql);

         }
        db.setTransactionSuccessful();
        db.endTransaction();
        
 
 数据分页显示
 
 不要一次性将所有的数据插入显示到listview中，很容易内存溢出。而是使用分页的方式，每次加载一定数量的数据，需要时再加载更多的数据
 主要方法就是select * from person limit ?,?
 
 其中第一个参数为当前页的第一个下标，第二个为页数中的数据量
 
 一般需要5个基本变量，总的数据量，每页的数据数，一共多少页，当前页码，当前页的数据list
 
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


 getListByCurrentPage类，得到当前页码数据list
 
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
