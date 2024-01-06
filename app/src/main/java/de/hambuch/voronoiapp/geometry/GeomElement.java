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

	public void setFillColor(@ColorInt int color) {
		fillPaint = new Paint();
		fillPaint.setStyle(Paint.Style.FILL);
		fillPaint.setColor(color);
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
