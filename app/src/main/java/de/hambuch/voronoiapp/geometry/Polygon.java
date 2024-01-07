package de.hambuch.voronoiapp.geometry;

import android.graphics.Canvas;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Enumeration;
import java.util.Vector;

/**
 * Abstract class for for all polygons.
 * 
 * @version 1.1 (26.12.2000)
 * @author Eric Hambuch (Eric.Hambuch@fernuni-hagen.de)
 */
public abstract class Polygon extends GeomElement {

	@NonNull
	protected final Vector<Point> points;
	protected int fillColor = 0x000000;

	public Polygon() {
		super();
		this.points = new Vector<Point>();
	}

	public Polygon(@NonNull Vector<Point> points) {
		super();
		this.points = points;
	}

	/**
	 * Checks if a point lies in the interior of this polygon.
	 * 
	 * @param p
	 * @return boolean <VAR>true</VAR> if <VAR>p</VAR> lies in the interior.
	 */
	public abstract boolean pointInPolygon(@NonNull Point p);

	/**
	 * add a point to the polygon.
	 * 
	 * @param point
	 */
	public void addPoint(@NonNull Point point) {
		points.addElement(point);
	}

	/**
	 * remove a point from the polygon.
	 * 
	 * @param point
	 */
	public void removePoint(@NonNull Point point) {
		points.removeElement(point);
	}

	public abstract void paint(@NonNull Canvas g);

	/**
	 * return an array of all points.
	 * 
	 * @return Point[] array of points or <VAR>null</VAR>
	 */
	@Nullable
	public Point[] toPoints() {
		int size = points.size();
		if (size == 0)
			return null;
		Point[] parray = new Point[size];
		for (int i = 0; i < size; i++)
			parray[i] = (Point) points.elementAt(i);
		return parray;
	}

	public int size() {
		return points.size();
	}

	@NonNull
	public String toString() {
		StringBuilder str = new StringBuilder("Polygon: ");
		Enumeration<Point> enume = points.elements();
		while (enume.hasMoreElements()) {
			str.append(enume.nextElement().toString());
		}
		return str.toString();
	}

	/**
	 * Clips the polygon to (xmin, ymin)-(xmax, ymax).
	 * 
	 * @param xmin
	 * @param ymin
	 * @param xmax
	 * @param  ymax
	 * @return Polygon the polygon clipped to the given rectangle (may be
	 *         <VAR>null</VAR> if empty!)
	 */
	public Polygon clipTo(float xmin, float ymin, float xmax, float ymax) {
		Vector<Point> points1 = cutBottom(this.points, ymax);
		Vector<Point> points2 = cutTop(points1, ymin);
		points1 = cutLeft(points2, xmin);
		points2 = cutRight(points1, xmax);
		if (points2.size() > 1) {
			SimplePolygon poly = new SimplePolygon(points2);
			poly.setColor(getColor());
			poly.setFillColor(getFillColor());
			return poly;
		}
		return null;
	}

	/*
	 * Clipping routines for every edge. See: W.D. Fellner: Computergrafik,
	 * B.I.Wissenschaftsverlag, 1992
	 */
	@NonNull
	protected Vector<Point> cutBottom(@NonNull Vector<Point> points, float ymax) {
		Vector<Point> newpoints = new Vector<Point>();
		int i = 0;
		while (i < points.size()) {
			Point p = (Point) points.get(i % points.size());
			Point q = (Point) points.get((i + 1) % points.size());
			/* normal point */
			if (p.getY() <= ymax) {
				/* is visible */
				newpoints.addElement(p);
			}
			/* intersection of (p,q) with clipping edge */
			if (p.getY() != q.getY()) {
				float t = (ymax - q.getY()) / (p.getY() - q.getY());
				float sx = q.getX() + t * (p.getX() - q.getX());
				if (t >= 0.0 && t <= 1.0)
					newpoints.addElement(new Point(sx, ymax));
			}
			i++;
		}
		return newpoints;
	}

	@NonNull
	protected Vector<Point> cutTop(@NonNull Vector<Point> points, float ymin) {
		Vector<Point> newpoints = new Vector<Point>();
		int i = 0;
		while (i < points.size()) {
			Point p = (Point) points.get(i % points.size());
			Point q = (Point) points.get((i + 1) % points.size());
			/* normal point */
			if (p.getY() >= ymin) {
				/* is visible */
				newpoints.addElement(p);
			}
			/* intersection of (lastPoint-p) with clipping edge */
			if (p.getY() != q.getY()) {
				float t = (ymin - q.getY()) / (p.getY() - q.getY());
				float sx = q.getX() + t * (p.getX() - q.getX());
				if (t >= 0.0 && t <= 1.0)
					newpoints.addElement(new Point(sx, ymin));
			}
			i++;
		}
		return newpoints;
	}

	protected Vector<Point> cutLeft(Vector<Point> points, float xmin) {
		Vector<Point> newpoints = new Vector<Point>();
		int i = 0;
		while (i < points.size()) {
			Point p = (Point) points.elementAt(i % points.size());
			Point q = (Point) points.elementAt((i + 1) % points.size());
			/* normal point */
			if (p.getX() >= xmin) {
				/* is visible */
				newpoints.addElement(p);
			}
			/* intersection of (p-q) with clipping edge */
			if (p.getX() != q.getX()) {
				float t = (xmin - q.getX()) / (p.getX() - q.getX());
				float sy = q.getY() + t * (p.getY() - q.getY());
				if (t >= 0.0 && t <= 1.0)
					newpoints.addElement(new Point(xmin, sy));
			}
			i++;
		}
		return newpoints;
	}

	protected Vector<Point> cutRight(Vector<Point> points, float xmax) {
		Vector<Point> newpoints = new Vector<Point>();
		int i = 0;
		while (i < points.size()) {
			Point p = (Point) points.elementAt(i % points.size());
			Point q = (Point) points.elementAt((i + 1) % points.size());
			/* normal point */
			if (p.getX() <= xmax) {
				/* is visible */
				newpoints.addElement(p);
			}
			/* intersection of (p-q) with clipping edge */
			if (p.getX() != q.getX()) {
				float t = (xmax - q.getX()) / (p.getX() - q.getX());
				float sy = q.getY() + t * (p.getY() - q.getY());
				if (t >= 0.0 && t <= 1.0)
					newpoints.addElement(new Point(xmax, sy));
			}
			i++;
		}
		return newpoints;
	}
}
