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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RoleDB extends SQLiteOpenHelper {
    final String TAG = "RoleDB";
    String table_containKeyword = "table_containKeyword";

    public Map<String, String> gameTableNameMap;
    public String stockListTableName = "stockList";

    private void gameTableNameInitialize(){
        gameTableNameMap = new HashMap<>(); // key: table명(=파일명), value : descript
        gameTableNameMap.put("consonantGame_LoL_skin", "LoL스킨");
        gameTableNameMap.put("consonantGame_LoL", "LoL");
        gameTableNameMap.put("consonantGame_LoL_skill", "LoL스킬");
        gameTableNameMap.put("consonantGame_Movie", "영화제목");
        gameTableNameMap.put("consonantGame_Drama", "드라마제목");
        gameTableNameMap.put("consonantGame_Nation", "나라이름");
    }

    Context context;
    SQLiteDatabase db;

    public Map<String, String> getGameTableNameMap(){
        if (gameTableNameMap==null) gameTableNameInitialize();
        return gameTableNameMap;
    }

    public RoleDB(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
        Log.i(TAG, "RoleDB");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "onCreate");
        gameTableNameInitialize();
        this.db = db;
         String qry = "create table "+table_containKeyword+"(" +
                "num integer primary key autoincrement," +
                "room varchar(30) not null," +
                "ky varchar(20) not null," +
                "kylength integer not null," +
                "value varchar(100) not null);";
        db.execSQL(qry);
        initializeContainRole();

        createStockList(stockListTableName, stockListTableName, db);
        gameTableNameMap.forEach((k,v)->{
            String tableName = k;
            String fileName = k;
            createConsonantQuiz(fileName, tableName, db);
        });

        Log.i(TAG, "onCreate end");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "onUpgrade");
        //db.execSQL("DROP TABLE IF EXISTS " + table_containKeyword);
        onCreate(db);
    }

    final void initializeContainRole(){
        String qry = "insert into "+table_containKeyword + "(room, ky, kylength, value) values" +
                "('룸이름',    '(키워드)안녕',    2,   '(대답)안녕하세요?')," +
                "('승엽',    'ㅇㄹㅇㄹㅇㅁㄴㅇ',  7,'ㅁㄴㅇㅁㄴㅇㅁㅇ')";

        db.execSQL(qry);
    }

    public void insertContainKeyword(String room, String key, String value, SQLiteDatabase db){
        if(db ==null) db = getReadableDatabase();
        try{
            String qry = "insert into "+table_containKeyword + "(room, ky,kylength, value) values('" +
                    room+"','"+key+"',"+key.length()+",'"+value+"');";
            db.execSQL(qry);
        }catch(Exception e){
            Log.i(TAG, "insertContainKeyword");
            e.printStackTrace();}


    }

    public Cursor getContainKeyword(String room, String key,SQLiteDatabase db){
        if(db ==null) db = getReadableDatabase();
        String qry = "select value from "+ table_containKeyword + " where (room='"+room+"' and ky like'%"+key+"%');";
        System.out.println("getconain "+qry);

        Cursor cursor = db.rawQuery(qry,null);
        System.out.println("getconain "+cursor.getCount());
        while(cursor.moveToNext()){
            System.out.println("getconain "+cursor.getString(0));
        }
        return cursor;
    }

    public boolean deleteContainKey(String room, int num,SQLiteDatabase db){
        try {
            if(db ==null) db = getReadableDatabase();
            String qry = "delete from " + table_containKeyword + " where num = " + num + ";";
            db.execSQL(qry);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public Cursor getContainsKeyList(String room,SQLiteDatabase db){
        if(db ==null) db = getReadableDatabase();
        String qry;
        if(room==null)
            //qry = "select room,ky,value,num from "+ table_containKeyword + " order by kylength desc;";
            qry = "select room,ky,value,num from "+ table_containKeyword + " order by num asc;";
        else
            qry = "select room,ky,value,num from "+ table_containKeyword + " where room='"+room+"' order by num asc;";
        Cursor cursor = db.rawQuery(qry,null);
        return cursor;
    }

    public Cursor getStockList(String keyword,SQLiteDatabase db){
        if(db ==null) db = getReadableDatabase();
        String qry = String.format("select stockType, stockCode, stockName from %s where stockName like '%%%s%%' order by stockType; ", stockListTableName, keyword);
        Cursor cursor = db.rawQuery(qry,null);
        return cursor;
    }

    public boolean createStockList(String fileNameInAsset, String stockTableName, SQLiteDatabase db){
        try {
            if(db ==null) db = getReadableDatabase();

            String createTable = "create table if not exists "+stockTableName+"(" +
                    "stockType varchar(100) not null," +
                    "stockName varchar(100) not null," +
                    "stockCode varchar(100) not null primary key);";
            db.execSQL(createTable);

            ///////////////////////////////////
            BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open(fileNameInAsset, AssetManager.ACCESS_BUFFER)));
            StringBuilder qry = new StringBuilder("insert into " + stockTableName + "(stockType, stockCode, stockName) values");
            String keyword = null;
            while (null != (keyword = br.readLine())){
                String[] v = keyword.split("\t");
                if(keyword.trim().length()<4) break;
                qry.append(  String.format("('%s', '%s', '%s'),",v[0],v[1],v[2])  );
            }
            qry.deleteCharAt(qry.length()-1);
            db.execSQL(qry.toString());

            viewStockList(stockTableName, db);
            return true;
        }catch(Exception e){e.printStackTrace();}
        return false;
    }
    public boolean createConsonantQuiz(String fileNameInAsset, String tableNaming, SQLiteDatabase db){
        try {
            if(db ==null) db = getReadableDatabase();

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

            viewConsonantQuizList(tableNaming, db);
            return true;
        }catch(Exception e){e.printStackTrace();}
        return false;
    }
    public void viewConsonantQuizList(String tableName, SQLiteDatabase db){
        if(db ==null) db = getReadableDatabase();
        String checkValues = "select * from "+ tableName + " order by num asc;";
        Cursor cursor = db.rawQuery(checkValues,null);
        while(cursor.moveToNext()) {
            String num = cursor.getString(0);
            String value = cursor.getString(1);
            Log.i(TAG, "QUIZ(n/v): " + num + "/" + value);
        }
    }
    public void viewStockList(String tableName, SQLiteDatabase db){
        if(db ==null) db = getReadableDatabase();
        String checkValues = "select * from "+ tableName + ";";
        Cursor cursor = db.rawQuery(checkValues,null);
        while(cursor.moveToNext()) {
            String stockType = cursor.getString(0);
            String stockCode = cursor.getString(1);
            String stockName = cursor.getString(1);
            Log.i(TAG, stockType+"/"+stockCode+"/"+stockName);
        }
    }
    public int getTableSize(String tableName,SQLiteDatabase db){
        if(db ==null) db = getReadableDatabase();
        String qry = "select * from "+ tableName +";";
        Cursor cursor = db.rawQuery(qry,null);
        return cursor.getCount();
    }
    public String getConsonantQuestion(String tableName,SQLiteDatabase db){
        if(db ==null) db = getReadableDatabase();
        int size = getTableSize(tableName, db);
        Random r = new Random();
        r.setSeed(System.currentTimeMillis());
        String qry = "select value from "+ tableName +" where num= "+ (r.nextInt(size)+1) + ";";
        Cursor cursor = db.rawQuery(qry,null);
        cursor.moveToNext();
        return cursor.getString(0);
    }
    public boolean putConsonantQuestion(String tableName, String keyword,SQLiteDatabase db) {
        if(db ==null) db = getReadableDatabase();
        String qry = "insert into "+ tableName +"(value) values('"+ keyword + "');";
        db.execSQL(qry);
        return true;
    }


}
