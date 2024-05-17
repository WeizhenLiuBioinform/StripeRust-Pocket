package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import enity.Pic;

public class PicDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "pic.db";
    private static final String TABLE_NAME = "pic_info";
    private static PicDBHelper mHelper = null;
    private SQLiteDatabase mRDB = null;
    private SQLiteDatabase mWDB = null;

    private PicDBHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    public static PicDBHelper getInstance(Context context) {
        if (mHelper == null) {
            mHelper = new PicDBHelper(context);
        }
        return mHelper;
    }

    //建立连接
    public SQLiteDatabase openReadLink(){
        if(mRDB == null || !mRDB.isOpen()){
            mRDB = mHelper.getReadableDatabase();
        }
        return mRDB;
    }
    public SQLiteDatabase openWriteLink(){
        if(mWDB == null || !mWDB.isOpen()){
            mWDB = mHelper.getReadableDatabase();
        }
        return mWDB;
    }

    //关闭连接
    public void closelink(){
        if(mRDB !=null && mRDB.isOpen()){
            mRDB.close();;
            mRDB = null;
        }
        if(mWDB !=null && mWDB.isOpen()){
            mWDB.close();;
            mWDB = null;
        }
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                " path_leaf VARCHAR NOT NULL," +
                " path_seg VARCHAR NOT NULL," +
                " severity FLOAT NOT NULL," +
                " name VARCHAR NOT NULL," +
                " date VARCHAR NOT NULL);";
        db.execSQL(sql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void insert(Pic pic) {
        ContentValues values = new ContentValues();
        values.put("path_leaf", pic.path_leaf);
        values.put("path_seg", pic.path_seg);
        values.put("severity", pic.severity);
        values.put("name", pic.name);
        values.put("date", pic.date);
        mWDB.insert(TABLE_NAME, null, values);

    }

    public long delete(String name){
        //return mWDB.delete(TABLE_NAME,"1=1",null);
        return mWDB.delete(TABLE_NAME,"name=?",new String[]{name});
    }

    public long delete_all(){
        return mWDB.delete(TABLE_NAME,"1=1",null);
    }

    public long update(String name_old,String name_new,String date){
        ContentValues values = new ContentValues();
        values.put("name", name_new);
        values.put("date", date);
        return mWDB.update(TABLE_NAME,values,"name=?", new String[]{name_old});
    }

    public List<Pic> queryAll(){
        List<Pic> list = new ArrayList<>();
       Cursor cursor = mRDB.query(TABLE_NAME,null,null,null,null,null,null);
       while (cursor.moveToNext()){
           Pic pic = new Pic();
           pic.id = cursor.getInt(0);
           pic.path_leaf = cursor.getString(1);
           pic.path_seg = cursor.getString(2);
           pic.severity = cursor.getFloat(3);
           pic.name = cursor.getString(4);
           pic.date = cursor.getString(5);
           list.add(pic);
       }
       return list;
    }

    public List<Pic> querybyid(String id){
        List<Pic> list = new ArrayList<>();
        Cursor cursor = mRDB.query(TABLE_NAME,null,"_id=?",new String[]{id},null,null,null);
        while (cursor.moveToNext()){
            Pic pic = new Pic();
            pic.id = cursor.getInt(0);
            pic.path_leaf = cursor.getString(1);
            pic.path_seg = cursor.getString(2);
            pic.severity = cursor.getFloat(3);
            pic.name = cursor.getString(4);
            pic.date = cursor.getString(5);
            list.add(pic);
        }
        return list;
    }

}
