package de.hambuch.voronoiapp;

import android.graphics.Rect;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import de.hambuch.voronoiapp.algo.DelauTriangle;
import de.hambuch.voronoiapp.geometry.Circle;
import de.hambuch.voronoiapp.geometry.GeomElement;
import de.hambuch.voronoiapp.geometry.Point;
import de.hambuch.voronoiapp.geometry.Ray;
import de.hambuch.voronoiapp.geometry.Region;
import de.hambuch.voronoiapp.geometry.Segment;
import de.hambuch.voronoiapp.geometry.SimplePolygon;
import de.hambuch.voronoiapp.geometry.Triangle;

/**
 * Utility class that exports a set of (primitive) geometry elements to a SVG.
 */
public class SvgExporter {
    protected final PrintWriter writer;

    public SvgExporter(@NonNull OutputStream outputStream) {
        writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
    }
    public void export(@NonNull List<GeomElement> elements, @NonNull Rect rect) throws IOException {
        // Write header
        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<svg xmlns=\"http://www.w3.org/2000/svg\" " +
                " width=\""+rect.width()+"px\" height=\""+rect.height()+"px\" viewBox=\"0 0 "+rect.width()+" "+rect.height()+"\">");
        writer.println("<title>Export of VoronoiApp</title>");
        for(GeomElement element: elements) {
            if(element instanceof Point)
                export((Point)element);
            else if (element instanceof Segment) {
                export((Segment)element);
            } else if (element instanceof Circle) {
                export((Circle)element);
            } else if(element instanceof Triangle) {
                export((Triangle)element);
            } else if(element instanceof SimplePolygon) {
                export((SimplePolygon) element);
            } else if(element instanceof Region) {
                SimplePolygon poly = (SimplePolygon) ((Region)element).clipTo(rect.left, rect.top, rect.width(), rect.height());
                if(poly != null) // export only if visible
                    export(poly);
            } else if(element instanceof Ray) {
                Segment segment = ((Ray)element).clipTo(rect.left, rect.top, rect.width(), rect.height());
                if(segment != null)
                    export(segment);
            }
            else
                writer.println("<!-- Cannot export "+element.getClass().getName()+"-->");
        }
        writer.println("</svg>");
        writer.flush();
    }

    private void export(Point point) {
        writer.print("<circle cx=\""+point.getX()+"\" cy=\""+point.getY()+"\" r=\"3\"");
        writer.print(" stroke=\"#");
        writer.print(Integer.toHexString(point.getColor()));
        writer.print("\" fill=\"#");
        writer.print(Integer.toHexString(point.getColor()));
        writer.println("\"/>");
    }

    private void export(Circle circle) {
        writer.print("<circle cx=\""+circle.getCenter().getX()+"\" cy=\""+circle.getCenter().getY()+"\" r=\""+circle.getRadius()+"\"");
        exportColor(circle);
        writer.println("/>");
    }

    private void export(Segment line) {
        writer.print("<line x1=\""+line.getStartpoint().getX()+"\" y1=\""+line.getStartpoint().getY()+"\" x2=\""+line.getEndpoint().getX()+"\" y2=\""+line.getEndpoint().getY()+"\"");
        exportColor(line);
        writer.println("/>");
    }

    private void export(Triangle triangle) {
        writer.print("<polygon points=\"");
        writer.print(triangle.getPointA().getX());
        writer.print(",");
        writer.print(triangle.getPointA().getY());
        writer.print(" ");
        writer.print(triangle.getPointB().getX());
        writer.print(",");
        writer.print(triangle.getPointB().getY());
        writer.print(" ");
        writer.print(triangle.getPointC().getX());
        writer.print(",");
        writer.print(triangle.getPointC().getY());
        writer.print("\" fill=\"none\""); // and close
        exportColor(triangle);
        writer.println("/>");
    }

    private void export(DelauTriangle triangle) {
        if(triangle.isHalfplane()) {
            export(new Segment(triangle.getPointA(), triangle.getPointB()));
        } else export((Triangle) triangle);
    }

    private void export(SimplePolygon polygon) {
        Point[] points = polygon.toPoints();
        if (points != null && points.length > 1) {
            writer.print("<polygon points=\"");
            writer.print(points[0].getX());
            writer.print(",");
            writer.print(points[0].getY());
            for (int i = 1; i < points.length; i++) {
                writer.print(" ");
                writer.print(points[i].getX());
                writer.print(",");
                writer.print(points[i].getY());
            }
            writer.print("\" fill=\"none\""); // and close
            exportColor(polygon);
            writer.println("/>");
        }
    }

    private void exportColor(GeomElement element) {
        writer.print(" stroke=\"#");
        writer.print(Integer.toHexString(element.getColor()));
        writer.print("\" ");
    }
}
