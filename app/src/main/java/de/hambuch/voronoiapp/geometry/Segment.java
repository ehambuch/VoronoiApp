package de.hambuch.voronoiapp.geometry;

import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Segment represents a line segment in R<SUP>2</SUP> defined by two points.
 * 
 * @author Eric Hambuch
 * @version 1.2 (9.2.2001)
 */
public class Segment extends GeomElement implements Edge, Cloneable {

	/* for clipping */
	private static final int CLIP_LEFT = 1;
	private static final int CLIP_RIGHT = 2;
	private static final int CLIP_TOP = 4;
	private static final int CLIP_BOTTOM = 8;

	protected Point startPoint;
	protected Point endPoint;

	/**
	 * Create a segment defined by two points
	 * 
	 * @param startpoint
	 * @param endpoint
	 */
	public Segment(@NonNull Point startpoint, @NonNull Point endpoint) {
		this.startPoint = startpoint;
		this.endPoint = endpoint;
	}

	public Segment(@NonNull Point startpoint, @NonNull Point endpoint, int color) {
		super(color);
		this.startPoint = startpoint;
		this.endPoint = endpoint;
	}

	public Segment(float sx, float sy, float ex, float ey) {
		this.startPoint = new Point(sx, sy);
		this.endPoint = new Point(ex, ey);
	}

	/**
	 * Tests whether point lies left, right, on segment or before/after start-
	 * or endpoint of segment.
	 *
	 * @param a
	 * @param b
	 * @param c
	 * @return int <VAR>POINT_ONEDGE</VAR>,
	 *         <VAR>POINT_LEFT</VAR>,<VAR>POINT_RIGHT</VAR>, or
	 *         <VAR>POINT_BEFORE/BEHIND</VAR> if points lies before startpoint
	 *         or after endpoint
	 */
	public static int pointTest(@NonNull Point a, @NonNull Point b, @NonNull Point c) {
		float ax = a.getX();
		float ay = a.getY();
		float bx = b.getX();
		float by = b.getY();
		;
		float cx = c.getX();
		float cy = c.getY();
		float area = (bx - ax) * (cy - ay) - (cx - ax) * (by - ay);
		if (area > 0.0)
			return POINT_LEFT;
		else if (area < 0.0)
			return POINT_RIGHT;
		else {
			float directionX = bx - ax, directionY = by - ay;
			if (directionX > 0) {
				if (cx < ax)
					return POINT_BEFORE;
				if (bx < cx)
					return POINT_BEHIND;
				return POINT_ONEDGE;
			}
			if (directionX < 0) {
				if (cx > ax)
					return POINT_BEFORE;
				if (bx > cx)
					return POINT_BEHIND;
				return POINT_ONEDGE;
			}
			if (directionY > 0) {
				if (cy < ay)
					return POINT_BEFORE;
				if (by < cy)
					return POINT_BEHIND;
				return POINT_ONEDGE;
			}
			if (directionY < 0) {
				if (cy > ay)
					return POINT_BEFORE;
				if (by > cy)
					return POINT_BEHIND;
				return POINT_ONEDGE;
			}
		}
		return POINT_ERROR;
	}

	public int pointTest(@NonNull Point point) {
		return pointTest(startPoint, endPoint, point);
	}

