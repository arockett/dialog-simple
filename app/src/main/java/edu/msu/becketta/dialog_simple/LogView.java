package edu.msu.becketta.dialog_simple;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Aaron Beckett on 1/5/2016.
 */
public class LogView extends View {

    /************************** MEMBERS *****************************/

    /**
     * Key that is used to save the state of our parameters in a bundle
     */
    private static final String STATE_PARAMETERS = "state_parameters";

    /**
     * {@link FreeDrawView} that is used to make new annotations
     */
    private FreeDrawView freeDrawView;

    /**
     * The annotations that have been made on the current page
     */
    private diaLog currentLog;
    private ArrayList<Path> paths = new ArrayList<>();

    /**
     * The current parameters
     */
    private Parameters params = new Parameters();

    /**
     * The image bitmap. None initially.
     */
    private Bitmap imageBitmap = null;

    /**
     * The annotation canvas, bitmap, and paint
     */
    private Canvas annotCanvas;
    private Bitmap annotBitmap;
    private Paint annotBitmapPaint;
    private Paint annotPaint;

    /**
     * First touch status
     */
    private Touch touch1 = new Touch();

    /**
     * Second touch status
     */
    private Touch touch2 = new Touch();

    /************************** CONSTRUCTION *****************************/

    public LogView(Context context) {
        super(context);
        init(null, 0);
    }

