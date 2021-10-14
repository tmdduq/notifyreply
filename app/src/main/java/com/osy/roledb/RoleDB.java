package com.osy.roledb;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class RoleDB extends SQLiteOpenHelper {
    final String TAG = "RoleDB";
    String table_containKeyword = "table_containKeyword";
    String table_matchKeyword = "table_matchKeyword";
    Context context;
    SQLiteDatabase db;

    public RoleDB(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db = db;
         String qry = "create table "+table_containKeyword+"(" +
                "num integer primary key autoincrement," +
                "room varchar(30) not null," +
                "ky varchar(20) not null," +
                "kylength integer not null," +
                "value varchar(100) not null);";
        db.execSQL(qry);
        initializeContainRole();

        qry = "create table "+table_matchKeyword+"(" +
                "num integer primary key autoincrement," +
                "room varchar(30) not null," +
                "ky varchar(20) not null," +
                "kylength integer not null," +
                "value varchar(100) not null);";
        db.execSQL(qry);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + table_containKeyword);
        db.execSQL("DROP TABLE IF EXISTS " + table_matchKeyword);
        onCreate(db);
    }

    final void initializeContainRole(){
        String qry = "insert into "+table_containKeyword + "(room, ky, kylength, value) values" +
                "('ㅇㅅㅇ',    '안녕',    2,   '안녕하세요?')," +
                "('ㅇㅅㅇ',    '히오스',  3,'시공쪼아')";
        db.execSQL(qry);
    }

    public void insertContainKeyword(String room, String key, String value){
        SQLiteDatabase db = getWritableDatabase();
        try{
            String qry = "insert into "+table_containKeyword + "(room, ky,kylength, value) values('" +
                    room+"','"+key+"',"+key.length()+",'"+value+"');";
            db.execSQL(qry);
        }catch(Exception e){
            Log.i(TAG, "insertContainKeyword");
            e.printStackTrace();}


    }

    public Cursor getContainKeyword(String room, String key){
        SQLiteDatabase db = getReadableDatabase();
        String qry = "select value from "+ table_containKeyword + " where (room='"+room+"' and ky like'%"+key+"%');";
        System.out.println("getconain "+qry);

        Cursor cursor = db.rawQuery(qry,null);
        System.out.println("getconain "+cursor.getCount());
        while(cursor.moveToNext()){
            System.out.println("getconain "+cursor.getString(0));
        }
        return cursor;
    }

    public boolean deleteContainKey(String room, int num){
        try {
            SQLiteDatabase db = getReadableDatabase();
            String qry = "delete from " + table_containKeyword + " where room= '" + room + "' and num = " + num + ";";
            db.execSQL(qry);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }

    }

    public Cursor getContainsKeyList(String room){
        SQLiteDatabase db = getReadableDatabase();
        String qry;
        if(room==null)
            qry = "select room,ky,value,num from "+ table_containKeyword + " order by kylength desc;";
        else
            qry = "select room,ky,value,num from "+ table_containKeyword + " where room='"+room+"' order by num asc;";
        Cursor cursor = db.rawQuery(qry,null);
        return cursor;
    }
    public Cursor getMatchKeyList(){
        SQLiteDatabase db = getReadableDatabase();
        String qry;
        qry = "select value from "+ table_matchKeyword + ";";

        Cursor cursor = db.rawQuery(qry,null);
        return cursor;
    }

}
