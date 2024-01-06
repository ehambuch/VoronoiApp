package de.hambuch.voronoiapp.geometry;

import android.graphics.Canvas;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * represents a Line in R<SUP>2</SUP>.
 * Definied by a point and direction x,y.
 *
 * @author Eric Hambuch
 * @version 1.0 (30.10.2000)
 */

public class Line extends GeomElement implements Cloneable, Edge {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6669576517536781865L;
	
	/* the line is described by (x,y)=(x_s,y_s)+t*(dx,dy) */
	@NonNull
	protected Point startPoint;
    protected float directionX;
    protected float directionY;

    /** Create a line defined by two points
     *
     * @param startpoint
     * @param directionpoint
     *
     */
	public Line(Point startpoint, Point directionpoint) {
		this.startPoint = startpoint;
		directionX = directionpoint.getX() - startpoint.getX();
		directionY = directionpoint.getY() - startpoint.getY();
   }

    /** Create a line defined by a point and direction in x,y
     *
     * @param startpoint
     * @param directX direction in X (left/right)
     * @param directY direction in Y (up/down)
     */
   public Line(@NonNull Point startpoint, float directX, float directY) {
		this.startPoint = startpoint;
		directionX = directX;
		directionY = directY;
   }

	public void setStartpoint(@NonNull Point p) {
		startPoint = p;
	}

	@NonNull
	public Point getStartpoint() {
		return startPoint;
	}
	public float getDirectionX() {
		return directionX;
	}
	public float getDirectionY() {
		return directionY;
	}
	public void setDirection(float dx, float dy) {
		directionX = dx;
		directionY = dy;
	}
    /**
     * Checks whether a point is left oder right from this line.
     *
     * @param point
     * @return int <VAR>POINT_LEFT</VAR>, <VAR>POINT_RIGHT</VAR> or <VAR>POINT_ONEDGE</VAR> if point lies on the line.
     */
   public int pointTest(@NonNull Point point) {
		double ax = startPoint.getX();
		double ay = startPoint.getY();
		double bx = ax+directionX;
		double by = ay+directionY;
		double cx = point.getX();
		double cy = point.getY();
		double area = (cx - ax) * (cy + ax)+(bx - cx) * (by + cy)+(ax - bx) * (ay + by);
		if(area > 0.0) return POINT_LEFT;
		if(area < 0.0) return POINT_RIGHT;
		return POINT_ONEDGE;
   }

   @NonNull
   public Object clone() {
		Line line = new Line(startPoint, directionX, directionY);
		line.setColor(getColor());
		return line;
   }

	@Nullable
	public Point intersect(@NonNull Edge edge) {
		if(edge instanceof Segment)
			return ((Segment)edge).intersect(this);
		if(edge instanceof Ray)
			return ((Ray)edge).intersect(this);
		if(edge instanceof Line) {
			float dx1 = directionX;
			float dy1 = directionY;
			float dx2 = ((Line)edge).getDirectionX();
			float dy2 = ((Line)edge).getDirectionY();
			float mdiv = dx1*(-dy2)+dx2*dy1;
			if(Math.abs(mdiv) < PARALLEL) return null; // parallel
			float m = ( (((Line)edge).getStartpoint().getX() - startPoint.getX()) * (-dy2) +
						 (((Line)edge).getStartpoint().getY() - startPoint.getY()) * dx2 ) / mdiv;
			return new Point(startPoint.getX()+m*dx1, startPoint.getY()+m*dy1);
		}
		return null;
    }

	public float gradient() {
		float grad = 0.0f;
		if (directionY == 0.0) {
			if (directionX < 0.0)
				grad = (float)Math.PI;
			else
				grad = 0.0f;
		} else if (directionX == 0.0) {
			if(directionY > 0.0) grad = (float)Math.PI / 2.0f;
			if(directionY < 0.0) grad = (float)Math.PI * 1.5f;
		} else {
			grad = (float)Math.atan(Math.abs(directionY / directionX));
			/* as atan() only returns values between -pi/2 and pi/2 we have to correct the sign
			   and determinate the quadrant */
			if(directionY >= 0.0f && directionX < 0.0f)
				grad = grad + (float)Math.PI / 2.0f;
			else if(directionY < 0.0f && directionX < 0.0f)
				grad = grad + (float)Math.PI;
			else if(directionY < 0.0f && directionX >= 0.0f)
				grad = grad + (float)Math.PI * 1.5f;
		}
		return grad;
	}

	@NonNull
	public Segment clipTo(float xmin, float ymin, float xmax, float ymax) {
	   float x0 = startPoint.getX();
	   float y0 = startPoint.getY();
	   float x1 = x0, y1 = y0, x2 = x0, y2 = y0;

		if(directionX > 0.0f) {
		    x1 = xmin;
		    y1 = y0 - (x1-x0)/directionX*directionY;
		    x2 = xmax;
		    y2 = y0 + (x2-x0)/directionX*directionY;
		} else if(directionX < 0.0f) {
		    x1 = xmax;
		    y1 = y0 - (x1-x0)/directionX*directionY;
		    x2 = xmin;
		    y2 = y0 + (x2-x0)/directionX*directionY;
		} else {
		    if(directionY > 0.0f)
				y2 = ymax;
		    else
				y2 = ymin;
		}
		float[] koords = Segment.clipping(x1,y1,x2,y2,xmin,ymin,xmax,ymax);
		Segment segment = new Segment(koords[0],koords[1],koords[2],koords[3]);
		segment.setColor(getColor());
		return segment;
   }

   @NonNull
   public String toString() {
		return startPoint.toString()+"-Line: dx="+directionX+",dy="+directionY;
   }

   public void paint(@NonNull Canvas graphics) {
	   float x0 = startPoint.getX();
	   float y0 = startPoint.getY();
		float x1 = x0, y1= y0, x2 = x1, y2 = y1;
		float xmax = 8000.0f; /* maximum x value (depends of course on screen size) */
		float ymax = 8000.0f;
		if(directionX == 0.0) {
			y1 = 0.0f;
			y2 = ymax;
		} else if(directionY == 0.0f) {
			x1 = 0.0f;
			x2 = xmax;
		} else {
			/* clip to top/bottom */
			float t = (0-y0)/directionY;
			x1 = x0 + t*directionX;
			t = (ymax-y0)/directionY;
			x2 = x0 + t*directionX;
			/* then clip left/right */
			t = (0-x1)/directionX;
			y1 = y0 + t*directionY;
			t = (xmax-x1)/directionX;
			y2 = y0 + t*directionY;
			// Clipping could be done better!
		}
		graphics.drawLine((float)x1,(float)y1,(float)x2,(float)y2,getLinePaint());
	}
}
