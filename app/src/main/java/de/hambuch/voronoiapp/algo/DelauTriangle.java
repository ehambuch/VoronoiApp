package de.hambuch.voronoiapp.algo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import de.hambuch.voronoiapp.geometry.Circle;
import de.hambuch.voronoiapp.geometry.DCEL;
import de.hambuch.voronoiapp.geometry.Point;
import de.hambuch.voronoiapp.geometry.Segment;
import de.hambuch.voronoiapp.geometry.Triangle;

/**
 * This class provides a delaunay triangle with references to the three
 * neighbours.
 * 
 * @version 1.0
 * @author Eric Hambuch
 */
class DelauTriangle extends Triangle {

	private boolean halfplane;

	/* all neighbours are connected */
	DelauTriangle neighbourAB;
	DelauTriangle neighbourBC;
	DelauTriangle neighbourCA;

	/* only for VoronoiDiagram.structure() */
	transient DCEL dcelAB;
	transient DCEL dcelBC;
	transient DCEL dcelCA;

	private Circle circumcircle;

	transient public boolean visited = false;

	/**
	 * A Delaunay-Triangle. The points have to be in counterclockwise order!
	 * 
	 * @param Point
	 *            a
	 * @param Point
	 *            b
	 * @param Point
	 *            c
	 */
	public DelauTriangle(Point a, Point b, Point c) {
		super(a, b, c);
		halfplane = false;
		if (area() < 0.0) {
			System.err
					.println("Warning: points are not in counterclockwise order: "
							+ a + "," + b + "," + c);
		}
		circumcircle = new Circle(a, b, c);
	}

	/**
	 * A &quot;degenerated&quot; Delaunay-Triangle (left halfplane of a-b)
	 * 
	 * @param Point
	 *            a
	 * @param point
	 *            b
	 */
	public DelauTriangle(Point a, Point b) {
		super(a, b, a);
		halfplane = true;
		circumcircle = new Circle(a, b, a);
	}

	/**
	 * Tests, if a point lies in a triangle (or halfplane)
	 * 
	 * @param Point
	 *            p
	 * @return <VAR>INTRIANGLE</VAR> if point lies in the triangle (or
	 *         halfplane), otherwise <VAR>OUTOFTRIANGLE</VAR> or
	 *         <VAR>ONTRIANGLE</VAR>
	 * @see Triangle.pointInTriangle
	 */
	public int pointInTriangle(Point p) {
		if (!halfplane) {
			return super.pointInTriangle(p);
		} else {
			int a = Segment.pointTest(pointA, pointB, p);
			if (a == Segment.POINT_RIGHT)
				return OUTOFTRIANGLE;
			if (a == Segment.POINT_LEFT)
				return INTRIANGLE;
		}
		return ONTRIANGLE;
	}

	/**
	 * Tests, if a point lies in the circumcircle of this triangle (or in the
	 * full halfplane)
	 * 
	 * @param Point
	 *            p
	 * @return <VAR>true if point lies in the circumcircle (or halfplane),
	 *         otherwise (point on border or out of) <VAR>false</VAR>
	 */
	public boolean pointInCircumcircle(Point p) {
		if (!halfplane) {
			if (circumcircle.pointInCircle(p) == Circle.INCIRCLE)
				return true;
		} else {
			if (Segment.pointTest(pointA, pointB, p) == Segment.POINT_RIGHT)
				return true;
		}
		return false;
	}

	/**
	 * return the circumcircle of this triangle (only useful for non-halfplane
	 * triangles)
	 * 
	 * @return Circle
	 */
	public Circle circumCircle() {
		return circumcircle;
	}

	/**
	 * returns if this triangle is a halfplane (degenerated triangle) or a real
	 * triangle.
	 * 
	 * @return boolean <VAR>true</VAR> iff this triangle is a halfplane.
	 */
	public boolean isHalfplane() {
		return halfplane;
	}

	public DelauTriangle getNeighbourAB() {
		return neighbourAB;
	}

	public DelauTriangle getNeighbourBC() {
		return neighbourBC;
	}

	public DelauTriangle getNeighbourCA() {
		return neighbourCA;
	}

	/**
	 * Extend a halfplane to a &quot;real&quot; triangle by adding a third
	 * point. This point should not be collinear!
	 * 
	 * @param Point
	 *            point
	 */
	public void extendTriangle(Point point) {
		/*
		 * we have C+
		 * 
		 * 
		 * A+-----+B
		 * 
		 * (C left from AB) so we can easily extend to:
		 * 
		 * C+ / \ / \ A+-----+B
		 * 
		 * and have ccw order
		 */
		pointC = point;
		halfplane = false;
		if (area() < 0.0) {
			System.err
					.println("Warning: extend points are not in counterclockwise order: "
							+ pointA + "," + pointB + "," + pointC);
		}
		circumcircle = new Circle(pointA, pointB, pointC);
	}

	public void replaceNeighbour(DelauTriangle old, DelauTriangle neu) {
		if (neighbourAB == old)
			neighbourAB = neu;
		else if (neighbourBC == old)
			neighbourBC = neu;
		else if (neighbourCA == old)
			neighbourCA = neu;
		else
			System.err.println("Error: " + this.toString()
					+ ".replaceNeighbour " + old + " with " + neu);
	}

	/**
	 * return the neighbour in counterclockwise order that has endpoint p
	 * 
	 * @param Point
	 *            p
	 * @return DelauTriangle triangle in ccw order
	 */
	public DelauTriangle neighbour(Point p) {
		if (pointA == p)
			return neighbourCA; /* check A, B first (for halfplanes!!) */
		if (pointB == p)
			return neighbourAB;
		if (pointC == p)
			return neighbourBC;
		System.err.println("Error in neighbour(" + p + ")");
		return null;
	}

	public void setPointA(Point a) {
		super.setPointA(a);
		circumcircle = new Circle(pointA, pointB, pointC);
	}

	public void setPointB(Point b) {
		super.setPointB(b);
		circumcircle = new Circle(pointA, pointB, pointC);
	}

	public void setPointC(Point c) {
		super.setPointC(c);
		circumcircle = new Circle(pointA, pointB, pointC);
	}

	public String toString() {
		if (halfplane)
			return "H" + pointA + "-" + pointB;
		return super.toString();
	}

	public void paint(Canvas graphics) {
		if (halfplane) {
			Paint paint = new Paint();
			paint.setColor(Color.RED);
			graphics.drawLine((float) pointA.getX(), (float) pointA.getY(),
					(float) pointB.getX(), (float) pointB.getY(), paint);
		} else
			super.paint(graphics);
	}
}
