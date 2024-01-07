package de.hambuch.voronoiapp.geometry;

import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A superclass for all geometic objects that can be painted.
 *
 * @version 1.0
 * @author Eric Hambuch
 */
public abstract class GeomElement {
	private final Paint linePaint = new Paint();
	private Paint fillPaint = null;
	
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
	
	public void setColor(@ColorInt int color) {
		linePaint.setStyle(Paint.Style.STROKE);
		linePaint.setColor(color);
	}

	public @ColorInt int getColor() {
		return linePaint.getColor();
	}

	/**
	 * Set a color to fill the element (if supported).
	 * @param color the color or 0 to clear the filling
	 */
	public void setFillColor(@ColorInt int color) {
		if(color == 0) {
			fillPaint = null;
		} else {
			fillPaint = new Paint();
			fillPaint.setStyle(Paint.Style.FILL);
			fillPaint.setColor(color);
		}
	}

	/**
	 * Returns the fill color of this element. The color may be <VAR>0</VAR>,
	 * which means, that this element won't be filled.
	 *
	 * @return the color or 0
	 */
	public int getFillColor() {
		return fillPaint != null ? fillPaint.getColor() : 0;
	}

	@NonNull
	protected Paint getLinePaint() {
		return linePaint;
	}

	@Nullable
	protected Paint getFillPaint() {
		return fillPaint;
	}

	public abstract void paint(@NonNull Canvas graphics);

}
