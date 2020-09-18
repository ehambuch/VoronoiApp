package de.hambuch.voronoiapp.geometry;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * A superclass for all geometic objects that can be painted.
 *
 * @version 1.0
 * @author Eric Hambuch
 */
public abstract class GeomElement {

	@Deprecated
	protected int color = Color.BLACK;
	private final Paint linePaint = new Paint();
	private final Paint fillPaint = new Paint();
	
	public GeomElement() {
		init();
	}

	public GeomElement(int color) {
		setColor(color);
		init();
	}

	private void init() {
		linePaint.setStrokeWidth(3.0f);
	}
	
	public int getColor() { return color; }
	
	public void setColor(int color) { 
		this.color = color; 
		linePaint.setColor(color);
		fillPaint.setColor(color);
	}

	protected Paint getLinePaint() {
		return linePaint;
	}
	
	protected Paint getFillPaint() {
		return fillPaint;
	}
	public abstract void paint(Canvas graphics);
}
