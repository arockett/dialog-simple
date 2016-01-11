package edu.msu.becketta.dialog_simple;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;

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
    private static final String TABLE_LOGS = "Logs";

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
        String CREATE_LOGS_TABLE = "CREATE TABLE " + TABLE_LOGS +
                "(" +
                    "id INTEGER PRIMARY KEY, " +
                    "name TEXT, " +
                    "image ," +
                    "annots " +
                ")";

        db.execSQL(CREATE_LOGS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGS);
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

            log.saveAnnotationsXml(xml);

            xml.endDocument();

        } catch (IOException e) {
            return false;
        }

        String xmlStr = writer.toString();
        Log.i("XML", xmlStr);

        /*
         * Insert/update database
         */

        return true;
    }

    public diaLog loadLog() {
        diaLog log = new diaLog();

        return log;
    }
}
