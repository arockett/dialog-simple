package edu.msu.becketta.dialog_simple;

import android.graphics.Bitmap;
import android.graphics.Path;

import java.util.ArrayList;

/**
 * Created by Aaron Beckett on 1/10/2016.
 */
public class diaLog {

    private String name;
    private Bitmap image;
    private ArrayList<Annotation> annotations = new ArrayList<>();

    public void addAnnotation(Annotation newAnnot) {
        annotations.add(newAnnot);
    }

    /********************** GETTERS AND SETTERS **********************/

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public Bitmap getImage() {
        return image;
    }

    public ArrayList<Path> getPaths() {
        ArrayList<Path> paths = new ArrayList<>();
        for (Annotation a : annotations) {
            paths.add(a.getPath());
        }
        return paths;
    }
}
