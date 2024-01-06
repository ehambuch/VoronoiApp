package de.hambuch.voronoiapp.geometry;


/**
 * A class that provides a quad-edge data structure (QEDS).
 * <PRE>
 *   next2left     \      /   next2right
 *                  \ *p2/
 *                    |
 *           *left    |     * right
 *                    *p1
 *                  /   \
 *   next1right    /     \   next1left  <--- !!!!!
 * </PRE>
 *
 * @version 1.0 (20.1.2001)
 * @author Eric Hambuch
 */
public class QEDS extends DCEL {

    /* next QEDS in counterwise and clockwise order from point1 and point2. For rays next2left AND right should be null. */
    private QEDS next1left = null; /* next1right is next1 from DCEL */
    private QEDS next2left = null; /* next2right is next2 from DCEL */

    public QEDS(Point p, Point q, Point right, Point left) {
		super(p, q, right, left);
    }

    public QEDS getNext1Left() {
		return next1left;
    }
    public void setNext1Left(QEDS next) {
		next1left = next;
    }
    public QEDS getNext2Left() {
		return next2left;
    }
    public void setNext2Left(QEDS next) {
		next2left = next;
    }
    public QEDS getNext1Right() {
		return (QEDS)next1;
	}
  	public void setNext1Right(QEDS next) {
		next1 = next;
	}
	public QEDS getNext2Right() {
		return (QEDS)next2;
	}
	public void setNext2Right(QEDS next) {
		next2 = next;
    }

   	/**
	 * connect an other QEDS to this one on the left side, depending on the common point.
	 * Call <CODE>next.connect(this)</CODE> for reverse connection.
	 *
	 * @param next QEDS to connect with this left hand
	 */
    public void connectLeft(QEDS next) {
		if(next != null) {
			if(point2 != null) {
				if(point2.equals(next.getPoint1()) || point2.equals(next.getPoint2())) {
					next2left = next;
					return;
				}
			}
			if(point1 != null) {
				if(point1.equals(next.getPoint1()) || point1.equals(next.getPoint2())) {
					next1left = next;
					return;
				}
			}
		}
		/* no common point: do nothing */
	}
	/**
	 * connect to the right hand side of this QEDS.
	 *
	 * @param next QEDS to connect with right hand
	 */
	public void connectRight(QEDS next) {
		connect(next); /* use DCEL.connect */
	}
}
