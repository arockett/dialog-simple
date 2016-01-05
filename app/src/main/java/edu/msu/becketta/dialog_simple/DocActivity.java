package edu.msu.becketta.dialog_simple;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class DocActivity extends AppCompatActivity {

    /************************** MEMBERS *****************************/

    /**
     * Request code when selecting a picture
     */
    private static final int SELECT_PICTURE = 1;

    /************************** CONSTRUCTION *****************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_doc, menu);
        return true;
    }

    /************************** METHODS *****************************/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            case R.id.action_new:
                break;
            case R.id.action_load:
                break;
            case R.id.action_save:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /********************** GETTERS AND SETTERS *************************/

}
