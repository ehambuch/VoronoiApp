package de.hambuch.voronoiapp.algo;

import android.graphics.Canvas;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.hambuch.voronoiapp.VoronoiApp;
import de.hambuch.voronoiapp.geometry.GeomElement;
import de.hambuch.voronoiapp.geometry.Point;
import de.hambuch.voronoiapp.geometry.Segment;
import de.hambuch.voronoiapp.geometry.Triangle;

/**
 * A robust implementation of the Delaunay-Triangulation. For details see
 * &quot;A Java Applet form the Dynamic Visualization of Voronoi Diagrams&quot;,
 * FernUniversit&auml;t Hagen, 1996. For robustness you should only use integer
 * coordinates.
 * 
 * @version 1.2 (20.1.2001)
 * @author Eric Hambuch (Eric.Hambuch@fernuni-hagen.de)
 */
public class DelaunayTriangulation extends GeomElement {

	public interface Visitor {
		public void visit(@NonNull DelauTriangle triangle);
	}
	
	/* reference to first triangle of triangulation */
	private DelauTriangle firstTriangle;

	/* reference to first "triangle" (halfplane) of convex hull */
	private DelauTriangle firstHullTriangle;

	private int points = 0;

	/* only for collinear points */
	private boolean allCollinear = true;
	private Point firstPoint, lastPoint;
	private DelauTriangle firstColTriag, lastColTriag;

	/* all points in this triangulation */
	private List<Point> allPoints;

	public DelaunayTriangulation() {
		allPoints = new ArrayList<>();
	}

	public void clear() {
		allPoints.clear();
		points = 0;
		firstPoint = null;
		lastPoint = null;
		firstHullTriangle = null;
		firstColTriag = null;
		lastColTriag = null;
		firstTriangle = null;
		allCollinear = true;
	}

	@Nullable
	public Point findPoint(float x, float y, float d) {
		Point thisP = new Point(x, y);
		DelauTriangle t = find(firstTriangle, thisP);
		if (t == null)
			return null;
		// todo check sorting : who is nearest?
		if (t.getPointA().distance(thisP) < d)
			return t.getPointA();
		if (t.getPointB().distance(thisP) < d)
			return t.getPointB();
		if (t.getPointC().distance(thisP) < d)
			return t.getPointC();
		return null;
	}

