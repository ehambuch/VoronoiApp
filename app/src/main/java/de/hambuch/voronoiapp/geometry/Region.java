package de.hambuch.voronoiapp.geometry;

import android.graphics.Canvas;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Enumeration;
import java.util.Vector;

/**
 * A (convex) voronoi region is a kind of an &quot;open polygon&quot; that may
 * bound an open area by some rays. So a region consists of segments and rays in
 * counterclockwise order. It contains a kernel point in it's interior. A region
 * without any points means the whole R<SUP>2</SUP>.
 * 
 * @version 1.2 (9.3.2001)
 * @author Eric Hambuch
 */
public class Region extends Polygon implements Cloneable {

	/* a point in the kernel of this region */
	protected Point innerPoint;

	public Region(Point kernelpoint) {
		super();
		this.innerPoint = kernelpoint;
	}

	public Region(Vector<Point> points, Point kernelpoint) {
		super(points);
		this.innerPoint = kernelpoint;
	}

	public boolean isOpen() {
		Enumeration<Point> enume = points.elements();
		while (enume.hasMoreElements()) {
			Point p = (Point) enume.nextElement();
			if (p.isInfinityPoint())
				return true;
		}
		return false;
	}

	public void setKernelPoint(@NonNull Point p) {
		this.innerPoint = p;
	}

	@NonNull
	public Point getKernelPoint() {
		return innerPoint;
	}

	public boolean pointInPolygon(@NonNull Point point) {
		/*
		 * connection between point and innerPoint may not cross any border
		 * edges -> point is in (star-shaped) region.
		 */
		if (points.size() <= 1)
			return true; /* the whole R^2 => p is in */
		Point p, q, r;
		Ray ray = new Ray(point, innerPoint);
		for (int i = 0; i <= points.size(); i++) {
			p = (Point) points.elementAt(i % points.size());
			q = (Point) points.elementAt((i + 1) % points.size());
			r = (Point) points.elementAt((i + 2) % points.size());
			if (r.isInfinityPoint()) {
				/* ray from p-> q */
				if (ray.intersect(new Ray(p, q)) != null)
					return false;
			} else if (p.isInfinityPoint()) {
				/* ray from r->q */
				if (ray.intersect(new Ray(r, q)) != null)
					return false;
			} else {
				/* segment */
				if (ray.intersect(new Segment(p, q)) != null)
					return false;
			}
		}
		return true;
	}

	/**
	 * Clips the region to an given rectangle (xmin, ymin)-(xmax, ymax)
	 * 
	 * @param xmin
	 * @param ymin
	 * @param xmax
	 * @param ymax
	 * @return Polygon the clipped polygon. May be <VAR>null</VAR> if invisible.
	 */

