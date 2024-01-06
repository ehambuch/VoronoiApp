package de.hambuch.voronoiapp.geometry;

import android.graphics.Canvas;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * This class provides a ray starting at a point and pointing in a given
 * direction.
 * 
 * @version 1.1 (9.2.2001)
 * @author Eric Hambuch
 */
public class Ray extends GeomElement implements Cloneable, Edge {

	@NonNull
	protected Point startPoint;
	protected float directionX;
	protected float directionY;

	/**
	 * Create a ray that starts at <VAR>startpoint</VAR> in direction defined by
	 * <VAR>directionpoint</VAR>
	 * 
	 * @param startpoint
	 * @param directionpoint
	 */
	public Ray(@NonNull Point startpoint, @NonNull Point directionpoint) {
		this.startPoint = startpoint;
		directionX = directionpoint.getX() - startpoint.getX();
		directionY = directionpoint.getY() - startpoint.getY();
	}

	/**
	 * Create a ray that starts at <VAR>startpoint</VAR> in a given direction
	 * 
	 * @param startpoint
	 * @param directX
	 * @param directY
	 */
	public Ray(@NonNull Point startpoint, float directX, float directY) {
		this.startPoint = startpoint;
		directionX = directX;
		directionY = directY;
	}

	/**
	 * Create a ray by a startpoint and a gradient.
	 * 
	 * @param startpoint
	 * @param gradient between [0, 2pi[
	 */
	public Ray(@NonNull Point startpoint, double gradient) {
		this.startPoint = startpoint;
		directionX = (float) Math.cos(gradient) * 10.0f;
		directionY = (float) Math.sin(gradient) * 10.0f;
	}

	@NonNull
	public Point getStartpoint() {
		return startPoint;
	}

	public void setStartpoint(Point startpoint) {
		this.startPoint = startpoint;
	}

	public void setDirection(float directX, float directY) {
		this.directionX = directX;
		this.directionY = directY;
	}

	public void setDirection(@NonNull Point directionPoint) {
		this.directionX = directionPoint.getX() - this.startPoint.getX();
		this.directionY = directionPoint.getY() - this.startPoint.getY();
	}

	public float getDirectionX() {
		return directionX;
	}

	public float getDirectionY() {
		return directionY;
	}

	@NonNull
	public Object clone() {
		Ray ray = new Ray(startPoint, directionX, directionY);
		ray.setColor(getColor());
		return ray;
	}

	public int pointTest(@NonNull Point point) {
		float ax = startPoint.getX();
		float ay = startPoint.getY();
		float bx = ax + directionX;
		float by = ay + directionY;
		float cx = point.getX();
		float cy = point.getY();
		float area = (cx - ax) * (cy + ax) + (bx - cx) * (by + cy) + (ax - bx)
				* (ay + by);
		if (area > 0.0f)
			return POINT_LEFT;
		else if (area < 0.0f)
			return POINT_RIGHT;
		else {
			if (directionX > 0.0f) {
				if (cx < ax)
					return POINT_BEFORE;
				return POINT_ONEDGE;
			}
			if (directionX < 0.0f) {
				if (cx > ax)
					return POINT_BEFORE;
				return POINT_ONEDGE;
			}
			if (directionY > 0.0f) {
				if (cy < ay)
					return POINT_BEFORE;
				return POINT_ONEDGE;
			}
			if (directionY < 0.0f) {
				if (cy > ay)
					return POINT_BEFORE;
				return POINT_ONEDGE;
			}
		}
		return POINT_ERROR;
	}

	@Nullable
	public Point intersect(@NonNull Edge edge) {
		if (edge instanceof Segment) {
			return ((Segment) edge).intersect(this);
		}
		if (edge instanceof Ray) {
			float x0 = startPoint.getX();
			float y0 = startPoint.getY();
			float x1x0 = directionX;
			float y1y0 = directionY;
			float x2 = ((Ray) edge).getStartpoint().getX();
			float y2 = ((Ray) edge).getStartpoint().getY();
			float x3x2 = ((Ray) edge).getDirectionX(); /* = x3-x2 */
			float y3y2 = ((Ray) edge).getDirectionY();
			float mdiv = (x1x0) * (-y3y2) + x3x2 * (y1y0);
			if ((float) Math.abs(mdiv) < PARALLEL)
				return null;
			float m = ((x2 - x0) * (-y3y2) + x3x2 * (y2 - y0)) / mdiv;
			if (m < 0.0f)
				return null;
			float n = ((x1x0) * (y2 - y0) - (x2 - x0) * (y1y0)) / mdiv;
			if (n < 0.0f)
				return null;
			return new Point(x0 + m * (x1x0), y0 + m * (y1y0));
		}
		if (edge instanceof Line) {
			float x0 = startPoint.getX();
			float y0 = startPoint.getY();
			float x1x0 = directionX;
			float y1y0 = directionY;
			float x2 = ((Line) edge).getStartpoint().getX();
			float y2 = ((Line) edge).getStartpoint().getY();
			float x3x2 = ((Line) edge).getDirectionX(); /* = x3-x2 */
			float y3y2 = ((Line) edge).getDirectionY();
			float mdiv = (x1x0) * (-y3y2) + x3x2 * (y1y0);
			if ((float) Math.abs(mdiv) < PARALLEL)
				return null;
			float m = ((x2 - x0) * (-y3y2) + x3x2 * (y2 - y0)) / mdiv;
			if (m < 0.0f)
				return null;
			float n = ((x1x0) * (y2 - y0) - (x2 - x0) * (y1y0)) / mdiv;
			return new Point(x0 + m * (x1x0), y0 + m * (y1y0));
		}
		return null;
	}

