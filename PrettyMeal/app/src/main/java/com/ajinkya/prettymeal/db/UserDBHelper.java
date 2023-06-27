package com.ajinkya.prettymeal.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ajinkya.prettymeal.model.UserInfo;

import java.util.ArrayList;

public class UserDBHelper extends SQLiteOpenHelper {
    private static final String TAG = "UserDBHelper";
    private static final String DB_NAME = "user.db"; // 数据库的名称
    private static final int DB_VERSION = 1; // 数据库的版本号
    private static UserDBHelper mHelper = null; // 数据库帮助器的实例
    private SQLiteDatabase mDB = null; // 数据库的实例
    public static final String TABLE_Customer = "Customer";
    public static final String TABLE_Business = "Business";
    public static final String TABLE_CustomerHistory = "CustomerHistory";
    public static final String TABLE_BusinessHistory = "BusinessHistory";

    public UserDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    private UserDBHelper(Context context, int version) {
        super(context, DB_NAME, null, version);
    }

    /**
    * 利用单例模式获取数据库帮助器的唯一实例
    **/
    public static UserDBHelper getInstance(Context context, int version) {
        if (version > 0 && mHelper == null) {
            mHelper = new UserDBHelper(context, version);
        } else if (mHelper == null) {
            mHelper = new UserDBHelper(context);
        }
        return mHelper;
    }

    /**
     * 构造数据库，将外部数据库与帮助器内部的数据库连接
     **/
    public void setmDB(SQLiteDatabase mDB) {
        this.mDB = mDB;
    }

    /**
     * 打开数据库的读连接
     **/
    public SQLiteDatabase openReadLink() {
        if (mDB == null || !mDB.isOpen()) {
            mDB = mHelper.getReadableDatabase();
        }
        return mDB;
    }

    /**
     * 打开数据库的写连接
     **/
    public SQLiteDatabase openWriteLink() {
        if (mDB == null || !mDB.isOpen()) {
            mDB = mHelper.getWritableDatabase();
        }
        return mDB;
    }

    /**
     * 关闭数据库连接
     **/
    public void closeLink() {
        if (mDB != null && mDB.isOpen()) {
            mDB.close();
            mDB = null;
        }
    }

    /**
     * 创建数据库,执行建表语句
     **/
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate");
        //如果存在TABLE_NAME，删除该表格
//        String drop_TABLE_Customer = "DROP TABLE IF EXISTS " + TABLE_Customer + ";";
//        db.execSQL(drop_TABLE_Customer);
//        String drop_TABLE_Business = "DROP TABLE IF EXISTS " + TABLE_Business + ";";
//        db.execSQL(drop_TABLE_Business);

        String create_TABLE_Customer = "CREATE TABLE IF NOT EXISTS " + TABLE_Customer + " ("
                + "customer_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                //演示数据库升级时要先把下面这行注释
                + "customer_name VARCHAR NOT NULL" + ",customer_phone VARCHAR NOT NULL"
                + ",customer_email VARCHAR "+ ",customer_password VARCHAR"
                + ",customer_address VARCHAR "+ ",customer_profileUrl VARCHAR" + ");";
        db.execSQL(create_TABLE_Customer);

        String create_TABLE_Business = "CREATE TABLE IF NOT EXISTS  " + TABLE_Business + " ("
                + "business_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                //演示数据库升级时要先把下面这行注释
                + "business_name VARCHAR NOT NULL" + ",business_phone VARCHAR NOT NULL"
                + ",business_email VARCHAR "+ ",business_password VARCHAR"
                + ",business_address VARCHAR "+ ",business_profileUrl VARCHAR" + ");";
        db.execSQL(create_TABLE_Business);

        String create_TABLE_CustomerHistory = "CREATE TABLE IF NOT EXISTS " + TABLE_CustomerHistory + " ("
                + "customerHistory_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                //演示数据库升级时要先把下面这行注释
                + "customerHistory_Name VARCHAR NOT NULL"
                + ",customerHistory_DateTime VARCHAR "+ ",customerHistory_TransactionNo VARCHAR"
                +");";
        db.execSQL(create_TABLE_CustomerHistory);

