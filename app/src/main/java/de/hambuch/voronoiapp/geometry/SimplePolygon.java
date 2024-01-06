package de.hambuch.voronoiapp.geometry;

import android.graphics.Canvas;

import androidx.annotation.NonNull;

import java.util.Enumeration;
import java.util.Vector;

/**
 * A simple Polygon (no crossing edges, no holes).
 *
 * @version 1.1 (24.1.2001)
 * @author Eric Hambuch (Eric.Hambuch@fernuni-hagen.de)
 */

public class SimplePolygon extends Polygon implements Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1084936749239423555L;

	public SimplePolygon() {
		super();
   }

   public SimplePolygon(Vector<Point> points) {
		super(points);
   }

   public Vector<Segment> toSegments() {
		Vector<Segment> segs = new Vector<Segment>();
		int i;
		Enumeration<Point> enume = points.elements();
		if(points.size() > 1) {
			Point firstp = (Point)enume.nextElement();
			Point lastp = firstp, nextp;
			while(enume.hasMoreElements()) {
				nextp = (Point)enume.nextElement();
				segs.add(new Segment(lastp, nextp));
				lastp = nextp;
			}
			segs.add(new Segment(lastp, firstp));
		}
		return segs;
   }

   public boolean pointInPolygon(@NonNull Point point) {
		Ray ray = new Ray(point, 1.0f, 1.0f);
		int intersects = 0;
		Vector<Segment> segs = toSegments();
		Enumeration<Segment> enume=segs.elements();
		Segment seg;
		while(enume.hasMoreElements()) {
			seg = (Segment)enume.nextElement();
			if(ray.intersect(seg) != null) intersects++; // doesn't work for point ON the polygon's border
		}
		if((intersects % 2) == 1) return true;
		return false;
   }


   @NonNull
   public Object clone() {
		SimplePolygon polygon = new SimplePolygon(points);
		polygon.setColor(getColor());
		polygon.setFillColor(fillColor);
		return polygon;
   }

   public void paint(@NonNull Canvas g) {
		if(points.size() > 1) {
		    if(fillColor > 0) {
				// TODO g.setColor(fillColor);
				int nPoints = points.size();
				int[] xPoints = new int[nPoints];
				int[] yPoints = new int[nPoints];
				Enumeration<Point> enume = points.elements();
				Point p;
				int i=0;
				while(enume.hasMoreElements()) {
				    p = (Point)enume.nextElement();
				    xPoints[i] = (int)p.getX();
				    yPoints[i] = (int)p.getY();
				    i++;
				}
				// TODO
				//g.fillPolygon(xPoints, yPoints, nPoints);
		    }
		    paintOutline(g);
		}
   }

   protected void paintOutline(Canvas g) {
		if(points.size() > 1) {
		    Enumeration<Point> enume = points.elements();
		    Point firstp = (Point)enume.nextElement();
		    Point lastp = firstp, nextp;
		    while(enume.hasMoreElements()) {
				nextp = (Point)enume.nextElement();
				g.drawLine((float)lastp.getX(), (float)lastp.getY(),
						   (float)nextp.getX(), (float)nextp.getY(), getLinePaint());
				lastp = nextp;
		    }
		    g.drawLine((float)lastp.getX(), (float)lastp.getY(),
				       (float)firstp.getX(), (float)firstp.getY(), getLinePaint());
		}
   }
}
