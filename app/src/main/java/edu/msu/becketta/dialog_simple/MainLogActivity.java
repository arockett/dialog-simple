package edu.msu.becketta.dialog_simple;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;

public class MainLogActivity extends AppCompatActivity {

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
                createNewLog();
                break;
            case R.id.action_load:
                loadLog();
                break;
            case R.id.action_save:
                saveLog();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void createNewLog() {
        // Get a picture from the gallery to draw on
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    private void loadLog() {

    }

    private void saveLog() {
        LogActivityFragment fragment = (LogActivityFragment) getSupportFragmentManager().findFragmentById(R.id.log_fragment);
        fragment.saveLog();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PICTURE && resultCode == Activity.RESULT_OK) {
            // Response from the picture selection activity
            Uri imageUri = data.getData();

            // We have to query the database to determine the document ID for the image
            Cursor cursor = getContentResolver().query(imageUri, null, null, null, null);
            cursor.moveToFirst();
            String document_id = cursor.getString(0);
            document_id = document_id.substring(document_id.lastIndexOf(":")+1);
            cursor.close();

            // Next, we query the content provider to find the path for this
            // document id.
            cursor = getContentResolver().query(
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
            cursor.moveToFirst();
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();

            String name = "";
            if(path != null) {
                File f = new File(path);
                name = f.getName();
            }

            // send this image path to the Log fragment
            LogActivityFragment fragment = (LogActivityFragment) getSupportFragmentManager().findFragmentById(R.id.log_fragment);
            fragment.startNewLog(name, imageUri);
        }
    }

    /********************** GETTERS AND SETTERS *************************/

}
