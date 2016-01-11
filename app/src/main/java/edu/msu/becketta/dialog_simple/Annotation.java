package edu.msu.becketta.dialog_simple;

import android.graphics.Path;
import android.graphics.PointF;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Aaron Beckett on 1/5/2016.
 */
public class Annotation {

    /**
     * The data structure that stores the raw point data for each mark
     */
    private ArrayList<ArrayList<PointF>> curves = new ArrayList<>();

    /**
     * We must keep track of the current curve being recorded
     */
    private int currentCurve = -1;

    public void startNewCurve(float x, float y) {
        currentCurve++;
        curves.add(new ArrayList<PointF>());
        addPointToCurrentCurve(x, y);
    }

    public void addPointToCurrentCurve(float x, float y) {
        curves.get(currentCurve).add(new PointF(x, y));
    }

    public Path getPath() {
        Path path = new Path();
        float x0, y0, x, y;
        for (ArrayList<PointF> curve : curves) {
            x0 = curve.get(0).x;
            y0 = curve.get(0).y;
            path.moveTo(x0, y0);
            for (int i = 1; i < curve.size(); i++) {
                x = curve.get(i).x;
                y = curve.get(i).y;
                path.quadTo(x0, y0, (x + x0) / 2, (y + y0) / 2);
                x0 = x;
                y0 = y;
            }
            path.lineTo(x0, y0);
        }
        return path;
    }

    public boolean isEmpty() {
        return curves.size() == 0;
    }

    public void saveAnnotationsXml(XmlSerializer xml) throws IOException {
        xml.startTag(null, "annotation");

        for (ArrayList<PointF> curve : curves) {
            xml.startTag(null, "curve");

            for (PointF point : curve) {
                xml.startTag(null, "point");
                xml.attribute(null, "x", Float.toString(point.x));
                xml.attribute(null, "y", Float.toString(point.y));
                xml.endTag(null, "point");
            }

            xml.endTag(null, "curve");
        }

        xml.endTag(null, "annotation");
    }

    public void loadAnnotationsXml(XmlPullParser xml) throws IOException, XmlPullParserException {
        curves = new ArrayList<>();

        xml.nextTag();
        while (xml.getName().equals("curve")) {
            xml.nextTag();

            ArrayList<PointF> newCurve = new ArrayList<>();
            while (xml.getName().equals("point")) {
                float x = Float.parseFloat(xml.getAttributeValue(null, "x"));
                float y = Float.parseFloat(xml.getAttributeValue(null, "y"));
                PointF p = new PointF(x, y);
                newCurve.add(p);

                Utilities.skipToEndTag(xml);
                xml.nextTag();
            }
            curves.add(newCurve);

            Utilities.skipToEndTag(xml);
            xml.nextTag();
        }
    }
}
