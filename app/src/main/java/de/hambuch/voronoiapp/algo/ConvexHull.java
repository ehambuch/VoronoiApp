package de.hambuch.voronoiapp.algo;

import android.graphics.Canvas;
import android.graphics.Color;

import androidx.annotation.NonNull;

import de.hambuch.voronoiapp.geometry.Point;
import de.hambuch.voronoiapp.geometry.Segment;
import de.hambuch.voronoiapp.geometry.SimplePolygon;


/**
 * A convex hull of a set of points based on the Delaunay triangulation.
 *
 * @version 1.0
 * @author Eric Hambuch
 * @see DelaunayTriangulation
 */
public class ConvexHull extends de.hambuch.voronoiapp.geometry.GeomElement {

    private DelaunayTriangulation delaunay;

    public ConvexHull() {
		super(Color.GREEN);
		delaunay = new DelaunayTriangulation();
    }

    public ConvexHull(DelaunayTriangulation delau) {
		super(Color.GREEN);
		delaunay = delau;
    }

    public void insertPoint(de.hambuch.voronoiapp.geometry.Point p) throws VoronoiException {
		delaunay.insertPoint(p);
    }
    public void deletePoint(Point p) {
		delaunay.deletePoint(p);
    }

    public int pointInHull(Point p) {
		DelauTriangle tstart = delaunay.getFirstHullTriangle();
		if(tstart != null) {
			DelauTriangle t = tstart;
			do {
				if(Segment.pointTest(t.getPointA(), t.getPointB(), p) != Segment.POINT_LEFT) return 0;
				// kein Test, auf Edge etc.
				t = t.getNeighbourBC();
			} while (t != tstart);
		} else {
			return 0;
		}
		return 1;
    }

	@NonNull
	public SimplePolygon toPolygon() {
		DelauTriangle tstart = delaunay.getFirstHullTriangle();
		SimplePolygon poly = new SimplePolygon();
		poly.setColor(getColor());
		if(tstart != null) {
			DelauTriangle t = tstart;
			do {
				poly.addPoint(t.getPointA());
				t = t.getNeighbourCA();
			} while (t!=tstart);
		}
		return poly;
	}

    public void paint(@NonNull Canvas g) {
		DelauTriangle tstart = delaunay.getFirstHullTriangle();
		if(tstart != null) {
		    DelauTriangle t = tstart;
		    do {
				Segment.drawSegment(g, t.getPointA(), t.getPointB(), getLinePaint());
				t = t.getNeighbourBC();
		    } while(t != tstart);
		}
    }
}
