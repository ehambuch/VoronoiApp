package de.hambuch.voronoiapp.algo;

import android.graphics.Canvas;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Iterator;
import java.util.List;

import de.hambuch.voronoiapp.geometry.GeomElement;
import de.hambuch.voronoiapp.geometry.Point;
import de.hambuch.voronoiapp.geometry.QEDS;
import de.hambuch.voronoiapp.geometry.Ray;
import de.hambuch.voronoiapp.geometry.Region;
import de.hambuch.voronoiapp.geometry.Segment;

/**
 * A voronoi diagram in R<SUP>2</SUP>. This is a robost implementation (use
 * integer coordinates for input sites) and handles all degenerated cases (so
 * far centers of cocircular sites are treated as different points). This
 * implementation is based on an incremental construction of the
 * Delaunay-Triangulation.
 * 
 * @see DelaunayTriangulation
 * 
 * @version 1.4 (29.2.2001)
 * @author Eric Hambuch (Eric.Hambuch@fernuni-hagen.de)
 */

public class VoronoiDiagram extends GeomElement {

	/* the construction is based on a delaunay triangulation */
	@NonNull
	protected final DelaunayTriangulation delaunay;

	public VoronoiDiagram() {
		super(Color.BLUE);
		delaunay = new DelaunayTriangulation();
	}

	public VoronoiDiagram(@NonNull DelaunayTriangulation delaunay) {
		super(Color.BLUE);
		this.delaunay = delaunay;
	}

	@NonNull
	public DelaunayTriangulation getDelaunay() {
		return this.delaunay;
	}

	/**
	 * insert a new site to this voronoi diagram. Make sure, that the diagrams
	 * doensn't alreays contain the new site.
	 * 
	 * @param point the new site
	 * @throws VoronoiException
	 */
	public void insertPoint(@NonNull Point point) throws VoronoiException {
		delaunay.insertPoint(point);
	}

	/**
	 * delete a site from the voronoi diagram.
	 * 
	 * @param point site to delete
	 */
	public void deletePoint(@NonNull Point point) {
		delaunay.deletePoint(point);
	}

	/**
	 * move a site in this voronoi diagram to a new position (newx, newy).
	 * Please make sure, that no two sites share the same position!
	 * 
	 * @param point the site to move
	 * @param newx new x coordinate
	 * @param newy new y coordinate the site is moved to
	 */
	public void movePoint(@NonNull Point point, float newx, float newy) {
		delaunay.movePoint(point, newx, newy);
	}

	/**
	 * returns the sites of this diagram.
	 * 
	 * @return Enumeration an enumeration of Points
	 */
	@NonNull
	public Iterator<Point> points() {
		return delaunay.points();
	}

	/**
	 * return the number of sites.
	 * 
	 * @return int number of sites
	 */
	public int size() {
		return delaunay.size();
	}

	/**
	 * This method returns the region of a given site <VAR>p</VAR>. If there is
	 * only one point this will return a region without any bounds (the full
	 * R<SUP>2</SUP>).
	 * 
	 * @param p a site of this voronoi diagram.
	 * @return Region the region of p or <VAR>null</VAR> in case of error.
	 * @see Region
	 */