	/**
	 * Insert a new point into the triangulation. Please make sure, that p isn't
	 * already inserted!
	 * 
	 * @param p
	 * @throws VoronoiException
	 */
	public void insertPoint(@NonNull Point p) throws VoronoiException {
		if (allPoints.contains(p))
			throw new VoronoiException("Point " + p
					+ " already in triangulation");
		points++;
		allPoints.add(p);

		if (points == 1) {
			firstPoint = p;
		} else if (points == 2) {
			/* start triangulation: we have firstPoint +---+ p */
			lastPoint = p;
			if (firstPoint.compare(lastPoint) > 0) {
				lastPoint = firstPoint;
				firstPoint = p;
			}
			/*
			 * create the two halfplanes H(a,p) and H(p,a) so we get:
			 * 
			 * firstPoint (A) +---->+ lastPoint (B) firstPoint (B) +<----+
			 * lastPoint (A)
			 */
			firstColTriag = new DelauTriangle(firstPoint, lastPoint);
			lastColTriag = firstColTriag;
			DelauTriangle t = new DelauTriangle(lastPoint, firstPoint);
			firstColTriag.neighbourBC = t;
			firstColTriag.neighbourCA = t;
			firstColTriag.neighbourAB = t;
			t.neighbourAB = firstColTriag;

			t.neighbourBC = firstColTriag;
			t.neighbourCA = firstColTriag;
			firstTriangle = firstColTriag;
			firstHullTriangle = firstColTriag;
		} else { /* >= 3 points */
			if (!allCollinear) {
				/* there are "real" triangles */
				DelauTriangle triag = find(firstTriangle, p);
				if (triag.isHalfplane()) {
					firstTriangle = extendHull(triag, p);
				} else {
					firstTriangle = extendTriag(triag, p);
				}
			} else {
				/*
				 * points are still collinear, sort them in lexicographic order
				 */
				int side = Segment.pointTest(firstPoint, lastPoint, p);
				DelauTriangle t, tp;
				switch (side) {
				/* new point isn't collinear */
				case Segment.POINT_RIGHT:
					firstTriangle = extendHull(firstColTriag.neighbourAB, p);
					allCollinear = false;
					break;
				case Segment.POINT_LEFT:
					firstTriangle = extendHull(firstColTriag, p);
					allCollinear = false;
					break;
				/*
				 * points are still collinear -> insert p in lexicographic order
				 */
				case Segment.POINT_ONEDGE:
					DelauTriangle u = firstColTriag;
					while (p.compare(u.getPointA()) > 0)
						u = u.getNeighbourBC();
					u = u.getNeighbourCA(); // we went to far!
					t = new DelauTriangle(p, u.getPointB());
					tp = new DelauTriangle(u.getPointB(), p);
					u.setPointB(p);
					u.neighbourAB.setPointA(p);
					t.neighbourAB = tp;
					tp.neighbourAB = t;
					/*
					 * now we have u p t +---->+-----> +<----+<----+ tp
					 */
					if (u == lastColTriag) {
						t.neighbourBC = tp;
						tp.neighbourCA = t;
						lastColTriag = t;
					} else {
						t.neighbourBC = u.neighbourBC;
						u.neighbourBC.neighbourCA = t;
						tp.neighbourCA = u.neighbourAB.neighbourCA;
						u.neighbourAB.neighbourCA.neighbourBC = tp;
					}
					t.neighbourCA = u;
					u.neighbourBC = t;
					tp.neighbourBC = u.neighbourAB;
					u.neighbourAB.neighbourCA = tp;
					break;
				case Segment.POINT_BEFORE:
					t = new DelauTriangle(p, firstPoint);
					tp = new DelauTriangle(firstPoint, p);
					t.neighbourAB = tp;
					tp.neighbourAB = t;
					t.neighbourCA = tp;
					tp.neighbourBC = t;
					t.neighbourBC = firstColTriag;
					firstColTriag.neighbourCA = t;
					tp.neighbourCA = firstColTriag.neighbourAB;
					firstColTriag.neighbourAB.neighbourBC = tp;
					firstColTriag = t;
					firstPoint = p;
					firstHullTriangle = firstColTriag;
					break;
				case Segment.POINT_BEHIND:
					t = new DelauTriangle(lastPoint, p);
					tp = new DelauTriangle(p, lastPoint);
					t.neighbourAB = tp;
					tp.neighbourAB = t;
					t.neighbourBC = tp;
					lastColTriag.neighbourBC = t;
					t.neighbourCA = lastColTriag;
					tp.neighbourCA = t;
					tp.neighbourBC = lastColTriag.neighbourAB;
					lastColTriag.neighbourAB.neighbourCA = tp;
					lastColTriag = t;
					lastPoint = p;
					break;
				}
			}
			/* start flipping to correct conflicts in Delaunay */
			DelauTriangle t = firstTriangle;
			do {
				flip(t);
				t = t.neighbour(t.getPointC());
			} while (t != firstTriangle && !t.isHalfplane());
			/* behebt anscheinend unsere flipping-probleme ! */
			flip(firstTriangle.neighbourCA);
			// checkConsistence();
		}
	}

	/**
	 * Delete a point from the Delaunay triangulation.
	 * 
	 * @param p
	 */
	public void deletePoint(@NonNull Point p) {
		if (allPoints.contains(p)) {
			points--;
			allPoints.remove(p);
			/*
			 * we don't have a special algorithm for deleting points, so we
			 * rebuild the whole structure. Better have a look at: O. Devillers:
			 * On Deletion in Delaunay Triangulation, ACM 15th Symp. Comp. Geom
			 * 99, pp.181-188
			 */
			rebuild();
		}
	}

	/**
	 * Moves the position of a point in this triangulation to new coordinates
	 * (newX, newY).
	 * 
	 * @param p
	 * @param newX
	 * @param newY
	 */
	public void movePoint(@NonNull Point p, float newX, float newY) {
		/*
		 * we simply rebuild everything. We have to make sure, that this point
		 * doens't equal to another point of our triangulation!
		 */
		Point check = new Point(newX, newY);
		if (!allPoints.contains(check)) {
			p.setX(newX);
			p.setY(newY);
			rebuild();
		}
	}

