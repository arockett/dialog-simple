package edu.msu.becketta.dialog_simple;

import android.support.v4.app.Fragment;
import android.os.Bundle;
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
    }
}