	@Nullable
	public Region toRegion(@NonNull Point p) {
		Region region = new Region(p);
		if (delaunay.size() == 0) {
			return null;
		} else if (delaunay.size() == 1) {
			return region; /* whole R^2 */
		} else {
			if (delaunay.areCollinear()) {
				DelauTriangle t = delaunay.getFirstHullTriangle();
				DelauTriangle start = t;
				while (!t.getPointA().equals(p)) {
					t = t.neighbourBC;
					if (t == start)
						return null; /* Error: p not one of the collinear points */
				}
				Point[] points = toHalfplaneRegion(t, p);
				for (int i = 0; i < 5; i++)
					region.addPoint(points[i]);
				if (delaunay.size() > 2 && (t.neighbour(p) != t.neighbourAB)) {
					t = t.neighbour(p).neighbourAB;
					points = toHalfplaneRegion(t, p);
					for (int i = 1; i < 4; i++)
						region.addPoint(points[i]);
				}
			} else {
				DelauTriangle t = delaunay.find(delaunay.getFirstTriangle(), p);
				if (t != null) {
					if (t.isHalfplane())
						t = t.neighbourAB; // start with an "real" triangle.;
					DelauTriangle tstart = t;
					Point q, lastPoint = null;
					do {
						if (t.isHalfplane()) {
							Point[] points = toHalfplaneRegion(t, p);
							t = t.neighbour(p);
							/*
							 * we need only the direction points for both rays.
							 * But toHalfplaneRegion() creates direction points
							 * relative to the halfplane tringle t and not our
							 * last point. So these points may point to the
							 * wrong direction, if the circumcenter lies outside
							 * of a triangle.
							 */
							float x = points[0].getX(), y = points[0].getY();
							region.addPoint(new Point(lastPoint.getX() - x
									+ points[1].getX(), lastPoint.getY() - y
									+ points[1].getY()));
							region.addPoint(points[2]);
							/*
							 * get the center of our next triangle (important
							 * for correct direction points)
							 */
							x = points[4].getX();
							y = points[4].getY();
							lastPoint = t.neighbour(p).circumCircle()
									.getCenter();
							region.addPoint(new Point(lastPoint.getX() - x
									+ points[3].getX(), lastPoint.getY() - y
									+ points[3].getY()));
							region.addPoint(lastPoint);
						} else {
							q = t.circumCircle().getCenter();
							if (lastPoint == null) { /*
													 * don't add cocircular
													 * points twice
													 */
								region.addPoint(q);
							} else {
								if (!q.equals(lastPoint))
									region.addPoint(q);
							}
							lastPoint = q;
						} // isHalfplane
						t = t.neighbour(p);
					} while (tstart != t);
				} // t!=null
			} // areCollinear
		} // if size
		return region;
	}