	@Nullable
	public Polygon clipTo(float xmin, float ymin, float xmax, float ymax) {
		/*
		 * we try to close our region so that we can use the polygon clipping
		 * algorithm
		 */
		Vector<Point> points1 = new Vector<Point>(this.points.size());
		if (points.size() <= 1) {
			/* the whole R^2 clips to (xmin,ymin,xmax,ymax) */
			points1.addElement(new Point(xmin, ymin)); /*
														 * mind the correct
														 * orientation !
														 */
			points1.addElement(new Point(xmax, ymin));
			points1.addElement(new Point(xmax, ymax));
			points1.addElement(new Point(xmin, ymax));
		} else {
			float dist = (xmax - xmin) * (xmax - xmin) + (ymax - ymin)
					* (ymax - ymin);
			int i = 0;
			while (i < points.size()) {
				Point p = (Point) points.elementAt(i);
				Point q = (Point) points.elementAt((i + 1) % points.size());
				Point r = (Point) points.elementAt((i + 2) % points.size());
				points1.addElement(p);

				if (r.isInfinityPoint()) { // rays from p->q and t->s
					Point s = (Point) points.elementAt((i + 3) % points.size());
					Point t = (Point) points.elementAt((i + 4) % points.size());
					// now we take a point on ray p->q that is far away ...
					Ray ray1 = new Ray(p, q);
					Ray ray2 = new Ray(t, s);
					points1.addElement(ray1.pointOnRay(dist + p.getX()
							* p.getX() + p.getY() * p.getY()));
					// an extra point between the rays...
					Line l1 = new Line(p, q);
					Line l2 = new Line(t, s);
					Point isec = l1.intersect(l2);
					Ray ray3;

					if (isec == null) { // lines are parallel
						if (p.equals(t)) { // is a full halfplane
							isec = new Point((p.getX() + t.getX()) / 2.0f, (p
									.getY() + t.getY()) / 2.0f);
							ray3 = new Ray(isec, innerPoint);
						} else { // is a stripe (collinear points)
							ray3 = new Ray(innerPoint,
									(ray1.getDirectionX() + ray2
											.getDirectionX()) / 2.0f, (ray1
											.getDirectionY() + ray2
											.getDirectionY()) / 2.0f);
						}
					} else {
						ray3 = new Ray(isec, innerPoint);
					}
					points1.addElement(ray3.pointOnRay(3.0f * dist));
					// and a point on ray t->s far away
					points1.addElement(ray2.pointOnRay(dist + t.getX()
							* t.getX() + t.getY() * t.getY()));
					i += 3;
				} // if
				i++;
			} // while
			Vector<Point> points2 = super.cutBottom(points1, ymax);
			points1 = super.cutTop(points2, ymin);
			points2 = super.cutLeft(points1, xmin);
			points1 = super.cutRight(points2, xmax);
		} // if

		if (points1.size() > 1) {
			SimplePolygon poly = new SimplePolygon(points1);
			poly.setColor(getColor());
			poly.setFillColor(fillColor);
			return poly;
		}
		return null;
	}

	/**
	 * If you wan't to cut a ray (s+------) from a point that lies onto the ray
	 * and get a ray line (s+---+f+----- ) you need a new direction point. This
	 * method will create one, that the new ray from <VAR>from</VAR> points into
	 * the same direction like <VAR>ray</VAR>. Make sure, that the startpoint of
	 * <VAR>ray</VAR> is different from <VAR>from</VAR> !!!!
	 * 
	 * @param ray
	 * @param from a point on the ray
	 * @return Point a new direction point for a ray with startpoint
	 *         <VAR>from</VAR> and some direction line <VAR>ray</VAR>
	 */
	private Point getDirectionPoint(Ray ray, Point from) {
		float x = from.getX() + (from.getX() - ray.getStartpoint().getX())
				* ray.getDirectionX();
		float y = from.getY() + (from.getY() - ray.getStartpoint().getY())
				* ray.getDirectionY();
		return new Point(x, y);
	}

	/**
	 * paints the region either filled or only the borders (depending on
	 * setFillColor()).
	 * 
	 * @param g
	 */
	public void paint(@NonNull Canvas g) {
		if (fillColor > 0) {
			Polygon poly = clipTo(0.0f, 0.0f, 5000.0f, 5000.0f); // besser
																	// getClipBounds()
																	// abfragen!
			if (poly != null)
				poly.paint(g);
		} else {
			if (points.size() > 2) {
				int i = 0;
				while (i <= points.size()) {
					Point p = (Point) points.elementAt(i % points.size());
					Point q = (Point) points.elementAt((i + 1) % points.size());
					Point r = (Point) points.elementAt((i + 2) % points.size());
					if (r.isInfinityPoint()) {
						Ray ray = new Ray(p, q);
						ray.setColor(getColor());
						ray.paint(g);
						i++;
					} else if (p.isInfinityPoint()) {
						Ray ray = new Ray(r, q);
						ray.setColor(getColor());
						ray.paint(g);
						i++;
					} else {
						Segment.drawSegment(g, p, q, getLinePaint());
					}
					i++;
				} // while
			} // if
			innerPoint.paint(g);
		} // if
	}

	@NonNull
	public String toString() {
		StringBuilder text = new StringBuilder("Region " + innerPoint + " :");
		Enumeration<Point> enume = points.elements();
		while (enume.hasMoreElements()) {
			text.append(enume.nextElement().toString());
		}
		return text.toString();
	}

	@NonNull
	public Object clone() {
		Region region = new Region(this.points, this.innerPoint);
		region.setColor(getColor());
		region.setFillColor(this.fillColor);
		return region;
	}
}