        String create_TABLE_BusinessHistory = "CREATE TABLE IF NOT EXISTS  " + TABLE_BusinessHistory + " ("
                + "businessHistory_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                //演示数据库升级时要先把下面这行注释
                + "businessHistory_Name VARCHAR NOT NULL"
                + ",businessHistory_DateTime VARCHAR "+ ",businessHistory_TransactionNo VARCHAR"
                +");";
        db.execSQL(create_TABLE_BusinessHistory);
    }

    /**
     * 修改数据库,执行表结构变更语句
     **/
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade oldVersion=" + oldVersion + ", newVersion=" + newVersion);
//        if (newVersion > 1) {
//            //Android的ALTER命令不支持一次添加多列，只能分多次添加
//            String alter_sql = "ALTER TABLE " + TABLE_Customer + " ADD COLUMN " + "phone VARCHAR;";
//            Log.d(TAG, "alter_sql:" + alter_sql);
//            db.execSQL(alter_sql);
//            alter_sql = "ALTER TABLE " + TABLE_Business + " ADD COLUMN " + "password VARCHAR;";
//            Log.d(TAG, "alter_sql:" + alter_sql);
//            db.execSQL(alter_sql);
//        }
    }

    /** 根据指定条件删除表记录
     * 调用如下
     UserDBHelper mHelper = null;
     SQLiteDatabase sql = mHelper.openWriteLink();
     mHelper.setmDB(sql);//将数据库实例传到数据库帮助器里

     mHelper.delete("par_id='3'");
     * */
    public void delete(String table,String condition) {
        // 执行删除记录动作
        mDB.delete(table, condition, null);
    }

    /**
     * 删除该表的所有记录,返回删除记录的数目
     **/
    public int deleteAll(String table) {
        return mDB.delete(table, "1=1", null);
    }

    /**
     * 往该表添加一条记录
     **/
    public void insert(String table, UserInfo info) {
        ArrayList<UserInfo> infoArray = new ArrayList<UserInfo>();
        infoArray.add(info);
        insert(table,infoArray);
    }

    /**
     * 往该表添加多条记录
     **/
    public void insert(String table,ArrayList<UserInfo> infoArray) {
        for (int i = 0; i < infoArray.size(); i++) {
            UserInfo info = infoArray.get(i);
            ArrayList<UserInfo> tempArray = new ArrayList<UserInfo>();
            ContentValues cv = new ContentValues();
            String condition = "";
            // 注意条件语句的等号后面要用单引号括起来
            switch (table){
                case TABLE_BusinessHistory:
                    cv.put("businessHistory_Name", info.UserName);
                    cv.put("businessHistory_DateTime", info.history_DateTime);
                    cv.put("businessHistory_TransactionNo", info.history_TransactionNo);
                    break;
                case TABLE_CustomerHistory:
                    cv.put("customerHistory_Name", info.UserName);
                    cv.put("customerHistory_DateTime", info.history_DateTime);
                    cv.put("customerHistory_TransactionNo", info.history_TransactionNo);
                    break;
                case TABLE_Customer:
//                    // 如果存在同样的手机号码，则更新记录
//                    if (info.UserMobileNo != null && info.UserMobileNo.length() > 0) {
//                        condition = String.format("UserMobileNo ='%s'", info.UserMobileNo);
//                        tempArray = query(TABLE_Customer,condition);//更新指定的表记录
//                        if (tempArray.size() > 0) {
//                            update(TABLE_Customer, info, condition);
//                            continue;
//                        }
//                    }
                    cv.put("customer_name", info.UserName);
                    cv.put("customer_phone", info.UserMobileNo);
                    cv.put("customer_email", info.UserEmail);
                    cv.put("customer_password", info.UserPassword);
                    cv.put("customer_address", info.UserAddress);
                    cv.put("customer_profileUrl", info.UserProfileUrl);
                    break;
                case TABLE_Business:
                    // 如果存在同样的手机号码，则更新记录
//                    if (info.UserMobileNo != null && info.UserMobileNo.length() > 0) {
//                        condition = String.format("UserMobileNo ='%s'", info.UserMobileNo);
//                        tempArray = query(TABLE_Customer,condition);//更新指定的表记录
//                        if (tempArray.size() > 0) {
//                            update(TABLE_Customer, info, condition);
//                            continue;
//                        }
//                    }
                    cv.put("business_name", info.UserName);
                    cv.put("business_phone", info.UserMobileNo);
                    cv.put("business_email", info.UserEmail);
                    cv.put("business_password", info.UserPassword);
                    cv.put("business_address", info.UserAddress);
                    cv.put("business_profileUrl", info.UserProfileUrl);
                    break;
                default:
                    break;
            }
            mDB.insert(table, "", cv);
        }
    }


    /** 根据条件 <par_id='7'> 找到指定的表记录，更新 par_password内容
     * 调用如下
     UserDBHelper mHelper = null;
     SQLiteDatabase sql = mHelper.openWriteLink();
     mHelper.setmDB(sql);//将数据库实例传到数据库帮助器里

     UserInfo updateduser = new UserInfo();
     updateduser.par_password = "00";
     mHelper.update(updateduser,"par_id='7'");
     * */
    public void update(String table, UserInfo info, String condition) {
        ContentValues cv = new ContentValues();
        switch (table){
            case TABLE_Customer:
                cv.put("customer_password", info.UserPassword);
                break;
            case TABLE_Business:
                cv.put("business_password", info.UserPassword);
                break;
            default:
                break;
        }
        mDB.update(table, cv, condition, null);
    }

    /** 根据条件 <par_id='8'> 找到指定的表记录，更新指定的关键字内容
     * 调用如下
     UserDBHelper mHelper = null;
     SQLiteDatabase sql = mHelper.openWriteLink();
     mHelper.setmDB(sql);//将数据库实例传到数据库帮助器里

     UserInfo updateduser = new UserInfo();
     updateduser.par_phone = "00";
     mHelper.updateKey(updateduser,"par_phone","par_id='8'");
     * */
    public void updateKey(String table,UserInfo info,String key,String condition) {
        ContentValues cv = new ContentValues();
        switch (table){
            case TABLE_Customer:
                switch(key){
                    case "customer_name":
                        cv.put("customer_name", info.UserName);
                        break;
                    case "customer_phone":
                        cv.put("customer_phone", info.UserMobileNo);
                        break;
                    case "customer_email":
                        cv.put("customer_email", info.UserEmail);
                        break;
                    case "customer_password":
                        cv.put("customer_password", info.UserPassword);
                        break;
                    case "customer_address":
                        cv.put("customer_address", info.UserAddress);
                        break;
                    case "customer_profileUrl":
                        cv.put("customer_profileUrl", info.UserProfileUrl);
                        break;
                    default:
                        break;
                }
                break;
            case TABLE_Business:
                switch(key){
                    case "business_name":
                        cv.put("business_name", info.UserName);
                        break;
                    case "business_phone":
                        cv.put("business_phone", info.UserMobileNo);
                        break;
                    case "business_email":
                        cv.put("business_email", info.UserEmail);
                        break;
                    case "business_password":
                        cv.put("business_password", info.UserPassword);
                        break;
                    case "business_address":
                        cv.put("business_address", info.UserAddress);
                        break;
                    case "business_profileUrl":
                        cv.put("business_profileUrl", info.UserProfileUrl);
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        mDB.update(table, cv, condition, null);
    }

    /** 返回数据表 TABLE_NAME 的所有记录:
     * 调用如下
     UserDBHelper mHelper = null;
     SQLiteDatabase sql = mHelper.openReadLink();
     mHelper.setmDB(sql);//将数据库实例传到数据库帮助器里

     ArrayList<UserInfo> infoArrayList = mHelper.queryAll();
     for(int i=0;i<infoArrayList.size();i++){
     Log.i("kk",""+infoArrayList.get(i).toString());
     }
     * */
    public ArrayList<UserInfo> queryAll(String table) {
        ArrayList<UserInfo> infoArray = new ArrayList<UserInfo>();
        // 执行记录查询动作，该语句返回结果集的游标
        String sql = String.format("select * from %s",table);
        Cursor cursor = mDB.rawQuery(sql, null);
        switch (table){
            case TABLE_BusinessHistory:
                // 循环取出游标指向的每条记录
                while (cursor.moveToNext()) {
                    UserInfo info = new UserInfo();
                    info.UserId = cursor.getInt(0);
                    info.UserName = cursor.getString(1);
                    info.history_DateTime = cursor.getString(2);
                    info.history_TransactionNo = cursor.getString(3);
                    infoArray.add(info);
                }
                break;
            case TABLE_CustomerHistory:
                // 循环取出游标指向的每条记录
                while (cursor.moveToNext()) {
                    UserInfo info = new UserInfo();
                    info.UserId = cursor.getInt(0);
                    info.UserName = cursor.getString(1);
                    info.history_DateTime = cursor.getString(2);
                    info.history_TransactionNo = cursor.getString(3);
                    infoArray.add(info);
                }
                break;
            case TABLE_Customer:
                // 循环取出游标指向的每条记录
                while (cursor.moveToNext()) {
                    UserInfo info = new UserInfo();
                    info.UserId = cursor.getInt(0);
                    info.UserName = cursor.getString(1);
                    info.UserMobileNo = cursor.getString(2);
                    info.UserEmail = cursor.getString(3);
                    info.UserPassword = cursor.getString(4);
                    info.UserAddress = cursor.getString(5);
                    info.UserProfileUrl = cursor.getString(6);
                    infoArray.add(info);
                }
                break;
            case TABLE_Business:
                // 循环取出游标指向的每条记录
                while (cursor.moveToNext()) {
                    UserInfo info = new UserInfo();
                    info.UserId = cursor.getInt(0);
                    info.UserName = cursor.getString(1);
                    info.UserMobileNo = cursor.getString(2);
                    info.UserEmail = cursor.getString(3);
                    info.UserPassword = cursor.getString(4);
                    info.UserAddress = cursor.getString(5);
                    info.UserProfileUrl = cursor.getString(6);
                    infoArray.add(info);
                }
                break;
            default:
                break;
        }
        cursor.close(); // 查询完毕，关闭游标
        return infoArray;
    }

    /** 根据指定条件查询记录，并返回结果数据队列
     * 调用如下
     UserDBHelper mHelper = null;
     SQLiteDatabase sql = mHelper.openReadLink();
     mHelper.setmDB(sql);//将数据库实例传到数据库帮助器里

     ArrayList<UserInfo> infoArrayList = mHelper.query("par_id='1'");
     for(int i=0;i<infoArrayList.size();i++){
     Log.i("kk",""+infoArrayList.get(i).toString());
     }
     * */
    public ArrayList<UserInfo> query(String table,String condition) {
        ArrayList<UserInfo> infoArray = new ArrayList<UserInfo>();
        //假设condition为"par_id = '1'"，返回数据表 TABLE_NAME 中 par_id 字段值为 1 的所有记录
        String sql = String.format("select * from %s where %s;",table,condition);
        Cursor cursor = null;
        if(sql != null){
            // 执行记录查询动作，该语句返回结果集的游标
            cursor = mDB.rawQuery(sql, null);
        }
        switch (table){
            case TABLE_BusinessHistory:
                // 循环取出游标指向的每条记录
                while (cursor.moveToNext()) {
                    UserInfo info = new UserInfo();
                    info.UserId = cursor.getInt(0);
                    info.UserName = cursor.getString(1);
                    info.history_DateTime = cursor.getString(2);
                    info.history_TransactionNo = cursor.getString(3);
                    infoArray.add(info);
                }
                break;
            case TABLE_CustomerHistory:
                // 循环取出游标指向的每条记录
                while (cursor.moveToNext()) {
                    UserInfo info = new UserInfo();
                    info.UserId = cursor.getInt(0);
                    info.UserName = cursor.getString(1);
                    info.history_DateTime = cursor.getString(2);
                    info.history_TransactionNo = cursor.getString(3);
                    infoArray.add(info);
                }
                break;
            case TABLE_Customer:
                // 循环取出游标指向的每条记录
                while (cursor.moveToNext()) {
                    UserInfo info = new UserInfo();
                    info.UserId = cursor.getInt(0);
                    info.UserName = cursor.getString(1);
                    info.UserMobileNo = cursor.getString(2);
                    info.UserEmail = cursor.getString(3);
                    info.UserPassword = cursor.getString(4);
                    info.UserAddress = cursor.getString(5);
                    info.UserProfileUrl = cursor.getString(6);
                    infoArray.add(info);
                }
                break;
            case TABLE_Business:
                // 循环取出游标指向的每条记录
                while (cursor.moveToNext()) {
                    UserInfo info = new UserInfo();
                    info.UserId = cursor.getInt(0);
                    info.UserName = cursor.getString(1);
                    info.UserMobileNo = cursor.getString(2);
                    info.UserEmail = cursor.getString(3);
                    info.UserPassword = cursor.getString(4);
                    info.UserAddress = cursor.getString(5);
                    info.UserProfileUrl = cursor.getString(6);
                    infoArray.add(info);
                }
                break;
            default:
                break;
        }
        cursor.close(); // 查询完毕，关闭游标
        return infoArray;
    }

    /** 根据手机号码查询指定记录
     * 调用如下
     UserDBHelper mHelper = null;
     SQLiteDatabase sql = mHelper.openReadLink();
     mHelper.setmDB(sql);//将数据库实例传到数据库帮助器里

     UserInfo info = mHelper.queryByPhone("11");
     Log.i("kk",""+info.toString());
     * */
    public UserInfo queryByPhone(String table,String phone) {
        UserInfo info = null;
        ArrayList<UserInfo> infoArray = new ArrayList<>();
        switch (table){
            case TABLE_Customer:
                infoArray = query(TABLE_Customer,String.format("UserMobileNo ='%s'", phone));
                break;
            case TABLE_Business:
                infoArray = query(TABLE_Business,String.format("UserMobileNo ='%s'", phone));
                break;
            default:
                break;
        }
        if (infoArray.size() > 0) {
            info = infoArray.get(0);
        }
        return info;
    }

}