	/**
	 * Inserts a new site temporarely and returns the new created region of this
	 * site. Make sure, that the site is not already in the diagram!
	 * 
	 * @param  p the new site
	 * @return Region the new region of p or <VAR>null</VAR> in case of error
	 * @see #toRegion
	 */
	@NonNull
	public Region toRegionNewPoint(@NonNull Point p) {
		try {
			insertPoint(p);
			Region region = toRegion(p);
			deletePoint(p);
			return region;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * returns five points that describes the both rays bounding an open region.
	 * Make sure that <VAR>p</VAR> is point A of <VAR>t</VAR> !
	 * 
	 * @param t a halfplane
	 * @param p one of the point on t
	 * @return Point[5] five points describing the open region
	 */

	private Point[] toHalfplaneRegion(DelauTriangle t, Point p) {
		float x = (t.getPointA().getX() + t.getPointB().getX()) / 2.0f;
		float y = (t.getPointA().getY() + t.getPointB().getY()) / 2.0f;
		Point[] points = new Point[5];
		points[0] = new Point(x, y);
		points[1] = new Point(x - 2.0f
				* (t.getPointB().getY() - t.getPointA().getY()), y + 2.0f
				* (t.getPointB().getX() - t.getPointA().getX())); // direction
																	// point
		points[2] = Point.getInfinityPoint();
		t = t.neighbour(p); // .neighbourCA; // ccw order !!
		x = (t.getPointA().getX() + t.getPointB().getX()) / 2.0f;
		y = (t.getPointA().getY() + t.getPointB().getY()) / 2.0f;
		points[3] = new Point(x - 2.0f
				* (t.getPointB().getY() - t.getPointA().getY()), y + 2.0f
				* (t.getPointB().getX() - t.getPointA().getX())); // direction
																	// point
		points[4] = new Point(x, y);
		return points;
	}

	/**
	 * locate the voronoi site that is nearest to the given point p
	 * 
	 * @param p a query point
	 * @return Point a voronoi point nearest to p (may be <VAR>null</VAR> if
	 *         voronoi diagram empty)
	 */
	@Nullable
	public Point pointLocation(@NonNull Point p) {
		if (size() == 1) {
			return (Point) delaunay.points().next();
		} else if (size() >= 2) {
			DelauTriangle t = delaunay.find(delaunay.getFirstTriangle(), p);
			if (t != null) {
				/*
				 * the nearest point must be one vertice of the triangle
				 * containing p or one of the neighbour triangles (iff center of
				 * circimcircle is not in triangle)
				 */
				Point n1 = nearest(t, p);
				Point n2 = nearest(t.neighbourAB, p);
				Point n3 = nearest(t.neighbourBC, p);
				Point n4 = nearest(t.neighbourCA, p);
				double d1 = p.distance(n1);
				double d2 = p.distance(n2);
				double d3 = p.distance(n3);
				double d4 = p.distance(n4); // could (of course) be optimized!
				if (d1 < d2 && d1 <= d3 && d1 <= d4)
					return n1;
				else if (d2 < d1 && d2 <= d3 && d2 <= d4)
					return n2;
				else if (d3 < d1 && d3 <= d2 && d3 <= d4)
					return n3;
				else if (d4 < d1 && d4 <= d2 && d4 <= d3)
					return n4;
				else
					return n1;
			}
			return null;
		} else { /* size = 0 */
			return null;
		}
	}

	private Point nearest(DelauTriangle t, Point p) {
		double dA = p.distance(t.getPointA());
		double dB = p.distance(t.getPointB());
		double dC = p.distance(t.getPointC());
		if (dA < dB && dA <= dC)
			return t.getPointA();
		else if (dB < dA && dB <= dC)
			return t.getPointB();
		else if (dC < dA && dC <= dB)
			return t.getPointC();
		else
			return t.getPointA();
	}

	/**
	 * return the nearest neighbour of a voronoi site p
	 * 
	 * @param p a voronoi point
	 * @return Point the nearest neighbour (a site of the neighbour cells)
	 */
	@Nullable
	public Point nearestNeighbour(@NonNull Point p) {
		if (size() <= 1)
			return null;
		double mindist = Double.POSITIVE_INFINITY;
		Point minpoint = null;
		DelauTriangle t = delaunay.find(delaunay.getFirstTriangle(), p);
		if (t != null) {
			if (t.getPointA() != p && t.getPointB() != p && t.getPointC() != p)
				return null; /* point p is not a site of our triangulation ! */
			DelauTriangle tstart = t;
			do {
				if (t.getPointA() != p) {
					double d = p.distance(t.getPointA());
					if (d < mindist) {
						mindist = d;
						minpoint = t.getPointA();
					}
				}
				if (t.getPointB() != p) {
					double d = p.distance(t.getPointB());
					if (d < mindist) {
						mindist = d;
						minpoint = t.getPointB();
					}
				}
				if (t.getPointC() != p) {
					double d = p.distance(t.getPointC());
					if (d < mindist) {
						mindist = d;
						minpoint = t.getPointC();
					}
				}
				t = t.neighbour(p);
			} while (t != tstart);
		}
		// austesten
		return minpoint;
	}

	/**
	 * force a rebuilding of the whole data structure.
	 */
	public void rebuild() {
		delaunay.rebuild();
	}

	/**
	 * returns the data structure in a QEDS/DCEL (quad-edge data structure,
	 * double-connected edge list). If all points are collinear (check
	 * areCollinear()) this will return <VAR>null</VAR>.
	 * 
	 * @return QEDS the voronoi diagram embedded in a QEDS oder <VAR>null</VAR>.
	 * @see QEDS
	 * @see de.hambuch.voronoiapp.geometry.DCEL
	 */
	@Nullable
	public QEDS structure() {
		DelauTriangle t = delaunay.getFirstTriangle();
		if (!delaunay.areCollinear()) {
			delaunay.resetVisited(t);
			getstruct(t);
			return (QEDS) t.dcelAB;
		}
		return null;
	}

	private void getstruct(DelauTriangle t) {
		if (t != null) {
			if (!t.visited) {
				if (!t.isHalfplane()) {
					/* only visit real triangles (important for DCEL-rays !!) ! */
					t.visited = true;
					DelauTriangle t1 = t.getNeighbourAB();
					DelauTriangle t2 = t.getNeighbourBC();
					DelauTriangle t3 = t.getNeighbourCA();

					Point c0 = t.circumCircle().getCenter();
					Point c1;

					if (!t1.visited) {
						/* create a new DCEL towards t1 */
						if (!t1.isHalfplane()) {
							c1 = t1.circumCircle().getCenter();
							t.dcelAB = new QEDS(c0, c1, t.getPointA(), t
									.getPointB());
						} else {
							t.dcelAB = new QEDS(c0, null, t.getPointA(), t
									.getPointB());
						}
					} else {
						/* copy DCEL from t1 */
						if (t1.neighbourAB == t)
							t.dcelAB = t1.dcelAB;
						else if (t1.neighbourBC == t)
							t.dcelAB = t1.dcelBC;
						else if (t1.neighbourCA == t)
							t.dcelAB = t1.dcelCA;
						/* else ERROR */
					}

					if (!t2.visited) {
						/* create a new DCEL towards t2 */
						if (!t2.isHalfplane()) {
							c1 = t2.circumCircle().getCenter();
							t.dcelBC = new QEDS(c0, c1, t.getPointB(), t
									.getPointC());
						} else {
							t.dcelBC = new QEDS(c0, null, t.getPointB(), t
									.getPointC());
						}
					} else {
						/* copy DCEL from t2 */
						if (t2.neighbourAB == t)
							t.dcelBC = t2.dcelAB;
						else if (t2.neighbourBC == t)
							t.dcelBC = t2.dcelBC;
						else if (t2.neighbourCA == t)
							t.dcelBC = t2.dcelCA;
						/* else ERROR */
					}

					if (!t3.visited) {
						/* create a new DCEL towards t3 */
						if (!t3.isHalfplane()) {
							c1 = t3.circumCircle().getCenter();
							t.dcelCA = new QEDS(c0, c1, t.getPointC(), t
									.getPointA());
						} else {
							t.dcelCA = new QEDS(c0, null, t.getPointC(), t
									.getPointA());
						}
					} else {
						/* copy DCEL from t3 */
						if (t3.neighbourAB == t)
							t.dcelCA = t3.dcelAB;
						else if (t3.neighbourBC == t)
							t.dcelCA = t3.dcelBC;
						else if (t3.neighbourCA == t)
							t.dcelCA = t3.dcelCA;
						/* else ERROR */
					}

					/* connect all QEDS/DCEL in cw/ccw order */
					((QEDS) t.dcelAB).connectRight((QEDS) t.dcelCA);
					((QEDS) t.dcelAB).connectLeft((QEDS) t.dcelBC);
					((QEDS) t.dcelBC).connectRight((QEDS) t.dcelAB);
					((QEDS) t.dcelBC).connectLeft((QEDS) t.dcelCA);
					((QEDS) t.dcelCA).connectRight((QEDS) t.dcelBC);
					((QEDS) t.dcelCA).connectLeft((QEDS) t.dcelAB);

					if (!t1.visited)
						getstruct(t1);
					if (!t2.visited)
						getstruct(t2);
					if (!t3.visited)
						getstruct(t3);
				} // !t.Halfplane
			} // !t.visited
		} // t != null
	}

	public void paint(@NonNull Canvas g) {
		DelauTriangle t = delaunay.getFirstTriangle();
		if (delaunay.areCollinear()) {
			t = delaunay.getFirstHullTriangle();
			DelauTriangle startt = t;
			if (t != null) {
				float x1, y1, x2, y2;
				Point p = new Point(0.0f, 0.0f);
				Ray ray = new Ray(p, 1.0f, 1.0f);
				ray.setColor(getColor());
				do {
					t = t.getNeighbourBC();
					x1 = t.getPointA().getX();
					y1 = t.getPointA().getY();
					x2 = t.getPointB().getX();
					y2 = t.getPointB().getY();
					p.setX((x1 + x2) / 2.0f);
					p.setY((y1 + y2) / 2.0f);
					ray.setStartpoint(p);
					ray.setDirection(-(y2 - y1), (x2 - x1));
					ray.paint(g);
				} while (startt != t);
			} // t!=null
		} // areCollinear
		else {
			delaunay.resetVisited(t);
			// g.setColor(color);
			paintit(g, t);
		}
	}

	private void paintit(Canvas g, DelauTriangle t) {
		if (t != null) {
			if (!t.visited) {
				t.visited = true;
				DelauTriangle t1 = t.neighbourAB;
				DelauTriangle t2 = t.neighbourBC;
				DelauTriangle t3 = t.neighbourCA;
				if (!t.isHalfplane()) {
					Point c0 = t.circumCircle().getCenter();
					Point c1;
					if (!t1.visited) {
						if (!t1.isHalfplane()) {
							c1 = t1.circumCircle().getCenter();
							Segment.drawSegment(g, c0, c1, getLinePaint());
						} else {
							Ray ray = new Ray(c0, -(t1.getPointB().getY() - t1
									.getPointA().getY()), t1.getPointB().getX()
									- t1.getPointA().getX());
							ray.setColor(getColor());
							ray.paint(g);
						}
					}
					if (!t2.visited) {
						if (!t2.isHalfplane()) {
							c1 = t2.circumCircle().getCenter();
							Segment.drawSegment(g, c0, c1, getLinePaint());
						} else {
							Ray ray = new Ray(c0, -(t2.getPointB().getY() - t2
									.getPointA().getY()), t2.getPointB().getX()
									- t2.getPointA().getX());
							ray.setColor(getColor());
							ray.paint(g);
						}
					}
					if (!t3.visited) {
						if (!t3.isHalfplane()) {
							c1 = t3.circumCircle().getCenter();
							Segment.drawSegment(g, c0, c1, getLinePaint());
						} else {
							Ray ray = new Ray(c0, -(t3.getPointB().getY() - t3
									.getPointA().getY()), t3.getPointB().getX()
									- t3.getPointA().getX());
							ray.setColor(getColor());
							ray.paint(g);
						}
					}
					/* Rekursion */
					if (!t1.visited) paintit(g, t1);
					if (!t2.visited) paintit(g, t2);
					if (!t3.visited) paintit(g, t3);
				} else {
					/* t is a Halfplane */
					if (!t1.visited) {
						if (!t1.isHalfplane()) {
							/* we have to visit only this neighbour */
							Point c0 = t1.circumCircle().getCenter();
							Ray ray = new Ray(c0, -(t.getPointB().getY() - t
									.getPointA().getY()), t.getPointB().getX()
									- t.getPointA().getX());
							ray.setColor(getColor());
							ray.paint(g);
						}
					}
				}

			}
		}
	}

	/**
	 * Converts the VoronoiDiagram into a list of geometric elements.
	 *
	 * @param toElements List of elements drawing the diagram.
	 */
	public void exportToElements(@NonNull List<GeomElement> toElements) {
		final DelauTriangle t = delaunay.getFirstTriangle();
		if (!delaunay.areCollinear()) {
			delaunay.resetVisited(t);
			exportit(t, toElements);
		} // TODO: export for collinear
	}

	private void exportit(@Nullable DelauTriangle t, @NonNull List<GeomElement> toElements) {
		if (t != null) {
			if (!t.visited) {
				t.visited = true;
				DelauTriangle t1 = t.neighbourAB;
				DelauTriangle t2 = t.neighbourBC;
				DelauTriangle t3 = t.neighbourCA;
				if (!t.isHalfplane()) {
					Point c0 = t.circumCircle().getCenter();
					Point c1;
					if (!t1.visited) {
						if (!t1.isHalfplane()) {
							c1 = t1.circumCircle().getCenter();
							toElements.add(new Segment(c0, c1, getLinePaint().getColor()));
						} else {
							Ray ray = new Ray(c0, -(t1.getPointB().getY() - t1
									.getPointA().getY()), t1.getPointB().getX()
									- t1.getPointA().getX());
							ray.setColor(getColor());
							toElements.add(ray);
						}
					}
					if (!t2.visited) {
						if (!t2.isHalfplane()) {
							c1 = t2.circumCircle().getCenter();
							toElements.add(new Segment(c0, c1, getLinePaint().getColor()));
						} else {
							Ray ray = new Ray(c0, -(t2.getPointB().getY() - t2
									.getPointA().getY()), t2.getPointB().getX()
									- t2.getPointA().getX());
							ray.setColor(getColor());
							toElements.add(ray);
						}
					}
					if (!t3.visited) {
						if (!t3.isHalfplane()) {
							c1 = t3.circumCircle().getCenter();
							toElements.add(new Segment(c0, c1, getLinePaint().getColor()));
						} else {
							Ray ray = new Ray(c0, -(t3.getPointB().getY() - t3
									.getPointA().getY()), t3.getPointB().getX()
									- t3.getPointA().getX());
							ray.setColor(getColor());
							toElements.add(ray);
						}
					}
					/* Rekursion */
					if (!t1.visited) exportit(t1, toElements);
					if (!t2.visited) exportit(t2, toElements);
					if (!t3.visited) exportit(t3, toElements);
				} else {
					/* t is a Halfplane */
					if (!t1.visited) {
						if (!t1.isHalfplane()) {
							/* we have to visit only this neighbour */
							Point c0 = t1.circumCircle().getCenter();
							Ray ray = new Ray(c0, -(t.getPointB().getY() - t
									.getPointA().getY()), t.getPointB().getX()
									- t.getPointA().getX());
							ray.setColor(getColor());
							toElements.add(ray);
						}
					}
				}
			}
		}
	}
}
