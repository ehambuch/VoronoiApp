package de.hambuch.voronoiapp.geometry;


/**
 * Edge can either be a Segment, a Ray or a Line.
 *
 * @see Line
 * @see Ray
 * @see Segment
 */
public interface Edge {

    public static final int POINT_LEFT   = 0;
    public static final int POINT_RIGHT  = 1;
    public static final int POINT_ONEDGE = 2;
    public static final int POINT_BEFORE = 3;
    public static final int POINT_BEHIND = 4;
    public static final int POINT_ERROR = -1;

	/**
	 * Epsilon for parallel lines (needed by <CODE>intersect()</CODE>).
	 */
	public static final float PARALLEL = 0.00001f;

    /**
     * Tests whether a points lies left, right or on this edge.
     * Segments and Rays may also return other values.
     *
     * @param Point point
     * @return int a value of <VAR>POINT_LEFT,POINT_RIGHT,POINT_ONEDGE</VAR> or other
     * @see Segment
     */
    abstract public int pointTest(Point point);

    /**
     * Test on intersection of two edges.
     *
     * @param Edge other edge to test with
     * @return Point a point of this intersection or <VAR>null</VAR>
     * @see Ray
     * @see Line
     * @see Segment
     */
    abstract public Point intersect(Edge edge);

    /**
     * Return the gradient angle of this edge.
     *
     * @return double gradient in [0, 2pi[
     */
    abstract public float gradient();

    /**
     * Clip an edge to a visible area (xmin,ymin)-(xmax,ymax)
     *
     * @param double xmin, ymin, xmax, ymax the area the edge should be clipped to
     * @return Segment a segment in the clipping area or <VAR>null</VAR> if the edge needn't be displayed
     */
    abstract public Segment clipTo(float xmin, float ymin, float xmax, float ymax);
}
