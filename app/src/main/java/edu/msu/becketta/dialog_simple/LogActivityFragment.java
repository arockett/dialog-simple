package edu.msu.becketta.dialog_simple;

import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A placeholder fragment containing a simple view.
 */
public class LogActivityFragment extends Fragment {

    public LogActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_doc, container, false);
    }

    public void setImagePath(String path) {
        LogView logView = (LogView) getView().findViewById(R.id.logView);
        logView.setImagePath(path);

        FloatingActionButton annotFAB = (FloatingActionButton) getView().findViewById(R.id.annotateFAB);
        annotFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAnnotate();
            }
        });
        annotFAB.setVisibility(View.VISIBLE);
    }

    public void toggleAnnotate() {
        Log.i("FAB", "Pressed");
    }
}