	public float gradient() {
		float grad = 0.0f;
		if (directionY == 0.0f) { // implementation not perfect for directionY
									// close to 0.0!!
			if (directionX < 0.0f)
				grad = (float) Math.PI;
			else
				grad = 0.0f;
		} else if (directionX == 0.0) {
			if (directionY > 0.0f)
				grad = (float) Math.PI / 2.0f;
			if (directionY < 0.0f)
				grad = (float) Math.PI * 1.5f;
		} else {
			grad = (float) Math.atan(Math.abs(directionY / directionX));
			/*
			 * as atan() only returns values between -pi/2 and pi/2 we have to
			 * correct the sign and check the quadrant
			 */
			if (directionY >= 0.0f && directionX < 0.0f)
				grad = grad + (float) Math.PI / 2.0f;
			else if (directionY < 0.0f && directionX < 0.0f)
				grad = grad + (float) Math.PI;
			else if (directionY <= 0.0f && directionX >= 0.0f)
				grad = grad + (float) Math.PI * 1.5f;
		}
		return grad;
	}

	/**
	 * Returns a point on the ray that is <VAR>distance</VAR> away from the
	 * startpoint.
	 * 
	 * @param distance (a distance from startpoint &gt; 0)
	 * @return Point a point on the ray
	 */
	@NonNull
	public Point pointOnRay(float distance) {
		if (distance <= 0.0f)
			return startPoint; // no negative distances !!
		float dsquare = (float) Math.sqrt(directionX * directionX + directionY
				* directionY);
		return new Point(startPoint.getX() + distance * directionX / dsquare,
				startPoint.getY() + distance * directionY / dsquare);
	}

	@NonNull
	public Segment clipTo(float xmin, float ymin, float xmax, float ymax) {
		float x1 = startPoint.getX();
		float y1 = startPoint.getY();
		float x2 = x1, y2 = y1;

		if (directionX > 0.0f) {
			x2 = xmax;
			y2 = y1 + (x2 - x1) / directionX * directionY;
		} else if (directionX < 0.0f) {
			x2 = xmin;
			y2 = y1 + (x2 - x1) / directionX * directionY;
		} else {
			x2 = x1;
			if (directionY > 0.0)
				y2 = ymax;
			else
				y2 = ymin;
		}
		float[] koords = Segment.clipping(x1, y1, x2, y2, xmin, ymin, xmax,
				ymax);
		final Segment segment = new Segment(koords[0], koords[1], koords[2], koords[3]);
		segment.setColor(getColor());
		return segment;
	}

	@NonNull
	public String toString() {
		return startPoint.toString() + "-Ray: dx=" + directionX + ",dy="
				+ directionY;
	}

	public void paint(@NonNull Canvas graphics) {
		float x1 = startPoint.getX();
		float y1 = startPoint.getY();
		float x2 = x1, y2 = y1;
		float xmax = 16000.0f; /* maximum size of graphics ?! */
		float ymax = 16000.0f;
		/* Ray is vertical */
		if (directionX == 0.0f) {
			x2 = x1;
			if (directionY < 0f)
				y2 = 0.0f;
			else
				y2 = ymax;
		} else if (directionX > 0.0f) {
			y2 = y1 + (xmax - x1) / directionX * directionY;
			x2 = xmax;
		} else {
			y2 = y1 + (0 - x1) / directionX * directionY;
			x2 = 0.0f;
		}

		float koords[] = Segment.clipping(x1, y1, x2, y2, 0.0f, 0.0f, 16000.0f,
				16000.0f);
		if (koords != null) {
			graphics.drawLine((float) koords[0], (float) koords[1],
					(float) koords[2], (float) koords[3], getLinePaint());
		}
	}
}
