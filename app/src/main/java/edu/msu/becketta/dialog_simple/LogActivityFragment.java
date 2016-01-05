package edu.msu.becketta.dialog_simple;

import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * A placeholder fragment containing a simple view.
 */
public class LogActivityFragment extends Fragment {

    private enum Mode { DISABLED, READ, ANNOTATE }

    /**
     * Key string for saving the state of current mode.
     */
    private static final String STATE_CURRENT_MODE = "curent_mode";

    /**
     * The current mode, reading or annotating
     */
    private Mode currentMode = Mode.DISABLED;

    /**
     * {@link LogView} that shows a PDF page as a {@link android.graphics.Bitmap}
     */
    private LogView logView;

    /**
     * {@link FloatingActionButton} for starting a new {@link Annotation}
     */
    private FloatingActionButton annotFAB;

    private LinearLayout annotButtons;

    public LogActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_doc, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        annotButtons = (LinearLayout) view.findViewById(R.id.annotationButtons);
        annotFAB = (FloatingActionButton) view.findViewById(R.id.annotateFAB);
        annotFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnnotating();
            }
        });
        logView = (LogView) view.findViewById(R.id.logView);
        logView.setFreeDrawView((FreeDrawView) view.findViewById(R.id.freeDrawView));

        Button saveButton = (Button) view.findViewById(R.id.saveAnnotButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAnnotation();
            }
        });
        Button discardButton = (Button) view.findViewById(R.id.discardAnnotButton);
        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discardAnnotation();
            }
        });

        if (savedInstanceState != null) {
            currentMode = (Mode) savedInstanceState.getSerializable(STATE_CURRENT_MODE);
            logView.loadState(savedInstanceState);
            updateUI();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(STATE_CURRENT_MODE, currentMode);

        logView.saveState(outState);
    }

    public void setImagePath(String path) {
        logView.setImagePath(path);
        currentMode = Mode.READ;
        updateUI();
    }

    public void startAnnotating() {
        logView.startAnnotation();
        currentMode = Mode.ANNOTATE;
        updateUI();
    }

    public void saveAnnotation() {
        logView.finishAnnotation(false);
        currentMode = Mode.READ;
        updateUI();
    }

    public void discardAnnotation() {
        logView.finishAnnotation(true);
        currentMode = Mode.READ;
        updateUI();
    }

    private void updateUI() {
        if (currentMode == Mode.READ) {
            annotButtons.setVisibility(View.GONE);
            annotFAB.setVisibility(View.VISIBLE);
        } else if (currentMode == Mode.ANNOTATE) {
            annotFAB.setVisibility(View.INVISIBLE);
            annotButtons.setVisibility(View.VISIBLE);
        } else {
            annotButtons.setVisibility(View.GONE);
            annotFAB.setVisibility(View.INVISIBLE);
        }
    }
}
