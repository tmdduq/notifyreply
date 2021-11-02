package com.osy.roledb;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

public class RoleDB extends SQLiteOpenHelper {
    final String TAG = "RoleDB";
    String table_containKeyword = "table_containKeyword";
    String consonantQuiz_lol_skin = "consonantQuiz_lol_skin";
    String consonantQuiz_lol = "consonantQuiz_lol";
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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + table_containKeyword);
        onCreate(db);
    }

    final void initializeContainRole(){
        String qry = "insert into "+table_containKeyword + "(room, ky, kylength, value) values" +
                "('룸이름',    '(키워드)안녕',    2,   '(대답)안녕하세요?')," +
                "('ㄹㅇㄹ',    'ㅇㄴ',  3,'ㅇㄴㅎㅅㅇ?')";
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
            String qry = "delete from " + table_containKeyword + " where num = " + num + ";";
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
            //qry = "select room,ky,value,num from "+ table_containKeyword + " order by kylength desc;";
            qry = "select room,ky,value,num from "+ table_containKeyword + " order by num asc;";
        else
            qry = "select room,ky,value,num from "+ table_containKeyword + " where room='"+room+"' order by num asc;";
        Cursor cursor = db.rawQuery(qry,null);
        return cursor;
    }

    public boolean createConsonantQuiz(String fileNameInAsset, String tableNaming){
        try {
            SQLiteDatabase db = getReadableDatabase();

            String deleteValues = "drop table if exists "+tableNaming+";";
            db.execSQL(deleteValues);

            String createTable = "create table if not exists "+tableNaming+"(" +
                    "num integer primary key autoincrement," +
                    "value varchar(100) not null);";
            db.execSQL(createTable);

            ///////////////////////////////////
            BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open(fileNameInAsset, AssetManager.ACCESS_BUFFER)));
            StringBuilder qry = new StringBuilder("insert into " + tableNaming + "(value) values");
            String keyword = null;
            while (null != (keyword = br.readLine())){
                if(keyword.trim().matches("")) break;
                qry.append("('"+keyword+ "'),");
            }
            qry.deleteCharAt(qry.length()-1);
            db.execSQL(qry.toString());

            viewConsonantQuizList(tableNaming);
            return true;
        }catch(Exception e){e.printStackTrace();}
        return false;
    }
    public void viewConsonantQuizList(String tableName){
        String checkValues = "select * from "+ tableName + " order by num asc;";
        Cursor cursor = db.rawQuery(checkValues,null);
        while(cursor.moveToNext()) {
            String num = cursor.getString(0);
            String value = cursor.getString(1);
            Log.i(TAG, "QUIZ(n/v): " + num + "/" + value);
        }
    }
    public int getTableSize(String tableName){
        SQLiteDatabase db = getReadableDatabase();
        String qry = "select * from "+ tableName +";";
        Cursor cursor = db.rawQuery(qry,null);
        return cursor.getCount();
    }
    public String getConsonantQuestion(String tableName){
        SQLiteDatabase db = getReadableDatabase();
        int size = getTableSize(tableName);
        Random r = new Random();
        r.setSeed(System.currentTimeMillis());
        String qry = "select value from "+ tableName +" where num= "+ (r.nextInt(size)+1) + ";";
        Cursor cursor = db.rawQuery(qry,null);
        cursor.moveToNext();
        return cursor.getString(0);
    }
    public boolean putConsonantQuestion(String tableName, String keyword) {
        SQLiteDatabase db = getReadableDatabase();
        String qry = "insert into "+ tableName +"(value) values('"+ keyword + "');";
        db.execSQL(qry);
        return true;
    }


}
