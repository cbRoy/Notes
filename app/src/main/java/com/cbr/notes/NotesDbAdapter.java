package com.cbr.notes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class NotesDbAdapter {

    public static final String KEY_ID       = "_id";
    public static final String KEY_TITLE    = "title";
    public static final String KEY_BODY     = "body";

    private static final String TAG = "NotesDbAdapter";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DB_CREATE =
            "create table notes (_id integer primary key autoincrement, "
            + "title text not null, body text not null);";

    private static final String DB_NAME = "data";
    private static final String DB_TABLE = "notes";
    private static final int DB_VERSION = 1;

    private final Context mContext;

    private static class DatabaseHelper extends SQLiteOpenHelper{
        DatabaseHelper(Context context){
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db){
            db.execSQL(DB_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading Database from " + oldVersion + " to "
                    + newVersion + ", which will detroy all data!");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    public NotesDbAdapter(Context context){
        this.mContext = context;
    }

    public NotesDbAdapter open() throws SQLException{
        mDbHelper = new DatabaseHelper(mContext);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        mDbHelper.close();
    }

    public long createNote(String title, String body){
        ContentValues cv = new ContentValues();
        cv.put(KEY_TITLE, title);
        cv.put(KEY_BODY, body);

        return mDb.insert(DB_TABLE,null,cv);
    }

    public boolean deleteNote(long id){
        return mDb.delete(DB_TABLE, KEY_ID + "=" + id,null) > 0;
    }

    public Cursor fetchAll(){
        return mDb.query(DB_TABLE, new String[] {KEY_ID,KEY_TITLE,KEY_BODY},
                null,null,null,null,null);
    }

    public Cursor fetchNote(long id) throws SQLException{
        Cursor mCursor = mDb.query(true, DB_TABLE, new String[]
                {KEY_ID,KEY_TITLE,KEY_BODY}, KEY_ID + '=' + id,
                null,null,null,null,null);
        if(mCursor != null){
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public boolean updateNote(long id, String title, String body){
        ContentValues cv = new ContentValues();
        cv.put(KEY_TITLE, title);
        cv.put(KEY_BODY, body);

        return mDb.update(DB_TABLE, cv, KEY_ID + "=" + id, null) > 0;
    }
}
