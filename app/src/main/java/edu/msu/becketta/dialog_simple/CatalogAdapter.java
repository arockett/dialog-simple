package edu.msu.becketta.dialog_simple;

/**
 * Created by Aaron Beckett on 1/11/2016.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * An adapter so that list boxes can display a list of filenames from
 * the cloud server.
 */
public class CatalogAdapter extends BaseAdapter {

    /**
     * The items we display in the list box. Initially this is
     * null until we get items from the server.
     */
    private ArrayList<LocalBase.Item> items = new ArrayList<LocalBase.Item>();

    /**
     * Constructor
     */
    public CatalogAdapter(final View view) {
        // Create a thread to load the catalog
        new Thread(new Runnable() {

            @Override
            public void run() {
                ArrayList<LocalBase.Item> newItems = getCatalog(view.getContext());
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

    public ArrayList<LocalBase.Item> getCatalog(Context context) {

        LocalBase localBase = LocalBase.getInstance(context);
        ArrayList<LocalBase.Item> newItems = localBase.getdiaLogs();

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
