package de.hambuch.voronoiapp.geometry;

import androidx.annotation.NonNull;

/**
 * A twin-edge data structure for our FCVD edges.
 * For rays one of the pointers <CODE>prevEdge</CODE> or <CODE>nextEdge</CODE>  * is <VAR>null</VAR>.
 *
 *
 * @author Eric Hambuch
 * @version 1.0 (30.1.2001)
 * @see FarthestColorVD
 */
public class TEDS {

    /* reference to twin edge */
    private TEDS twin;

    /* points for segment */
    private Point point1, point2;

    /* prev/next TEDS (cyclic list) */
    private TEDS prevEdge, nextEdge;

    /* belogs to voronoi site */
    private Point voronoiPoint;

	/**
	 * can be used by application
	 */
	public boolean visited = false;

    public TEDS() {
    }
    public TEDS(@NonNull Point point1, @NonNull Point point2, @NonNull Point site) {
		this.point1 = point1;
		this.point2 = point2;
		this.voronoiPoint = site;
	}

    public TEDS getTwin() {
	return twin;
    }
    public void setTwin(TEDS twin) {
	this.twin = twin;
    }

    public Point getPoint1() {
	return point1;
    }
    public void setPoint1(Point p) {
	point1 = p;
    }
    public Point getPoint2() {
	return point2;
    }
    public void setPoint2(Point p) {
	point2 = p;
    }

    public TEDS getPrevEdge() {
	return prevEdge;
    }

    public void setPrevEdge(TEDS prev) {
	this.prevEdge = prev;
    }
    public TEDS getNextEdge() {
	return nextEdge;
    }
    public void setNextEdge(TEDS next) {
	this.nextEdge = next;
    }

    public void setVoronoiPoint(Point p) {
		this.voronoiPoint = p;
	}
	public Point getVoronoiPoint() {
		return voronoiPoint;
	}

    /**
     * Creates an edge (segment or ray) from this TEDS.
     *
     * @return Edge a Segment or a Ray
     * @see Segment
     * @see Ray
     */
    public Edge toEdge() {
	if(nextEdge == null) {
	    return new Ray(point1, point2);
	} else if(prevEdge == null) {
	    return new Ray(point2, point1);
	} else
	    return new Segment(point1, point2);
    }
}