	/**
	 * Extend the hull with a point <VAR>p</VAR> that lies in one of the hull
	 * triangles <VAR>t</VAR>.
	 * 
	 * @param t a hull triangle containing p
	 * @param p new point to add
	 * @return DelauTriangle one of a real triangle near to p
	 */
	private DelauTriangle extendHull(DelauTriangle t, Point p) {
		/* degenerated case */
		if (Segment.pointTest(t.getPointA(), t.getPointB(), p) == Segment.POINT_ONEDGE) {
			// aus VoroGlide abgeschrieben
			DelauTriangle dg = new DelauTriangle(t.getPointA(), t.getPointB(),
					p);
			DelauTriangle hp = new DelauTriangle(p, t.getPointB());
			t.setPointB(p);
			dg.neighbourAB = t.neighbourAB;
			dg.neighbourAB.replaceNeighbour(t, dg);
			dg.neighbourBC = hp;
			hp.neighbourAB = dg;
			dg.neighbourCA = t;
			t.neighbourAB = dg;
			hp.neighbourBC = t.neighbourBC;
			hp.neighbourBC.neighbourCA = hp;
			hp.neighbourCA = t;
			t.neighbourBC = hp;
			return dg;
			// nicht ausgetestet !!
		}
		DelauTriangle side1 = extendclockwise(t, p);
		DelauTriangle side2 = extendcounterclock(t, p);
		/* link both new hull triangles together */
		side1.neighbourCA = side2;
		side2.neighbourBC = side1;
		firstHullTriangle = side1;
		return t;
	}

	/**
	 * Extend the hull in clockwise direction, starting a triangle <VAR>t</VAR>
	 * by extending all hull triangles with <VAR>p</VAR> to real triangles and
	 * adding one new hull triangle.
	 * 
	 * @param t a hull triangle containing p
	 * @param p a new point to add
	 * @see #extendHull
	 * @return DelauTriangle the new hull triangle
	 */
	private DelauTriangle extendclockwise(DelauTriangle t, Point p) {
		/* t has to contain p and be a halfplane */
		DelauTriangle prevT = t;
		while (t.pointInTriangle(p) == Triangle.INTRIANGLE) {
			t.extendTriangle(p);
			prevT = t;
			t = t.neighbourBC; // we should only get halfplanes !!!
		}
		/* create new hull triangle */
		DelauTriangle newHullTriag = new DelauTriangle(p, prevT.getPointB());
		newHullTriag.neighbourAB = prevT;
		newHullTriag.neighbourBC = t;
		t.neighbourCA = newHullTriag;
		prevT.neighbourBC = newHullTriag;
		/* neighbourCA for newHullTriag can only be set by other side */
		return newHullTriag;
	}

	/**
	 * Extend the hull in counterclockwise order.
	 * 
	 * @param t a triangle that contains p
	 * @param p
	 * @return DelauTriangle a new hull triangle
	 * @see #extendclockwise
	 */
	private DelauTriangle extendcounterclock(DelauTriangle t, Point p) {
		DelauTriangle prevT = t;
		t = t.neighbourCA;
		/* we start at the next triangle (not t!) */
		while (t.pointInTriangle(p) == Triangle.INTRIANGLE) {
			t.extendTriangle(p);
			prevT = t;
			t = t.neighbourCA; // we should only get halfplanes
		}
		DelauTriangle newHullTriag = new DelauTriangle(prevT.getPointA(), p);
		newHullTriag.neighbourAB = prevT;
		newHullTriag.neighbourCA = t;
		t.neighbourBC = newHullTriag;
		prevT.neighbourCA = newHullTriag;
		/* neighbourBC for newHullTriag can only be set by other side */
		return newHullTriag;
	}

	/**
	 * Extend a inner triangle containing p to three new triangles.
	 * 
	 * @param t a triangle containing p
	 * @param p new point to add (into t)
	 * @return DelauTriangle one of the new triangles
	 */
	private DelauTriangle extendTriag(DelauTriangle t, Point p) {
		if (t.neighbourAB.isHalfplane()
				&& Segment.pointTest(t.getPointB(), t.getPointA(), p) == Segment.POINT_ONEDGE)
			return extendHull(t.neighbourAB, p);
		if (t.neighbourBC.isHalfplane()
				&& Segment.pointTest(t.getPointC(), t.getPointB(), p) == Segment.POINT_ONEDGE)
			return extendHull(t.neighbourBC, p);
		if (t.neighbourCA.isHalfplane()
				&& Segment.pointTest(t.getPointA(), t.getPointC(), p) == Segment.POINT_ONEDGE)
			return extendHull(t.neighbourCA, p);

		/*
		 * hier den Sonderfall, dass Punkt auf Kante zwischen zwei Dreiecken !!
		 * (s. fehler.dat)
		 */

		DelauTriangle h1, h2; /* VoroGlide-Code */
		h1 = new DelauTriangle(t.getPointC(), t.getPointA(), p);
		h2 = new DelauTriangle(t.getPointB(), t.getPointC(), p);
		t.setPointC(p);
		h1.neighbourAB = t.neighbourCA;
		h1.neighbourBC = t;
		h1.neighbourCA = h2;
		h2.neighbourAB = t.neighbourBC;
		h2.neighbourBC = h1;
		h2.neighbourCA = t;
		h1.neighbourAB.replaceNeighbour(t, h1);
		h2.neighbourAB.replaceNeighbour(t, h2);
		t.neighbourBC = h2;
		t.neighbourCA = h1;
		return t;
	}

