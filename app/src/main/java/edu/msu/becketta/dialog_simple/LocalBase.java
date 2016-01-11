package edu.msu.becketta.dialog_simple;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
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
         * TODO: Insert/update database
         */

        return true;
    }

    public diaLog loadLog() {
        diaLog log = new diaLog();

        return log;
    }

    /**
     * Nested class to store one catalog row
     */
    private static class Item {
        public String name = "";
        public String id = "";
    }

    /**
     * An adapter so that list boxes can display a list of filenames from
     * the cloud server.
     */
    public static class CatalogAdapter extends BaseAdapter {

        /**
         * The items we display in the list box. Initially this is
         * null until we get items from the server.
         */
        private ArrayList<Item> items = new ArrayList<Item>();

        /**
         * Constructor
         */
        public CatalogAdapter(final View view) {
            // Create a thread to load the catalog
            new Thread(new Runnable() {

                @Override
                public void run() {
                    ArrayList<Item> newItems = getCatalog();
                    if (newItems != null) {

                        items = newItems;

                        view.post(new Runnable() {

                            @Override
                            public void run() {
                                // Tell the adapter the data set has been changed
                                notifyDataSetChanged();
                            }

                        });
                    } else {
                        // Error condition!
                        view.post(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(view.getContext(), R.string.catalog_fail, Toast.LENGTH_SHORT).show();
                            }

                        });
                    }
                }
            }).start();
        }

        public ArrayList<Item> getCatalog() {
            ArrayList<Item> newItems = new ArrayList<Item>();

            /*
             * TODO: Get the diaLogs for the local database
             */

            return newItems;
        }

        public String getId(int position) {
            return items.get(position).id;
        }
        public String getName(int position) {
            return items.get(position).name;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if(view == null) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.catalog_item, parent, false);
            }

            TextView tv = (TextView)view.findViewById(R.id.textItem);
            tv.setText(items.get(position).name);

            return view;
        }
    }
}
