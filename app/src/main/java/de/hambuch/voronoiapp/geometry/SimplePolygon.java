package de.hambuch.voronoiapp.geometry;

import android.graphics.Canvas;
import android.graphics.Path;

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

   public SimplePolygon(@NonNull Vector<Point> points) {
		super(points);
   }

	@NonNull
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
		    if(getFillPaint() != null) {
				final Path path = new Path();
				path.setFillType(Path.FillType.EVEN_ODD);
				path.moveTo(points.get(0).getX(), points.get(0).getY());
				for(int i=1;i<points.size();i++)
					path.lineTo(points.get(i).getX(), points.get(i).getY());
				path.close();
				g.drawPath(path, getFillPaint());
		    }
			paintOutline(g); // paint outline seperately
		}
   }

   protected void paintOutline(@NonNull Canvas g) {
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