    public LogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public LogView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyle) {
        annotBitmapPaint = new Paint(Paint.DITHER_FLAG);

        annotPaint = new Paint();
        annotPaint.setAntiAlias(true);
        annotPaint.setDither(true);
        annotPaint.setColor(Color.argb(190, 235, 68, 68));
        annotPaint.setStyle(Paint.Style.STROKE);
        annotPaint.setStrokeJoin(Paint.Join.ROUND);
        annotPaint.setStrokeCap(Paint.Cap.ROUND);
        annotPaint.setStrokeWidth(12);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        annotBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        annotCanvas = new Canvas(annotBitmap);
    }

    public void saveState(Bundle bundle) {

        bundle.putSerializable(STATE_PARAMETERS, params);

        if (freeDrawView != null) {
            freeDrawView.saveState(bundle);
        }
    }

    public void loadState(Bundle savedInstanceState) {
        params = (Parameters)savedInstanceState.getSerializable(STATE_PARAMETERS);

        if (freeDrawView != null) {
            freeDrawView.loadState(savedInstanceState);
        }
    }

    /************************** METHODS *****************************/
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // If there is no image to draw, we do nothing
        if(imageBitmap == null) {
            return;
        }

        if (params.imageScale == -1f) {
            /*
             * Determine the margins and scale to draw the image
             * centered and scaled to maximum size on any display
             */
            // Get the canvas size
            float wid = canvas.getWidth();
            float hit = canvas.getHeight();

            // What would be the scale to draw the where it fits both
            // horizontally and vertically?
            float scaleH = wid / imageBitmap.getWidth();
            float scaleV = hit / imageBitmap.getHeight();

            // Use the lesser of the two
            params.imageScale = scaleH < scaleV ? scaleH : scaleV;

            // What is the scaled image size?
            float iWid = params.imageScale * imageBitmap.getWidth();
            float iHit = params.imageScale * imageBitmap.getHeight();

            // Determine the top and left margins to center
            params.marginLeft = (wid - iWid) / 2;
            params.marginTop = (hit - iHit) / 2;
        }

        // TODO: address scrolling and scaling
        /*
         * Draw the image bitmap
         */
        canvas.save();
        canvas.translate(params.marginLeft, params.marginTop);
        canvas.scale(params.imageScale, params.imageScale);
        canvas.drawBitmap(imageBitmap, 0, 0, null);
        canvas.restore();

        /*
         * Draw the annotations
         */
        if (annotBitmap != null && !paths.isEmpty()) {
            for (Path path : paths) {
                annotCanvas.drawPath(path, annotPaint);
            }
            canvas.drawBitmap(annotBitmap, 0, 0, annotBitmapPaint);
        }
    }

    public void newLog(String name, Uri imageUri) {
        clear();
        currentLog = new diaLog();
        currentLog.setName(name);
        setImageUri(imageUri);
    }

    public void loadLog(diaLog log) {
        clear();
        setImageUri(log.getImageUri());
        paths = log.getPaths();
        currentLog = log;
        invalidate();
    }

    private void clear() {
        params.imageScale = -1f;
        paths.clear();
        annotBitmap.eraseColor(Color.TRANSPARENT);
    }

    public boolean saveLog() {
        currentLog.setImageUri(params.imageUri);

        LocalBase localBase = LocalBase.getInstance(getContext());
        return localBase.saveLog(currentLog);
    }

    public void startAnnotation() {
        freeDrawView.enable();
    }

    public void finishAnnotation(boolean discard) {
        Annotation newAnnot = freeDrawView.disable();
        if (!discard && !newAnnot.isEmpty()) {
            currentLog.addAnnotation(newAnnot);
            paths.add(newAnnot.getPath());
        }
    }

    /********************** GETTERS AND SETTERS **********************/

    /**
     * Set an image URI based on a string representation.
     * @param imageUri Uri for the image file
     */
    public void setImageUri(String imageUri) {
        // We'll clear the old URI until we know a new one
        imageBitmap = null;
        params.imageUri = "";

        if(imageUri != null) {
            Uri uri = Uri.parse(imageUri);
            setImageUri(uri);
        }

        invalidate();
    }

    /**
     * Set the image URI. Load an image from any source,
     * including external sources.
     * @param uri URI for the image
     */
    public void setImageUri(final Uri uri) {
        final String scheme = uri.getScheme();
        if(scheme == null) {
            // If no scheme, we have no image
            imageBitmap = null;
            params.imageUri = "";
            return;
        }

        new Thread(new Runnable() {

            /**
             * Run the thread that loads the image
             */
            @Override
            public void run() {

                boolean success = false;
                try {
                    // This code has been modified to load content either
                    // from a content provider (local) or an arbitrary URL
                    // (internet)
                    InputStream input;
                    if(scheme.equals("content")) {
                        input = getContext().getContentResolver().openInputStream(uri);
                    } else {
                        URL url = new URL(uri.toString());
                        input = url.openStream();
                    }

                    imageBitmap = BitmapFactory.decodeStream(input);
                    input.close();
                    params.imageUri = uri.toString();

                    success = true;

                } catch(FileNotFoundException ex) {
                    // All of these are empty, since we
                    // indicate an exception by leaving success
                    // set to false.
                } catch(MalformedURLException ex) {
                } catch(IOException ex) {
                }

                if(!success) {
                    imageBitmap = null;
                    params.imageUri = "";
                }

                /**
                 * Post execute in the UI thread to invalidate and
                 * force a redraw.
                 */
                post(new Runnable() {

                    @Override
                    public void run() {
                        invalidate();
                    }
                });
            }

        }).start();
    }

    public void setFreeDrawView(FreeDrawView freeDrawView) {
        this.freeDrawView = freeDrawView;
    }

    /************************ NESTED CLASSES ***************************/

    private static class Parameters implements Serializable {
        /**
         * Path to the image file if one exists
         */
        public String imageUri = "";

        /**
         * Image drawing scale
         */
        private float imageScale = -1f;

        /**
         * Image left margin in pixels
         */
        private float marginLeft = 0;

        /**
         * Image top margin in pixels
         */
        private float marginTop = 0;
    }

    /**
     * Local class to handle the touch status for one touch.
     * We will have one object of this type for each of the
     * two possible touches.
     */
    private class Touch {
        /**
         * Touch id
         */
        public int id = -1;

        /**
         * Current x location
         */
        public float x = 0;

        /**
         * Current y location
         */
        public float y = 0;

        /**
         * Previous x location
         */
        public float lastX = 0;

        /**
         * Previous y location
         */
        public float lastY = 0;

        /**
         * Change in x value from previous
         */
        public float dX = 0;

        /**
         * Change in y value from previous
         */
        public float dY = 0;

        /**
         * Copy the current values to the previous values
         */
        public void copyToLast() {
            lastX = x;
            lastY = y;
        }

        /**
         * Compute the values of dX and dY
         */
        public void computeDeltas() {
            dX = x - lastX;
            dY = y - lastY;
        }
    }
}
