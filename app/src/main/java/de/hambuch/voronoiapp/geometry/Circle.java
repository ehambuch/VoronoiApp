package de.hambuch.voronoiapp.geometry;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.annotation.NonNull;

/**
 * A circle in R<SUP>2</SUP>.
 *
 * @author Eric Hambuch
 * @version 1.0 (18.10.2000)
 */
public class Circle extends GeomElement implements Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6813772201429991495L;
	
	public static final int OUTOFCIRCLE = 0;
	public static final int ONCIRCLE = -1;
	public static final int INCIRCLE = 1;

	private Point center;

	private float radius;

	/**
	 * Creates a circle given by a center point and a radius.
	 *
	 * @param Point center
	 * @param double radius
	 */
	public Circle(Point center, float radius) {
		this.center = center;
		this.radius = radius;
	}

	public Circle(Point center, float radius, int color) {
		super(color);
		this.center = center;
		this.radius = radius;
	}

	/**
	 * Creates a circle given by three points.
	 * These points should not be collinear!
	 *
	 * @param Point a, b, c
	 */
	public Circle(Point a, Point b, Point c) {
		float u = ((a.getX()-b.getX())*(a.getX()+b.getX()) + (a.getY()-b.getY())*(a.getY()+b.getY())) / 2.0f;
		float v = ((b.getX()-c.getX())*(b.getX()+c.getX()) + (b.getY()-c.getY())*(b.getY()+c.getY())) / 2.0f;
		float den = (a.getX()-b.getX())*(b.getY()-c.getY()) - (b.getX()-c.getX())*(a.getY()-b.getY());
		 if ( den==0.0f ) { // oops, degenerate case
		    center = a;
		    radius = Float.POSITIVE_INFINITY;
		}
	    else {
	       center =  new Point((u*(b.getY()-c.getY()) - v*(a.getY()-b.getY())) / den,
		                       (v*(a.getX()-b.getX()) - u*(b.getX()-c.getX())) / den);
		   radius = a.distance(center);
		}
	}

	/**
	 * returns the center of this circle
	 *
	 * @return Point center point
	 */
	public Point getCenter() {
		return center;
	}

	/**
	 * sets the center of this circle
	 *
	 * @param Point center
	 */
	public void setCenter(Point center) {
		this.center = center;
	}

	/**
	 * returns the radius
	 *
	 * @return double radius
	 */
	public float getRadius() {
		return radius;
	}

	/**
	 * sets the radius
	 *
	 * @param double radius
	 */
	public void setRadius(float radius) {
		this.radius = radius;
	}

	/**
	 * checks whether a points lies in, on or out of this circle.
	 * Returns <VAR>ONCIRCLE</VAR>, if the points lies on the border of this circle,
	 * <VAR>INCIRCLE</VAR>, if the points lies really in the circle; otherwise <VAR>OUTOFCIRCLE</VAR>.
	 *
	 * @param Point point
	 * @return int points lies in, on our out of circle
	 */
	public int pointInCircle(Point point) {
		float dist = point.distance(center);
		if(dist < radius)
			return INCIRCLE;
		else if(dist == radius)
			return ONCIRCLE;
		else
			return OUTOFCIRCLE;
	}

	@NonNull
	public Object clone() {
		return new Circle(center, radius, getColor());
	}

	@NonNull
	public String toString() {
		return center+",r="+radius;
	}

	public void paint(Canvas graphics) {
		int width = (int)(radius + radius);
		int height = (int)(radius + radius);
		int x = (int)(center.getX() - radius);
		int y = (int)(center.getY() - radius);
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(getColor());
		paint.setStrokeWidth(3.0f);
		graphics.drawOval(new RectF(x, y, x+width, y+height), paint);
	}
}