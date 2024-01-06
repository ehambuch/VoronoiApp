package de.hambuch.voronoiapp.geometry;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A class that provides a double-connected edge list (DCEL).
 * <PRE>
 *       \    /  next2
 *        \  /
 *          *p2
 *          |
 *   left   |    right
 *          *p1
 *         / \
 *   next1/   \
 * </PRE>
 *
 * Every edge is represented by (p1, p2). For rays p1 or p2 may be <VAR>null</VAR>.
 *
 * @version 1.1 (22.12.2000)
 * @author Eric Hambuch
 * @see Segment
 * @see Ray
 */


public class DCEL {

	/* the first and last point of the segment (or one direction point for rays) */
    protected Point point1 = null, point2 = null;
    /* data points right and left from this segment (for areas) */
    protected Point pointright = null, pointleft = null;
    /* next DCEL in counterwise order from point1 and point2. For rays next1 or next2 should be null. */
    protected DCEL next1 = null, next2 = null;

	public boolean visited = false;

    public DCEL(Point p, Point q, Point right, Point left) {
		this.point1 = p;
		this.point2 = q;
		this.pointright = right;
		this.pointleft = left;
    }

   /**
    * returns the data point on the right side of this edge.
    *
    * @return Point
    */
   public Point getPointRight() {
		return pointright;
   }
   /**
    * returns the data point on the left side of this edge.
    *
    * @return Point
    */
   public Point getPointLeft() {
		return pointleft;
   }
   public void setPointRight(Point right) {
		this.pointright = right;
	}
	public void setPointLeft(Point left) {
		this.pointleft = left;
	}

    /**
     * returns the first point
     *
     * @return Point
     */
    public Point getPoint1() {
		return point1;
    }
    public Point getPoint2() {
		return point2;
	}

    public DCEL getNext1() {
		return next1;
    }
    public void setNext1(DCEL next) {
		next1 = next;
    }
    public DCEL getNext2() {
		return next2;
    }
    public void setNext2(DCEL next) {
		next2 = next;
    }

	/**
	 * connect an other DCEL to this one, depending on the common point.
	 * Call <CODE>next.connect(this)</CODE> for reverse connection.
	 *
	 * @param next DCEL to connect with this
	 */
    public void connect(@Nullable DCEL next) {
		if(next != null) {
			if(point2 != null) {
				if(point2.equals(next.getPoint1()) || point2.equals(next.getPoint2())) {
					next2 = next;
					return;
				}
			}
			if(point1 != null) {
				if(point1.equals(next.getPoint1()) || point1.equals(next.getPoint2())) {
					next1 = next;
					return;
				}
			}
		}
		/* no common point: do nothing */
	}

	/**
	 * Creates an Edge (Segment or Ray) from this DCEL. The color is taken from the right point of this DCEL.
	 *
	 * @return Edge an edge representing this DCEL.
	 * @see Edge
	 */
	@NonNull
	public Edge toEdge() {
		Edge edge = null;
		 if(point1 != null && point2 != null) {
			edge = new Segment(point1, point2);
			if(pointright != null)
				((Segment)edge).setColor(pointright.getColor());
		} else if(point2 == null) {
			edge = new Ray(point1, -(pointright.getY()-pointleft.getY()),
				      pointright.getX() - pointleft.getX());
			if(pointright != null)
				((Ray)edge).setColor(pointright.getColor());
		} else if(point1 == null) {
			edge = new Ray(point2, -(pointright.getY()-pointleft.getY()),
							      pointright.getX() - pointleft.getX());
			if(pointright != null)
				((Ray)edge).setColor(pointright.getColor());
		}
		return edge;
	}

	@NonNull
	public String toString() {
		return "DCEL: "+point1+"-"+point2+": F_right="+pointright+"F_left="+pointleft;
	}
}
