package de.hambuch.voronoiapp.geometry;

import android.graphics.Canvas;

import androidx.annotation.NonNull;


/**
 * Represents a point in R<SUP>2</SUP>.
 *
 * @author Eric Hambuch (Eric.Hambuch@fernuni-hagen.de)
 * @version 1.1 (21.12.2000)
 */

public class Point extends GeomElement implements Cloneable {

   /**
	 * 
	 */
	private static final long serialVersionUID = 2559940843786582356L;

	/**
    * distance to which two points are equal (epsilon)
    */
   public static final double CLOSE = 0.0001;

   /**
    * special point at infinity
    */
   private static final Point inftyPoint = new Point(Float.POSITIVE_INFINITY,
		   Float.POSITIVE_INFINITY);

   protected float x;
   protected float y;

	public Point(float x, float y) {
		this.x = x;
		this.y = y;
	}

   public Point(int x, int y) {
		this.x = x;
		this.y = y;
   }

	public Point(float x, float y, int color) {
		super(color);
		this.x = x;
		this.y = y;
	}

	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	@NonNull
	public String toString() {
		return "("+x+","+y+")";
	}

	@NonNull
	public Object clone() {
		return new Point(x,y,getColor());
	}

   public static Point getInfinityPoint() {
		return inftyPoint;
   }

   public boolean isInfinityPoint() {
		if(x == Float.POSITIVE_INFINITY &&
		   y == Float.POSITIVE_INFINITY) return true;
		return false;
   }

   public boolean equals(Object o) {
		if(o instanceof Point) {
		    if(isInfinityPoint() && ((Point)o).isInfinityPoint()) return true;
		    if(this.distance((Point)o) < CLOSE) return true;
		}
		return false;
   }

	/**
	 * Calculates the euclid distance to a point.
	 *
	 * @param Point toPoint
	 * @return double distance between this point and <VAR>toPoint</VAR>
	 */
	public float distance(Point toPoint) {
		float x2 = toPoint.getX()-x;
		float y2 = toPoint.getY()-y;
		return (float)Math.sqrt(x2*x2+y2*y2);
	}

	/**
	 * Compares two points in lexicographic order
	 *
	 * @param Point secondPoint
	 * @return int -1 if (this &lt; second), 1 if (this &gt; second) and 0 if (this == second)
	 */
	public int compare(Point toPoint) {
		if(x < toPoint.getX() || (x == toPoint.getX() && y < toPoint.getY())) return -1;
		if(x > toPoint.getX() || (x == toPoint.getX() && y > toPoint.getY())) return 1;
		if(x == toPoint.getX() && y == toPoint.getY()) return 0;
		/* should never reach this statement! */
		return 0;
	}

	public void paint(Canvas graphics) {
		graphics.drawCircle((float)x, (float)y, 6, getFillPaint());
	}
}