	/**
	 * Locate the triangle that contains a given point in the interior or on the
	 * border.
	 * 
	 * @param start the triangle at which we start searching
	 * @param p the point to locate
	 * @return DelauTriangle a triangle containing <VAR>p</VAR>
	 */
	@Nullable
	public DelauTriangle find(@NonNull DelauTriangle start, @NonNull Point p) {
		boolean found = false;
		while (!found) {
			if (start == null)
				return null;
			if (start.getPointA() == p)
				return start;
			if (start.getPointB() == p)
				return start;
			if (start.getPointC() == p)
				return start;
			if (start.pointInTriangle(p) != Triangle.OUTOFTRIANGLE) {
				found = true;
			} else {
				/* walk-through */
				if (!start.isHalfplane()) {
					if (Segment.pointTest(start.getPointA(), start.getPointB(),
							p) == Segment.POINT_RIGHT) {
						start = start.neighbourAB;
					} else if (Segment.pointTest(start.getPointB(), start
							.getPointC(), p) == Segment.POINT_RIGHT) {
						start = start.neighbourBC;
					} else if (Segment.pointTest(start.getPointC(), start
							.getPointA(), p) == Segment.POINT_RIGHT) {
						start = start.neighbourCA;
					} else {
						Log.e(VoronoiApp.APPNAME, "Error locating point " + p
								+ " - should not happen!");
						found = true;
					}
				} else {
					/* suchen in Halfplanes */
					if (Segment.pointTest(start.getPointA(), start.getPointB(),
							p) == Segment.POINT_RIGHT)
						start = start.neighbourAB;
					else
						Log.e(VoronoiApp.APPNAME,"Internal error find(" + start + ","
								+ p + ")");
					// andere Faelle sollte nicht vorkommen !!
				}
			}
		}
		return start;
	}

	/**
	 * Do an edge flip of the triangle <VAR>t</VAR> with it's AB-neighbour.
	 * 
	 * @param t
	 */
	private void flip(DelauTriangle t) {
		/* from VoroGlide */
		DelauTriangle u = t.neighbourAB, v;

		if (u.isHalfplane() || !u.pointInCircumcircle(t.getPointC())) // kein
																		// flip
																		// n�tig
			return;

		if (t.getPointA() == u.getPointA()) {
			v = new DelauTriangle(u.getPointB(), t.getPointB(), t.getPointC());
			v.neighbourAB = u.neighbourBC;
			t.neighbourAB = u.neighbourAB;
		} else if (t.getPointA() == u.getPointB()) {
			v = new DelauTriangle(u.getPointC(), t.getPointB(), t.getPointC());
			v.neighbourAB = u.neighbourCA;
			t.neighbourAB = u.neighbourBC;
		} else if (t.getPointA() == u.getPointC()) {
			v = new DelauTriangle(u.getPointA(), t.getPointB(), t.getPointC());
			v.neighbourAB = u.neighbourAB;
			t.neighbourAB = u.neighbourCA;
		} else {
			Log.e(VoronoiApp.APPNAME,"Error in flip." + t);
			return;
		}

		v.neighbourBC = t.neighbourBC;
		v.neighbourAB.replaceNeighbour(u, v);
		v.neighbourBC.replaceNeighbour(t, v);
		t.neighbourBC = v;
		v.neighbourCA = t;
		t.setPointB(v.getPointA());
		t.neighbourAB.replaceNeighbour(u, t);

		// rekursiv weiter.
		flip(t); // Man beachte, da� t.c(=v.c) immer noch der
		flip(v); // zuletzt eingef�gte Punkt ist.
	}

	/**
	 * Rebuild the triangulation from internal vector <VAR>allPoints</VAR>.
	 */
	public void rebuild() {
		allCollinear = true;
		firstHullTriangle = null;
		firstColTriag = null;
		lastColTriag = null;
		firstTriangle = null;
		firstPoint = null;
		lastPoint = null;
		points = 0;
		List<Point> oldPoints = allPoints;
		allPoints = new ArrayList<>(oldPoints.size());
		for (Iterator<Point> iterator = oldPoints.iterator(); iterator.hasNext(); ) {
			try {
				insertPoint(iterator.next());
			} catch (Exception e) {
				Log.w(VoronoiApp.APPNAME, e);
			}
		}
	}

