package edu.msu.becketta.dialog_simple;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;

/**
 * Created by Aaron Beckett on 1/6/2016.
 */
public class LocalBase extends SQLiteOpenHelper {

    private static final String UTF8 = "UTF-8";

    private static LocalBase instance;

    // Database Info
    private static final String DATABASE_NAME = "LocalLogs.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_LOG = "Log";

    // Log Table Columns
    private static final String KEY_LOG_ID = "id";
    private static final String KEY_LOG_NAME = "name";
    private static final String KEY_LOG_XML = "xml";

    public static synchronized LocalBase getInstance(Context context) {
        if (instance == null) {
            instance = new LocalBase(context.getApplicationContext());
        }
        return instance;
    }

    public LocalBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGS_TABLE = "CREATE TABLE " + TABLE_LOG +
                "(" +
                    KEY_LOG_ID + " INTEGER PRIMARY KEY, " +
                    KEY_LOG_NAME + " TEXT, " +
                    KEY_LOG_XML + " BLOB" +
                ")";

        db.execSQL(CREATE_LOGS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOG);
            onCreate(db);
        }
    }

    public boolean saveLog(diaLog log) {
        String name = log.getName();
        if (name.length() == 0) {
            return false;
        }

        XmlSerializer xml = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try {
            xml.setOutput(writer);

            xml.startDocument(UTF8, true);

            log.saveToXml(xml);

            xml.endDocument();

        } catch (IOException e) {
            return false;
        }

        String xmlStr = writer.toString();

        /*
         * Insert/update database
         */
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_LOG_NAME, log.getName());
            values.put(KEY_LOG_XML, xmlStr);

            // Try updating a row with the same name
            // If no row is effected, insert a new record
            if(db.update(TABLE_LOG, values, "?=?", new String[]{KEY_LOG_NAME, values.getAsString(KEY_LOG_NAME)}) == 0) {
                db.insertOrThrow(TABLE_LOG, null, values);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("SQLite", "Error while trying to add diaLog to database");
        } finally {
            db.endTransaction();
        }

        return true;
    }

    public diaLog loadLog(String id) {
        /*
         * Get the diaLog with that id from the database
         */
        // SELECT * FROM Log WHERE id = 'id'
        String SELECT_QUERY = "SELECT * FROM " + TABLE_LOG +
                " WHERE " + KEY_LOG_ID + " = " + id;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_QUERY, null);
        diaLog newdiaLog = new diaLog();
        try {
            if (cursor.moveToFirst()) {
                String xmlStr = cursor.getString(cursor.getColumnIndex(KEY_LOG_XML));
                InputStream stream = new ByteArrayInputStream(xmlStr.getBytes("UTF-8"));

                XmlPullParser xml = Xml.newPullParser();
                xml.setInput(stream, "UTF-8");
                xml.nextTag();
                xml.require(XmlPullParser.START_TAG, null, "diaLog");
                newdiaLog.loadFromXml(xml);
            }
        } catch (Exception e) {
            Log.d("SQLite", "Error while trying to get diaLogs from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return newdiaLog;
    }

    public void clearLocalLogs() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_LOG, null, null);
    }

    public ArrayList<Item> getdiaLogs() {
        ArrayList<Item> diaLogItems = new ArrayList<>();

        /*
         * Get the diaLogs from the local database
         */
        // SELECT * FROM Log
        String SELECT_QUERY = "SELECT * FROM " + TABLE_LOG;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Item newItem = new Item();
                    newItem.id = cursor.getString(cursor.getColumnIndex(KEY_LOG_ID));
                    newItem.name = cursor.getString(cursor.getColumnIndex(KEY_LOG_NAME));
                    diaLogItems.add(newItem);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d("SQLite", "Error while trying to get diaLogs from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return diaLogItems;
    }

    /**
     * Nested class to store one catalog row
     */
    public class Item {
        public String name = "";
        public String id = "";
    }
}