	@Nullable
	public Point intersect(@NonNull Edge edge) {
		float x0 = startPoint.getX();
		float y0 = startPoint.getY();
		float x1 = endPoint.getX();
		float y1 = endPoint.getY();
		if (edge instanceof Segment) {
			float x2 = ((Segment) edge).getStartpoint().getX();
			float y2 = ((Segment) edge).getStartpoint().getY();
			float x3 = ((Segment) edge).getEndpoint().getX();
			float y3 = ((Segment) edge).getEndpoint().getY();
			float mdiv = (x1 - x0) * (y2 - y3) - (x2 - x3) * (y1 - y0);
			if ((float) Math.abs(mdiv) < PARALLEL)
				return null; /* parallel */
			float m = ((x2 - x0) * (y2 - y3) - (x2 - x3) * (y2 - y0)) / mdiv;
			if (m < 0.0f || m > 1.0f)
				return null;
			float n = ((x1 - x0) * (y2 - y0) - (x2 - x0) * (y1 - y0)) / mdiv;
			if (n < 0.0f || n > 1.0f)
				return null;
			return new Point(x0 + m * (x1 - x0), y0 + m * (y1 - y0));
		}
		if (edge instanceof Ray) {
			float x2 = ((Ray) edge).getStartpoint().getX();
			float y2 = ((Ray) edge).getStartpoint().getY();
			float x3x2 = ((Ray) edge).getDirectionX(); /* = x3-x2 */
			float y3y2 = ((Ray) edge).getDirectionY();
			float mdiv = (x1 - x0) * (-y3y2) + x3x2 * (y1 - y0);
			if ((float) Math.abs(mdiv) < PARALLEL)
				return null;
			float m = ((x2 - x0) * (-y3y2) + x3x2 * (y2 - y0)) / mdiv;
			if (m < 0.0f || m > 1.0f)
				return null;
			float n = ((x1 - x0) * (y2 - y0) - (x2 - x0) * (y1 - y0)) / mdiv;
			if (n < 0.0f)
				return null;
			return new Point(x0 + m * (x1 - x0), y0 + m * (y1 - y0));
		}
		if (edge instanceof Line) {
			float x2 = ((Line) edge).getStartpoint().getX();
			float y2 = ((Line) edge).getStartpoint().getY();
			float x3x2 = ((Line) edge).getDirectionX(); /* = x3-x2 */
			float y3y2 = ((Line) edge).getDirectionY();
			float mdiv = (x1 - x0) * (-y3y2) + x3x2 * (y1 - y0);
			if ((float) Math.abs(mdiv) < PARALLEL)
				return null; // parallel
			float m = ((x2 - x0) * (-y3y2) + x3x2 * (y2 - y0)) / mdiv;
			if (m < 0.0f || m > 1.0f)
				return null;
			return new Point(x0 + m * (x1 - x0), y0 + m * (y1 - y0));
		}
		return null;
	}

	public float gradient() {
		float dx = endPoint.getX() - startPoint.getX();
		float dy = endPoint.getY() - startPoint.getY();
		float grad = 0.0f;
		if (dy == 0.0f) {
			if (dx < 0.0f)
				grad = (float) Math.PI;
			else
				grad = 0.0f;
		} else if (dx == 0.0f) {
			if (dy > 0.0f)
				grad = (float) Math.PI / 2.0f;
			if (dy < 0.0f)
				grad = (float) Math.PI * 1.5f;
		} else {
			grad = (float) Math.atan(Math.abs(dy / dx));
			/*
			 * as atan() only returns values between -pi/2 and pi/2 we have to
			 * correct the sign and determinate the quadrant
			 */
			if (dy >= 0.0f && dx < 0.0f)
				grad = grad + (float) Math.PI / 2.0f;
			else if (dy < 0.0f && dx < 0.0f)
				grad = grad + (float) Math.PI;
			else if (dy < 0.0 && dx >= 0.0f)
				grad = grad + (float) Math.PI * 1.5f;
		}
		return grad;
	}

	@NonNull
	public Object clone() {
		Segment seg = new Segment(startPoint, endPoint);
		seg.setColor(getColor());
		return seg;
	}

	public void setStartpoint(@NonNull Point startpoint) {
		this.startPoint = startpoint;
	}

	public void setEndpoint(@NonNull Point endpoint) {
		this.endPoint = endpoint;
	}

	@NonNull
	public Point getStartpoint() {
		return startPoint;
	}

	@NonNull
	public Point getEndpoint() {
		return endPoint;
	}

	@Nullable
	public Segment clipTo(float xmin, float ymin, float xmax, float ymax) {
		float koords[] = clipping(startPoint.getX(), startPoint.getY(),
				endPoint.getX(), endPoint.getY(), xmin, ymin, xmax, ymax);
		if (koords == null)
			return null;
		Segment seg = new Segment(new Point(koords[0], koords[1]), new Point(
				koords[2], koords[3]));
		seg.setColor(getColor());
		return seg;
	}

	@NonNull
	public String toString() {
		return startPoint.toString() + "-" + endPoint.toString();
	}

	public void paint(@NonNull Canvas graphics) {
		drawSegment(graphics, startPoint, endPoint, getLinePaint());
	}

