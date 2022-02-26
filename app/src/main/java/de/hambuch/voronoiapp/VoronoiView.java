package de.hambuch.voronoiapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import java.util.Enumeration;

import de.hambuch.voronoiapp.algo.ConvexHull;
import de.hambuch.voronoiapp.algo.DelaunayTriangulation;
import de.hambuch.voronoiapp.algo.VoronoiDiagram;
import de.hambuch.voronoiapp.algo.VoronoiDiagramCircle;
import de.hambuch.voronoiapp.geometry.GeomElement;
import de.hambuch.voronoiapp.geometry.Point;

public class VoronoiView extends View {

	private DelaunayTriangulation triang;
	private GeomElement drawable = null;
	private BitmapDrawable background = null;

	public VoronoiView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setClickable(true);
	}

	public VoronoiView(Context context) {
		this(context, null, 0);
	}

	public VoronoiView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public void setTriangulation(DelaunayTriangulation triang) {
		this.triang = triang;
	}

	public void setBackgroundBitmap(Bitmap bitmap) {
		if (bitmap == null ) {
			this.background = null;
		}
		else {
			this.background = new BitmapDrawable(this.getResources(), bitmap);
		}
	}

	@Override
	public boolean performClick() {
		return super.performClick();
	}

	public void onDraw(Canvas canvas) {
		drawInternal(canvas);
	}

	private void drawInternal(Canvas canvas) {
		if (background != null) {
			background.setBounds(0, 0, getWidth()-1, getHeight()-1);
			background.draw(canvas);
		}
		else
			canvas.drawColor(Color.WHITE);

		if (drawable != null) {
			drawable.paint(canvas);
		}

		for (Enumeration<Point> p = triang.points(); p.hasMoreElements(); ) {
			p.nextElement().paint(canvas);
		}
	}

	/**
	 * Liefert das aktuelle Bild als Bitmap zurück. Dazu wird die Grafik komplett im Hintergrund nochmal neu aufgebaut.
	 * @return Grafik als Bitmap
	 */
	Bitmap getBitmap() {
		final Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888, false);
		final Canvas bitmapCanvas = new Canvas(bitmap);
		drawInternal(bitmapCanvas);
		return bitmap;
	}

	public void showDelaunay() {
		drawable = triang;
		invalidate();
	}

	public void showVoronoi() {
		drawable = new VoronoiDiagram(triang);
		invalidate();
	}

	public void showConvexHull() {
		drawable = new ConvexHull(triang);
		invalidate();
	}

	public void showMaxCircle() {
		drawable = new VoronoiDiagramCircle(triang);
		invalidate();
	}
}