	/**
	 * return a reference to a triangle of the Delaunay-Triangulation.
	 * 
	 * @return DelauTriangle oder <VAR>null</VAR> if empty.
	 * @see DelauTriangle
	 */
	@Nullable
	public DelauTriangle getFirstTriangle() {
		return firstTriangle;
	}

	/**
	 * return a reference to a (halfplane) triangle of the hull.
	 * 
	 * @return DelauTriangle oder <VAR>null</VAR> if empty.
	 * @see DelauTriangle
	 */
	@Nullable
	public DelauTriangle getFirstHullTriangle() {
		return firstHullTriangle;
	}

	/**
	 * returns true, iff all points are collinear. This is of course always true
	 * for zero up to two sites!
	 * 
	 * @return boolean <VAR>true</VAR> iff all sites collinear, else
	 *         <VAR>false</VAR>
	 */
	public boolean areCollinear() {
		return allCollinear;
	}

	@NonNull
	public String toString() {
		DelauTriangle t = firstTriangle;
		resetVisited(t);
		StringBuilder text = new StringBuilder();
		dotoString(t, text);
		return text.toString();
	}

	private void dotoString(DelauTriangle t, StringBuilder text) {
		if (t != null) {
			if (!t.visited) {
				t.visited = true;
				text.append(t.toString()).append(";");
				dotoString(t.neighbourAB, text);
				dotoString(t.neighbourBC, text);
				dotoString(t.neighbourCA, text);
			}
		}
	}

	/**
	 * Call this method before you work on the connected DelauTriangles. It will
	 * reset all <VAR>visited</VAR> flags on every triangle. You must have
	 * exclusive access to this data structure. Don't call other methods while
	 * walking through the triangles!!
	 * 
	 * @param t the triangle to start with (take getFirstTriangle()).
	 * @see DelauTriangle
	 */
	public void resetVisited(@Nullable DelauTriangle t) {
		if (t != null) {
			t.visited = false;
			t.dcelAB = null;
			t.dcelBC = null;
			t.dcelCA = null;
			if (t.neighbourAB != null)
				if (t.neighbourAB.visited)
					resetVisited(t.neighbourAB);
			if (t.neighbourBC != null)
				if (t.neighbourBC.visited)
					resetVisited(t.neighbourBC);
			if (t.neighbourCA != null)
				if (t.neighbourCA.visited)
					resetVisited(t.neighbourCA);
		}
	}

	public void paint(@NonNull Canvas g) {
		// g.setColor(color);
		resetVisited(firstTriangle);
		paintit(g, firstTriangle);
	}

	private void paintit(Canvas g, DelauTriangle t) {
		if (t != null) {
			if (!t.visited) {
				t.visited = true;
				if (!t.isHalfplane()) {
					Point a = t.getPointA();
					Point b = t.getPointB();
					Point c = t.getPointC();
					if (!t.neighbourAB.visited) 
						g.drawLine((float) a.getX(), (float) a.getY(),
								(float) b.getX(), (float) b.getY(),
								getLinePaint());
					if (!t.neighbourBC.visited)
						g.drawLine((float) b.getX(), (float) b.getY(),
								(float) c.getX(), (float) c.getY(),
								getLinePaint());
					if (!t.neighbourCA.visited)
						g.drawLine((float) c.getX(), (float) c.getY(),
								(float) a.getX(), (float) a.getY(),
								getLinePaint());
					if (!t.neighbourAB.visited) 
						paintit(g, t.neighbourAB);
					if (!t.neighbourBC.visited)
						paintit(g, t.neighbourBC);
					if (!t.neighbourCA.visited)
						paintit(g, t.neighbourCA);
				}
			}
		}
	}

	/**
	 * Visit all triangles once.
	 * 
	 * @param visitor the visitor
	 */
	public void visitTriangles(@NonNull Visitor visitor) {
		resetVisited(firstTriangle);
		visitTriangles(visitor, firstTriangle);
	}
	
	private void visitTriangles(Visitor visitor, DelauTriangle t) {
		if ( t != null ) {
			if (!t.visited) {
				t.visited = true;
				visitor.visit(t);
				visitTriangles(visitor, t.neighbourAB);
				visitTriangles(visitor, t.neighbourBC);
				visitTriangles(visitor, t.neighbourCA);
			}
		}
	}
	
	/**
	 * return an Enumeration of all sites.
	 * 
	 * @return iterator over all points
	 */
	@NonNull
	public Iterator<Point> points() {
		return allPoints.iterator();
	}

	/**
	 * return the number of sites in the triangulation.
	 * 
	 * @return int number of points
	 */
	public int size() {
		return points;
	}
}