	/**
	 * internal method for Cohen-Sutherland-Clipping. (see M.Berger,
	 * Computergrafik mit Pascal, Adisson-Wesley)
	 * 
	 * @param x1, y1, x2, y2 coordinates of a segment
	 * @param xmin, ymin, xmax, ymax clipping area
	 * @return double[4] new coordinates or <VAR>null</VAR> if segment is
	 *         invisible (not in the clipping area)
	 */
	@NonNull
	public static float[] clipping(float x1, float y1, float x2, float y2,
			float xmin, float ymin, float xmax, float ymax) {
		int dir1 = 0, dir2 = 0; /* Richtungen bestimmen */
		if (x1 < xmin)
			dir1 = CLIP_LEFT;
		else if (x1 > xmax)
			dir1 = CLIP_RIGHT;
		if (y1 < ymin)
			dir1 += CLIP_BOTTOM;
		else if (y1 > ymax)
			dir1 += CLIP_TOP;
		if (x2 < xmin)
			dir2 = CLIP_LEFT;
		else if (x2 > xmax)
			dir2 = CLIP_RIGHT;
		if (y2 < ymin)
			dir2 += CLIP_BOTTOM;
		else if (y2 > ymax)
			dir2 += CLIP_TOP;
		float m = 0.0f, minverse = 0.0f; /* Steigungen berechnen */
		if (x1 != x2)
			m = (y2 - y1) / (x2 - x1);
		if (y1 != y2)
			minverse = (x2 - x1) / (y2 - y1);

		float x, y;
		int dir;

		/* beide Punkte liegen in gleichen Bereichen -> ganz aus dem Bild! */
		if ((dir1 & dir2) != 0)
			return null;

		while (dir1 != 0 || dir2 != 0) {
			if (dir1 == 0) {
				dir = dir2;
				x = x2;
				y = y2;
			} else {
				dir = dir1;
				x = x1;
				y = y1;
			}
			if ((dir & CLIP_LEFT) != 0) {
				/* Clip_Left */
				y = m * (xmin - x1) + y1;
				x = xmin;
			} else if ((dir & CLIP_RIGHT) != 0) {
				/* Clip_Right */
				y = m * (xmax - x1) + y1;
				x = xmax;
			} else if ((dir & CLIP_BOTTOM) != 0) {
				/* Clip_Buttom */
				x = minverse * (ymin - y1) + x1;
				y = ymin;
			} else if ((dir & CLIP_TOP) != 0) {
				/* Clip_Top */
				x = minverse * (ymax - y1) + x1;
				y = ymax;
			}
			if (dir == dir1) {
				x1 = x;
				y1 = y;
				dir1 = 0;
				if (x1 < xmin)
					dir1 = CLIP_LEFT;
				else if (x1 > xmax)
					dir1 = CLIP_RIGHT;
				if (y1 < ymin)
					dir1 += CLIP_BOTTOM;
				else if (y1 > ymax)
					dir1 += CLIP_TOP;
			} else {
				x2 = x;
				y2 = y;
				dir2 = 0;
				if (x2 < xmin)
					dir2 = CLIP_LEFT;
				else if (x2 > xmax)
					dir2 = CLIP_RIGHT;
				if (y2 < ymin)
					dir2 += CLIP_BOTTOM;
				else if (y2 > ymax)
					dir2 += CLIP_TOP;
			}
		}
		float koords[] = new float[4];
		koords[0] = x1;
		koords[1] = y1;
		koords[2] = x2;
		koords[3] = y2;
		return koords;
	}

	/**
	 * Draw a segment from Point <VAR>a</VAR> to <VAR>b</VAR>. This method works
	 * with <VAR>double</VAR> coordinates and is necessary because of Java Bug
	 * 4252578. Graphics.drawLine() hangs up for coordinates greater 32767!
	 * 
	 * @param g
	 * @param a
	 * @param b
	 */
	public static void drawSegment(@NonNull Canvas g, @NonNull Point a, @NonNull Point b, @NonNull Paint paint) {
		float ax = a.getX(), ay = a.getY(), bx = b.getX(), by = b.getY();
		/*
		 * this is necessary because of Java Bug 4252578 (even in JDK1.3!!),
		 * Graphics.drawLine() hangs up in an endless loop, if the coordinates
		 * are greater than 32xxx !!
		 */
		if (ax < -16000.0f || ax > 16000.0f || ay < -16000.0f || ay > 16000.0f
				|| bx < -16000.0f || bx > 16000.0f || by < -16000.0f
				|| by > 16000.0f) {
			float koords[] = clipping(ax, ay, bx, by, 0.0f, 0.0f, 16000.0f,
					16000.0f);
			if (koords != null) {
				g.drawLine((float) koords[0], (float) koords[1],
						(float) koords[2], (float) koords[3], paint);
			}
		} else {
			g.drawLine((float) ax, (float) ay, (float) bx, (float) by, paint);
		}
	}
}
