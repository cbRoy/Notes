package com.cbr.notes;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DbFunctions {

    public static final String TAG = DbFunctions.class.getName();

    public static final String PACKAGE_NAME = "com.cbr.notes";
    public static final String DATABASE_NAME = "notes.db";
    public static final String DATABASE_TABLE = "notes";

    /** Contains: /data/data/com.cbr.notes/databases/notes.db **/
    private static final File DATA_DIRECTORY_DATABASE =
            new File(Environment.getDataDirectory() +
                    "/data/" + PACKAGE_NAME +
                    "/databases/" + DATABASE_NAME );

    public static boolean exportToFile(String exportDirectory){
        if(!SdPresent()) return false;
        String dateStamp = new SimpleDateFormat("yyyyMMdd", Locale.US).format(Calendar.getInstance().getTime());
        String outFilename = "notes_"+dateStamp+".db";

        File outFile = new File(new File(exportDirectory),outFilename);

        try{
            outFile.createNewFile();
            copyFile(DATA_DIRECTORY_DATABASE, outFile);
            return true;
        }catch(IOException e){
            e.printStackTrace();
            return false;
        }
    }

    public static boolean restoreDb(String restoreFileName){
        if(!SdPresent()) return false;

        File importFile = new File(restoreFileName);

        if(!checkDbIsValidFromFile(importFile)) return false;

        if(!importFile.exists()) return false;

        try{
            DATA_DIRECTORY_DATABASE.createNewFile();
            copyFile(importFile, DATA_DIRECTORY_DATABASE);
            return true;
        }catch(IOException e){
            e.printStackTrace();
            return false;
        }

    }

    private static boolean checkDbIsValidFromFile(File db){
        try{
            SQLiteDatabase SqlDb = SQLiteDatabase.openDatabase(db.getPath(), null, SQLiteDatabase.OPEN_READONLY);
            Cursor cursor = SqlDb.query(true, DATABASE_TABLE,
                    null, null, null, null, null, null, null);

            String[] allColumns = {NotesDbAdapter.KEY_ID, NotesDbAdapter.KEY_BODY, NotesDbAdapter.KEY_TITLE};
            for(String s : allColumns){
                cursor.getColumnIndexOrThrow(s);
            }
            cursor.close();
            SqlDb.close();
        }catch(IllegalArgumentException e ) {
            Log.d(TAG, "Database valid but not the right type");
            e.printStackTrace();
            return false;
        } catch( SQLiteException e ) {
            Log.d(TAG, "Database file is invalid.");
            e.printStackTrace();
            return false;
        } catch( Exception e){
            Log.d(TAG, "checkDbIsValid encountered an exception");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static void copyFile(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }

    private static boolean SdPresent(){
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }
}
