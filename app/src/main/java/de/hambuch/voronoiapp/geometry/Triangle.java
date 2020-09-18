package de.hambuch.voronoiapp.geometry;

import android.graphics.Canvas;

/**
 * This class represents a triangle in R<SUP>2</SUP>, definied by three points.
 * The orientation of the points may be chosen free!
 *
 * @author Eric Hambuch (Eric.Hambuch@fernuni-hagen.de)
 * @version 1.0 (30.10.2000)
 */

/* TESTED: 1.0: 30.10.2000 */
public class Triangle extends GeomElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1506807624679043374L;
	
	public static final int OUTOFTRIANGLE = 0;
	public static final int ONTRIANGLE = -1;
	public static final int INTRIANGLE = 1;

	protected Point pointA;
	protected Point pointB;
	protected Point pointC;

	public Triangle(Point pointA, Point pointB, Point pointC) {
		this.pointA = pointA;
		this.pointB = pointB;
		this.pointC = pointC;
	}
	public Triangle(Point pointA, Point pointB, Point pointC, int color) {
		super(color);
		this.pointA = pointA;
		this.pointB = pointB;
		this.pointC = pointC;
	}

	public Point getPointA() {
		return pointA;
	}

	public Point getPointB() {
		return pointB;
	}

	public Point getPointC() {
		return pointC;
	}

	public void setPointA(Point pointA) {
		this.pointA = pointA;
	}

	public void setPointB(Point pointB) {
		this.pointB = pointB;
	}

	public void setPointC(Point pointC) {
		this.pointC = pointC;
	}

	/**
	 * calculates the signed area of a triangle given by three points <VAR>p,q,r</VAR>.
	 * Useful to check whether r lies left (area>0) oder right (area<0) from pq.
	 *
	 * @return double signed area of Triag(p,q,r)
	 */
	public static double signedArea(Point p, Point q, Point r) {
		return ((r.getX() - p.getX()) * (r.getY() + p.getY())+
				(q.getX() - r.getX()) * (q.getY() + r.getY())+
				(p.getX() - q.getX()) * (p.getY() + q.getY())) /2.0;
	}

	/**
	 * checks, if a points lies in (<VAR>INTRIANGLE</VAR>), out of (<VAR>OUTOFTRIANGLE<VAR>) or on the border (<VAR>ONTRIANGLE</VAR>)
	 *
	 * @param Point point to check
	 * @return int one of the values <VAR>INTRIANGLE, OUTOFTRIANGLE, ONTRIANGLE</VAR>
	 */
	public int pointInTriangle(Point point) {
		Point a=pointA, b=pointB, c=pointC;
		if(area() < 0.0) {
			/* we need to ensure the orientation */
			Point swap = a;
			a=b;
			b=swap;
		}
		int a1 = Segment.pointTest(a, b, point);
		int a2 = Segment.pointTest(b, c, point);
		int a3 = Segment.pointTest(c, a, point);
		if(a1 == Segment.POINT_ONEDGE || a2 == Segment.POINT_ONEDGE || a3 == Segment.POINT_ONEDGE)
			return ONTRIANGLE;
		else if(a1 == Segment.POINT_LEFT && a2 == Segment.POINT_LEFT && a3 == Segment.POINT_LEFT)
			return INTRIANGLE;
		else
			return OUTOFTRIANGLE;
	}

	/**
	 * return the signed area of this triangle (depends on orientation).
	 *
	 * @return double Area of triangle
	 */
	public double area() {
		return signedArea(pointA, pointB, pointC);
	}

	public String toString() {
		return pointA+"-"+pointB+"-"+pointC;
	}

	public Object clone() {
		return new Triangle(pointA, pointB, pointC, getColor());
	}

	/**
	 * Ensures that the points are in counterclockwise order.
	 * If not yet, it will correct it by swapping two points.
	 */
	protected void counterclock() {
		if(area() < 0.0) {
			Point swap;
			swap = pointA;
			pointA = pointB;
			pointB = swap;
		}
	}

	public void paint(Canvas graphics) {
		graphics.drawLine((float)pointA.getX(), (float)pointA.getY(), (float)pointB.getX(), (float)pointB.getY(), getLinePaint());
		graphics.drawLine((float)pointB.getX(), (float)pointB.getY(), (float)pointC.getX(), (float)pointC.getY(), getLinePaint());
		graphics.drawLine((float)pointC.getX(), (float)pointC.getY(), (float)pointA.getX(), (float)pointA.getY(), getLinePaint());
	}

}
